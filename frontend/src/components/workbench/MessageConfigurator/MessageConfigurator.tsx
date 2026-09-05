import React, { useState, useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { MESSAGE_CONFIGS } from '../messageConfigs';
import { FormFieldset } from './FormFieldset';
import { createMessageZodSchema } from '../../../utils/formValidation';
import { useWorkbench } from '../../../context/WorkbenchContext';

interface MessageConfiguratorProps {
  messageKey: string;
  onGenerate: (payload: Record<string, any>) => void;
  onSend: (payload: Record<string, any>) => void;
  isLoading: boolean;
  mode?: 'generation' | 'dispatch';
}

export const MessageConfigurator: React.FC<MessageConfiguratorProps> = ({
  messageKey,
  onGenerate,
  onSend,
  isLoading,
  mode = 'generation',
}) => {
  const config = MESSAGE_CONFIGS[messageKey];
  const { getFormData, setFormDataForMessage, resetFormToPrefill, clearForm } = useWorkbench();

  // Dynamic schema resolution from field definitions
  const schema = useMemo(() => {
    return createMessageZodSchema(config?.sections || []);
  }, [config]);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, touchedFields },
  } = useForm<Record<string, any>>({
    resolver: zodResolver(schema),
    mode: 'onTouched',
    defaultValues: getFormData(messageKey),
  });

  const [showValidationSummary, setShowValidationSummary] = useState<boolean>(false);

  // Sync form default values when message type changes
  useEffect(() => {
    const data = getFormData(messageKey);
    const initialValues: Record<string, any> = {};
    config?.sections.forEach(sec => {
      sec.fields.forEach(f => {
        initialValues[f.key] = data[f.key] ?? '';
      });
    });
    reset(initialValues);
    setShowValidationSummary(false);
  }, [messageKey, reset, getFormData, config]);

  const handlePrefill = () => {
    resetFormToPrefill(messageKey);
    const prefill = MESSAGE_CONFIGS[messageKey]?.prefill || {};
    const prefillValues: Record<string, any> = {};
    config?.sections.forEach(sec => {
      sec.fields.forEach(f => {
        prefillValues[f.key] = prefill[f.key] ?? '';
      });
    });
    reset(prefillValues);
    setShowValidationSummary(false);
  };

  const handleClear = () => {
    clearForm(messageKey);
    const emptyValues: Record<string, any> = {};
    config?.sections.forEach(sec => {
      sec.fields.forEach(f => {
        emptyValues[f.key] = '';
      });
    });
    reset(emptyValues);
    setShowValidationSummary(false);
  };

  const buildPayload = (values: Record<string, any>): Record<string, any> => {
    const payload: Record<string, any> = {};
    config?.sections.forEach(section => {
      section.fields.forEach(field => {
        const val = values[field.key];
        if (val !== undefined && val !== '') {
          // For AMOUNT fields, preserve exact 2-decimal string formatting
          if (field.ruleType === 'AMOUNT') {
            payload[field.key] = val;
          } else if (field.type === 'number') {
            const num = parseFloat(val);
            if (!isNaN(num)) payload[field.key] = num;
          } else {
            payload[field.key] = val;
          }
        }
      });
    });
    return payload;
  };

  const onValidSubmit = (values: Record<string, any>, action: 'generate' | 'send') => {
    setShowValidationSummary(false);
    setFormDataForMessage(messageKey, values);
    const payload = buildPayload(values);
    if (action === 'generate') {
      onGenerate(payload);
    } else {
      onSend(payload);
    }
  };

  const onInvalidSubmit = () => {
    setShowValidationSummary(true);
  };

  const executeAction = (action: 'generate' | 'send') => {
    handleSubmit(
      values => onValidSubmit(values, action),
      () => onInvalidSubmit()
    )();
  };

  const errorCount = Object.keys(errors).length;

  if (!config) return <div className="p-4 text-[#6b7280]">Unknown message type: {messageKey}</div>;

  return (
    <div className="flex flex-col h-full">
      {/* Panel Header */}
      <div className="flex items-center justify-between px-4 py-3.5 border-b border-[#e4e9e6] bg-white flex-shrink-0">
        <div className="flex items-center gap-2">
          <span className="text-[13px] font-bold tracking-[0.2px] text-[#111827]">Message Configurator</span>
          <span
            className="text-[11px] font-bold px-2.5 py-1 rounded-[6px] ml-1"
            style={{
              fontFamily: "'JetBrains Mono', monospace",
              background: '#e6f6ec',
              color: '#15803d',
            }}
          >
            ISO:{config.isoCode.toUpperCase()}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={handleClear}
            className="text-[12px] text-[#6b7280] hover:text-[#dc2626] cursor-pointer flex items-center gap-1 transition-colors bg-transparent border-0"
          >
            &#x21BA; Clear Form
          </button>
        </div>
      </div>

      {/* Validation Summary Warning (if errors exist upon submission) */}
      {showValidationSummary && errorCount > 0 && (
        <div className="bg-red-50 border-b border-red-200 px-4 py-2.5 text-[11.5px] text-red-800 flex items-center justify-between animate-fadeIn">
          <div className="flex items-center gap-2">
            <span className="text-red-600 font-bold">⚠</span>
            <span>
              <strong>{errorCount} field validation error{errorCount > 1 ? 's' : ''} found.</strong> Please review the highlighted fields before generating XML.
            </span>
          </div>
          <button
            type="button"
            onClick={() => setShowValidationSummary(false)}
            className="text-red-500 hover:text-red-700 font-bold text-xs"
          >
            ✕
          </button>
        </div>
      )}

      {/* Scrollable Form Body */}
      <div className="flex-1 overflow-y-auto p-4">
        {config.sections.map(section => (
          <FormFieldset
            key={section.title}
            section={section}
            register={register}
            watch={watch}
            errors={errors}
            touched={touchedFields}
          />
        ))}

        {/* Action Bar */}
        <div className="flex gap-2.5 mt-2 pb-2">
          <button
            type="button"
            onClick={handlePrefill}
            className="flex-1 border border-[#16a34a] text-[#15803d] bg-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer hover:bg-[#f3faf5] transition-colors flex items-center justify-center gap-1.5 shadow-sm"
          >
            <span>📄</span>
            <span>Load Pre-filled Spec Data</span>
          </button>
          
          {mode === 'generation' ? (
            <button
              type="button"
              onClick={() => executeAction('generate')}
              disabled={isLoading}
              className="flex-[1.4] border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-1.5"
              style={{
                background: isLoading ? '#6b7280' : 'linear-gradient(180deg, #16a34a, #15803d)',
                boxShadow: isLoading ? 'none' : '0 4px 12px rgba(21,128,61,0.28)',
              }}
            >
              <span>⚡</span>
              <span>{isLoading ? 'Generating XML...' : 'Generate ISO 20022 XML'}</span>
            </button>
          ) : (
            <button
              type="button"
              onClick={() => executeAction('send')}
              disabled={isLoading}
              className="flex-[1.4] border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-1.5"
              style={{
                background: isLoading ? '#6b7280' : 'linear-gradient(180deg, #16a34a, #15803d)',
                boxShadow: isLoading ? 'none' : '0 4px 12px rgba(21,128,61,0.28)',
              }}
            >
              <span>🚀</span>
              <span>{isLoading ? 'Processing...' : 'Execute Request Pipeline'}</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

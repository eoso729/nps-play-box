import React, { useState, useEffect, useMemo } from 'react';
import { MESSAGE_CONFIGS } from '../messageConfigs';
import { FormFieldset } from './FormFieldset';
import { validateFormField, validateMessageForm } from '../../../utils/formValidation';
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
  const { getFormData, updateFormField, resetFormToPrefill, clearForm } = useWorkbench();

  const formData = getFormData(messageKey);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const [showValidationSummary, setShowValidationSummary] = useState<boolean>(false);

  // Reset errors/touched when message type changes
  useEffect(() => {
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  }, [messageKey]);

  const handleChange = (key: string, value: any) => {
    updateFormField(messageKey, key, value);

    // Real-time field validation if touched
    if (touched[key] && config) {
      let fieldDef;
      for (const sec of config.sections) {
        fieldDef = sec.fields.find(f => f.key === key);
        if (fieldDef) break;
      }
      if (fieldDef) {
        const res = validateFormField(fieldDef, value);
        setErrors(prevErr => {
          const nextErr = { ...prevErr };
          if (!res.valid && res.error) {
            nextErr[key] = res.error;
          } else {
            delete nextErr[key];
          }
          return nextErr;
        });
      }
    }
  };

  const handleBlur = (key: string) => {
    setTouched(prev => ({ ...prev, [key]: true }));

    if (config) {
      let fieldDef;
      for (const sec of config.sections) {
        fieldDef = sec.fields.find(f => f.key === key);
        if (fieldDef) break;
      }
      if (fieldDef) {
        const res = validateFormField(fieldDef, formData[key]);
        setErrors(prevErr => {
          const nextErr = { ...prevErr };
          if (!res.valid && res.error) {
            nextErr[key] = res.error;
          } else {
            delete nextErr[key];
          }
          return nextErr;
        });
      }
    }
  };

  const handlePrefill = () => {
    resetFormToPrefill(messageKey);
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  };

  const handleClear = () => {
    clearForm(messageKey);
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  };

  const buildPayload = (): Record<string, any> => {
    const payload: Record<string, any> = {};
    config?.sections.forEach(section => {
      section.fields.forEach(field => {
        const val = formData[field.key];
        if (val !== undefined && val !== '') {
          // Coerce number fields
          if (field.type === 'number') {
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

  const handleSubmit = (action: 'generate' | 'send') => {
    if (!config) return;

    // Validate all fields across all sections
    const formErrors = validateMessageForm(config.sections, formData);
    const errorKeys = Object.keys(formErrors);

    if (errorKeys.length > 0) {
      // Mark all error fields as touched so inline errors display immediately
      const allTouched: Record<string, boolean> = { ...touched };
      config.sections.forEach(sec => {
        sec.fields.forEach(f => {
          allTouched[f.key] = true;
        });
      });

      setTouched(allTouched);
      setErrors(formErrors);
      setShowValidationSummary(true);
      return;
    }

    setShowValidationSummary(false);
    const payload = buildPayload();
    if (action === 'generate') {
      onGenerate(payload);
    } else {
      onSend(payload);
    }
  };

  const errorCount = useMemo(() => Object.keys(errors).length, [errors]);

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
            formData={formData}
            errors={errors}
            touched={touched}
            onChange={handleChange}
            onBlur={handleBlur}
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
              onClick={() => handleSubmit('generate')}
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
              onClick={() => handleSubmit('send')}
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

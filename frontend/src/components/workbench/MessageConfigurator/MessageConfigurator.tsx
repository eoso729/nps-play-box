import React, { useState, useEffect } from 'react';
import { MESSAGE_CONFIGS } from '../messageConfigs';
import { FormFieldset } from './FormFieldset';

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
  const [formData, setFormData] = useState<Record<string, any>>(config?.prefill ? { ...config.prefill } : {});

  // Pre-fill form with defaults when message type changes
  useEffect(() => {
    if (config?.prefill) {
      setFormData({ ...config.prefill });
    } else {
      setFormData({});
    }
  }, [messageKey, config]);

  const handleChange = (key: string, value: any) => {
    setFormData(prev => ({ ...prev, [key]: value }));
  };

  const handlePrefill = () => {
    if (config?.prefill) {
      setFormData({ ...config.prefill });
    }
  };

  const handleClear = () => {
    setFormData({});
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
        <button
          type="button"
          onClick={handleClear}
          className="text-[12px] text-[#6b7280] hover:text-[#15803d] cursor-pointer flex items-center gap-1 transition-colors bg-transparent border-0"
        >
          &#x21BA; Clear Form
        </button>
      </div>

      {/* Scrollable Form Body */}
      <div className="flex-1 overflow-y-auto p-4">
        {config.sections.map(section => (
          <FormFieldset
            key={section.title}
            section={section}
            formData={formData}
            onChange={handleChange}
          />
        ))}

        {/* Action Bar */}
        <div className="flex gap-2.5 mt-1">
          <button
            type="button"
            onClick={handlePrefill}
            className="flex-1 border border-[#16a34a] text-[#15803d] bg-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer hover:bg-[#f3faf5] transition-colors"
          >
            Load Pre-filled Spec Data
          </button>
          
          {mode === 'generation' ? (
            <button
              type="button"
              onClick={() => onGenerate(buildPayload())}
              disabled={isLoading}
              className="flex-[1.4] border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed"
              style={{
                background: isLoading ? '#6b7280' : 'linear-gradient(180deg, #16a34a, #15803d)',
                boxShadow: isLoading ? 'none' : '0 4px 12px rgba(21,128,61,0.28)',
              }}
            >
              {isLoading ? 'Generating XML...' : 'Generate ISO 20022 XML'}
            </button>
          ) : (
            <button
              type="button"
              onClick={() => onSend(buildPayload())}
              disabled={isLoading}
              className="flex-[1.4] border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed"
              style={{
                background: isLoading ? '#6b7280' : 'linear-gradient(180deg, #16a34a, #15803d)',
                boxShadow: isLoading ? 'none' : '0 4px 12px rgba(21,128,61,0.28)',
              }}
            >
              {isLoading ? 'Processing...' : 'Execute Request Pipeline'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
;

import React from 'react';
import { UseFormRegister, UseFormWatch } from 'react-hook-form';
import { FieldsetDef } from '../../../types/workbench';

interface FormFieldsetProps {
  section: FieldsetDef;
  formData?: Record<string, any>;
  errors?: Record<string, any>;
  touched?: Record<string, any>;
  onChange?: (key: string, value: any) => void;
  onBlur?: (key: string) => void;
  injectedKeys?: Set<string>;
  register?: UseFormRegister<any>;
  watch?: UseFormWatch<any>;
}

export const FormFieldset: React.FC<FormFieldsetProps> = ({
  section,
  formData = {},
  errors = {},
  touched = {},
  onChange,
  onBlur,
  injectedKeys,
  register,
  watch,
}) => {
  return (
    <div className="border border-[#e4e9e6] rounded-[10px] mb-4 overflow-hidden shadow-sm bg-white">
      <div className="bg-[#f3faf5] px-3.5 py-2.5 text-[11.5px] font-bold text-[#0f3a22] tracking-[0.3px] border-b border-[#e4e9e6] flex items-center justify-between">
        <span>{section.title}</span>
        <span className="text-[10.5px] text-[#2d6a4f] font-normal font-mono">
          {section.fields.filter(f => f.required).length} required
        </span>
      </div>
      <div className="p-3.5 grid grid-cols-2 gap-3.5">
        {section.fields.map(field => {
          const registered = register ? register(field.key) : null;
          const rawError = errors[field.key];
          const errorMessage = typeof rawError === 'string' ? rawError : rawError?.message;
          const isTouched = !!touched[field.key];
          const isInvalid = isTouched && !!errorMessage;

          const currentValue = register && watch ? (watch(field.key) ?? '') : (formData[field.key] ?? '');
          const strValue = String(currentValue);
          const isValid = isTouched && !errorMessage && strValue.length > 0;
          const isContextInjected = !!injectedKeys?.has(field.key);

          const inputBaseClass = `w-full px-2.5 py-2 text-[12.5px] border rounded-[6px] text-[#111827] outline-none transition-all ${
            isInvalid
              ? 'border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-2 focus:ring-red-200'
              : isValid
              ? 'border-emerald-400/80 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100'
              : isContextInjected
              ? 'border-[#22a05a]/70 bg-[#f8fdfa] text-[#0f3a22] focus:border-[#16a34a] focus:ring-2 focus:ring-[#16a34a]/20'
              : 'border-[#e4e9e6] bg-white focus:border-[#16a34a] focus:ring-2 focus:ring-[#16a34a]/10'
          }`;

          return (
            <div
              key={field.key}
              className={field.fullWidth ? 'col-span-2' : ''}
            >
              <div className="flex items-center justify-between mb-1">
                <label className="block text-[11px] font-semibold text-[#4b5563] flex items-center gap-1">
                  <span>{field.label}</span>
                  {field.required && <span className="text-[#dc2626] ml-0.5">*</span>}
                  {isContextInjected && (
                    <span className="text-[9px] bg-[#e6f6ec] text-[#15803d] border border-[#c4ebd3] px-1.5 py-0.5 rounded font-bold ml-1 inline-flex items-center gap-0.5">
                      <span>✨</span> Context Mapped
                    </span>
                  )}
                </label>

                {field.maxLength && (
                  <span
                    className={`text-[9.5px] font-mono tracking-tight ${
                      strValue.length > field.maxLength
                        ? 'text-red-600 font-bold'
                        : strValue.length >= field.maxLength * 0.8
                        ? 'text-amber-600 font-semibold'
                        : 'text-[#9ca3af]'
                    }`}
                  >
                    {strValue.length}/{field.maxLength}
                  </span>
                )}
              </div>

              {field.type === 'select' ? (
                <div className="relative">
                  <select
                    {...(registered || {
                      value: currentValue,
                      onChange: e => onChange?.(field.key, e.target.value),
                      onBlur: () => onBlur?.(field.key),
                    })}
                    className={`${inputBaseClass} font-sans cursor-pointer`}
                    style={{ fontFamily: 'inherit' }}
                  >
                    <option value="">Select...</option>
                    {field.options?.map(opt => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>
              ) : field.type === 'textarea' ? (
                <textarea
                  rows={2}
                  placeholder={field.placeholder}
                  maxLength={field.maxLength}
                  className={`${inputBaseClass} resize-y`}
                  style={{ fontFamily: 'inherit' }}
                  {...(registered || {
                    value: currentValue,
                    onChange: e => onChange?.(field.key, e.target.value),
                    onBlur: () => onBlur?.(field.key),
                  })}
                />
              ) : (
                <div className="relative">
                  <input
                    type={field.type === 'number' ? 'number' : field.type === 'date' ? 'date' : 'text'}
                    step={field.ruleType === 'AMOUNT' ? '0.01' : undefined}
                    placeholder={field.placeholder}
                    maxLength={field.maxLength}
                    className={inputBaseClass}
                    style={{ fontFamily: 'inherit' }}
                    {...(registered || {
                      value: currentValue,
                      onChange: e => onChange?.(field.key, e.target.value),
                      onBlur: () => onBlur?.(field.key),
                    })}
                  />
                </div>
              )}

              {/* Error Message */}
              {isInvalid && (
                <div className="text-[10px] text-red-600 font-medium mt-1 flex items-start gap-1">
                  <span className="leading-none mt-0.5">⚠</span>
                  <span className="leading-tight">{errorMessage}</span>
                </div>
              )}

              {/* Helper Text (when no error) */}
              {!isInvalid && field.helperText && (
                <div className="text-[10px] text-[#6b7280] mt-0.5 leading-tight">
                  {field.helperText}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

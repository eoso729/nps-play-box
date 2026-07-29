import React from 'react';
import { FieldsetDef } from '../../../types/workbench';

interface FormFieldsetProps {
  section: FieldsetDef;
  formData: Record<string, any>;
  onChange: (key: string, value: any) => void;
}

export const FormFieldset: React.FC<FormFieldsetProps> = ({ section, formData, onChange }) => {
  return (
    <div className="border border-[#e4e9e6] rounded-[10px] mb-4 overflow-hidden">
      <div className="bg-[#f3faf5] px-3.5 py-2.5 text-[11.5px] font-bold text-[#0f3a22] tracking-[0.3px] border-b border-[#e4e9e6]">
        {section.title}
      </div>
      <div className="p-3.5 grid grid-cols-2 gap-3">
        {section.fields.map(field => (
          <div
            key={field.key}
            className={field.fullWidth ? 'col-span-2' : ''}
          >
            <label className="block text-[11px] font-semibold text-[#6b7280] mb-1.5">
              {field.label}
              {field.required && <span className="text-[#16a34a] ml-0.5">*</span>}
            </label>

            {field.type === 'select' ? (
              <select
                value={formData[field.key] ?? ''}
                onChange={e => onChange(field.key, e.target.value)}
                className="w-full px-2.5 py-2 text-[12.5px] border border-[#e4e9e6] rounded-[6px] bg-white text-[#111827] outline-none focus:border-[#16a34a] focus:ring-2 focus:ring-[#16a34a]/10 transition-all font-sans appearance-none cursor-pointer"
                style={{ fontFamily: 'inherit' }}
              >
                <option value="">Select...</option>
                {field.options?.map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            ) : (
              <input
                type={field.type === 'number' ? 'number' : field.type === 'date' ? 'date' : 'text'}
                value={formData[field.key] ?? ''}
                onChange={e => onChange(field.key, e.target.value)}
                placeholder={field.placeholder}
                className="w-full px-2.5 py-2 text-[12.5px] border border-[#e4e9e6] rounded-[6px] bg-white text-[#111827] outline-none focus:border-[#16a34a] focus:ring-2 focus:ring-[#16a34a]/10 transition-all"
                style={{ fontFamily: 'inherit' }}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

import React from 'react';

interface SignatureBoxProps {
  signedAt?: string;
  visible?: boolean;
}

export const SignatureBox: React.FC<SignatureBoxProps> = ({ signedAt, visible = true }) => {
  if (!visible) return null;

  const displayTime = signedAt
    ? new Date(signedAt).toLocaleString('en-NG', { timeZone: 'Africa/Lagos', hour12: false }).replace(',', '') + ' WAT'
    : '--';

  return (
    <div
      className="mx-3 my-2 p-3 rounded-lg text-[11.5px] flex-shrink-0"
      style={{ border: '1px solid #c9ecd6', background: '#f3faf5' }}
    >
      <div className="flex items-center gap-1.5 font-bold mb-1.5" style={{ color: '#15803d' }}>
        <span className="w-[7px] h-[7px] rounded-full flex-shrink-0" style={{ background: '#16a34a' }} />
        Signature: Valid
      </div>
      <div className="flex justify-between mt-0.5" style={{ color: '#6b7280' }}>
        <span>Algorithm</span>
        <span className="font-medium text-[#111827]">SHA-256 with RSA (2048)</span>
      </div>
      <div className="flex justify-between mt-0.5" style={{ color: '#6b7280' }}>
        <span>Certificate</span>
        <span className="font-medium text-[#111827]">NIBSS Sandbox Signing CA</span>
      </div>
      <div className="flex justify-between mt-0.5" style={{ color: '#6b7280' }}>
        <span>Signed At</span>
        <span className="font-medium text-[#111827]">{displayTime}</span>
      </div>
    </div>
  );
};

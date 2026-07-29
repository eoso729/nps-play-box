import React from 'react';

/**
 * XmlDiffChecker — Paste two XML documents to compare them side by side.
 * (Placeholder for future implementation.)
 */
export const XmlDiffChecker: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center h-screen bg-[#f6f9f7]">
      <div
        className="w-10 h-10 rounded-[10px] flex items-center justify-center text-white font-bold mb-4"
        style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)' }}
      >
        NPS
      </div>
      <h2 className="text-[20px] font-bold text-[#0f3a22] mb-2">XML Diff Checker</h2>
      <p className="text-[14px] text-[#6b7280]">Paste two XML documents to compare — coming soon.</p>
    </div>
  );
};

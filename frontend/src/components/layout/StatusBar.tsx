import React from 'react';

export const StatusBar: React.FC = () => {
  const rateUsed = 924;
  const rateMax = 1000;
  const ratePct = Math.round((rateUsed / rateMax) * 100);

  return (
    <div className="h-[34px] flex-shrink-0 bg-white border-t border-[#e4e9e6] flex items-center justify-between px-5 text-[11px] text-[#6b7280]">
      <div className="flex items-center gap-[22px]">
        <span className="flex items-center">
          <span
            className="w-[7px] h-[7px] rounded-full inline-block mr-1.5"
            style={{ background: '#16a34a' }}
          />
          Environment: NIBSS Sandbox v2.4
        </span>
        <span className="flex items-center gap-1.5">
          Rate Limit: {rateUsed} / {rateMax}
          <span
            className="w-[110px] h-[5px] rounded-[3px] inline-block overflow-hidden ml-1.5"
            style={{ background: '#e4e9e6' }}
          >
            <span
              className="h-full block rounded-[3px]"
              style={{ width: `${ratePct}%`, background: '#16a34a' }}
            />
          </span>
        </span>
      </div>
      <div className="flex gap-[18px]">
        <a href="#" className="text-[#6b7280] no-underline hover:text-[#15803d] transition-colors">Documentation</a>
        <a href="#" className="text-[#6b7280] no-underline hover:text-[#15803d] transition-colors">Change Log</a>
        <a href="#" className="text-[#6b7280] no-underline hover:text-[#15803d] transition-colors">Support</a>
      </div>
    </div>
  );
};

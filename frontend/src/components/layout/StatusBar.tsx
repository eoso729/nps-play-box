import React, { useState, useEffect } from 'react';

const getWATTime = () =>
  new Date().toLocaleTimeString('en-NG', {
    timeZone: 'Africa/Lagos',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });

export const StatusBar: React.FC = () => {
  const [time, setTime] = useState(getWATTime());

  useEffect(() => {
    const interval = setInterval(() => setTime(getWATTime()), 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="h-[34px] flex-shrink-0 bg-white border-t border-[#e4e9e6] flex items-center justify-between px-5 text-[11px] text-[#6b7280]">
      {/* Left: Environment info */}
      <div className="flex items-center gap-3">
        <span className="flex items-center gap-1.5">
          <span
            className="w-[7px] h-[7px] rounded-full inline-block flex-shrink-0"
            style={{ background: '#16a34a', boxShadow: '0 0 0 2.5px rgba(22,163,74,0.18)' }}
          />
          <span className="font-semibold text-[#0f3a22]">NIBSS Sandbox</span>
        </span>
        <span className="text-[#d1d5db]">·</span>
        <span className="font-mono text-[10.5px]">v2.4.0</span>
        <span className="text-[#d1d5db]">·</span>
        <span>ISO 20022 Compliant</span>
      </div>

      {/* Right: Live WAT clock */}
      <div className="flex items-center gap-2">
        <span className="text-[#9ca3af] text-[10.5px]">Live —</span>
        <span
          className="font-mono font-semibold tabular-nums"
          style={{ color: '#374151', letterSpacing: '0.02em' }}
        >
          {time}
        </span>
        <span
          className="text-[9.5px] font-bold px-1.5 py-0.5 rounded"
          style={{ background: '#f3faf5', color: '#22a05a', border: '1px solid #c4ebd3' }}
        >
          WAT
        </span>
      </div>
    </div>
  );
};

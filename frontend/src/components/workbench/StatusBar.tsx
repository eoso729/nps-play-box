import React from 'react';
import { ExternalLink, ShieldCheck, Activity, HelpCircle } from 'lucide-react';

export const StatusBar: React.FC = () => {
  const rateLimitUsed = 924;
  const rateLimitTotal = 1000;
  const rateLimitPercent = (rateLimitUsed / rateLimitTotal) * 100;

  return (
    <footer className="h-9 bg-[#090d16] border-t border-[#1e293b] px-4 flex items-center justify-between text-[11px] text-slate-400 font-mono select-none z-30">
      {/* Left: Environment Indicator */}
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="text-slate-300 font-medium">Environment:</span>
          <span className="text-emerald-400 font-semibold bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
            NIBSS Sandbox v2.4
          </span>
        </div>

        {/* Rate Limit Progress Bar */}
        <div className="hidden sm:flex items-center space-x-2 pl-4 border-l border-slate-800">
          <Activity className="w-3 h-3 text-slate-400" />
          <span className="text-slate-400">Rate Limit:</span>
          <div className="w-24 bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div
              className="bg-emerald-500 h-full rounded-full transition-all duration-500"
              style={{ width: `${rateLimitPercent}%` }}
            />
          </div>
          <span className="text-slate-300 font-semibold">{rateLimitUsed} / {rateLimitTotal}</span>
        </div>
      </div>

      {/* Right: Helpful Links */}
      <div className="flex items-center space-x-4">
        <a
          href="https://nibss-plc.com.ng"
          target="_blank"
          rel="noopener noreferrer"
          className="hover:text-emerald-400 flex items-center space-x-1 transition-colors"
        >
          <span>Documentation</span>
          <ExternalLink className="w-3 h-3" />
        </a>
        <a
          href="https://nibss-plc.com.ng"
          target="_blank"
          rel="noopener noreferrer"
          className="hover:text-emerald-400 flex items-center space-x-1 transition-colors hidden md:flex"
        >
          <span>Change Log</span>
          <ExternalLink className="w-3 h-3" />
        </a>
        <a
          href="https://nibss-plc.com.ng"
          target="_blank"
          rel="noopener noreferrer"
          className="hover:text-emerald-400 flex items-center space-x-1 transition-colors"
        >
          <span>Support</span>
          <ExternalLink className="w-3 h-3" />
        </a>
      </div>
    </footer>
  );
};

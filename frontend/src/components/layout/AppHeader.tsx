import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

interface AppHeaderProps {
  onCredentialsClick?: () => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ onCredentialsClick }) => {
  const { user, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const initials = user
    ? ((user.firstName?.[0] || '') + (user.lastName?.[0] || '') || user.username?.[0]?.toUpperCase() || 'U')
    : 'U';

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username
    : 'Developer';

  const userId = user?.username || 'NPSDEV001';

  return (
    <header className="h-16 flex-shrink-0 bg-white border-b border-[#e4e9e6] flex items-center justify-between px-6 z-10">
      <div className="flex items-center gap-3.5">
        <div
          className="w-[38px] h-[38px] rounded-[9px] flex items-center justify-center text-white font-bold text-[12px] flex-shrink-0"
          style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)', boxShadow: '0 4px 12px rgba(21,128,61,0.3)' }}
        >
          NPS
        </div>
        <div>
          <h1 className="text-[16px] font-bold text-[#0f3a22] m-0 leading-tight">NPS Play Box Engine</h1>
          <p className="text-[11.5px] text-[#6b7280] m-0 mt-[1px]">ISO 20022 Message Engineering Portal</p>
        </div>
      </div>

      <div className="flex items-center gap-2.5 relative">
        {/* User Chip Trigger */}
        <div
          onClick={() => setDropdownOpen(!dropdownOpen)}
          className="flex items-center gap-2.5 border border-[#e4e9e6] rounded-lg px-2.5 py-1.5 cursor-pointer hover:border-[#22a05a] hover:bg-gray-50 transition-all select-none"
        >
          <div className="w-7 h-7 rounded-full bg-[#e6f6ec] text-[#15803d] flex items-center justify-center font-bold text-[11px] flex-shrink-0 uppercase">
            {initials}
          </div>
          <div>
            <div className="text-[12.5px] font-semibold text-[#111827] leading-tight flex items-center gap-1">
              {displayName}
              <svg className={`w-3.5 h-3.5 text-gray-500 transition-transform ${dropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
            <div className="text-[10.5px] text-[#6b7280] leading-tight font-mono">{userId}</div>
          </div>
        </div>

        {/* Dropdown Menu */}
        {dropdownOpen && (
          <>
            {/* Overlay to close on click outside */}
            <div className="fixed inset-0 z-10" onClick={() => setDropdownOpen(false)}></div>
            
            <div className="absolute right-0 top-full mt-1.5 w-52 bg-white border border-[#e4e9e6] rounded-xl shadow-lg py-1.5 z-20 animate-in fade-in slide-in-from-top-2 duration-100">
              {onCredentialsClick && (
                <button
                  type="button"
                  onClick={() => {
                    setDropdownOpen(false);
                    onCredentialsClick();
                  }}
                  className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                >
                  Credentials and Tokens
                </button>
              )}
              
              {onCredentialsClick && <div className="border-t border-[#e4e9e6] my-1"></div>}

              <button
                type="button"
                onClick={() => {
                  setDropdownOpen(false);
                  logout();
                }}
                className="w-full text-left px-4 py-2 text-[13px] font-semibold text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors cursor-pointer"
              >
                Sign Out
              </button>
            </div>
          </>
        )}
      </div>
    </header>
  );
};

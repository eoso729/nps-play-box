import React from 'react';
import { useAuth } from '../../context/AuthContext';

interface AppHeaderProps {
  onCredentialsClick?: () => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ onCredentialsClick }) => {
  const { user, logout } = useAuth();

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

      <div className="flex items-center gap-2.5">
        <button
          type="button"
          onClick={onCredentialsClick}
          className="flex items-center gap-1.5 border border-[#e4e9e6] bg-white px-3 py-[7px] rounded-lg text-[12.5px] font-semibold text-[#111827] hover:border-[#22a05a] hover:text-[#15803d] transition-colors cursor-pointer"
        >
          Credentials and Tokens
        </button>

        <button
          type="button"
          onClick={logout}
          className="flex items-center gap-1.5 border border-red-100 bg-white px-3 py-[7px] rounded-lg text-[12.5px] font-semibold text-red-600 hover:border-red-500 hover:bg-red-50 transition-colors cursor-pointer"
        >
          Sign Out
        </button>

        <div className="flex items-center gap-2.5 border border-[#e4e9e6] rounded-lg px-2.5 py-1.5 cursor-pointer hover:border-[#22a05a] transition-colors">
          <div className="w-7 h-7 rounded-full bg-[#e6f6ec] text-[#15803d] flex items-center justify-center font-bold text-[11px] flex-shrink-0 uppercase">
            {initials}
          </div>
          <div>
            <div className="text-[12.5px] font-semibold text-[#111827] leading-tight">{displayName}</div>
            <div className="text-[10.5px] text-[#6b7280] leading-tight font-mono">{userId}</div>
          </div>
        </div>
      </div>
    </header>
  );
};

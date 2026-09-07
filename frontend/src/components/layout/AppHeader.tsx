import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LogOut,
  ChevronDown,
  ExternalLink,
} from 'lucide-react';

interface AppHeaderProps {
  onCredentialsClick?: () => void;
  activeMode?: 'generation' | 'dispatch';
  onModeChange?: (mode: 'generation' | 'dispatch') => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({
  onCredentialsClick,
  activeMode = 'generation',
  onModeChange,
}) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const initials = user
    ? ((user.firstName?.[0] || '') + (user.lastName?.[0] || '') || user.username?.[0]?.toUpperCase() || 'U')
    : 'U';

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username
    : 'User';

  const userId = user?.username || 'NPSDEV001';

  // Detect current context
  const isPlatformAdmin = location.pathname.startsWith('/platform-admin') || location.pathname.startsWith('/admin/platform');
  const isTenantAdmin = location.pathname.startsWith('/tenant-admin') || location.pathname.startsWith('/admin/tenant');
  const isAdminPage = isPlatformAdmin || isTenantAdmin;

  const roleBadge = user?.role === 'PLATFORM_ADMIN'
    ? { label: 'Platform Admin', color: 'bg-emerald-700 text-white' }
    : user?.role === 'TENANT_ADMIN'
    ? { label: 'Tenant Admin', color: 'bg-blue-700 text-white' }
    : user?.role === 'DEVELOPER'
    ? { label: 'Developer', color: 'bg-teal-700 text-white' }
    : user?.role === 'VIEWER'
    ? { label: 'Viewer (Read-Only)', color: 'bg-slate-600 text-white' }
    : null;

  return (
    <header className="h-16 flex-shrink-0 bg-white border-b border-[#e4e9e6] flex items-center justify-between px-6 z-10 sticky top-0">
      {/* Left: Logo + Brand */}
      <div className="flex items-center gap-3.5">
        <div
          className="w-[38px] h-[38px] rounded-[9px] flex items-center justify-center text-white font-bold text-[12px] flex-shrink-0 cursor-pointer"
          style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)', boxShadow: '0 4px 12px rgba(21,128,61,0.3)' }}
          onClick={() => {
            if (user?.role === 'PLATFORM_ADMIN') navigate('/platform-admin');
            else if (user?.role === 'TENANT_ADMIN') navigate('/tenant-admin');
            else navigate('/workbench');
          }}
        >
          NPS
        </div>
        <div>
          <h1 className="text-[16px] font-bold text-[#0f3a22] m-0 leading-tight">NPS Play Box Engine</h1>
          <p className="text-[11.5px] text-[#6b7280] m-0 mt-[1px]">ISO 20022 Message Engineering Portal</p>
        </div>
      </div>

      {/* Center: Context-aware navigation */}
      {isPlatformAdmin ? (
        /* Platform Admin: context label + workbench shortcut */
        <div className="hidden md:flex items-center gap-3">
          <div className="flex items-center gap-2 text-[12px] text-gray-500">
            <span className="text-gray-300">/</span>
            <span className="font-semibold text-[#0f3a22]">Platform Administration</span>
          </div>
          <button
            type="button"
            onClick={() => navigate('/workbench')}
            className="flex items-center gap-1.5 px-3 py-1.5 text-[12px] font-semibold rounded-lg text-gray-500 hover:text-[#0f3a22] hover:bg-gray-100 transition-all cursor-pointer border border-[#e4e9e6]"
          >
            <ExternalLink className="w-3.5 h-3.5" />
            Workbench
          </button>
        </div>
      ) : isTenantAdmin ? (
        /* Tenant Admin: context label + workbench shortcut */
        <div className="hidden md:flex items-center gap-3">
          <div className="flex items-center gap-2 text-[12px] text-gray-500">
            <span className="text-gray-300">/</span>
            <span className="font-semibold text-[#0f3a22]">Tenant Administration</span>
          </div>
          <button
            type="button"
            onClick={() => navigate('/workbench')}
            className="flex items-center gap-1.5 px-3 py-1.5 text-[12px] font-semibold rounded-lg text-gray-500 hover:text-[#0f3a22] hover:bg-gray-100 transition-all cursor-pointer border border-[#e4e9e6]"
          >
            <ExternalLink className="w-3.5 h-3.5" />
            Workbench
          </button>
        </div>
      ) : (
        /* Workbench / Default Nav */
        <div className="flex items-center gap-2">
          {/* Mode switcher pill */}
          <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 shadow-inner">
            <button
              type="button"
              onClick={() => { if (onModeChange) onModeChange('generation'); else navigate('/workbench'); }}
              className={`px-3.5 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer flex items-center gap-1.5 ${
                activeMode === 'generation'
                  ? 'bg-white text-[#16a34a] shadow-[0_1px_3px_rgba(0,0,0,0.08)]'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <span className={`w-2 h-2 rounded-full ${activeMode === 'generation' ? 'bg-[#16a34a]' : 'bg-gray-400'}`} />
              XML Generation
            </button>
            <button
              type="button"
              onClick={() => { if (onModeChange) onModeChange('dispatch'); else navigate('/workbench'); }}
              className={`px-3.5 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer flex items-center gap-1.5 ${
                activeMode === 'dispatch'
                  ? 'bg-white text-[#16a34a] shadow-[0_1px_3px_rgba(0,0,0,0.08)]'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <span className={`w-2 h-2 rounded-full ${activeMode === 'dispatch' ? 'bg-[#16a34a]' : 'bg-gray-400'}`} />
              Pipeline Execution
            </button>
          </div>

          {/* Divider */}
          <div className="h-5 w-px flex-shrink-0" style={{ background: '#d1d5db' }} />

          {/* Tool navigation — visually distinct from mode switcher */}
          <div className="flex items-center gap-0.5">
            <button
              type="button"
              onClick={() => navigate('/orchestrator')}
              className="px-2.5 py-1.5 text-[11.5px] font-medium rounded-lg text-[#6b7280] hover:text-[#0f3a22] hover:bg-[#f0f4f1] transition-all flex items-center gap-1.5 cursor-pointer bg-transparent border-0"
            >
              <span className="text-[11px]">⚡</span>
              Flow Orchestrator
            </button>
            <button
              type="button"
              onClick={() => navigate('/inspector')}
              className="px-2.5 py-1.5 text-[11.5px] font-medium rounded-lg text-[#6b7280] hover:text-[#0f3a22] hover:bg-[#f0f4f1] transition-all flex items-center gap-1.5 cursor-pointer bg-transparent border-0"
            >
              <span className="text-[11px]">🔍</span>
              Fix My XML
            </button>
            <button
              type="button"
              onClick={() => navigate('/diff')}
              className="px-2.5 py-1.5 text-[11.5px] font-medium rounded-lg text-[#6b7280] hover:text-[#0f3a22] hover:bg-[#f0f4f1] transition-all flex items-center gap-1.5 cursor-pointer bg-transparent border-0"
            >
              <span className="text-[11px]">⚖️</span>
              Diff Checker
            </button>
          </div>
        </div>
      )}

      {/* Right: User chip + dropdown */}
      <div className="flex items-center gap-2.5 relative">
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
              {roleBadge && (
                <span className={`text-[9px] font-bold px-1.5 py-0.5 rounded-full ml-1 ${roleBadge.color}`}>
                  {roleBadge.label}
                </span>
              )}
              <ChevronDown className={`w-3.5 h-3.5 text-gray-500 transition-transform ${dropdownOpen ? 'rotate-180' : ''}`} />
            </div>
            <div className="text-[10.5px] text-[#6b7280] leading-tight font-mono">{userId}</div>
          </div>
        </div>

        {dropdownOpen && (
          <>
            <div className="fixed inset-0 z-10" onClick={() => setDropdownOpen(false)} />
            <div className="absolute right-0 top-full mt-1.5 w-56 bg-white border border-[#e4e9e6] rounded-xl shadow-lg py-1.5 z-20">
              {/* Role badge */}
              {roleBadge && (
                <div className="px-4 py-2 border-b border-[#e4e9e6]">
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${roleBadge.color}`}>
                    {roleBadge.label}
                  </span>
                  <p className="text-[11px] text-gray-500 mt-1">{user?.email}</p>
                </div>
              )}

              {onCredentialsClick && (
                <button
                  type="button"
                  onClick={() => { setDropdownOpen(false); onCredentialsClick(); }}
                  className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                >
                  Credentials & Tokens
                </button>
              )}

              {/* Admin navigation shortcuts — only show links to other pages, not the current one */}
              {user?.role === 'PLATFORM_ADMIN' && (
                <>
                  {!isPlatformAdmin && (
                    <button
                      type="button"
                      onClick={() => { setDropdownOpen(false); navigate('/platform-admin'); }}
                      className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                    >
                      Platform Dashboard
                    </button>
                  )}
                  {!isTenantAdmin && (
                    <button
                      type="button"
                      onClick={() => { setDropdownOpen(false); navigate('/tenant-admin'); }}
                      className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                    >
                      Tenant Administration
                    </button>
                  )}
                </>
              )}
              {user?.role === 'TENANT_ADMIN' && !isTenantAdmin && (
                <button
                  type="button"
                  onClick={() => { setDropdownOpen(false); navigate('/tenant-admin'); }}
                  className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                >
                  Tenant Dashboard
                </button>
              )}

              {isAdminPage && (
                <button
                  type="button"
                  onClick={() => { setDropdownOpen(false); navigate('/workbench'); }}
                  className="w-full text-left px-4 py-2 text-[13px] font-semibold text-gray-700 hover:bg-[#e6f6ec]/50 hover:text-[#15803d] transition-colors cursor-pointer"
                >
                  Go to Workbench
                </button>
              )}

              <div className="border-t border-[#e4e9e6] my-1" />

              <button
                type="button"
                onClick={() => { setDropdownOpen(false); logout(); navigate('/login'); }}
                className="w-full text-left px-4 py-2 text-[13px] font-semibold text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors cursor-pointer flex items-center gap-2"
              >
                <LogOut className="w-3.5 h-3.5" />
                Sign Out
              </button>
            </div>
          </>
        )}
      </div>
    </header>
  );
};

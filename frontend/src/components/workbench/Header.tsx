import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  Key,
  LogOut,
  User,
  GitCompare,
  Terminal,
  Copy,
  Check,
  ShieldCheck,
  X,
  ExternalLink,
} from 'lucide-react';

export const Header: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [showTokenModal, setShowTokenModal] = useState(false);
  const [copiedField, setCopiedField] = useState<string | null>(null);

  const handleCopy = (text: string, fieldName: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(fieldName);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isDiffActive = location.pathname.startsWith('/diff');

  return (
    <>
      <header className="h-16 bg-[#0f172a] border-b border-[#1e293b] px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30 shadow-md">
        {/* Left: Brand Identity */}
        <div className="flex items-center space-x-6">
          <Link to="/workbench/pain013" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-500 via-teal-600 to-indigo-600 flex items-center justify-center text-white font-extrabold text-lg tracking-wider shadow-lg shadow-emerald-950/40 group-hover:scale-105 transition-transform duration-200">
              NPS
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-white font-bold text-base tracking-tight group-hover:text-emerald-400 transition-colors">
                  NPS Play Box Engine
                </span>
                <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  v2.4-SBX
                </span>
              </div>
              <p className="text-xs text-slate-400 font-medium">
                ISO 20022 Message Engineering Portal
              </p>
            </div>
          </Link>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center space-x-1 pl-6 border-l border-[#1e293b]">
            <Link
              to="/workbench/pain013"
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-2 transition-all ${
                !isDiffActive
                  ? 'bg-slate-800 text-emerald-400 border border-slate-700 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Terminal className="w-3.5 h-3.5" />
              <span>Workbench</span>
            </Link>
            <Link
              to="/diff"
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-2 transition-all ${
                isDiffActive
                  ? 'bg-slate-800 text-emerald-400 border border-slate-700 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <GitCompare className="w-3.5 h-3.5" />
              <span>XML Diff Checker</span>
            </Link>
          </nav>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-3">
          {/* Credentials & Tokens Pill Button */}
          <button
            onClick={() => setShowTokenModal(true)}
            className="px-3 py-1.5 rounded-lg bg-slate-800/90 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-700 text-xs font-medium flex items-center space-x-2 shadow-sm transition-all hover:border-emerald-500/40"
          >
            <Key className="w-3.5 h-3.5 text-amber-400" />
            <span className="hidden sm:inline">Credentials & Tokens</span>
          </button>

          {/* User Chip */}
          <div className="flex items-center space-x-2 bg-slate-900/80 border border-slate-800 pl-2 py-1 pr-1.5 rounded-xl">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center text-white font-bold text-xs shadow-inner">
              {user?.username ? user.username.substring(0, 2).toUpperCase() : 'JD'}
            </div>
            <div className="hidden sm:block text-left pr-1">
              <div className="text-xs font-bold text-slate-200 leading-none">
                {user?.username || 'John Developer'}
              </div>
              <div className="text-[10px] font-mono text-emerald-400 leading-tight">
                NPSDEV001
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Logout"
              className="p-1 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
            >
              <LogOut className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </header>

      {/* Credentials Modal */}
      {showTokenModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-200">
          <div className="bg-[#0f172a] border border-[#1e293b] rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-5 relative">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center space-x-3">
                <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white tracking-tight">
                    NIBSS Sandbox Credentials & JWT Tokens
                  </h3>
                  <p className="text-xs text-slate-400">
                    Client credentials and active session bearer tokens for API integration.
                  </p>
                </div>
              </div>
              <button
                onClick={() => setShowTokenModal(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-4 font-mono text-xs">
              {/* Institution Code */}
              <div className="space-y-1.5">
                <label className="text-slate-400 text-[11px] font-semibold uppercase tracking-wider block">
                  Institution ID (Participant Code)
                </label>
                <div className="flex items-center justify-between bg-slate-950 p-2.5 rounded-lg border border-slate-800 text-slate-200">
                  <span>NIBSS-FINTECH-991028</span>
                  <button
                    onClick={() => handleCopy('NIBSS-FINTECH-991028', 'inst')}
                    className="p-1 text-slate-400 hover:text-emerald-400"
                  >
                    {copiedField === 'inst' ? (
                      <Check className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Sandbox API Key */}
              <div className="space-y-1.5">
                <label className="text-slate-400 text-[11px] font-semibold uppercase tracking-wider block">
                  Sandbox API Key
                </label>
                <div className="flex items-center justify-between bg-slate-950 p-2.5 rounded-lg border border-slate-800 text-slate-200">
                  <span>sbx_key_992183746610293847566123</span>
                  <button
                    onClick={() =>
                      handleCopy('sbx_key_992183746610293847566123', 'apikey')
                    }
                    className="p-1 text-slate-400 hover:text-emerald-400"
                  >
                    {copiedField === 'apikey' ? (
                      <Check className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Bearer Token */}
              <div className="space-y-1.5">
                <label className="text-slate-400 text-[11px] font-semibold uppercase tracking-wider block">
                  Active JWT Bearer Token
                </label>
                <div className="bg-slate-950 p-2.5 rounded-lg border border-slate-800 text-slate-300 break-all text-[11px] max-h-24 overflow-y-auto relative">
                  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJKb2huIERldmVsb3BlciIsImlhdCI6MTcyMjI4OTk2NiwiZXhwIjoxNzIyMzc2MzY2LCJyb2xlcyI6WyJOSUJTU19ERVZFTE9QRVIiXX0.sbx_signature_verify_ok
                  <button
                    onClick={() =>
                      handleCopy(
                        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJKb2huIERldmVsb3BlciIsImlhdCI6MTcyMjI4OTk2NiwiZXhwIjoxNzIyMzc2MzY2LCJyb2xlcyI6WyJOSUJTU19ERVZFTE9QRVIiXX0.sbx_signature_verify_ok',
                        'jwt'
                      )
                    }
                    className="absolute top-2 right-2 p-1 bg-slate-900 rounded text-slate-400 hover:text-emerald-400"
                  >
                    {copiedField === 'jwt' ? (
                      <Check className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                onClick={() => setShowTokenModal(false)}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs rounded-xl shadow transition-colors"
              >
                Close Window
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

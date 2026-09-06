import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { impersonationApi } from '../api/impersonation';

interface JwtPayload {
  impersonated?: boolean;
  sessionUuid?: string;
  supportUserId?: number;
  email?: string;
  tenantSlug?: string;
  exp?: number;
}

function parseJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export const ImpersonationBanner: React.FC = () => {
  const { token, logout } = useAuth();
  const [timeRemaining, setTimeRemaining] = useState<string>('');
  const [isTerminating, setIsTerminating] = useState<boolean>(false);

  const payload = token ? parseJwt(token) : null;
  const isImpersonation = Boolean(payload?.impersonated);
  const sessionUuid = payload?.sessionUuid;
  const exp = payload?.exp ? payload.exp * 1000 : null;

  useEffect(() => {
    if (!isImpersonation || !exp) return;

    const updateTimer = () => {
      const now = Date.now();
      const diff = exp - now;

      if (diff <= 0) {
        setTimeRemaining('Expired');
        logout();
      } else {
        const minutes = Math.floor(diff / 60000);
        const seconds = Math.floor((diff % 60000) / 1000);
        setTimeRemaining(`${minutes}m ${seconds.toString().padStart(2, '0')}s`);
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  }, [isImpersonation, exp, logout]);

  const handleTerminate = async () => {
    if (!sessionUuid || isTerminating) return;
    setIsTerminating(true);

    try {
      await impersonationApi.terminateSession(sessionUuid, 'User ended impersonation session');
    } catch (error) {
      console.error('Failed to terminate session on server:', error);
    } finally {
      logout();
      window.location.href = '/';
    }
  };

  if (!isImpersonation) return null;

  return (
    <div
      role="alert"
      className="fixed top-0 left-0 right-0 z-50 bg-amber-500 text-slate-900 px-4 py-2 shadow-md flex items-center justify-between border-b border-amber-600 font-medium text-sm"
    >
      <div className="flex items-center space-x-3">
        <span className="text-lg" aria-hidden="true">⚠️</span>
        <div>
          <span className="font-bold uppercase tracking-wider text-xs bg-amber-700 text-white px-2 py-0.5 rounded mr-2">
            Impersonation Active
          </span>
          <span>
            You are viewing this account in support impersonation mode.
          </span>
          <span className="ml-3 font-mono font-semibold bg-amber-400 px-2 py-0.5 rounded text-xs">
            Expires in: {timeRemaining || '...'}
          </span>
        </div>
      </div>
      <div>
        <button
          type="button"
          onClick={handleTerminate}
          disabled={isTerminating}
          className="bg-red-600 hover:bg-red-700 text-white text-xs font-semibold px-3 py-1.5 rounded transition shadow disabled:opacity-50"
        >
          {isTerminating ? 'Ending...' : 'End Session'}
        </button>
      </div>
    </div>
  );
};

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { MessageWorkbench } from './MessageWorkbench';

export const WorkbenchPage: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-[#f6f9f7]">
        <div className="flex flex-col items-center gap-3">
          <div
            className="w-9 h-9 rounded-[9px] flex items-center justify-center text-white font-bold text-[12px] animate-pulse"
            style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)' }}
          >
            NPS
          </div>
          <p className="text-[13px] text-[#6b7280]">Loading session...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <MessageWorkbench />;
};

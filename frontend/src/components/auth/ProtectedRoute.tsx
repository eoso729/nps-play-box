import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
  redirectTo?: string;
}

const LoadingSpinner: React.FC = () => (
  <div className="flex items-center justify-center h-screen bg-[#f6f9f7]">
    <div className="flex flex-col items-center gap-4">
      <div
        className="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-[13px] animate-pulse"
        style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)' }}
      >
        NPS
      </div>
      <p className="text-[13px] text-[#6b7280] font-medium">Authenticating session...</p>
    </div>
  </div>
);

const AccessDenied: React.FC = () => (
  <div className="flex items-center justify-center h-screen bg-[#f6f9f7]">
    <div className="text-center max-w-sm px-6">
      <div className="w-14 h-14 rounded-2xl bg-red-100 flex items-center justify-center mx-auto mb-4">
        <svg className="w-7 h-7 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
            d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <h2 className="text-lg font-bold text-gray-900 mb-2">Access Restricted</h2>
      <p className="text-sm text-gray-500">
        You do not have permission to view this page. Contact your administrator if you believe this is an error.
      </p>
    </div>
  </div>
);

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
  redirectTo = '/login',
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && user.role) {
    if (!allowedRoles.includes(user.role)) {
      return <AccessDenied />;
    }
  }

  return <>{children}</>;
};

import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { WorkbenchProvider } from './context/WorkbenchContext';
import { ImpersonationBanner } from './components/ImpersonationBanner';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { useAuth } from './context/AuthContext';

const AuthScreen = React.lazy(() => import('./components/AuthScreen').then(m => ({ default: m.AuthScreen })));
const WorkbenchPage = React.lazy(() => import('./components/workbench/WorkbenchPage').then(m => ({ default: m.WorkbenchPage })));
const XmlDiffChecker = React.lazy(() => import('./components/workbench/XmlDiffChecker').then(m => ({ default: m.XmlDiffChecker })));
const XmlInspectorPage = React.lazy(() => import('./components/inspector/XmlInspectorPage').then(m => ({ default: m.XmlInspectorPage })));
const FlowOrchestratorPage = React.lazy(() => import('./components/orchestrator/FlowOrchestratorPage').then(m => ({ default: m.FlowOrchestratorPage })));
const TenantAdminDashboard = React.lazy(() => import('./features/tenant-admin/pages/TenantAdminDashboard').then(m => ({ default: m.TenantAdminDashboard })));
const PlatformAdminDashboard = React.lazy(() => import('./features/platform-admin/pages/PlatformAdminDashboard').then(m => ({ default: m.PlatformAdminDashboard })));
const AcceptInvitationPage = React.lazy(() => import('./features/tenant-admin/pages/AcceptInvitationPage').then(m => ({ default: m.AcceptInvitationPage })));

const PageLoadingFallback: React.FC = () => (
  <div className="flex items-center justify-center min-h-screen bg-[#f6f9f7]">
    <div className="flex flex-col items-center gap-3">
      <div className="w-8 h-8 border-2 border-[#16a34a]/20 border-t-[#16a34a] rounded-full animate-spin" />
      <span className="text-[12px] font-semibold text-gray-600">Loading module...</span>
    </div>
  </div>
);

/** Redirects the root path to the correct home based on role */
const RoleRedirect: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <PageLoadingFallback />;
  if (!isAuthenticated || !user) return <Navigate to="/login" replace />;
  if (user.role === 'PLATFORM_ADMIN') return <Navigate to="/platform-admin" replace />;
  if (user.role === 'TENANT_ADMIN') return <Navigate to="/tenant-admin" replace />;
  return <Navigate to="/workbench" replace />;
};

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <WorkbenchProvider>
          <BrowserRouter>
            <ImpersonationBanner />
            <Suspense fallback={<PageLoadingFallback />}>
              <Routes>
                {/* Public auth routes */}
                <Route path="/login" element={<AuthScreen />} />
                <Route path="/register" element={<AuthScreen />} />
                <Route path="/accept-invitation" element={<AcceptInvitationPage />} />

                {/* Platform admin – only PLATFORM_ADMIN */}
                <Route path="/platform-admin" element={
                  <ProtectedRoute allowedRoles={['PLATFORM_ADMIN']}>
                    <PlatformAdminDashboard />
                  </ProtectedRoute>
                } />

                {/* Tenant admin – PLATFORM_ADMIN can also view */}
                <Route path="/tenant-admin" element={
                  <ProtectedRoute allowedRoles={['TENANT_ADMIN', 'PLATFORM_ADMIN']}>
                    <TenantAdminDashboard />
                  </ProtectedRoute>
                } />

                {/* Workbench tools – any authenticated user */}
                <Route path="/workbench" element={
                  <ProtectedRoute>
                    <WorkbenchPage />
                  </ProtectedRoute>
                } />
                <Route path="/workbench/:messageId" element={
                  <ProtectedRoute>
                    <WorkbenchPage />
                  </ProtectedRoute>
                } />
                <Route path="/orchestrator" element={
                  <ProtectedRoute>
                    <FlowOrchestratorPage />
                  </ProtectedRoute>
                } />
                <Route path="/orchestrator/:flowId" element={
                  <ProtectedRoute>
                    <FlowOrchestratorPage />
                  </ProtectedRoute>
                } />
                <Route path="/flows" element={
                  <ProtectedRoute>
                    <FlowOrchestratorPage />
                  </ProtectedRoute>
                } />
                <Route path="/flows/:flowId" element={
                  <ProtectedRoute>
                    <FlowOrchestratorPage />
                  </ProtectedRoute>
                } />
                <Route path="/inspector" element={
                  <ProtectedRoute>
                    <XmlInspectorPage />
                  </ProtectedRoute>
                } />
                <Route path="/health-check" element={
                  <ProtectedRoute>
                    <XmlInspectorPage />
                  </ProtectedRoute>
                } />
                <Route path="/fix-xml" element={
                  <ProtectedRoute>
                    <XmlInspectorPage />
                  </ProtectedRoute>
                } />
                <Route path="/diff" element={
                  <ProtectedRoute>
                    <XmlDiffChecker />
                  </ProtectedRoute>
                } />

                {/* Root — smart redirect based on role */}
                <Route path="/" element={<RoleRedirect />} />
                <Route path="*" element={<RoleRedirect />} />
              </Routes>
            </Suspense>
          </BrowserRouter>
        </WorkbenchProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;

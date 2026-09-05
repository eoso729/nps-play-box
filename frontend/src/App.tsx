import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { WorkbenchProvider } from './context/WorkbenchContext';

const AuthScreen = React.lazy(() => import('./components/AuthScreen').then(m => ({ default: m.AuthScreen })));
const WorkbenchPage = React.lazy(() => import('./components/workbench/WorkbenchPage').then(m => ({ default: m.WorkbenchPage })));
const XmlDiffChecker = React.lazy(() => import('./components/workbench/XmlDiffChecker').then(m => ({ default: m.XmlDiffChecker })));
const XmlInspectorPage = React.lazy(() => import('./components/inspector/XmlInspectorPage').then(m => ({ default: m.XmlInspectorPage })));
const FlowOrchestratorPage = React.lazy(() => import('./components/orchestrator/FlowOrchestratorPage').then(m => ({ default: m.FlowOrchestratorPage })));

const PageLoadingFallback: React.FC = () => (
  <div className="flex items-center justify-center min-h-screen bg-[#f6f9f7]">
    <div className="flex flex-col items-center gap-3">
      <div className="w-8 h-8 border-2 border-[#16a34a]/20 border-t-[#16a34a] rounded-full animate-spin" />
      <span className="text-[12px] font-semibold text-gray-600">Loading module...</span>
    </div>
  </div>
);

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
            <Suspense fallback={<PageLoadingFallback />}>
              <Routes>
                <Route path="/login" element={<AuthScreen />} />
                <Route path="/register" element={<AuthScreen />} />
                <Route path="/workbench" element={<WorkbenchPage />} />
                <Route path="/workbench/:messageId" element={<WorkbenchPage />} />
                <Route path="/orchestrator" element={<FlowOrchestratorPage />} />
                <Route path="/orchestrator/:flowId" element={<FlowOrchestratorPage />} />
                <Route path="/flows" element={<FlowOrchestratorPage />} />
                <Route path="/flows/:flowId" element={<FlowOrchestratorPage />} />
                <Route path="/inspector" element={<XmlInspectorPage />} />
                <Route path="/health-check" element={<XmlInspectorPage />} />
                <Route path="/fix-xml" element={<XmlInspectorPage />} />
                <Route path="/diff" element={<XmlDiffChecker />} />
                <Route path="/" element={<Navigate to="/workbench" replace />} />
                <Route path="*" element={<Navigate to="/workbench" replace />} />
              </Routes>
            </Suspense>
          </BrowserRouter>
        </WorkbenchProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;

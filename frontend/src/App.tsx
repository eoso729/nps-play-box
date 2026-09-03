import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { WorkbenchProvider } from './context/WorkbenchContext';
import { AuthScreen } from './components/AuthScreen';
import { WorkbenchPage } from './components/workbench/WorkbenchPage';
import { XmlDiffChecker } from './components/workbench/XmlDiffChecker';
import { XmlInspectorPage } from './components/inspector/XmlInspectorPage';
import { FlowOrchestratorPage } from './components/orchestrator/FlowOrchestratorPage';

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
          </BrowserRouter>
        </WorkbenchProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;

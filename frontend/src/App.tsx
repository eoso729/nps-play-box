import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { AuthScreen } from './components/AuthScreen';
import { WorkbenchPage } from './components/workbench/WorkbenchPage';
import { XmlDiffChecker } from './components/workbench/XmlDiffChecker';

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
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<AuthScreen />} />
            <Route path="/register" element={<AuthScreen />} />
            <Route path="/workbench" element={<WorkbenchPage />} />
            <Route path="/workbench/:messageId" element={<WorkbenchPage />} />
            <Route path="/diff" element={<XmlDiffChecker />} />
            <Route path="/" element={<Navigate to="/workbench/pain013" replace />} />
            <Route path="*" element={<Navigate to="/workbench/pain013" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;

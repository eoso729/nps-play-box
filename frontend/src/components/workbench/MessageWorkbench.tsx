import React, { useCallback } from 'react';
import { AppHeader } from '../layout/AppHeader';
import { Sidebar } from '../layout/Sidebar';
import { StatusBar } from '../layout/StatusBar';
import { MessageConfigurator } from './MessageConfigurator/MessageConfigurator';
import { PipelinePanel } from './Pipeline/PipelinePanel';
import { generateXml, sendMessage } from '../../api/workbench';
import { MessageKey } from '../../types/workbench';
import { useWorkbench } from '../../context/WorkbenchContext';

export const MessageWorkbench: React.FC = () => {
  const {
    activeMessage,
    setActiveMessage,
    workbenchMode,
    setWorkbenchMode,
    sidebarCollapsed,
    setSidebarCollapsed,
    result,
    setResult,
  } = useWorkbench();

  const handleSelectMessage = useCallback((key: string) => {
    setActiveMessage(key);
  }, [setActiveMessage]);

  const handleGenerate = useCallback(async (payload: Record<string, any>) => {
    setResult(prev => ({ ...prev, isLoading: true, error: null }));
    try {
      const data = await generateXml(activeMessage as MessageKey, payload);
      setResult({
        plainXml: data.plainXml,
        signedXml: data.signedXml,
        generatedAt: data.generatedAt,
        messageId: data.messageId,
        serviceResponse: null,
        isLoading: false,
        error: null,
      });
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || err?.message || 'XML generation failed';
      setResult(prev => ({
        ...prev,
        isLoading: false,
        error: errMsg,
      }));
    }
  }, [activeMessage]);

  const handleSend = useCallback(async (payload: Record<string, any>) => {
    setResult(prev => ({ ...prev, isLoading: true, error: null }));
    try {
      const data = await sendMessage(activeMessage as MessageKey, payload);
      setResult({
        plainXml: data.plainXml,
        signedXml: data.signedXml,
        generatedAt: undefined,
        messageId: data.messageId,
        serviceResponse: data.serviceResponse,
        isLoading: false,
        error: null,
      });
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || err?.message || 'Pipeline execution failed';
      setResult(prev => ({
        ...prev,
        isLoading: false,
        error: errMsg,
      }));
    }
  }, [activeMessage]);

  return (
    <div className="flex flex-col" style={{ height: '100vh', overflow: 'hidden', background: '#f6f9f7' }}>
      {/* Header */}
      <AppHeader
        activeMode={workbenchMode}
        onModeChange={setWorkbenchMode}
      />

      {/* Body Row */}
      <div className="flex flex-1 min-h-0">
        {/* Sidebar */}
        <Sidebar
          activeMessage={activeMessage}
          onSelect={handleSelectMessage}
          collapsed={sidebarCollapsed}
          onToggleCollapse={() => setSidebarCollapsed(prev => !prev)}
        />

        {/* Main Content */}
        <main className="flex flex-1 min-w-0 overflow-hidden">
          {/* Left: Message Configurator */}
          <div
            className="flex flex-col border-r border-[#e4e9e6] overflow-hidden"
            style={{ flex: '1.15' }}
          >
            <MessageConfigurator
              messageKey={activeMessage}
              onGenerate={handleGenerate}
              onSend={handleSend}
              isLoading={result.isLoading}
              mode={workbenchMode}
            />
          </div>

          {/* Right: Pipeline Panel */}
          <div className="flex flex-1 min-w-0 overflow-hidden" style={{ flex: 2 }}>
            {result.error && (
              <div
                className="absolute top-16 right-4 z-50 max-w-sm px-4 py-3 rounded-lg border text-[12.5px] font-medium shadow-lg"
                style={{ background: '#fee2e2', borderColor: '#fecaca', color: '#dc2626' }}
              >
                {result.error}
              </div>
            )}
            <PipelinePanel result={result} mode={workbenchMode} />
          </div>
        </main>
      </div>

      {/* Status Bar */}
      <StatusBar />
    </div>
  );
};


import React, { createContext, useContext, useState, useCallback } from 'react';
import { MESSAGE_CONFIGS } from '../components/workbench/messageConfigs';
import { PipelineResult } from '../types/workbench';

export const EMPTY_RESULT: PipelineResult = {
  plainXml: null,
  signedXml: null,
  generatedAt: undefined,
  messageId: undefined,
  serviceResponse: null,
  isLoading: false,
  error: null,
};

const ACTIVE_MESSAGE_KEY = 'nps_workbench_active_message';
const WORKBENCH_MODE_KEY = 'nps_workbench_mode';
const FORMS_DATA_KEY = 'nps_workbench_forms_data';

function loadInitialFormsData(): Record<string, Record<string, any>> {
  try {
    const raw = sessionStorage.getItem(FORMS_DATA_KEY);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch (e) {
    console.warn('Failed to parse saved forms data from sessionStorage', e);
  }
  return {};
}

function loadInitialActiveMessage(): string {
  const saved = sessionStorage.getItem(ACTIVE_MESSAGE_KEY);
  if (saved && MESSAGE_CONFIGS[saved]) {
    return saved;
  }
  return 'pain.013';
}

function loadInitialMode(): 'generation' | 'dispatch' {
  const saved = sessionStorage.getItem(WORKBENCH_MODE_KEY);
  if (saved === 'dispatch' || saved === 'generation') {
    return saved;
  }
  return 'generation';
}

interface WorkbenchContextType {
  activeMessage: string;
  setActiveMessage: (key: string) => void;
  workbenchMode: 'generation' | 'dispatch';
  setWorkbenchMode: (mode: 'generation' | 'dispatch') => void;
  sidebarCollapsed: boolean;
  setSidebarCollapsed: React.Dispatch<React.SetStateAction<boolean>>;
  getFormData: (messageKey: string) => Record<string, any>;
  updateFormField: (messageKey: string, fieldKey: string, value: any) => void;
  setFormDataForMessage: (messageKey: string, data: Record<string, any>) => void;
  resetFormToPrefill: (messageKey: string) => void;
  clearForm: (messageKey: string) => void;
  result: PipelineResult;
  setResult: (updater: PipelineResult | ((prev: PipelineResult) => PipelineResult)) => void;
  getResultForMessage: (messageKey: string) => PipelineResult;
  setResultForMessage: (messageKey: string, updater: PipelineResult | ((prev: PipelineResult) => PipelineResult)) => void;
}

const WorkbenchContext = createContext<WorkbenchContextType | undefined>(undefined);

export const WorkbenchProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeMessage, setActiveMessageState] = useState<string>(loadInitialActiveMessage);
  const [workbenchMode, setWorkbenchModeState] = useState<'generation' | 'dispatch'>(loadInitialMode);
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(false);
  const [formsData, setFormsData] = useState<Record<string, Record<string, any>>>(loadInitialFormsData);
  const [pipelineResults, setPipelineResults] = useState<Record<string, PipelineResult>>({});

  const setActiveMessage = useCallback((key: string) => {
    setActiveMessageState(key);
    try {
      sessionStorage.setItem(ACTIVE_MESSAGE_KEY, key);
    } catch (e) {
      console.warn('Failed to save active message to sessionStorage', e);
    }
  }, []);

  const setWorkbenchMode = useCallback((mode: 'generation' | 'dispatch') => {
    setWorkbenchModeState(mode);
    try {
      sessionStorage.setItem(WORKBENCH_MODE_KEY, mode);
    } catch (e) {
      console.warn('Failed to save workbench mode to sessionStorage', e);
    }
  }, []);

  const getFormData = useCallback((messageKey: string): Record<string, any> => {
    if (formsData[messageKey]) {
      return formsData[messageKey];
    }
    if (MESSAGE_CONFIGS[messageKey]?.prefill) {
      return { ...MESSAGE_CONFIGS[messageKey].prefill };
    }
    return {};
  }, [formsData]);

  const updateFormField = useCallback((messageKey: string, fieldKey: string, value: any) => {
    setFormsData(prev => {
      const currentMsgData = prev[messageKey] || (MESSAGE_CONFIGS[messageKey]?.prefill ? { ...MESSAGE_CONFIGS[messageKey].prefill } : {});
      const updatedMsgData = { ...currentMsgData, [fieldKey]: value };
      const next = { ...prev, [messageKey]: updatedMsgData };
      try {
        sessionStorage.setItem(FORMS_DATA_KEY, JSON.stringify(next));
      } catch (e) {
        console.warn('Failed to save forms data to sessionStorage', e);
      }
      return next;
    });
  }, []);

  const setFormDataForMessage = useCallback((messageKey: string, data: Record<string, any>) => {
    setFormsData(prev => {
      const next = { ...prev, [messageKey]: data };
      try {
        sessionStorage.setItem(FORMS_DATA_KEY, JSON.stringify(next));
      } catch (e) {
        console.warn('Failed to save forms data to sessionStorage', e);
      }
      return next;
    });
  }, []);

  const resetFormToPrefill = useCallback((messageKey: string) => {
    setFormsData(prev => {
      const prefill = MESSAGE_CONFIGS[messageKey]?.prefill ? { ...MESSAGE_CONFIGS[messageKey].prefill } : {};
      const next = { ...prev, [messageKey]: prefill };
      try {
        sessionStorage.setItem(FORMS_DATA_KEY, JSON.stringify(next));
      } catch (e) {
        console.warn('Failed to save forms data to sessionStorage', e);
      }
      return next;
    });
  }, []);

  const clearForm = useCallback((messageKey: string) => {
    setFormsData(prev => {
      const next = { ...prev, [messageKey]: {} };
      try {
        sessionStorage.setItem(FORMS_DATA_KEY, JSON.stringify(next));
      } catch (e) {
        console.warn('Failed to save forms data to sessionStorage', e);
      }
      return next;
    });
  }, []);

  const getResultForMessage = useCallback((messageKey: string): PipelineResult => {
    return pipelineResults[messageKey] || EMPTY_RESULT;
  }, [pipelineResults]);

  const setResultForMessage = useCallback((messageKey: string, updater: PipelineResult | ((prev: PipelineResult) => PipelineResult)) => {
    setPipelineResults(prev => {
      const current = prev[messageKey] || EMPTY_RESULT;
      const nextVal = typeof updater === 'function' ? updater(current) : updater;
      return { ...prev, [messageKey]: nextVal };
    });
  }, []);

  const result = pipelineResults[activeMessage] || EMPTY_RESULT;

  const setResult = useCallback((updater: PipelineResult | ((prev: PipelineResult) => PipelineResult)) => {
    setResultForMessage(activeMessage, updater);
  }, [activeMessage, setResultForMessage]);

  return (
    <WorkbenchContext.Provider
      value={{
        activeMessage,
        setActiveMessage,
        workbenchMode,
        setWorkbenchMode,
        sidebarCollapsed,
        setSidebarCollapsed,
        getFormData,
        updateFormField,
        setFormDataForMessage,
        resetFormToPrefill,
        clearForm,
        result,
        setResult,
        getResultForMessage,
        setResultForMessage,
      }}
    >
      {children}
    </WorkbenchContext.Provider>
  );
};

export const useWorkbench = () => {
  const context = useContext(WorkbenchContext);
  if (!context) {
    throw new Error('useWorkbench must be used within a WorkbenchProvider');
  }
  return context;
};

import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  ORCHESTRATOR_FLOWS,
  computeClientNextStepPrefill,
} from './orchestratorConfigs';
import {
  FlowDefinition,
  FlowStepDefinition,
  StepExecutionResult,
  StepExecutionStatus,
} from '../../types/orchestrator';
import { executeFlowStep, autoRunWorkflow } from '../../api/orchestrator';
import { MESSAGE_CONFIGS } from '../workbench/messageConfigs';
import { FormFieldset } from '../workbench/MessageConfigurator/FormFieldset';
import { validateFormField, validateMessageForm } from '../../utils/formValidation';

export const FlowOrchestratorPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { flowId: paramFlowId } = useParams<{ flowId?: string }>();

  // Active Flow selection
  const [activeFlowId, setActiveFlowId] = useState<string>(() => {
    if (paramFlowId && ORCHESTRATOR_FLOWS.some(f => f.id === paramFlowId)) {
      return paramFlowId;
    }
    return 'direct-debit';
  });

  const activeFlow: FlowDefinition = useMemo(() => {
    return ORCHESTRATOR_FLOWS.find(f => f.id === activeFlowId) || ORCHESTRATOR_FLOWS[0];
  }, [activeFlowId]);

  // Current active step within the flow
  const [activeStepIndex, setActiveStepIndex] = useState<number>(0);

  // Variant selections (e.g. pain.008 vs pacs.003 for direct debit step 3)
  const [stepVariants, setStepVariants] = useState<Record<number, string>>({});

  // Complete form payloads per step (mirroring Pipeline Execution)
  const [stepPayloads, setStepPayloads] = useState<Record<number, Record<string, any>>>({});

  // Auto-injected field keys tracking per step
  const [autoInjectedKeysByStep, setAutoInjectedKeysByStep] = useState<Record<number, Set<string>>>({});

  // Cumulative session context
  const [context, setContext] = useState<Record<string, string>>({});

  // Results per step
  const [stepResults, setStepResults] = useState<Record<number, StepExecutionResult>>({});

  // Step statuses: pending | running | completed | error
  const [stepStatuses, setStepStatuses] = useState<Record<number, StepExecutionStatus>>({});

  // Form field errors & touched states for current step
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const [showValidationSummary, setShowValidationSummary] = useState<boolean>(false);

  // Inspector & modal states
  const [activeInspectorTab, setActiveInspectorTab] = useState<'plainXml' | 'signedXml' | 'response' | 'context'>('plainXml');
  const [isAutoRunning, setIsAutoRunning] = useState(false);
  const [isExecutingStep, setIsExecutingStep] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null);

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Sync flow when URL param changes
  useEffect(() => {
    if (paramFlowId && ORCHESTRATOR_FLOWS.some(f => f.id === paramFlowId) && paramFlowId !== activeFlowId) {
      handleSwitchFlow(paramFlowId);
    }
  }, [paramFlowId]);

  // Initialize flow state using full spec prefill from MESSAGE_CONFIGS
  const initFlow = (flow: FlowDefinition) => {
    const initialPayloads: Record<number, Record<string, any>> = {};
    const initialStatuses: Record<number, StepExecutionStatus> = {};
    const initialInjected: Record<number, Set<string>> = {};

    flow.steps.forEach((step, idx) => {
      const msgType = stepVariants[idx] || step.messageType;
      const specPrefill = MESSAGE_CONFIGS[msgType]?.prefill ? { ...MESSAGE_CONFIGS[msgType].prefill } : {};
      initialPayloads[idx] = { ...specPrefill, ...step.defaultPayload };
      initialStatuses[idx] = 'pending';
      initialInjected[idx] = new Set();
    });

    setStepPayloads(initialPayloads);
    setStepStatuses(initialStatuses);
    setAutoInjectedKeysByStep(initialInjected);
    setStepResults({});
    setContext({});
    setActiveStepIndex(0);
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  };

  useEffect(() => {
    initFlow(activeFlow);
  }, [activeFlowId]);

  // Reset errors/touched when active step or variant changes
  useEffect(() => {
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  }, [activeStepIndex, stepVariants]);

  const currentStep: FlowStepDefinition = activeFlow.steps[activeStepIndex] || activeFlow.steps[0];
  const activeMessageType = stepVariants[activeStepIndex] || currentStep.messageType;
  const currentConfig = MESSAGE_CONFIGS[activeMessageType];
  const currentPayload = stepPayloads[activeStepIndex] || (currentConfig?.prefill ? { ...currentConfig.prefill } : {});
  const currentResult = stepResults[activeStepIndex];

  // Switch flow
  const handleSwitchFlow = (newFlowId: string) => {
    setActiveFlowId(newFlowId);
    navigate(`/orchestrator/${newFlowId}`);
  };

  // Update a form field in the current step with real-time validation
  const handleFieldChange = (key: string, value: any) => {
    setStepPayloads(prev => ({
      ...prev,
      [activeStepIndex]: {
        ...(prev[activeStepIndex] || {}),
        [key]: value,
      },
    }));

    // If field has been touched, validate immediately
    if (touched[key] && currentConfig) {
      let fieldDef;
      for (const sec of currentConfig.sections) {
        fieldDef = sec.fields.find(f => f.key === key);
        if (fieldDef) break;
      }
      if (fieldDef) {
        const res = validateFormField(fieldDef, value);
        setErrors(prevErr => {
          const nextErr = { ...prevErr };
          if (!res.valid && res.error) {
            nextErr[key] = res.error;
          } else {
            delete nextErr[key];
          }
          return nextErr;
        });
      }
    }

    // If user edited an auto-injected field, clear its sparkle
    if (autoInjectedKeysByStep[activeStepIndex]?.has(key)) {
      setAutoInjectedKeysByStep(prev => {
        const nextSet = new Set(prev[activeStepIndex]);
        nextSet.delete(key);
        return { ...prev, [activeStepIndex]: nextSet };
      });
    }
  };

  // On blur field validation
  const handleFieldBlur = (key: string) => {
    setTouched(prev => ({ ...prev, [key]: true }));

    if (currentConfig) {
      let fieldDef;
      for (const sec of currentConfig.sections) {
        fieldDef = sec.fields.find(f => f.key === key);
        if (fieldDef) break;
      }
      if (fieldDef) {
        const res = validateFormField(fieldDef, currentPayload[key]);
        setErrors(prevErr => {
          const nextErr = { ...prevErr };
          if (!res.valid && res.error) {
            nextErr[key] = res.error;
          } else {
            delete nextErr[key];
          }
          return nextErr;
        });
      }
    }
  };

  // Reset form to spec prefill plus context overlay
  const handleResetToPrefill = () => {
    const specPrefill = currentConfig?.prefill ? { ...currentConfig.prefill } : {};
    const { prefill, autoInjectedKeys } = computeClientNextStepPrefill(
      activeFlowId,
      activeStepIndex,
      activeMessageType,
      context,
      specPrefill
    );

    setStepPayloads(prev => ({ ...prev, [activeStepIndex]: prefill }));
    setAutoInjectedKeysByStep(prev => ({ ...prev, [activeStepIndex]: autoInjectedKeys }));
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
    showToast(`Loaded pre-filled spec data for ${activeMessageType}`, 'info');
  };

  // Clear current step form
  const handleClearForm = () => {
    setStepPayloads(prev => ({ ...prev, [activeStepIndex]: {} }));
    setAutoInjectedKeysByStep(prev => ({ ...prev, [activeStepIndex]: new Set() }));
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
    showToast(`Cleared form for Step ${activeStepIndex + 1}`, 'info');
  };

  // Switch message variant for current step (e.g. pain.008 vs pacs.003)
  const handleVariantChange = (msgType: string) => {
    setStepVariants(prev => ({ ...prev, [activeStepIndex]: msgType }));
    const specPrefill = MESSAGE_CONFIGS[msgType]?.prefill ? { ...MESSAGE_CONFIGS[msgType].prefill } : {};
    const { prefill, autoInjectedKeys } = computeClientNextStepPrefill(
      activeFlowId,
      activeStepIndex,
      msgType,
      context,
      specPrefill
    );
    setStepPayloads(prev => ({ ...prev, [activeStepIndex]: prefill }));
    setAutoInjectedKeysByStep(prev => ({ ...prev, [activeStepIndex]: autoInjectedKeys }));
    setErrors({});
    setTouched({});
    setShowValidationSummary(false);
  };

  // Build clean payload across all sections (mirroring Pipeline Execution MessageConfigurator)
  const buildCurrentPayload = (): Record<string, any> => {
    const payload: Record<string, any> = {};
    if (!currentConfig) return currentPayload;

    currentConfig.sections.forEach(section => {
      section.fields.forEach(field => {
        const val = currentPayload[field.key];
        if (val !== undefined && val !== '') {
          // For AMOUNT fields, preserve exact 2-decimal string formatting
          if (field.ruleType === 'AMOUNT') {
            payload[field.key] = val;
          } else if (field.type === 'number') {
            const num = parseFloat(val);
            if (!isNaN(num)) payload[field.key] = num;
          } else {
            payload[field.key] = val;
          }
        }
      });
    });

    // Also preserve any additional keys in currentPayload (e.g. custom parameters)
    Object.keys(currentPayload).forEach(k => {
      if (payload[k] === undefined && currentPayload[k] !== undefined && currentPayload[k] !== '') {
        payload[k] = currentPayload[k];
      }
    });

    return payload;
  };

  // Execute single step (GENERATE or SEND)
  const handleExecuteStep = async (action: 'GENERATE' | 'SEND') => {
    if (!currentConfig) return;

    // 1. Validate all fields across all sections, exactly like MessageConfigurator
    const formErrors = validateMessageForm(currentConfig.sections, currentPayload);
    const errorKeys = Object.keys(formErrors);

    if (errorKeys.length > 0) {
      const allTouched: Record<string, boolean> = { ...touched };
      currentConfig.sections.forEach(sec => {
        sec.fields.forEach(f => {
          allTouched[f.key] = true;
        });
      });

      setTouched(allTouched);
      setErrors(formErrors);
      setShowValidationSummary(true);
      showToast(`${errorKeys.length} validation issue(s) detected in ${activeMessageType}. Please check fields.`, 'error');
      return;
    }

    setShowValidationSummary(false);
    setIsExecutingStep(true);
    setStepStatuses(prev => ({ ...prev, [activeStepIndex]: 'running' }));

    const payload = buildCurrentPayload();

    try {
      const res = await executeFlowStep(
        activeFlowId,
        activeStepIndex,
        activeMessageType,
        action,
        payload,
        context
      );

      if (res.success) {
        // Merge extracted context
        const newContext = { ...context, ...(res.extractedContext || {}) };
        setContext(newContext);
        setStepResults(prev => ({ ...prev, [activeStepIndex]: res }));
        setStepStatuses(prev => ({ ...prev, [activeStepIndex]: 'completed' }));
        showToast(
          `${currentStep.title} (${activeMessageType}) executed successfully! Captured ${
            Object.keys(res.extractedContext || {}).length
          } context variables.`,
          'success'
        );

        // Pre-fill next step automatically with full spec data + new context
        const nextIdx = activeStepIndex + 1;
        if (nextIdx < activeFlow.steps.length) {
          const nextStepDef = activeFlow.steps[nextIdx];
          const nextMsgType = stepVariants[nextIdx] || nextStepDef.messageType;
          const nextBaseSpec = MESSAGE_CONFIGS[nextMsgType]?.prefill
            ? { ...MESSAGE_CONFIGS[nextMsgType].prefill }
            : {};
          const existingNextPayload = stepPayloads[nextIdx] || {};

          const { prefill, autoInjectedKeys } = computeClientNextStepPrefill(
            activeFlowId,
            nextIdx,
            nextMsgType,
            newContext,
            { ...nextBaseSpec, ...existingNextPayload }
          );
          setStepPayloads(prev => ({ ...prev, [nextIdx]: prefill }));
          setAutoInjectedKeysByStep(prev => ({ ...prev, [nextIdx]: autoInjectedKeys }));
        }
      } else {
        setStepStatuses(prev => ({ ...prev, [activeStepIndex]: 'error' }));
        setStepResults(prev => ({ ...prev, [activeStepIndex]: res }));
        showToast(`Step failed: ${res.errorMessage || 'Execution error'}`, 'error');
      }
    } catch (err: any) {
      setStepStatuses(prev => ({ ...prev, [activeStepIndex]: 'error' }));
      showToast(`Step execution failed: ${err.message || err}`, 'error');
    } finally {
      setIsExecutingStep(false);
    }
  };

  // Proceed to next step
  const handleProceedNext = () => {
    if (activeStepIndex + 1 < activeFlow.steps.length) {
      const nextIdx = activeStepIndex + 1;
      setActiveStepIndex(nextIdx);
      if (stepResults[nextIdx]?.plainXml) {
        setActiveInspectorTab('plainXml');
      }
    }
  };

  // 1-Click Auto Run Flow with full spec data and dynamic context
  const handleAutoRun = async () => {
    setIsAutoRunning(true);
    showToast(`Starting automated journey for ${activeFlow.name}...`, 'info');

    try {
      const step0Payload = stepPayloads[0] || (MESSAGE_CONFIGS[activeFlow.steps[0].messageType]?.prefill ? { ...MESSAGE_CONFIGS[activeFlow.steps[0].messageType].prefill } : {});
      const res = await autoRunWorkflow(activeFlowId, 'GENERATE', step0Payload, context);

      if (res.success) {
        const updatedResults: Record<number, StepExecutionResult> = {};
        const updatedStatuses: Record<number, StepExecutionStatus> = {};

        res.stepsTranscript.forEach(stepRes => {
          updatedResults[stepRes.stepIndex] = {
            success: true,
            messageId: stepRes.messageId,
            plainXml: stepRes.plainXml,
            signedXml: stepRes.signedXml,
            serviceResponse: stepRes.serviceResponse,
            executionTime: stepRes.executionTime,
            extractedContext: stepRes.extractedContext,
            actionTaken: 'GENERATE',
            executedAt: new Date().toLocaleTimeString(),
          };
          updatedStatuses[stepRes.stepIndex] = 'completed';
        });

        setStepResults(updatedResults);
        setStepStatuses(updatedStatuses);
        setContext(res.finalContext || {});
        setActiveStepIndex(activeFlow.steps.length - 1);
        showToast(`🎉 Entire ${activeFlow.name} completed successfully in ${res.executionDuration}!`, 'success');
      } else {
        showToast(`Auto-run error: ${res.errorMessage}`, 'error');
      }
    } catch (err: any) {
      console.warn('Auto run failed, executing sequential fallback', err);
      // Fallback: run each step sequentially in frontend with full spec prefill
      let currentCtx = { ...context };
      for (let i = 0; i < activeFlow.steps.length; i++) {
        setActiveStepIndex(i);
        setStepStatuses(prev => ({ ...prev, [i]: 'running' }));
        const stepDef = activeFlow.steps[i];
        const msgType = stepVariants[i] || stepDef.messageType;

        const specBase = MESSAGE_CONFIGS[msgType]?.prefill ? { ...MESSAGE_CONFIGS[msgType].prefill } : {};
        const { prefill, autoInjectedKeys } = computeClientNextStepPrefill(
          activeFlowId,
          i,
          msgType,
          currentCtx,
          { ...specBase, ...(stepPayloads[i] || {}) }
        );
        setStepPayloads(prev => ({ ...prev, [i]: prefill }));
        setAutoInjectedKeysByStep(prev => ({ ...prev, [i]: autoInjectedKeys }));

        const stepRes = await executeFlowStep(activeFlowId, i, msgType, 'GENERATE', prefill, currentCtx);
        if (!stepRes.success) {
          setStepStatuses(prev => ({ ...prev, [i]: 'error' }));
          showToast(`Step ${i + 1} (${msgType}) failed: ${stepRes.errorMessage}`, 'error');
          break;
        }

        currentCtx = { ...currentCtx, ...(stepRes.extractedContext || {}) };
        setContext(currentCtx);
        setStepResults(prev => ({ ...prev, [i]: stepRes }));
        setStepStatuses(prev => ({ ...prev, [i]: 'completed' }));
      }
      showToast(`Completed workflow simulation`, 'success');
    } finally {
      setIsAutoRunning(false);
    }
  };

  // Copy to clipboard
  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    showToast(`Copied ${label} to clipboard!`, 'info');
  };

  // Export full journey run JSON
  const handleExportJourney = () => {
    const exportData = {
      flowId: activeFlow.id,
      flowName: activeFlow.name,
      exportedAt: new Date().toISOString(),
      finalContext: context,
      steps: activeFlow.steps.map((step, idx) => ({
        stepIndex: idx,
        title: step.title,
        messageType: stepVariants[idx] || step.messageType,
        status: stepStatuses[idx] || 'pending',
        payload: stepPayloads[idx] || {},
        result: stepResults[idx] || null,
      })),
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `nps-workflow-${activeFlow.id}-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Workflow journey transcript exported successfully!', 'success');
  };

  // Quick navigation to Health Check with current XML
  const handleOpenInHealthCheck = () => {
    if (!currentResult?.plainXml) {
      showToast('No XML generated yet for this step', 'error');
      return;
    }
    localStorage.setItem('nps_inspector_xml', currentResult.plainXml);
    localStorage.setItem('nps_inspector_title', `${currentStep.title} (${activeMessageType})`);
    navigate('/inspector', {
      state: {
        xml: currentResult.plainXml,
        title: `${currentStep.title} (${activeMessageType})`,
      },
    });
  };

  // Quick navigation to Diff Checker with current XML
  const handleOpenInDiff = () => {
    if (!currentResult?.plainXml) {
      showToast('No XML generated yet for this step', 'error');
      return;
    }
    localStorage.setItem('nps_diff_source_xml', currentResult.plainXml);
    localStorage.setItem('nps_diff_source_title', `${currentStep.title} (${activeMessageType})`);
    navigate('/diff', {
      state: {
        xml: currentResult.plainXml,
        title: `${currentStep.title} (${activeMessageType})`,
      },
    });
  };

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username
    : 'Developer';

  const completedStepsCount = Object.values(stepStatuses).filter(s => s === 'completed').length;
  const progressPercent = Math.round((completedStepsCount / activeFlow.steps.length) * 100);
  const errorCount = useMemo(() => Object.keys(errors).length, [errors]);

  return (
    <div className="flex flex-col h-screen overflow-hidden bg-[#f6f9f7] font-sans text-gray-800">
      {/* Toast Alert */}
      {toast && (
        <div
          className={`fixed top-4 right-6 z-50 px-4 py-3 rounded-xl shadow-xl flex items-center gap-3 text-[13px] font-semibold transition-all animate-in fade-in slide-in-from-top-2 border ${
            toast.type === 'error'
              ? 'bg-red-50 text-red-700 border-red-200'
              : toast.type === 'info'
              ? 'bg-blue-50 text-blue-700 border-blue-200'
              : 'bg-emerald-50 text-emerald-800 border-emerald-200'
          }`}
        >
          <span className="text-[16px]">
            {toast.type === 'error' ? '⚠️' : toast.type === 'info' ? 'ℹ️' : '✅'}
          </span>
          <span>{toast.message}</span>
        </div>
      )}

      {/* Main App Header */}
      <header className="h-16 flex-shrink-0 bg-white border-b border-[#e4e9e6] flex items-center justify-between px-6 z-10">
        <div className="flex items-center gap-3.5">
          <div
            className="w-[38px] h-[38px] rounded-[9px] flex items-center justify-center text-white font-bold text-[12px] flex-shrink-0 cursor-pointer"
            style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)', boxShadow: '0 4px 12px rgba(21,128,61,0.3)' }}
            onClick={() => navigate('/workbench')}
          >
            NPS
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-[16px] font-bold text-[#0f3a22] m-0 leading-tight">Flow Orchestrator</h1>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#e6f6ec] text-[#15803d] border border-[#c4ebd3]">
                Guided Journeys
              </span>
            </div>
            <p className="text-[11.5px] text-[#6b7280] m-0 mt-[1px]">
              Multi-step ISO 20022 workflow sequences with automated context passing
            </p>
          </div>
        </div>

        {/* Top Nav Mode Switcher Tabs */}
        <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 shadow-inner">
          <button
            type="button"
            onClick={() => navigate('/workbench')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Message Workbench
          </button>
          <button
            type="button"
            className="px-3.5 py-1.5 text-[12px] font-bold rounded-lg bg-white text-[#16a34a] shadow-[0_1px_3px_rgba(0,0,0,0.08)] flex items-center gap-1.5 cursor-default border-0"
          >
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            Flow Orchestrator
          </button>
          <button
            type="button"
            onClick={() => navigate('/inspector')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Fix My XML & Health Check
          </button>
          <button
            type="button"
            onClick={() => navigate('/diff')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Diff Checker
          </button>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 border border-[#e4e9e6] rounded-lg px-3 py-1.5 bg-gray-50">
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            <span className="text-[12.5px] font-semibold text-gray-700">{displayName}</span>
          </div>
        </div>
      </header>

      {/* Journey Selector Bar & Global Actions */}
      <div className="bg-white border-b border-[#e4e9e6] px-6 py-2.5 flex items-center justify-between flex-shrink-0 gap-4">
        {/* Journey Switcher Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto py-0.5">
          {ORCHESTRATOR_FLOWS.map(flow => {
            const isSelected = flow.id === activeFlowId;
            return (
              <button
                key={flow.id}
                type="button"
                onClick={() => handleSwitchFlow(flow.id)}
                className={`flex items-center gap-2.5 px-4 py-2 rounded-xl text-[12.5px] font-semibold transition-all cursor-pointer whitespace-nowrap border ${
                  isSelected
                    ? 'bg-[#e6f6ec] text-[#0f3a22] border-[#22a05a] shadow-[0_2px_8px_rgba(34,160,90,0.15)] ring-1 ring-[#22a05a]'
                    : 'bg-white text-gray-600 border-[#e1e9e3] hover:border-gray-300 hover:bg-gray-50'
                }`}
              >
                <span className="text-[15px]">{flow.icon}</span>
                <span>{flow.name}</span>
                <span
                  className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                    isSelected ? 'bg-[#22a05a] text-white' : 'bg-gray-100 text-gray-500'
                  }`}
                >
                  {flow.steps.length} Steps
                </span>
              </button>
            );
          })}
        </div>

        {/* Global Journey Actions */}
        <div className="flex items-center gap-2.5 flex-shrink-0">
          <button
            type="button"
            onClick={handleAutoRun}
            disabled={isAutoRunning}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-[#16a34a] to-[#15803d] text-white text-[12.5px] font-bold shadow-md hover:from-[#15803d] hover:to-[#0f5132] transition-all cursor-pointer disabled:opacity-50"
          >
            {isAutoRunning ? (
              <>
                <svg className="animate-spin h-3.5 w-3.5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                Auto-Running Journey...
              </>
            ) : (
              <>
                <span>⚡</span>
                1-Click Auto Run
              </>
            )}
          </button>

          <button
            type="button"
            onClick={() => initFlow(activeFlow)}
            className="px-3 py-2 text-[12px] font-semibold text-gray-600 hover:text-gray-900 border border-[#e1e9e3] rounded-xl hover:bg-gray-50 transition-all cursor-pointer"
            title="Reset Journey"
          >
            🔄 Reset Journey
          </button>

          <button
            type="button"
            onClick={handleExportJourney}
            className="px-3 py-2 text-[12px] font-semibold text-gray-600 hover:text-gray-900 border border-[#e1e9e3] rounded-xl hover:bg-gray-50 transition-all cursor-pointer"
            title="Export Journey Transcript JSON"
          >
            📤 Export Run
          </button>
        </div>
      </div>

      {/* Stepper Timeline & Progression */}
      <div className="bg-[#f2f7f4] border-b border-[#e1e9e3] px-6 py-3 flex-shrink-0">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <span className="text-[12px] font-bold text-[#0f3a22] uppercase tracking-wider">
              Journey Progression:
            </span>
            <span className="text-[12px] font-semibold text-[#16a34a]">
              {completedStepsCount} of {activeFlow.steps.length} Steps Completed ({progressPercent}%)
            </span>
          </div>

          <div className="w-48 bg-gray-200 rounded-full h-2 overflow-hidden shadow-inner">
            <div
              className="bg-gradient-to-r from-[#22a05a] to-[#15803d] h-2 transition-all duration-500 rounded-full"
              style={{ width: `${progressPercent}%` }}
            ></div>
          </div>
        </div>

        {/* Step Nodes Row */}
        <div className="flex items-center gap-2 overflow-x-auto py-1">
          {activeFlow.steps.map((step, idx) => {
            const isCurrent = idx === activeStepIndex;
            const status = stepStatuses[idx] || 'pending';
            const hasCompleted = status === 'completed';
            const hasError = status === 'error';
            const isRunning = status === 'running';

            return (
              <React.Fragment key={step.stepId}>
                <div
                  onClick={() => setActiveStepIndex(idx)}
                  className={`flex items-center gap-3 px-3.5 py-2 rounded-xl border transition-all cursor-pointer flex-shrink-0 select-none ${
                    isCurrent
                      ? 'bg-white border-[#22a05a] shadow-[0_3px_10px_rgba(34,160,90,0.2)] ring-2 ring-[#22a05a]/30'
                      : hasCompleted
                      ? 'bg-[#eaf5ee] border-[#b9e3cb] hover:bg-[#e0f0e6]'
                      : 'bg-white/70 border-gray-200 hover:bg-white text-gray-500'
                  }`}
                >
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center font-bold text-[11px] flex-shrink-0 ${
                      hasCompleted
                        ? 'bg-[#16a34a] text-white shadow-sm'
                        : isRunning
                        ? 'bg-blue-500 text-white animate-pulse'
                        : hasError
                        ? 'bg-red-500 text-white'
                        : isCurrent
                        ? 'bg-[#0f3a22] text-white'
                        : 'bg-gray-200 text-gray-600'
                    }`}
                  >
                    {hasCompleted ? '✓' : idx + 1}
                  </div>

                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-[12px] font-bold text-gray-800 leading-tight">
                        {step.title}
                      </span>
                      <span className="font-mono text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-600 font-semibold">
                        {stepVariants[idx] || step.messageType}
                      </span>
                    </div>
                    <div className="text-[10px] text-gray-500 leading-tight mt-0.5">
                      {step.role}
                    </div>
                  </div>
                </div>

                {idx < activeFlow.steps.length - 1 && (
                  <div className="text-gray-300 font-bold text-[14px] flex-shrink-0 px-1">
                    ➔
                  </div>
                )}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      {/* Active Session Context Bar (Shows live accumulated context variables) */}
      <div className="bg-[#0b2818] border-b border-[#133d26] px-6 py-2 text-[#dff3e6] flex items-center justify-between flex-shrink-0 gap-4 overflow-x-auto">
        <div className="flex items-center gap-2 flex-shrink-0">
          <span className="text-[11px] font-bold uppercase tracking-wider text-[#7fb894] flex items-center gap-1">
            <span>🔗</span> Flow Context:
          </span>
        </div>

        <div className="flex items-center gap-3 overflow-x-auto py-0.5 flex-1">
          {Object.keys(context).length === 0 ? (
            <span className="text-[11.5px] text-[#7fb894] italic">
              Context is currently empty. Execute Step 1 to extract session variables (MandateId, SessionID, EndToEndId, etc.).
            </span>
          ) : (
            <>
              {context.mandateId && (
                <div
                  onClick={() => copyToClipboard(context.mandateId, 'MandateId')}
                  className="flex items-center gap-1.5 bg-[#123e26] hover:bg-[#1a5534] border border-[#22a05a]/50 rounded-lg px-2.5 py-1 text-[11px] cursor-pointer transition-colors"
                  title="Click to copy MandateId"
                >
                  <span className="text-[#9fd8b3] font-semibold">MandateId:</span>
                  <span className="font-mono text-white font-bold">{context.mandateId}</span>
                  <span className="text-[9px] text-[#7fb894]">📋</span>
                </div>
              )}

              {(context.nameEnquiryMsgId || context.sessionId) && (
                <div
                  onClick={() =>
                    copyToClipboard(context.nameEnquiryMsgId || context.sessionId, 'NameEnquiryMsgId')
                  }
                  className="flex items-center gap-1.5 bg-[#123e26] hover:bg-[#1a5534] border border-[#22a05a]/50 rounded-lg px-2.5 py-1 text-[11px] cursor-pointer transition-colors"
                  title="Click to copy Session / Name Enquiry Reference"
                >
                  <span className="text-[#9fd8b3] font-semibold">SessionID:</span>
                  <span className="font-mono text-white font-bold truncate max-w-[140px]">
                    {context.nameEnquiryMsgId || context.sessionId}
                  </span>
                  <span className="text-[9px] text-[#7fb894]">📋</span>
                </div>
              )}

              {context.endToEndId && (
                <div
                  onClick={() => copyToClipboard(context.endToEndId, 'EndToEndId')}
                  className="flex items-center gap-1.5 bg-[#123e26] hover:bg-[#1a5534] border border-[#22a05a]/50 rounded-lg px-2.5 py-1 text-[11px] cursor-pointer transition-colors"
                  title="Click to copy EndToEndId"
                >
                  <span className="text-[#9fd8b3] font-semibold">EndToEndId:</span>
                  <span className="font-mono text-white font-bold truncate max-w-[140px]">
                    {context.endToEndId}
                  </span>
                  <span className="text-[9px] text-[#7fb894]">📋</span>
                </div>
              )}

              {context.amount && (
                <div className="flex items-center gap-1.5 bg-[#123e26] border border-[#22a05a]/40 rounded-lg px-2.5 py-1 text-[11px]">
                  <span className="text-[#9fd8b3] font-semibold">Amount:</span>
                  <span className="font-mono text-emerald-300 font-bold">
                    NGN {Number(context.amount).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                </div>
              )}

              {context.debtorAccountNumber && (
                <div className="flex items-center gap-1.5 bg-[#123e26] border border-[#22a05a]/40 rounded-lg px-2.5 py-1 text-[11px]">
                  <span className="text-[#9fd8b3] font-semibold">Debtor:</span>
                  <span className="font-mono text-gray-200">
                    {context.debtorAccountNumber} ({context.debtorName || 'Debtor'})
                  </span>
                </div>
              )}

              {context.creditorAccountNumber && (
                <div className="flex items-center gap-1.5 bg-[#123e26] border border-[#22a05a]/40 rounded-lg px-2.5 py-1 text-[11px]">
                  <span className="text-[#9fd8b3] font-semibold">Creditor:</span>
                  <span className="font-mono text-gray-200">
                    {context.creditorAccountNumber} ({context.creditorName || 'Creditor'})
                  </span>
                </div>
              )}
            </>
          )}
        </div>

        {Object.keys(context).length > 0 && (
          <button
            type="button"
            onClick={() => copyToClipboard(JSON.stringify(context, null, 2), 'Session Context Dictionary')}
            className="text-[11px] font-semibold text-[#9fd8b3] hover:text-white flex items-center gap-1 flex-shrink-0 cursor-pointer"
          >
            <span>📋</span> Copy Context JSON
          </button>
        )}
      </div>

      {/* Main Split Layout: Form Configurator on Left, Output & Inspector on Right */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Pane: Full Message Form Configurator (Exactly like Pipeline Execution) */}
        <div className="w-1/2 border-r border-[#e4e9e6] flex flex-col bg-white overflow-hidden">
          {/* Panel Header */}
          <div className="px-5 py-3.5 border-b border-[#e4e9e6] bg-white flex items-center justify-between flex-shrink-0">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-[14px] font-bold tracking-[0.2px] text-[#111827]">
                  {currentStep.title}
                </span>
                <span
                  className="text-[11px] font-bold px-2.5 py-0.5 rounded-[6px]"
                  style={{
                    fontFamily: "'JetBrains Mono', monospace",
                    background: '#e6f6ec',
                    color: '#15803d',
                  }}
                >
                  ISO: {currentConfig?.isoCode.toUpperCase() || activeMessageType}
                </span>
                <span className="text-[10px] font-bold uppercase tracking-wider text-[#15803d] bg-[#e6f6ec] px-2 py-0.5 rounded-full border border-[#c4ebd3]">
                  {currentStep.role}
                </span>
              </div>
              <p className="text-[11.5px] text-[#6b7280] m-0 mt-0.5">
                {currentStep.description}
              </p>
            </div>

            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={handleClearForm}
                className="text-[12px] text-[#6b7280] hover:text-[#dc2626] cursor-pointer flex items-center gap-1 transition-colors bg-transparent border-0"
              >
                &#x21BA; Clear Form
              </button>
            </div>
          </div>

          {/* Variant Switcher (e.g. pain.008 vs pacs.003 for direct debit step 3) */}
          {currentStep.variantOptions && currentStep.variantOptions.length > 1 && (
            <div className="px-5 py-2.5 bg-[#f8faf9] border-b border-[#e4e9e6] flex items-center gap-2 flex-shrink-0">
              <span className="text-[11.5px] font-bold text-gray-700">Message Variant:</span>
              <div className="flex bg-[#edf2ee] p-0.5 rounded-lg border border-[#e1e9e3]">
                {currentStep.variantOptions.map(variant => (
                  <button
                    key={variant.messageType}
                    type="button"
                    onClick={() => handleVariantChange(variant.messageType)}
                    className={`px-2.5 py-1 text-[11.5px] font-semibold rounded-md transition-all cursor-pointer ${
                      activeMessageType === variant.messageType
                        ? 'bg-white text-[#15803d] shadow-sm font-bold'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    {variant.label}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Validation Summary Warning (if errors exist upon submission) */}
          {showValidationSummary && errorCount > 0 && (
            <div className="bg-red-50 border-b border-red-200 px-5 py-2 text-[11.5px] text-red-800 flex items-center justify-between animate-fadeIn flex-shrink-0">
              <div className="flex items-center gap-2">
                <span className="text-red-600 font-bold">⚠</span>
                <span>
                  <strong>{errorCount} field validation error{errorCount > 1 ? 's' : ''} found.</strong> Please review the highlighted fields before executing.
                </span>
              </div>
              <button
                type="button"
                onClick={() => setShowValidationSummary(false)}
                className="text-red-500 hover:text-red-700 font-bold text-xs cursor-pointer"
              >
                ✕
              </button>
            </div>
          )}

          {/* Context Auto-Injected Notification Banner */}
          {autoInjectedKeysByStep[activeStepIndex] && autoInjectedKeysByStep[activeStepIndex].size > 0 && (
            <div className="bg-[#e8f7ee] border-b border-[#aee4c3] px-5 py-2 flex items-center gap-2.5 text-[11.5px] text-[#0f5132] flex-shrink-0">
              <span className="text-[14px]">✨</span>
              <span>
                <strong>Context Auto-Populated:</strong> {autoInjectedKeysByStep[activeStepIndex].size} field(s) were automatically injected from preceding steps. You can freely modify any value.
              </span>
            </div>
          )}

          {/* Scrollable Form Body with All Sections from MESSAGE_CONFIGS */}
          <div className="flex-1 overflow-y-auto p-5">
            {currentConfig ? (
              currentConfig.sections.map(section => (
                <FormFieldset
                  key={section.title}
                  section={section}
                  formData={currentPayload}
                  errors={errors}
                  touched={touched}
                  onChange={handleFieldChange}
                  onBlur={handleFieldBlur}
                  injectedKeys={autoInjectedKeysByStep[activeStepIndex]}
                />
              ))
            ) : (
              <div className="p-4 text-gray-500">Configuration not found for {activeMessageType}</div>
            )}

            {/* Action Bar (Prefill + Generate + Send + Proceed) */}
            <div className="mt-4 pt-4 border-t border-[#e4e9e6] flex flex-col gap-3">
              <div className="flex items-center gap-2.5">
                <button
                  type="button"
                  onClick={handleResetToPrefill}
                  className="flex-1 border border-[#16a34a] text-[#15803d] bg-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer hover:bg-[#f3faf5] transition-colors flex items-center justify-center gap-1.5 shadow-sm"
                >
                  <span>📄</span>
                  <span>Load Pre-filled Spec Data</span>
                </button>

                <button
                  type="button"
                  onClick={() => handleExecuteStep('GENERATE')}
                  disabled={isExecutingStep || isAutoRunning}
                  className="flex-1 border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-1.5"
                  style={{
                    background: isExecutingStep ? '#6b7280' : 'linear-gradient(180deg, #16a34a, #15803d)',
                    boxShadow: isExecutingStep ? 'none' : '0 4px 12px rgba(21,128,61,0.28)',
                  }}
                >
                  <span>⚡</span>
                  <span>{isExecutingStep ? 'Generating...' : 'Generate ISO 20022 XML'}</span>
                </button>

                <button
                  type="button"
                  onClick={() => handleExecuteStep('SEND')}
                  disabled={isExecutingStep || isAutoRunning}
                  className="flex-1 border-0 text-white py-2.5 rounded-lg text-[12.5px] font-bold cursor-pointer transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-1.5 bg-[#0f3a22] hover:bg-[#1a5534] shadow-md"
                >
                  <span>🚀</span>
                  <span>{isExecutingStep ? 'Processing...' : 'Send to Pipeline'}</span>
                </button>
              </div>

              {activeStepIndex + 1 < activeFlow.steps.length && (
                <button
                  type="button"
                  onClick={handleProceedNext}
                  disabled={stepStatuses[activeStepIndex] !== 'completed'}
                  className="w-full py-2.5 rounded-lg border border-[#22a05a] bg-white text-[#15803d] hover:bg-[#e6f6ec] text-[12.5px] font-bold transition-all cursor-pointer disabled:opacity-40 disabled:pointer-events-none flex items-center justify-center gap-2 shadow-sm"
                >
                  <span>Proceed to Step {activeStepIndex + 2}: {activeFlow.steps[activeStepIndex + 1].title}</span>
                  <span>➔</span>
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Right Pane: Inspector (Plain XML / Signed XML / Service Response / Context Diff) */}
        <div className="w-1/2 flex flex-col bg-[#f9fbf9] overflow-hidden">
          {/* Inspector Header Tabs & Tools */}
          <div className="h-12 border-b border-[#e4e9e6] bg-white px-5 flex items-center justify-between flex-shrink-0">
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => setActiveInspectorTab('plainXml')}
                className={`px-3 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer ${
                  activeInspectorTab === 'plainXml'
                    ? 'bg-[#e6f6ec] text-[#15803d]'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Plain XML
              </button>
              <button
                type="button"
                onClick={() => setActiveInspectorTab('signedXml')}
                className={`px-3 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer ${
                  activeInspectorTab === 'signedXml'
                    ? 'bg-[#e6f6ec] text-[#15803d]'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Signed XML
              </button>
              <button
                type="button"
                onClick={() => setActiveInspectorTab('response')}
                className={`px-3 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer ${
                  activeInspectorTab === 'response'
                    ? 'bg-[#e6f6ec] text-[#15803d]'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Service Response
                {currentResult?.serviceResponse && (
                  <span className="ml-1.5 w-2 h-2 rounded-full bg-emerald-500 inline-block"></span>
                )}
              </button>
              <button
                type="button"
                onClick={() => setActiveInspectorTab('context')}
                className={`px-3 py-1.5 text-[12px] font-bold rounded-lg transition-all cursor-pointer ${
                  activeInspectorTab === 'context'
                    ? 'bg-[#e6f6ec] text-[#15803d]'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Step Context
              </button>
            </div>

            {/* Quick Cross-App Action Links */}
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleOpenInHealthCheck}
                disabled={!currentResult?.plainXml}
                className="px-2.5 py-1 text-[11px] font-semibold text-gray-700 bg-gray-50 border border-gray-200 rounded-lg hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-300 transition-all cursor-pointer disabled:opacity-40"
                title="Inspect this XML in Health Check & Fixer"
              >
                🔍 Health Check
              </button>

              <button
                type="button"
                onClick={handleOpenInDiff}
                disabled={!currentResult?.plainXml}
                className="px-2.5 py-1 text-[11px] font-semibold text-gray-700 bg-gray-50 border border-gray-200 rounded-lg hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-300 transition-all cursor-pointer disabled:opacity-40"
                title="Compare this XML in Diff Checker"
              >
                ⚖️ Diff Checker
              </button>
            </div>
          </div>

          {/* Inspector Tab Content Area */}
          <div className="flex-1 overflow-auto p-4 font-mono text-[12px] bg-[#0d2116] text-[#c9e8d4]">
            {activeInspectorTab === 'plainXml' && (
              <div>
                {currentResult?.plainXml ? (
                  <div className="relative">
                    <button
                      type="button"
                      onClick={() => copyToClipboard(currentResult.plainXml || '', 'Plain XML')}
                      className="absolute top-2 right-2 px-2.5 py-1 text-[11px] bg-[#1a402c] text-white rounded hover:bg-[#22553a] transition-all cursor-pointer"
                    >
                      📋 Copy XML
                    </button>
                    <pre className="whitespace-pre overflow-x-auto leading-5 text-[#b9f2cf]">
                      {currentResult.plainXml}
                    </pre>
                  </div>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-center text-gray-400 py-16">
                    <span className="text-[32px] mb-2">📄</span>
                    <p className="font-sans text-[13px] font-semibold text-gray-300">No XML Generated Yet</p>
                    <p className="font-sans text-[11.5px] text-gray-400 max-w-xs mt-1">
                      Click <strong>Generate ISO 20022 XML</strong> or <strong>Send to Pipeline</strong> to trigger this step and inspect the output.
                    </p>
                  </div>
                )}
              </div>
            )}

            {activeInspectorTab === 'signedXml' && (
              <div>
                {currentResult?.signedXml ? (
                  <div className="relative">
                    <button
                      type="button"
                      onClick={() => copyToClipboard(currentResult.signedXml || '', 'Signed XML')}
                      className="absolute top-2 right-2 px-2.5 py-1 text-[11px] bg-[#1a402c] text-white rounded hover:bg-[#22553a] transition-all cursor-pointer"
                    >
                      📋 Copy Signed XML
                    </button>
                    <pre className="whitespace-pre overflow-x-auto leading-5 text-[#9ee7b9]">
                      {currentResult.signedXml}
                    </pre>
                  </div>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-center text-gray-400 py-16">
                    <span className="text-[32px] mb-2">🔏</span>
                    <p className="font-sans text-[13px] font-semibold text-gray-300">Signed XML Not Generated</p>
                    <p className="font-sans text-[11.5px] text-gray-400 max-w-xs mt-1">
                      Generate XML to produce the canonical signed payload with XML-DSig envelope.
                    </p>
                  </div>
                )}
              </div>
            )}

            {activeInspectorTab === 'response' && (
              <div>
                {currentResult?.serviceResponse ? (
                  <div className="space-y-4 font-sans text-gray-200">
                    <div className="flex items-center gap-3 bg-[#133222] p-3 rounded-lg border border-[#1e4d34]">
                      <div
                        className={`w-3 h-3 rounded-full ${
                          currentResult.serviceResponse.statusCode === 200 ? 'bg-emerald-400' : 'bg-red-400'
                        }`}
                      ></div>
                      <div>
                        <div className="text-[13px] font-bold text-white">
                          HTTP Status: {currentResult.serviceResponse.statusCode}
                        </div>
                        <div className="text-[11px] text-gray-400 font-mono">
                          Latency: {currentResult.serviceResponse.executionTimeMs}ms &bull; Timestamp: {currentResult.serviceResponse.timestamp || 'Now'}
                        </div>
                      </div>
                    </div>

                    <div>
                      <div className="text-[11px] font-bold uppercase tracking-wider text-[#7fb894] mb-1.5 font-mono">
                        Raw Service Response Body:
                      </div>
                      <pre className="p-3 bg-[#08170f] rounded-lg border border-[#1a402c] text-[#a4efc1] font-mono text-[11.5px] overflow-x-auto">
                        {currentResult.serviceResponse.rawResponseBody || 'No response body returned'}
                      </pre>
                    </div>
                  </div>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-center text-gray-400 py-16">
                    <span className="text-[32px] mb-2">📡</span>
                    <p className="font-sans text-[13px] font-semibold text-gray-300">No Network Dispatch Yet</p>
                    <p className="font-sans text-[11.5px] text-gray-400 max-w-xs mt-1">
                      Click <strong>Send to Pipeline</strong> to transmit the signed XML to the NIBSS / NPS simulator.
                    </p>
                  </div>
                )}
              </div>
            )}

            {activeInspectorTab === 'context' && (
              <div className="space-y-4 font-sans text-gray-200">
                <div>
                  <h3 className="text-[13px] font-bold text-white mb-1">
                    Variables Extracted by This Step:
                  </h3>
                  <p className="text-[11px] text-gray-400 mb-3">
                    These values were extracted from the request/response and automatically injected into subsequent journey steps.
                  </p>

                  {currentResult?.extractedContext && Object.keys(currentResult.extractedContext).length > 0 ? (
                    <div className="grid grid-cols-2 gap-2">
                      {Object.entries(currentResult.extractedContext).map(([k, v]) => (
                        <div key={k} className="p-2.5 bg-[#123020] rounded-lg border border-[#1b4830]">
                          <div className="text-[10.5px] font-bold uppercase text-[#7fb894] font-mono">{k}</div>
                          <div className="text-[12px] font-mono text-white break-all mt-0.5">{v}</div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-[11.5px] text-gray-400 italic">No variables extracted yet. Execute this step first.</p>
                  )}
                </div>

                <div className="pt-3 border-t border-[#1a402c]">
                  <h3 className="text-[13px] font-bold text-white mb-1">
                    Cumulative Session Context:
                  </h3>
                  <pre className="p-3 bg-[#08170f] rounded-lg border border-[#1a402c] text-[#a4efc1] font-mono text-[11.5px] overflow-x-auto">
                    {JSON.stringify(context, null, 2)}
                  </pre>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

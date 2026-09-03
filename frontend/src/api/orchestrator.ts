import { apiClient } from './client';
import {
  FlowDefinition,
  StepExecutionResult,
  FlowMapNextStepResult,
  FlowAutoRunResult,
} from '../types/orchestrator';
import { generateXml, sendMessage } from './workbench';

export const getOrchestratorFlows = async (): Promise<FlowDefinition[]> => {
  const res = await apiClient.get<FlowDefinition[]>('/api/orchestrator/flows');
  return res.data;
};

export const getOrchestratorFlowById = async (flowId: string): Promise<FlowDefinition> => {
  const res = await apiClient.get<FlowDefinition>(`/api/orchestrator/flows/${flowId}`);
  return res.data;
};

export const executeFlowStep = async (
  flowId: string,
  stepIndex: number,
  messageType: string,
  action: 'GENERATE' | 'SEND',
  payload: Record<string, any>,
  currentContext: Record<string, string>
): Promise<StepExecutionResult> => {
  try {
    const res = await apiClient.post<any>('/api/orchestrator/execute-step', {
      flowId,
      stepIndex,
      messageType,
      action,
      payload,
      currentContext,
    });
    return {
      success: res.data.success,
      messageId: res.data.messageId,
      plainXml: res.data.plainXml,
      signedXml: res.data.signedXml,
      serviceResponse: res.data.serviceResponse,
      executionTime: res.data.executionTime,
      extractedContext: res.data.extractedContext,
      errorMessage: res.data.errorMessage,
      executedAt: new Date().toLocaleTimeString(),
      actionTaken: action,
    };
  } catch (err: any) {
    // Graceful fallback to direct workbench pipeline if orchestrator endpoint is not reached
    console.warn('Orchestrator endpoint failed, falling back to standard pipeline', err);
    if (action === 'SEND') {
      const sendRes = await sendMessage(messageType as any, payload);
      return {
        success: true,
        messageId: sendRes.messageId,
        plainXml: sendRes.plainXml,
        signedXml: sendRes.signedXml,
        serviceResponse: sendRes.serviceResponse,
        executedAt: new Date().toLocaleTimeString(),
        actionTaken: 'SEND',
      };
    } else {
      const genRes = await generateXml(messageType as any, payload);
      return {
        success: true,
        messageId: genRes.messageId,
        plainXml: genRes.plainXml,
        signedXml: genRes.signedXml,
        executedAt: new Date().toLocaleTimeString(),
        actionTaken: 'GENERATE',
      };
    }
  }
};

export const mapNextStepContext = async (
  flowId: string,
  targetStepIndex: number,
  targetMessageType: string,
  context: Record<string, string>,
  previousStepPayload?: Record<string, any>
): Promise<FlowMapNextStepResult> => {
  const res = await apiClient.post<FlowMapNextStepResult>('/api/orchestrator/map-next-step', {
    flowId,
    targetStepIndex,
    targetMessageType,
    context,
    previousStepPayload,
  });
  return res.data;
};

export const autoRunWorkflow = async (
  flowId: string,
  action: 'GENERATE' | 'SEND' = 'GENERATE',
  initialStepPayload?: Record<string, any>,
  initialContext?: Record<string, string>
): Promise<FlowAutoRunResult> => {
  const res = await apiClient.post<FlowAutoRunResult>('/api/orchestrator/run-flow', {
    flowId,
    action,
    initialStepPayload,
    initialContext,
  });
  return res.data;
};

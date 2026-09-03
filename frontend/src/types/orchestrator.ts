import { ServicePushResult } from './workbench';

export type StepExecutionStatus = 'pending' | 'running' | 'completed' | 'error';

export interface FlowStepDefinition {
  stepIndex: number;
  stepId: string;
  messageType: string;
  isoCode: string;
  title: string;
  description: string;
  role: string; // e.g., "Originating Bank", "Debtor Bank", "Clearing Switch"
  requiredContextKeys?: string[];
  producedContextKeys?: string[];
  defaultPayload: Record<string, any>;
  variantOptions?: { label: string; messageType: string; isoCode: string }[];
}

export interface FlowDefinition {
  id: string;
  name: string;
  category: string;
  description: string;
  badge: string;
  icon: string;
  steps: FlowStepDefinition[];
}

export interface StepExecutionResult {
  success: boolean;
  messageId?: string;
  plainXml?: string | null;
  signedXml?: string | null;
  serviceResponse?: ServicePushResult | null;
  executionTime?: string;
  extractedContext?: Record<string, string>;
  errorMessage?: string;
  executedAt?: string;
  actionTaken?: 'GENERATE' | 'SEND';
}

export interface FlowMapNextStepResult {
  flowId: string;
  targetStepIndex: number;
  targetMessageType: string;
  prefilledPayload: Record<string, any>;
  mappedFields: Array<{
    fieldKey: string;
    value: any;
    sourceKey: string;
    description: string;
  }>;
}

export interface FlowAutoRunResult {
  success: boolean;
  flowId: string;
  flowName: string;
  totalSteps: number;
  executedSteps: number;
  stepsTranscript: Array<{
    stepIndex: number;
    messageType: string;
    messageId?: string;
    plainXml?: string;
    signedXml?: string;
    serviceResponse?: ServicePushResult | null;
    executionTime?: string;
    extractedContext?: Record<string, string>;
    errorMessage?: string;
  }>;
  finalContext: Record<string, string>;
  executionDuration: string;
  errorMessage?: string;
}

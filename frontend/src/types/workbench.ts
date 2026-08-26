export interface XmlGenerationResponseDto {
  messageType: string;
  messageId: string;
  plainXml: string;
  signedXml: string;
  generatedAt: string;
}

export interface ServicePushResult {
  statusCode: number;
  rawResponseBody: string;
  executionTimeMs: number;
  success: boolean;
  timestamp: string;
}

export interface MessageSendResponseDto {
  messageType: string;
  messageId: string;
  plainXml: string;
  signedXml: string;
  serviceResponse: ServicePushResult;
}

export type PipelineResult = {
  plainXml: string | null;
  signedXml: string | null;
  generatedAt?: string;
  messageId?: string;
  serviceResponse?: ServicePushResult | null;
  isLoading: boolean;
  error: string | null;
};

export type MessageKey =
  | 'pacs.008'
  | 'pacs.002'
  | 'pacs.028'
  | 'acmt.023'
  | 'acmt.024'
  | 'pain.009'
  | 'pain.010'
  | 'pain.011'
  | 'pain.012'
  | 'pacs.003'
  | 'pain.013'
  | 'pain.014'
  | 'camt.060'
  | 'camt.052'
  | 'camt.053'
  | 'pain.001'
  | 'pain.002'
  | 'pain.008'
  | 'pacs.004';

export interface FieldDef {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select' | 'textarea';
  required?: boolean;
  placeholder?: string;
  fullWidth?: boolean;
  options?: { value: string; label: string }[];
}

export interface FieldsetDef {
  title: string;
  fields: FieldDef[];
}

export interface MessageConfig {
  key: MessageKey;
  label: string;
  isoCode: string;
  category: string;
  sections: FieldsetDef[];
  prefill: Record<string, any>;
}

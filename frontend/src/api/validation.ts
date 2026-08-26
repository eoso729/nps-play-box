import { apiClient } from './client';
import {
  ValidationReport,
  XmlInspectRequest,
  XmlAutoFixRequest,
  XmlAutoFixResponse,
  MessageSample,
} from '../types/validation';

export const inspectXml = async (
  xmlContent: string,
  messageType?: string
): Promise<ValidationReport> => {
  const req: XmlInspectRequest = { xmlContent, messageType };
  const res = await apiClient.post<ValidationReport>('/api/validation/inspect', req);
  return res.data;
};

export const autoFixXml = async (
  request: XmlAutoFixRequest
): Promise<XmlAutoFixResponse> => {
  const res = await apiClient.post<XmlAutoFixResponse>('/api/validation/auto-fix', request);
  return res.data;
};

export const getAllSamples = async (): Promise<MessageSample[]> => {
  const res = await apiClient.get<MessageSample[]>('/api/validation/samples');
  return res.data;
};

export const getSample = async (messageType: string): Promise<MessageSample> => {
  const res = await apiClient.get<MessageSample>(`/api/validation/samples/${messageType}`);
  return res.data;
};

export const getValidationRules = async (): Promise<Record<string, any>> => {
  const res = await apiClient.get<Record<string, any>>('/api/validation/rules');
  return res.data;
};

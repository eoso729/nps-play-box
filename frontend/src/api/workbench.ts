import { apiClient } from './client';
import { XmlGenerationResponseDto, MessageSendResponseDto, MessageKey } from '../types/workbench';

const GENERATE_ENDPOINTS: Partial<Record<MessageKey, string>> = {
  'pain.013': '/api/generate/payment-activation-pain013',
  'pain.001': '/api/generate/payment-initiation-pain001',
  'pain.008': '/api/generate/direct-debit-pain008',
  'pain.009': '/api/generate/mandate-creation-pain009',
  'pain.010': '/api/generate/mandate-amendment-pain010',
  'pain.011': '/api/generate/mandate-cancellation-pain011',
  'pacs.008': '/api/generate/transfer-pacs008',
  'pacs.003': '/api/generate/customer-direct-debit-pacs003',
  'pacs.004': '/api/generate/payment-return-pacs004',
  'acmt.023': '/api/generate/name-verification-acmt023',
  'acmt.024': '/api/generate/name-verification-report-acmt024',
  'camt.060': '/api/generate/balance-enquiry-camt060',
  'pacs.002': '/api/generate/payment-status-report-pacs002',
};

const SEND_ENDPOINTS: Partial<Record<MessageKey, string>> = {
  'pain.013': '/api/payment-activation-pain013',
  'pain.001': '/api/payment-initiation-pain001',
  'pain.008': '/api/direct-debit-pain008',
  'pain.009': '/api/mandate-creation-pain009',
  'pain.010': '/api/mandate-amendment-pain010',
  'pain.011': '/api/mandate-cancellation-pain011',
  'pacs.008': '/api/transfer-pacs008',
  'pacs.003': '/api/customer-direct-debit-pacs003',
  'pacs.004': '/api/payment-return-pacs004',
  'acmt.023': '/api/name-verification-acmt023',
  'camt.060': '/api/balance-enquiry-camt060',
};

export const generateXml = async (
  messageKey: MessageKey,
  payload: Record<string, any>
): Promise<XmlGenerationResponseDto> => {
  const endpoint = GENERATE_ENDPOINTS[messageKey];
  if (!endpoint) {
    throw new Error(`XML Generation endpoint not configured for ${messageKey}. Please use the Health Check tool to inspect & repair.`);
  }
  const res = await apiClient.post<XmlGenerationResponseDto>(endpoint, payload);
  return res.data;
};

export const sendMessage = async (
  messageKey: MessageKey,
  payload: Record<string, any>
): Promise<MessageSendResponseDto> => {
  const endpoint = SEND_ENDPOINTS[messageKey];
  if (!endpoint) {
    throw new Error(`Pushing to pipeline is not supported for ${messageKey}`);
  }
  const res = await apiClient.post<MessageSendResponseDto>(endpoint, payload);
  return res.data;
};

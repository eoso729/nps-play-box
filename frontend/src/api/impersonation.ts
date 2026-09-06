import { apiClient } from './client';

export interface ImpersonationSessionResponse {
  sessionUuid: string;
  supportUserId: number;
  supportUserEmail: string;
  targetUserId: number;
  targetUserEmail: string;
  targetTenantId: number;
  targetTenantName: string;
  status: string;
  reason: string;
  approvalReason?: string;
  approvedByEmail?: string;
  maxDurationMinutes: number;
  startedAt?: string;
  expiresAt?: string;
  terminatedAt?: string;
  terminationReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ImpersonationTokenResponse {
  impersonationToken: string;
  sessionUuid: string;
  targetUserEmail: string;
  targetTenantSlug: string;
  expiresAt: string;
}

export const impersonationApi = {
  requestSession: (targetUserId: number, reason: string, durationMinutes = 240) =>
    apiClient.post<ImpersonationSessionResponse>('/api/v1/impersonation/request', {
      targetUserId,
      reason,
      durationMinutes,
    }),

  approveSession: (sessionUuid: string, approvalReason: string) =>
    apiClient.post<ImpersonationSessionResponse>(`/api/v1/impersonation/${sessionUuid}/approve`, {
      approvalReason,
    }),

  rejectSession: (sessionUuid: string, rejectionReason: string) =>
    apiClient.post<ImpersonationSessionResponse>(`/api/v1/impersonation/${sessionUuid}/reject`, {
      rejectionReason,
    }),

  startSession: (sessionUuid: string) =>
    apiClient.post<ImpersonationTokenResponse>(`/api/v1/impersonation/${sessionUuid}/start`),

  terminateSession: (sessionUuid: string, reason = 'User terminated session') =>
    apiClient.post(`/api/v1/impersonation/${sessionUuid}/terminate`, null, {
      params: { reason },
    }),

  getPendingSessions: (page = 0, size = 20) =>
    apiClient.get('/api/v1/impersonation/pending', { params: { page, size } }),

  getMySessions: (page = 0, size = 20) =>
    apiClient.get('/api/v1/impersonation/my-sessions', { params: { page, size } }),

  getTenantSessions: (tenantId: number, page = 0, size = 20) =>
    apiClient.get(`/api/v1/impersonation/tenant/${tenantId}`, { params: { page, size } }),

  getSession: (sessionUuid: string) =>
    apiClient.get<ImpersonationSessionResponse>(`/api/v1/impersonation/${sessionUuid}`),
};

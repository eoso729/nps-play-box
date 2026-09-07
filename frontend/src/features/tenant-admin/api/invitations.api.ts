import { apiClient } from '../../../api/client';
import { TenantInvitation } from '../types/tenant-admin.types';

export interface VerifyInvitationResult {
  token: string;
  email: string;
  role: string;
  tenantId: number;
  tenantName: string;
  tenantSlug: string;
  valid: boolean;
  message: string;
  expiresAt: string;
}

export interface AcceptInvitationPayload {
  token: string;
  firstName: string;
  lastName: string;
  password: string;
}

export const invitationsApi = {
  getInvitations: async (): Promise<TenantInvitation[]> => {
    const res = await apiClient.get<TenantInvitation[]>('/api/v1/users/invitations');
    return res.data;
  },

  createInvitation: async (data: { email: string; role: string }): Promise<TenantInvitation> => {
    const res = await apiClient.post<TenantInvitation>('/api/v1/users/invite', data);
    return res.data;
  },

  resendInvitation: async (data: { email: string; role: string }): Promise<TenantInvitation> => {
    const res = await apiClient.post<TenantInvitation>('/api/v1/users/invite', data);
    return res.data;
  },

  cancelInvitation: async (invitationId: number | string): Promise<void> => {
    await apiClient.delete(`/api/v1/users/invitations/${invitationId}`);
  },

  verifyInvitation: async (token: string): Promise<VerifyInvitationResult> => {
    const res = await apiClient.get<VerifyInvitationResult>(`/api/v1/users/invitations/verify?token=${encodeURIComponent(token)}`);
    return res.data;
  },

  acceptInvitation: async (data: AcceptInvitationPayload): Promise<any> => {
    const res = await apiClient.post('/api/v1/users/accept-invitation', data);
    return res.data;
  },
};

import { apiClient } from '../../../api/client';
import { TenantInvitation } from '../types/tenant-admin.types';

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
};

import { apiClient } from '../../../api/client';
import { PlatformSeatRequest } from '../types/platform-admin.types';

export const platformSeatRequestsApi = {
  getAllPendingSeatRequests: async (): Promise<PlatformSeatRequest[]> => {
    const res = await apiClient.get<PlatformSeatRequest[]>('/api/v1/quota/requests/all');
    return res.data;
  },

  approveSeatRequest: async (
    requestId: number | string,
    data?: { reviewNote?: string }
  ): Promise<PlatformSeatRequest> => {
    const res = await apiClient.patch<PlatformSeatRequest>(
      `/api/v1/quota/requests/${requestId}/approve`,
      data || {}
    );
    return res.data;
  },

  denySeatRequest: async (
    requestId: number | string,
    denialReason: string
  ): Promise<PlatformSeatRequest> => {
    const res = await apiClient.patch<PlatformSeatRequest>(
      `/api/v1/quota/requests/${requestId}/deny`,
      { denialReason }
    );
    return res.data;
  },
};

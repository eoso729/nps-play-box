import { apiClient } from '../../../api/client';
import {
  QuotaStatus,
  SeatRequestPayload,
  SeatRequestResponse,
  SeatUtilization,
} from '../types/tenant-admin.types';

export const seatsApi = {
  getSeatUsage: async (): Promise<SeatUtilization> => {
    const res = await apiClient.get<SeatUtilization>('/api/v1/users/seats/utilization');
    return res.data;
  },

  getQuotaStatus: async (): Promise<QuotaStatus> => {
    const res = await apiClient.get<QuotaStatus>('/api/v1/quota/status');
    return res.data;
  },

  requestAdditionalSeats: async (data: SeatRequestPayload): Promise<SeatRequestResponse> => {
    const res = await apiClient.post<SeatRequestResponse>('/api/v1/quota/request', data);
    return res.data;
  },

  getTenantSeatRequests: async (): Promise<SeatRequestResponse[]> => {
    const res = await apiClient.get<SeatRequestResponse[]>('/api/v1/quota/requests');
    return res.data;
  },
};

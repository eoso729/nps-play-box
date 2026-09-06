import { apiClient } from '../../../api/client';
import { TenantDetails } from '../types/tenant-admin.types';

export const settingsApi = {
  getCurrentTenant: async (): Promise<TenantDetails> => {
    const res = await apiClient.get<TenantDetails>('/api/v1/tenants/current');
    return res.data;
  },

  getTenantById: async (tenantId: number | string): Promise<TenantDetails> => {
    const res = await apiClient.get<TenantDetails>(`/api/v1/tenants/${tenantId}`);
    return res.data;
  },
};

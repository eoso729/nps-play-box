import { apiClient } from '../../../api/client';
import {
  CreateTenantPayload,
  PlatformTenant,
  SpringPage,
  TenantFilters,
  UpdateTenantPayload,
} from '../types/platform-admin.types';

export const platformTenantsApi = {
  getTenants: async (filters?: TenantFilters): Promise<SpringPage<PlatformTenant>> => {
    const params = new URLSearchParams();
    if (filters?.page !== undefined) params.append('page', String(filters.page));
    if (filters?.size !== undefined) params.append('size', String(filters.size));
    if (filters?.sortBy) {
      const dir = filters.sortOrder || 'desc';
      params.append('sort', `${filters.sortBy},${dir}`);
    }

    const res = await apiClient.get<SpringPage<PlatformTenant>>(`/api/v1/tenants?${params.toString()}`);
    return res.data;
  },

  getTenant: async (tenantId: number | string): Promise<PlatformTenant> => {
    const res = await apiClient.get<PlatformTenant>(`/api/v1/tenants/${tenantId}`);
    return res.data;
  },

  createTenant: async (data: CreateTenantPayload): Promise<PlatformTenant> => {
    const res = await apiClient.post<PlatformTenant>('/api/v1/tenants', data);
    return res.data;
  },

  updateTenant: async (
    tenantId: number | string,
    data: UpdateTenantPayload
  ): Promise<PlatformTenant> => {
    const res = await apiClient.put<PlatformTenant>(`/api/v1/tenants/${tenantId}`, data);
    return res.data;
  },

  updateTenantStatus: async (
    tenantId: number | string,
    status: string
  ): Promise<PlatformTenant> => {
    const res = await apiClient.patch<PlatformTenant>(`/api/v1/tenants/${tenantId}/status`, {
      status,
    });
    return res.data;
  },

  updateSeatQuota: async (
    tenantId: number | string,
    maxSeats: number
  ): Promise<PlatformTenant> => {
    const res = await apiClient.patch<PlatformTenant>(`/api/v1/tenants/${tenantId}/seats`, {
      maxSeats,
    });
    return res.data;
  },

  deleteTenant: async (tenantId: number | string): Promise<void> => {
    await apiClient.delete(`/api/v1/tenants/${tenantId}`);
  },
};

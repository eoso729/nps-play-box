import { apiClient } from '../../../api/client';
import { SpringPage, TenantUser, UserFilters } from '../types/tenant-admin.types';

export const usersApi = {
  getUsers: async (filters?: UserFilters): Promise<SpringPage<TenantUser>> => {
    const params = new URLSearchParams();
    if (filters?.page !== undefined) params.append('page', String(filters.page));
    if (filters?.size !== undefined) params.append('size', String(filters.size));
    if (filters?.sortBy) {
      const dir = filters.sortOrder || 'desc';
      params.append('sort', `${filters.sortBy},${dir}`);
    }

    const res = await apiClient.get<SpringPage<TenantUser>>(`/api/v1/users?${params.toString()}`);
    return res.data;
  },

  getUser: async (userId: number | string): Promise<TenantUser> => {
    const res = await apiClient.get<TenantUser>(`/api/v1/users/${userId}`);
    return res.data;
  },

  updateUser: async (
    userId: number | string,
    data: { firstName?: string; lastName?: string; email?: string }
  ): Promise<TenantUser> => {
    const res = await apiClient.put<TenantUser>(`/api/v1/users/${userId}`, data);
    return res.data;
  },

  updateUserRole: async (userId: number | string, role: string): Promise<TenantUser> => {
    const res = await apiClient.patch<TenantUser>(`/api/v1/users/${userId}/role`, { role });
    return res.data;
  },

  deactivateUser: async (userId: number | string): Promise<TenantUser> => {
    const res = await apiClient.patch<TenantUser>(`/api/v1/users/${userId}/deactivate`);
    return res.data;
  },

  reactivateUser: async (userId: number | string): Promise<TenantUser> => {
    const res = await apiClient.patch<TenantUser>(`/api/v1/users/${userId}/reactivate`);
    return res.data;
  },

  deleteUser: async (userId: number | string): Promise<void> => {
    await apiClient.delete(`/api/v1/users/${userId}`);
  },
};

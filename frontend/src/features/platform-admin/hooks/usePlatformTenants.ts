import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { platformTenantsApi } from '../api/platform-tenants.api';
import { CreateTenantPayload, TenantFilters, UpdateTenantPayload } from '../types/platform-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const usePlatformTenants = (filters?: TenantFilters) => {
  return useQuery({
    queryKey: ['platform-tenants', filters],
    queryFn: () => platformTenantsApi.getTenants(filters),
    staleTime: 15000,
  });
};

export const usePlatformTenant = (tenantId: number | string) => {
  return useQuery({
    queryKey: ['platform-tenant', tenantId],
    queryFn: () => platformTenantsApi.getTenant(tenantId),
    enabled: !!tenantId,
  });
};

export const useCreateTenant = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (data: CreateTenantPayload) => platformTenantsApi.createTenant(data),
    onSuccess: (tenant) => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast(`Tenant "${tenant.name}" provisioned successfully`, 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to provision tenant', 'error');
    },
  });
};

export const useUpdateTenant = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      tenantId,
      data,
    }: {
      tenantId: number | string;
      data: UpdateTenantPayload;
    }) => platformTenantsApi.updateTenant(tenantId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-tenant'] });
      showToast('Tenant profile updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update tenant', 'error');
    },
  });
};

export const useUpdateTenantStatus = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      tenantId,
      status,
    }: {
      tenantId: number | string;
      status: string;
    }) => platformTenantsApi.updateTenantStatus(tenantId, status),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast(`Tenant status changed to ${variables.status}`, 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update tenant status', 'error');
    },
  });
};

export const useUpdateSeatQuota = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      tenantId,
      maxSeats,
    }: {
      tenantId: number | string;
      maxSeats: number;
    }) => platformTenantsApi.updateSeatQuota(tenantId, maxSeats),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast('Tenant seat quota updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update seat quota', 'error');
    },
  });
};

export const useDeleteTenant = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (tenantId: number | string) => platformTenantsApi.deleteTenant(tenantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast('Tenant soft-deleted successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to delete tenant', 'error');
    },
  });
};

import { apiClient } from '../../../api/client';
import { PlatformAuditItem, PlatformAuditFilters, SpringPage } from '../types/platform-admin.types';

export const platformAuditApi = {
  getPlatformAuditEvents: async (
    filters?: PlatformAuditFilters
  ): Promise<SpringPage<PlatformAuditItem>> => {
    const params = new URLSearchParams();
    if (filters?.page !== undefined) params.append('page', String(filters.page));
    if (filters?.size !== undefined) params.append('size', String(filters.size));
    if (filters?.tenantId !== undefined) params.append('tenantId', String(filters.tenantId));
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.resourceType) params.append('resourceType', filters.resourceType);

    const res = await apiClient.get<SpringPage<PlatformAuditItem>>(
      `/api/v1/audit/admin/events?${params.toString()}`
    );
    return res.data;
  },

  exportPlatformAuditCsv: async (filters?: PlatformAuditFilters): Promise<void> => {
    const params = new URLSearchParams();
    if (filters?.tenantId !== undefined) params.append('tenantId', String(filters.tenantId));
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);

    const res = await apiClient.get(`/api/v1/audit/admin/export/csv?${params.toString()}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `platform-audit-${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },

  exportPlatformAuditJson: async (filters?: PlatformAuditFilters): Promise<void> => {
    const params = new URLSearchParams();
    if (filters?.tenantId !== undefined) params.append('tenantId', String(filters.tenantId));
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);

    const res = await apiClient.get(`/api/v1/audit/admin/export/json?${params.toString()}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/json' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `platform-audit-${Date.now()}.json`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },
};

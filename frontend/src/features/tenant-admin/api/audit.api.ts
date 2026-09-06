import { apiClient } from '../../../api/client';
import { AuditEventItem, AuditFilters, SpringPage } from '../types/tenant-admin.types';

export const auditApi = {
  getAuditEvents: async (filters?: AuditFilters): Promise<SpringPage<AuditEventItem>> => {
    const params = new URLSearchParams();
    if (filters?.page !== undefined) params.append('page', String(filters.page));
    if (filters?.size !== undefined) params.append('size', String(filters.size));
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.resourceType) params.append('resourceType', filters.resourceType);

    const res = await apiClient.get<SpringPage<AuditEventItem>>(`/api/v1/audit/events?${params.toString()}`);
    return res.data;
  },

  exportAuditCsv: async (filters?: AuditFilters): Promise<void> => {
    const params = new URLSearchParams();
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);

    const res = await apiClient.get(`/api/v1/audit/export/csv?${params.toString()}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `audit-events-${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },

  exportAuditJson: async (filters?: AuditFilters): Promise<void> => {
    const params = new URLSearchParams();
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.actorUsername) params.append('actorUsername', filters.actorUsername);
    if (filters?.status) params.append('status', filters.status);

    const res = await apiClient.get(`/api/v1/audit/export/json?${params.toString()}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/json' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `audit-events-${Date.now()}.json`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },
};

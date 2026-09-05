import { useQuery } from '@tanstack/react-query';
import { platformAuditApi } from '../api/platform-audit.api';
import { PlatformAuditFilters } from '../types/platform-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const usePlatformAuditLogs = (filters?: PlatformAuditFilters) => {
  return useQuery({
    queryKey: ['platform-audit-logs', filters],
    queryFn: () => platformAuditApi.getPlatformAuditEvents(filters),
    staleTime: 15000,
  });
};

export const useExportPlatformAudit = () => {
  const { showToast } = useToast();

  const exportCsv = async (filters?: PlatformAuditFilters) => {
    try {
      await platformAuditApi.exportPlatformAuditCsv(filters);
      showToast('System audit logs exported as CSV', 'success');
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to export audit CSV', 'error');
    }
  };

  const exportJson = async (filters?: PlatformAuditFilters) => {
    try {
      await platformAuditApi.exportPlatformAuditJson(filters);
      showToast('System audit logs exported as JSON', 'success');
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to export audit JSON', 'error');
    }
  };

  return { exportCsv, exportJson };
};

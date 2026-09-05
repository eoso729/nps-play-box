import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../api/audit.api';
import { AuditFilters } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useAuditEvents = (filters?: AuditFilters) => {
  return useQuery({
    queryKey: ['tenant-audit-events', filters],
    queryFn: () => auditApi.getAuditEvents(filters),
    staleTime: 15000,
  });
};

export const useExportAudit = () => {
  const { showToast } = useToast();

  const exportCsv = async (filters?: AuditFilters) => {
    try {
      await auditApi.exportAuditCsv(filters);
      showToast('Audit trail CSV exported successfully', 'success');
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to export CSV', 'error');
    }
  };

  const exportJson = async (filters?: AuditFilters) => {
    try {
      await auditApi.exportAuditJson(filters);
      showToast('Audit trail JSON exported successfully', 'success');
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to export JSON', 'error');
    }
  };

  return { exportCsv, exportJson };
};

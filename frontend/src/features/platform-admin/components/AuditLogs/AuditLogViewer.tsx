import React, { useState } from 'react';
import { ShieldAlert, Download, FileJson, AlertCircle } from 'lucide-react';
import { usePlatformAuditLogs, useExportPlatformAudit } from '../../hooks/usePlatformAudit';
import { PlatformAuditFilters } from '../../types/platform-admin.types';
import { AuditLogFiltersComponent } from './AuditLogFilters';
import { AuditLogList } from './AuditLogList';
import { Button } from '../../../shared/components/Button';

export const AuditLogViewer: React.FC = () => {
  const [filters, setFilters] = useState<PlatformAuditFilters>({
    page: 0,
    size: 20,
  });

  const { data, isLoading, error } = usePlatformAuditLogs(filters);
  const { exportCsv, exportJson } = useExportPlatformAudit();

  const logs = data?.content || [];
  const total = data?.totalElements ?? logs.length;
  const currentPage = filters.page || 0;
  const pageSize = filters.size || 20;

  const handlePageChange = (newPage: number) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
  };

  return (
    <div className="space-y-4">
      {/* Header and Export Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-indigo-600" />
            System-Wide Audit Trail
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Immutable security ledger tracking platform-level modifications, auth events, and impersonation sessions
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="outline"
            onClick={() => exportCsv(filters)}
            className="text-gray-700 hover:bg-gray-100"
          >
            <Download className="w-3.5 h-3.5 mr-1.5" />
            Export CSV
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={() => exportJson(filters)}
            className="text-gray-700 hover:bg-gray-100"
          >
            <FileJson className="w-3.5 h-3.5 mr-1.5" />
            Export JSON
          </Button>
        </div>
      </div>

      {/* Filter Controls */}
      <AuditLogFiltersComponent filters={filters} onFiltersChange={setFilters} />

      {/* Audit Log Table */}
      {isLoading ? (
        <div className="bg-white rounded-xl border border-gray-200/80 p-8 shadow-sm space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-center gap-4 animate-pulse">
              <div className="h-4 bg-gray-200 rounded w-24"></div>
              <div className="h-4 bg-gray-200 rounded w-48"></div>
              <div className="h-4 bg-gray-200 rounded w-32"></div>
              <div className="h-4 bg-gray-200 rounded w-16"></div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="p-8 text-center text-rose-700 text-sm bg-white rounded-xl border border-gray-200">
          <AlertCircle className="w-8 h-8 mx-auto text-rose-500 mb-2" />
          Failed to load platform audit trail events.
        </div>
      ) : (
        <AuditLogList
          logs={logs}
          total={total}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={handlePageChange}
        />
      )}
    </div>
  );
};

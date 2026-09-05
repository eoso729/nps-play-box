import React, { useState } from 'react';
import { useAuditEvents, useExportAudit } from '../../hooks/useAuditEvents';
import { AuditFilters } from '../../types/tenant-admin.types';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { Button } from '../../../shared/components/Button';
import { Badge } from '../../../shared/components/Badge';
import { Shield, Search, Download, FileJson, RefreshCw, CheckCircle2, AlertCircle } from 'lucide-react';

export const AuditLogTab: React.FC = () => {
  const [filters, setFilters] = useState<AuditFilters>({
    page: 0,
    size: 20,
  });

  const { data: pageData, isLoading, error, refetch } = useAuditEvents(filters);
  const { exportCsv, exportJson } = useExportAudit();

  const events = pageData?.content || [];
  const totalElements = pageData?.totalElements ?? events.length;

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFilters((prev) => ({
      ...prev,
      actorUsername: e.target.value || undefined,
      page: 0,
    }));
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilters((prev) => ({
      ...prev,
      status: e.target.value || undefined,
      page: 0,
    }));
  };

  const handleEventTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilters((prev) => ({
      ...prev,
      eventType: e.target.value || undefined,
      page: 0,
    }));
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return new Date(dateStr).toLocaleString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] shadow-sm overflow-hidden">
      {/* Header and Controls */}
      <div className="p-5 sm:p-6 border-b border-[#e4e9e6]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-gray-900 leading-tight">
                Compliance Audit Trail
              </h3>
              <span className="text-xs font-semibold bg-[#edf2ee] text-[#15803d] px-2.5 py-0.5 rounded-full border border-[#d5e2d8]">
                {totalElements} events
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Immutable record of security, user lifecycle, and permission mutations.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              icon={<Download className="w-3.5 h-3.5 text-gray-600" />}
              onClick={() => exportCsv(filters)}
            >
              Export CSV
            </Button>
            <Button
              size="sm"
              variant="outline"
              icon={<FileJson className="w-3.5 h-3.5 text-gray-600" />}
              onClick={() => exportJson(filters)}
            >
              Export JSON
            </Button>
            <Button
              size="sm"
              variant="ghost"
              icon={<RefreshCw className="w-3.5 h-3.5 text-gray-500" />}
              onClick={() => refetch()}
            >
              Refresh
            </Button>
          </div>
        </div>

        {/* Filters */}
        <div className="flex flex-col sm:flex-row gap-3 items-center">
          <div className="w-full sm:w-80">
            <Input
              type="text"
              placeholder="Filter by actor username..."
              value={filters.actorUsername || ''}
              onChange={handleSearchChange}
              leftIcon={<Search className="w-4 h-4" />}
            />
          </div>

          <div className="w-full sm:w-48">
            <Select
              value={filters.eventType || ''}
              onChange={handleEventTypeChange}
              options={[
                { value: '', label: 'All Event Types' },
                { value: 'USER_INVITED', label: 'User Invited' },
                { value: 'ROLE_CHANGED', label: 'Role Changed' },
                { value: 'USER_DEACTIVATED', label: 'User Deactivated' },
                { value: 'USER_REACTIVATED', label: 'User Reactivated' },
                { value: 'SEATS_REQUESTED', label: 'Seats Requested' },
                { value: 'IMPERSONATION_STARTED', label: 'Impersonation Started' },
              ]}
            />
          </div>

          <div className="w-full sm:w-36">
            <Select
              value={filters.status || ''}
              onChange={handleStatusChange}
              options={[
                { value: '', label: 'All Statuses' },
                { value: 'SUCCESS', label: 'Success' },
                { value: 'FAILURE', label: 'Failure' },
                { value: 'WARNING', label: 'Warning' },
              ]}
            />
          </div>
        </div>
      </div>

      {/* Events Table */}
      {isLoading ? (
        <div className="p-6 space-y-3 animate-pulse">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-10 bg-gray-100 rounded-lg" />
          ))}
        </div>
      ) : error ? (
        <div className="p-8 text-center text-xs text-red-700 bg-red-50/50">
          <p className="font-semibold mb-2">Unable to load audit trail.</p>
          <Button size="sm" variant="outline" onClick={() => refetch()}>
            Retry
          </Button>
        </div>
      ) : events.length === 0 ? (
        <div className="p-12 text-center">
          <div className="w-12 h-12 rounded-full bg-[#f6f9f7] text-[#15803d] flex items-center justify-center mx-auto mb-3 border border-[#e4e9e6]">
            <Shield className="w-6 h-6" />
          </div>
          <h4 className="text-sm font-bold text-gray-900 mb-1">No audit events recorded</h4>
          <p className="text-xs text-gray-500 max-w-sm mx-auto">
            Organizational actions such as user updates and role changes will appear here.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-[#fbfdfc] border-b border-[#e4e9e6] text-gray-500 font-semibold uppercase tracking-wider text-[10px]">
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4">Action / Event</th>
                <th className="py-3 px-4">Actor</th>
                <th className="py-3 px-4">Resource</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">IP Address</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#e4e9e6] text-gray-700">
              {events.map((evt) => (
                <tr key={evt.id || evt.eventId} className="hover:bg-[#fbfdfc] transition-colors">
                  <td className="py-3 px-4 font-mono text-gray-500 whitespace-nowrap">
                    {formatDate(evt.createdAt)}
                  </td>
                  <td className="py-3 px-4 font-semibold text-gray-900">
                    <span className="font-mono bg-gray-100 text-gray-800 px-1.5 py-0.5 rounded text-[11px]">
                      {evt.eventType || evt.action}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <span className="font-medium text-gray-900">
                      {evt.actorUsername || 'System'}
                    </span>
                    {evt.actorRole && (
                      <span className="text-[10px] text-gray-400 block">{evt.actorRole}</span>
                    )}
                  </td>
                  <td className="py-3 px-4 font-mono text-gray-600">
                    {evt.resourceType ? `${evt.resourceType}:${evt.resourceId || ''}` : '-'}
                  </td>
                  <td className="py-3 px-4">
                    {evt.status === 'SUCCESS' ? (
                      <Badge variant="success" icon={<CheckCircle2 className="w-3 h-3" />}>
                        Success
                      </Badge>
                    ) : evt.status === 'FAILURE' ? (
                      <Badge variant="danger" icon={<AlertCircle className="w-3 h-3" />}>
                        Failure
                      </Badge>
                    ) : (
                      <Badge variant="warning">{evt.status}</Badge>
                    )}
                  </td>
                  <td className="py-3 px-4 font-mono text-gray-500">{evt.ipAddress || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

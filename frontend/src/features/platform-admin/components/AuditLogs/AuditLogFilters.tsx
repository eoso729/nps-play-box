import React from 'react';
import { Search, RotateCcw } from 'lucide-react';
import { PlatformAuditFilters } from '../../types/platform-admin.types';

interface AuditLogFiltersProps {
  filters: PlatformAuditFilters;
  onFiltersChange: (filters: PlatformAuditFilters) => void;
}

const COMMON_EVENT_TYPES = [
  'USER_CREATED',
  'USER_UPDATED',
  'USER_DELETED',
  'ROLE_ASSIGNED',
  'TENANT_CREATED',
  'TENANT_STATUS_CHANGED',
  'SEAT_QUOTA_UPDATED',
  'IMPERSONATION_REQUESTED',
  'IMPERSONATION_APPROVED',
  'IMPERSONATION_REJECTED',
  'IMPERSONATION_STARTED',
  'IMPERSONATION_TERMINATED',
  'LOGIN_SUCCESS',
  'LOGIN_FAILURE',
];

export const AuditLogFiltersComponent: React.FC<AuditLogFiltersProps> = ({
  filters,
  onFiltersChange,
}) => {
  const handleActorChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onFiltersChange({
      ...filters,
      actorUsername: e.target.value || undefined,
      page: 0,
    });
  };

  const handleEventTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onFiltersChange({
      ...filters,
      eventType: e.target.value || undefined,
      page: 0,
    });
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onFiltersChange({
      ...filters,
      status: e.target.value || undefined,
      page: 0,
    });
  };

  const handleReset = () => {
    onFiltersChange({
      page: 0,
      size: filters.size || 20,
    });
  };

  const hasActiveFilters = Boolean(
    filters.actorUsername || filters.eventType || filters.status || filters.tenantId
  );

  return (
    <div className="flex flex-wrap items-center gap-3 p-4 bg-gray-50/80 rounded-xl border border-gray-200/80 mb-5">
      {/* Actor search */}
      <div className="relative flex-1 min-w-[180px] max-w-xs">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          placeholder="Filter by actor username..."
          value={filters.actorUsername || ''}
          onChange={handleActorChange}
          className="w-full pl-9 pr-3 py-1.5 text-xs border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
        />
      </div>

      {/* Event Type select */}
      <div className="min-w-[170px]">
        <select
          value={filters.eventType || ''}
          onChange={handleEventTypeChange}
          className="w-full text-xs border border-gray-300 rounded-lg px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
        >
          <option value="">All Event Types</option>
          {COMMON_EVENT_TYPES.map((type) => (
            <option key={type} value={type}>
              {type.replace(/_/g, ' ')}
            </option>
          ))}
        </select>
      </div>

      {/* Status select */}
      <div className="min-w-[130px]">
        <select
          value={filters.status || ''}
          onChange={handleStatusChange}
          className="w-full text-xs border border-gray-300 rounded-lg px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
        >
          <option value="">All Statuses</option>
          <option value="SUCCESS">Success</option>
          <option value="FAILURE">Failure</option>
          <option value="WARNING">Warning</option>
        </select>
      </div>

      {/* Reset */}
      {hasActiveFilters && (
        <button
          type="button"
          onClick={handleReset}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-gray-600 hover:text-gray-900 bg-gray-200/70 hover:bg-gray-200 rounded-lg transition-colors"
          title="Reset audit filters"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          Reset Filters
        </button>
      )}
    </div>
  );
};

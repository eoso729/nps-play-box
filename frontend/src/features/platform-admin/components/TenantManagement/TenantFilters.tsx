import React from 'react';
import { Search, Plus, RotateCcw } from 'lucide-react';
import { TenantFilters, TenantStatus, SubscriptionTier } from '../../types/platform-admin.types';
import { Button } from '../../../shared/components/Button';

interface TenantFiltersProps {
  filters: TenantFilters;
  onFiltersChange: (filters: TenantFilters) => void;
  onCreateTenant: () => void;
}

export const TenantFiltersComponent: React.FC<TenantFiltersProps> = ({
  filters,
  onFiltersChange,
  onCreateTenant,
}) => {
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onFiltersChange({
      ...filters,
      search: e.target.value || undefined,
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

  const handleTierChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onFiltersChange({
      ...filters,
      subscriptionTier: e.target.value || undefined,
      page: 0,
    });
  };

  const handleReset = () => {
    onFiltersChange({
      page: 0,
      size: filters.size || 10,
    });
  };

  const hasActiveFilters = Boolean(filters.search || filters.status || filters.subscriptionTier);

  return (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 mb-5">
      <div className="flex flex-1 flex-wrap items-center gap-3">
        {/* Search Input */}
        <div className="relative flex-1 min-w-[200px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search by tenant name or slug..."
            value={filters.search || ''}
            onChange={handleSearchChange}
            className="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 bg-white"
          />
        </div>

        {/* Status Filter */}
        <div className="min-w-[140px]">
          <select
            value={filters.status || ''}
            onChange={handleStatusChange}
            className="w-full text-sm border border-gray-300 rounded-lg px-3 py-2 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
          >
            <option value="">All Statuses</option>
            <option value={TenantStatus.ACTIVE}>Active</option>
            <option value={TenantStatus.TRIAL}>Trial</option>
            <option value={TenantStatus.SUSPENDED}>Suspended</option>
            <option value={TenantStatus.DEACTIVATED}>Deactivated</option>
          </select>
        </div>

        {/* Subscription Tier Filter */}
        <div className="min-w-[150px]">
          <select
            value={filters.subscriptionTier || ''}
            onChange={handleTierChange}
            className="w-full text-sm border border-gray-300 rounded-lg px-3 py-2 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
          >
            <option value="">All Tiers</option>
            <option value={SubscriptionTier.STANDARD}>Standard</option>
            <option value={SubscriptionTier.PROFESSIONAL}>Professional</option>
            <option value={SubscriptionTier.ENTERPRISE}>Enterprise</option>
          </select>
        </div>

        {/* Reset Filter Button */}
        {hasActiveFilters && (
          <button
            type="button"
            onClick={handleReset}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-gray-100 hover:bg-gray-200/80 rounded-lg transition-colors"
            title="Reset filters"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset
          </button>
        )}
      </div>

      {/* Provision Tenant Action */}
      <div className="flex items-center gap-2">
        <Button
          variant="primary"
          size="sm"
          onClick={onCreateTenant}
          className="whitespace-nowrap"
        >
          <Plus className="w-4 h-4 mr-1.5" />
          Provision Tenant
        </Button>
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import { ChevronLeft, ChevronRight, Building2, AlertCircle } from 'lucide-react';
import { usePlatformTenants } from '../../hooks/usePlatformTenants';
import { PlatformTenant, TenantFilters } from '../../types/platform-admin.types';
import { TenantFiltersComponent } from './TenantFilters';
import { TenantListItem } from './TenantListItem';
import { CreateTenantModal } from './CreateTenantModal';
import { SeatQuotaModal } from './SeatQuotaModal';
import { TenantDetailModal } from './TenantDetailModal';
import { Button } from '../../../shared/components/Button';

interface TenantListProps {
  onRequestImpersonation?: (tenant: PlatformTenant) => void;
}

export const TenantList: React.FC<TenantListProps> = ({
  onRequestImpersonation,
}) => {
  const [filters, setFilters] = useState<TenantFilters>({
    page: 0,
    size: 10,
  });

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [quotaTenant, setQuotaTenant] = useState<PlatformTenant | null>(null);
  const [detailTenant, setDetailTenant] = useState<PlatformTenant | null>(null);

  const { data, isLoading, error } = usePlatformTenants(filters);

  const tenants = data?.content || [];
  const totalElements = data?.totalElements ?? tenants.length;
  const totalPages = data?.totalPages ?? Math.ceil(totalElements / (filters.size || 10));
  const currentPage = filters.page || 0;

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      setFilters((prev) => ({ ...prev, page: newPage }));
    }
  };

  return (
    <div className="space-y-4">
      {/* Search & Filters */}
      <TenantFiltersComponent
        filters={filters}
        onFiltersChange={setFilters}
        onCreateTenant={() => setCreateModalOpen(true)}
      />

      {/* Main Table Container */}
      <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-4">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="flex items-center gap-4 animate-pulse">
                <div className="w-10 h-10 rounded-xl bg-gray-200"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                  <div className="h-3 bg-gray-100 rounded w-1/3"></div>
                </div>
                <div className="h-6 bg-gray-200 rounded w-20"></div>
                <div className="h-6 bg-gray-200 rounded w-24"></div>
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="p-8 text-center text-rose-700 text-sm">
            <AlertCircle className="w-8 h-8 mx-auto text-rose-500 mb-2" />
            Failed to load enterprise tenants. Please try refreshing.
          </div>
        ) : tenants.length === 0 ? (
          <div className="py-16 text-center text-gray-500 text-sm">
            <Building2 className="w-10 h-10 mx-auto text-gray-300 mb-3" />
            <p className="font-semibold text-gray-700">No enterprise tenants found</p>
            <p className="text-xs text-gray-400 mt-1">
              Try adjusting your search criteria or provision a new tenant
            </p>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCreateModalOpen(true)}
              className="mt-4"
            >
              Provision Tenant
            </Button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200/80 text-[11px] font-semibold uppercase tracking-wider text-gray-500">
                  <th className="px-5 py-3.5">Tenant Organization</th>
                  <th className="px-5 py-3.5">Tier</th>
                  <th className="px-5 py-3.5">Status</th>
                  <th className="px-5 py-3.5">Seat Allocation</th>
                  <th className="px-5 py-3.5">Created</th>
                  <th className="px-5 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {tenants.map((tenant) => (
                  <TenantListItem
                    key={tenant.id}
                    tenant={tenant}
                    onManageQuota={() => setQuotaTenant(tenant)}
                    onViewDetails={() => setDetailTenant(tenant)}
                    onRequestImpersonation={() => {
                      if (onRequestImpersonation) {
                        onRequestImpersonation(tenant);
                      }
                    }}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="px-5 py-3.5 border-t border-gray-100 flex items-center justify-between text-xs text-gray-600 bg-gray-50/40">
            <div>
              Showing <span className="font-semibold">{currentPage * (filters.size || 10) + 1}</span> to{' '}
              <span className="font-semibold">
                {Math.min((currentPage + 1) * (filters.size || 10), totalElements)}
              </span>{' '}
              of <span className="font-semibold">{totalElements}</span> tenants
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                title="Previous Page"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="font-medium text-gray-700">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                type="button"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                title="Next Page"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      <CreateTenantModal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
      />

      <SeatQuotaModal
        isOpen={Boolean(quotaTenant)}
        onClose={() => setQuotaTenant(null)}
        tenant={quotaTenant}
      />

      <TenantDetailModal
        isOpen={Boolean(detailTenant)}
        onClose={() => setDetailTenant(null)}
        tenant={detailTenant}
      />
    </div>
  );
};

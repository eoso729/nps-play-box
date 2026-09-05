import React, { useState } from 'react';
import {
  Building2,
  AlertTriangle,
  Trash2,
  CheckCircle2,
  Lock,
} from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Badge } from '../../../shared/components/Badge';
import { ProgressBar } from '../../../shared/components/ProgressBar';
import {
  useUpdateTenantStatus,
  useDeleteTenant,
} from '../../hooks/usePlatformTenants';
import { PlatformTenant, TenantStatus } from '../../types/platform-admin.types';

interface TenantDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  tenant: PlatformTenant | null;
}

export const TenantDetailModal: React.FC<TenantDetailModalProps> = ({
  isOpen,
  onClose,
  tenant,
}) => {
  const updateStatus = useUpdateTenantStatus();
  const deleteTenant = useDeleteTenant();

  const [confirmDelete, setConfirmDelete] = useState(false);

  if (!tenant) return null;

  const used = tenant.usedSeats || 0;
  const quota = tenant.maxSeats || 1;
  const utilization = Math.min(100, Math.round((used / quota) * 100));

  const handleStatusChange = async (newStatus: TenantStatus) => {
    try {
      await updateStatus.mutateAsync({
        tenantId: tenant.id,
        status: newStatus,
      });
    } catch {
      // Handled by mutation toast
    }
  };

  const handleDelete = async () => {
    try {
      await deleteTenant.mutateAsync(tenant.id);
      setConfirmDelete(false);
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case TenantStatus.ACTIVE:
        return <Badge variant="success" size="md">ACTIVE</Badge>;
      case TenantStatus.TRIAL:
        return <Badge variant="info" size="md">TRIAL</Badge>;
      case TenantStatus.SUSPENDED:
        return <Badge variant="danger" size="md">SUSPENDED</Badge>;
      case TenantStatus.DEACTIVATED:
        return <Badge variant="neutral" size="md">DEACTIVATED</Badge>;
      default:
        return <Badge variant="neutral" size="md">{status}</Badge>;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Tenant Details & Administration"
      description="View tenant metadata and manage operational lifecycle status"
      maxWidth="lg"
    >
      <div className="p-5 space-y-5">
        {/* Header Summary */}
        <div className="flex items-start justify-between bg-gray-50 border border-gray-200/80 rounded-xl p-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 border border-emerald-200 flex items-center justify-center text-emerald-700">
              <Building2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-gray-900">{tenant.name}</h3>
              <p className="text-xs text-gray-500 font-mono mt-0.5">
                {tenant.slug}.npsplaybox.com
              </p>
            </div>
          </div>
          <div>{getStatusBadge(tenant.status)}</div>
        </div>

        {/* Metadata Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
          <div className="bg-white border border-gray-100 rounded-lg p-3">
            <span className="text-gray-400 block mb-1">Tenant ID / UUID</span>
            <span className="font-mono text-gray-700 break-all font-medium">
              #{tenant.id} · {tenant.tenantUuid?.substring(0, 8)}...
            </span>
          </div>
          <div className="bg-white border border-gray-100 rounded-lg p-3">
            <span className="text-gray-400 block mb-1">Subscription Tier</span>
            <span className="font-semibold text-gray-900">
              {tenant.subscriptionTier || 'Standard'}
            </span>
          </div>
          <div className="bg-white border border-gray-100 rounded-lg p-3">
            <span className="text-gray-400 block mb-1">Provisioned On</span>
            <span className="text-gray-700 font-medium">
              {new Date(tenant.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>

        {/* Capacity / Seat Progress */}
        <div className="bg-white border border-gray-200/80 rounded-xl p-4">
          <div className="flex items-center justify-between text-xs mb-2">
            <span className="font-semibold text-gray-900">Seat Capacity & Allocation</span>
            <span className="text-gray-600">
              {used} / {quota} seats ({utilization}%)
            </span>
          </div>
          <ProgressBar value={utilization} size="md" />
        </div>

        {/* Lifecycle Status Management */}
        <div className="border-t border-gray-100 pt-4">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500 mb-3">
            Lifecycle Status Controls
          </h4>
          <div className="flex flex-wrap items-center gap-2">
            {tenant.status !== TenantStatus.ACTIVE && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleStatusChange(TenantStatus.ACTIVE)}
                isLoading={updateStatus.isPending}
                className="text-emerald-700 border-emerald-200 hover:bg-emerald-50"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
                Activate Tenant
              </Button>
            )}

            {tenant.status !== TenantStatus.SUSPENDED && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleStatusChange(TenantStatus.SUSPENDED)}
                isLoading={updateStatus.isPending}
                className="text-rose-700 border-rose-200 hover:bg-rose-50"
              >
                <Lock className="w-3.5 h-3.5 mr-1.5" />
                Suspend Tenant
              </Button>
            )}

            {tenant.status !== TenantStatus.TRIAL && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleStatusChange(TenantStatus.TRIAL)}
                isLoading={updateStatus.isPending}
                className="text-sky-700 border-sky-200 hover:bg-sky-50"
              >
                Set to Trial
              </Button>
            )}

            {tenant.status !== TenantStatus.DEACTIVATED && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleStatusChange(TenantStatus.DEACTIVATED)}
                isLoading={updateStatus.isPending}
                className="text-gray-700 border-gray-200 hover:bg-gray-50"
              >
                Deactivate
              </Button>
            )}
          </div>
        </div>

        {/* Danger Zone: Soft Delete */}
        <div className="border-t border-gray-100 pt-4">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-rose-600 mb-2">
            Danger Zone
          </h4>

          {!confirmDelete ? (
            <div className="flex items-center justify-between p-3 rounded-lg bg-rose-50/50 border border-rose-100">
              <div className="text-xs text-rose-900">
                <span className="font-semibold">Soft-delete this tenant organization</span>
                <p className="text-[11px] text-rose-700 mt-0.5">
                  Terminates active sessions, revokes access, and archives the tenant workspace.
                </p>
              </div>
              <Button
                variant="danger"
                size="sm"
                onClick={() => setConfirmDelete(true)}
              >
                <Trash2 className="w-3.5 h-3.5 mr-1.5" />
                Delete Tenant
              </Button>
            </div>
          ) : (
            <div className="p-4 rounded-lg bg-rose-50 border border-rose-200 space-y-3">
              <div className="flex items-start gap-2.5">
                <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="text-xs font-bold text-rose-900">
                    Are you absolutely sure you want to delete {tenant.name}?
                  </p>
                  <p className="text-[11px] text-rose-700 mt-1">
                    This action will revoke all user sessions and mark the tenant record as deleted.
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setConfirmDelete(false)}
                  disabled={deleteTenant.isPending}
                >
                  Cancel
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={handleDelete}
                  isLoading={deleteTenant.isPending}
                >
                  Confirm Delete
                </Button>
              </div>
            </div>
          )}
        </div>

        {/* Close Modal */}
        <div className="flex justify-end pt-3 border-t border-gray-100">
          <Button variant="ghost" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
};

import React, { useState, useEffect } from 'react';
import { Layers, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Input } from '../../../shared/components/Input';
import { ProgressBar } from '../../../shared/components/ProgressBar';
import { useUpdateSeatQuota } from '../../hooks/usePlatformTenants';
import { PlatformTenant } from '../../types/platform-admin.types';

interface SeatQuotaModalProps {
  isOpen: boolean;
  onClose: () => void;
  tenant: PlatformTenant | null;
}

export const SeatQuotaModal: React.FC<SeatQuotaModalProps> = ({
  isOpen,
  onClose,
  tenant,
}) => {
  const updateQuota = useUpdateSeatQuota();
  const [quota, setQuota] = useState<number>(tenant?.maxSeats || 10);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (tenant) {
      setQuota(tenant.maxSeats || 10);
      setError(null);
    }
  }, [tenant]);

  if (!tenant) return null;

  const used = tenant.usedSeats || 0;
  const currentMax = tenant.maxSeats || 1;
  const currentPercentage = Math.min(100, Math.round((used / currentMax) * 100));

  const handleQuotaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseInt(e.target.value, 10);
    setQuota(isNaN(val) ? 0 : val);
    if (val < used) {
      setError(`New quota cannot be less than currently claimed seats (${used})`);
    } else if (val <= 0) {
      setError('Seat quota must be a positive number');
    } else {
      setError(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (quota < used) {
      setError(`New quota cannot be less than currently claimed seats (${used})`);
      return;
    }
    if (quota <= 0) {
      setError('Seat quota must be a positive number');
      return;
    }

    try {
      await updateQuota.mutateAsync({
        tenantId: tenant.id,
        maxSeats: quota,
      });
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Adjust Tenant Seat Quota"
      description={`Modify commercial licensed capacity for ${tenant.name}`}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="p-5 space-y-4">
        {/* Tenant Summary Banner */}
        <div className="bg-gray-50 border border-gray-200/80 rounded-xl p-3.5 flex items-center justify-between">
          <div>
            <span className="font-semibold text-gray-900 text-sm">{tenant.name}</span>
            <div className="text-xs text-gray-500 font-mono mt-0.5">{tenant.slug}.npsplaybox.com</div>
          </div>
          <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200">
            {tenant.subscriptionTier || 'Standard'}
          </span>
        </div>

        {/* Current Allocation & Usage */}
        <div className="bg-blue-50/60 border border-blue-100 rounded-xl p-4">
          <div className="flex justify-between items-center text-xs mb-1.5 font-medium text-blue-900">
            <span>Current Quota Utilization</span>
            <span>
              {used} / {currentMax} claimed ({currentPercentage}%)
            </span>
          </div>
          <ProgressBar value={currentPercentage} size="sm" />
        </div>

        {/* New Quota Input */}
        <div>
          <Input
            label="New Max Licensed Seats *"
            type="number"
            min={used}
            max={10000}
            value={quota.toString()}
            onChange={handleQuotaChange}
            error={error || undefined}
            helperText={`Minimum allowed: ${used} seats (to accommodate active users)`}
            leftIcon={<Layers className="w-4 h-4 text-gray-400" />}
          />
        </div>

        {/* Informational Alerts */}
        {quota === used && (
          <div className="flex items-start gap-2 text-xs text-amber-800 bg-amber-50 border border-amber-200 rounded-lg p-3">
            <AlertTriangle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
            <p>
              Setting the quota equal to active seats ({used}) will prevent this organization from
              inviting additional team members until quota is expanded.
            </p>
          </div>
        )}

        {quota > used && (
          <div className="flex items-start gap-2 text-xs text-emerald-800 bg-emerald-50 border border-emerald-200 rounded-lg p-3">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
            <p>
              Provides capacity for <strong>{quota - used}</strong> additional users to be onboarded.
            </p>
          </div>
        )}

        {/* Modal Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <Button
            type="button"
            variant="ghost"
            onClick={onClose}
            disabled={updateQuota.isPending}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={updateQuota.isPending}
            disabled={!!error}
          >
            Update Quota
          </Button>
        </div>
      </form>
    </Modal>
  );
};

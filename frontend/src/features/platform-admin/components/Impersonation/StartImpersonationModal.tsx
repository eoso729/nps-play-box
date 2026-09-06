import React, { useState, useEffect } from 'react';
import { ShieldAlert, Building2, Clock, User } from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { useRequestImpersonation } from '../../hooks/usePlatformImpersonation';
import { usePlatformTenants } from '../../hooks/usePlatformTenants';
import { PlatformTenant } from '../../types/platform-admin.types';

interface StartImpersonationModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialTenant?: PlatformTenant | null;
}

export const StartImpersonationModal: React.FC<StartImpersonationModalProps> = ({
  isOpen,
  onClose,
  initialTenant,
}) => {
  const requestImpersonation = useRequestImpersonation();
  const { data: tenantsData } = usePlatformTenants({ size: 100 });

  const [selectedTenantId, setSelectedTenantId] = useState<number | string>('');
  const [targetUserId, setTargetUserId] = useState<string>('');
  const [reason, setReason] = useState<string>('');
  const [durationMinutes, setDurationMinutes] = useState<number>(60);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (initialTenant) {
      setSelectedTenantId(initialTenant.id);
    }
  }, [initialTenant]);

  const tenants = tenantsData?.content || [];

  const validate = () => {
    const errs: Record<string, string> = {};
    if (!targetUserId || isNaN(Number(targetUserId)) || Number(targetUserId) <= 0) {
      errs.targetUserId = 'Target user ID must be a positive integer';
    }
    if (!reason || reason.trim().length < 20) {
      errs.reason = 'Business justification must be at least 20 characters';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await requestImpersonation.mutateAsync({
        targetUserId: Number(targetUserId),
        reason: reason.trim(),
        durationMinutes,
      });
      setReason('');
      setTargetUserId('');
      setErrors({});
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Request Support Impersonation"
      description="Initiate a dual-authorized, time-bounded support session to troubleshoot tenant issues"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="p-5 space-y-4">
        {/* Security Warning Notice */}
        <div className="bg-amber-50 border border-amber-200/80 rounded-xl p-4 flex gap-3 text-xs text-amber-900">
          <ShieldAlert className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <p className="font-semibold text-amber-950">Dual Authorization & Audit Protocol</p>
            <p className="text-amber-800 leading-relaxed">
              Impersonation sessions cannot be self-approved. A secondary platform administrator must
              review and authorize this request. All actions performed during the session are logged
              to the immutable audit trail.
            </p>
          </div>
        </div>

        {/* Tenant Selection */}
        <div>
          <label htmlFor="tenant-select" className="block text-xs font-semibold text-gray-700 mb-1.5">
            Target Organization
          </label>
          <div className="relative">
            <select
              id="tenant-select"
              value={selectedTenantId}
              onChange={(e) => setSelectedTenantId(e.target.value)}
              className="w-full text-sm border border-gray-300 rounded-lg pl-9 pr-3 py-2 bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500"
            >
              <option value="">Select an organization (optional context)</option>
              {tenants.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name} ({t.slug})
                </option>
              ))}
            </select>
            <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
          </div>
        </div>

        {/* Target User ID */}
        <div>
          <Input
            label="Target User ID *"
            type="number"
            min={1}
            placeholder="e.g. 102"
            value={targetUserId}
            onChange={(e) => setTargetUserId(e.target.value)}
            error={errors.targetUserId}
            helperText="The internal numeric ID of the user account to be impersonated"
            leftIcon={<User className="w-4 h-4 text-gray-400" />}
          />
        </div>

        {/* Duration Selection */}
        <div>
          <Select
            label="Session Max Duration"
            value={durationMinutes.toString()}
            onChange={(e) => setDurationMinutes(Number(e.target.value))}
            options={[
              { value: '15', label: '15 Minutes (Quick check)' },
              { value: '30', label: '30 Minutes' },
              { value: '60', label: '1 Hour (Recommended)' },
              { value: '120', label: '2 Hours' },
              { value: '240', label: '4 Hours (Maximum allowable)' },
            ]}
            leftIcon={<Clock className="w-4 h-4 text-gray-400" />}
          />
        </div>

        {/* Justification Textarea */}
        <div>
          <label htmlFor="impersonation-reason" className="block text-xs font-semibold text-gray-700 mb-1.5">
            Business Justification *
          </label>
          <textarea
            id="impersonation-reason"
            rows={3}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Describe the customer support ticket, defect, or operational reason requiring impersonation..."
            className={`w-full px-3.5 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 ${
              errors.reason
                ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                : 'border-gray-300 focus:ring-purple-500/20 focus:border-purple-500'
            }`}
          />
          {errors.reason ? (
            <p className="text-xs text-rose-600 mt-1">{errors.reason}</p>
          ) : (
            <p className="text-[11px] text-gray-500 mt-1">
              Minimum 20 characters. Will be recorded in the security audit event.
            </p>
          )}
        </div>

        {/* Modal Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <Button
            type="button"
            variant="ghost"
            onClick={onClose}
            disabled={requestImpersonation.isPending}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={requestImpersonation.isPending}
            className="bg-purple-600 hover:bg-purple-700 text-white"
          >
            Submit Request
          </Button>
        </div>
      </form>
    </Modal>
  );
};

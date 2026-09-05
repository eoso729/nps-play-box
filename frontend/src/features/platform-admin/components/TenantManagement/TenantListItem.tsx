import React from 'react';
import {
  Building2,
  Users,
  Settings,
  KeyRound,
} from 'lucide-react';
import { PlatformTenant, TenantStatus } from '../../types/platform-admin.types';
import { Badge } from '../../../shared/components/Badge';
import { ProgressBar } from '../../../shared/components/ProgressBar';

interface TenantListItemProps {
  tenant: PlatformTenant;
  onViewDetails: () => void;
  onManageQuota: () => void;
  onRequestImpersonation: () => void;
}

export const TenantListItem: React.FC<TenantListItemProps> = ({
  tenant,
  onViewDetails,
  onManageQuota,
  onRequestImpersonation,
}) => {
  const getStatusBadge = (status: string) => {
    switch (status) {
      case TenantStatus.ACTIVE:
        return <Badge variant="success" size="sm">Active</Badge>;
      case TenantStatus.TRIAL:
        return <Badge variant="info" size="sm">Trial</Badge>;
      case TenantStatus.SUSPENDED:
        return <Badge variant="danger" size="sm">Suspended</Badge>;
      case TenantStatus.DEACTIVATED:
        return <Badge variant="neutral" size="sm">Deactivated</Badge>;
      default:
        return <Badge variant="neutral" size="sm">{status}</Badge>;
    }
  };

  const getTierBadge = (tier: string) => {
    switch (tier?.toUpperCase()) {
      case 'ENTERPRISE':
        return <Badge variant="purple" size="sm">Enterprise</Badge>;
      case 'PROFESSIONAL':
        return <Badge variant="blue" size="sm">Professional</Badge>;
      case 'STANDARD':
        return <Badge variant="neutral" size="sm">Standard</Badge>;
      default:
        return <Badge variant="neutral" size="sm">{tier || 'Standard'}</Badge>;
    }
  };

  const quota = tenant.maxSeats || 1;
  const used = tenant.usedSeats || 0;
  const percentage = Math.min(100, Math.round((used / quota) * 100));

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <tr className="hover:bg-gray-50/80 transition-colors border-b border-gray-100 last:border-0">
      {/* Tenant Identity */}
      <td className="px-5 py-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-700 flex-shrink-0">
            <Building2 className="w-5 h-5" />
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-900 text-sm truncate">{tenant.name}</span>
            </div>
            <div className="flex items-center gap-1.5 text-xs text-gray-500 mt-0.5">
              <span className="font-mono text-[11px] text-gray-600 bg-gray-100 px-1.5 py-0.5 rounded">
                {tenant.slug}
              </span>
              <span>·</span>
              <span>{tenant.slug}.npsplaybox.com</span>
            </div>
          </div>
        </div>
      </td>

      {/* Subscription Tier */}
      <td className="px-5 py-4 whitespace-nowrap">
        {getTierBadge(tenant.subscriptionTier)}
      </td>

      {/* Status */}
      <td className="px-5 py-4 whitespace-nowrap">
        {getStatusBadge(tenant.status)}
      </td>

      {/* Seat Allocation & Progress */}
      <td className="px-5 py-4 min-w-[180px]">
        <div className="flex items-center justify-between text-xs mb-1">
          <span className="font-medium text-gray-700">
            {used} / {quota} seats
          </span>
          <span
            className={`font-semibold ${
              percentage >= 90 ? 'text-rose-600' : percentage >= 80 ? 'text-amber-600' : 'text-emerald-700'
            }`}
          >
            {percentage}%
          </span>
        </div>
        <ProgressBar value={percentage} size="sm" />
      </td>

      {/* Created Date */}
      <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-500">
        {formatDate(tenant.createdAt)}
      </td>

      {/* Actions */}
      <td className="px-5 py-4 whitespace-nowrap text-right text-xs">
        <div className="flex items-center justify-end gap-1.5">
          <button
            type="button"
            onClick={onManageQuota}
            className="px-2.5 py-1.5 font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 hover:text-emerald-700 transition-colors inline-flex items-center gap-1"
            title="Adjust Seat Quota"
          >
            <Users className="w-3.5 h-3.5" />
            Quota
          </button>
          <button
            type="button"
            onClick={onRequestImpersonation}
            className="px-2.5 py-1.5 font-medium text-purple-700 bg-purple-50 border border-purple-200 rounded-lg hover:bg-purple-100 transition-colors inline-flex items-center gap-1"
            title="Request Support Impersonation"
          >
            <KeyRound className="w-3.5 h-3.5" />
            Impersonate
          </button>
          <button
            type="button"
            onClick={onViewDetails}
            className="px-2.5 py-1.5 font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors inline-flex items-center gap-1"
            title="Manage Tenant Details"
          >
            <Settings className="w-3.5 h-3.5" />
            Manage
          </button>
        </div>
      </td>
    </tr>
  );
};

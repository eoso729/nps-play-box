import React from 'react';
import { TenantDetails } from '../../types/tenant-admin.types';
import { Building2, Hash, Layers, ShieldCheck, Calendar } from 'lucide-react';

interface GeneralSettingsProps {
  tenant: TenantDetails;
}

export const GeneralSettings: React.FC<GeneralSettingsProps> = ({ tenant }) => {
  const formattedDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h4 className="text-sm font-bold text-gray-900 mb-1">Organization Profile</h4>
        <p className="text-xs text-gray-500">
          Core tenant identification and subscription tier parameters.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <Building2 className="w-3.5 h-3.5 text-[#16a34a]" />
            Organization Name
          </div>
          <div className="text-sm font-bold text-gray-900">{tenant.name}</div>
        </div>

        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <Hash className="w-3.5 h-3.5 text-gray-500" />
            Organization Slug
          </div>
          <div className="text-sm font-bold text-gray-900 font-mono">{tenant.slug}</div>
        </div>

        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <Layers className="w-3.5 h-3.5 text-blue-600" />
            Subscription Plan
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-gray-900">
              {tenant.subscriptionTier || 'Standard'}
            </span>
            <span className="text-[11px] font-semibold bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full border border-blue-200">
              Active
            </span>
          </div>
        </div>

        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <ShieldCheck className="w-3.5 h-3.5 text-purple-600" />
            Tenant UUID
          </div>
          <div className="text-xs font-mono text-gray-700 truncate" title={tenant.tenantUuid}>
            {tenant.tenantUuid}
          </div>
        </div>

        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <Calendar className="w-3.5 h-3.5 text-gray-500" />
            Created On
          </div>
          <div className="text-sm font-bold text-gray-900">{formattedDate(tenant.createdAt)}</div>
        </div>

        <div className="p-4 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl">
          <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 mb-1">
            <Layers className="w-3.5 h-3.5 text-[#16a34a]" />
            Provisioned Quota
          </div>
          <div className="text-sm font-bold text-gray-900">{tenant.maxSeats} Total Seats</div>
        </div>
      </div>
    </div>
  );
};

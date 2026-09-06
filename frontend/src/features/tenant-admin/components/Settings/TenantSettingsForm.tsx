import React from 'react';
import { useTenantSettings } from '../../hooks/useTenantSettings';
import { GeneralSettings } from './GeneralSettings';
import { BrandingSettings } from './BrandingSettings';
import { Button } from '../../../shared/components/Button';
import { ShieldAlert } from 'lucide-react';

export const TenantSettingsForm: React.FC = () => {
  const { data: tenant, isLoading, error, refetch } = useTenantSettings();

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl border border-[#e4e9e6] p-6 shadow-sm animate-pulse space-y-6">
        <div className="h-6 bg-gray-200 rounded w-1/4" />
        <div className="grid grid-cols-2 gap-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-20 bg-gray-100 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  if (error || !tenant) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-center text-xs text-red-700">
        <ShieldAlert className="w-6 h-6 text-red-600 mx-auto mb-2" />
        <p className="font-bold text-sm mb-1">Failed to load tenant configuration</p>
        <p className="mb-4 text-red-600">Could not retrieve organizational parameters.</p>
        <Button size="sm" variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] p-6 shadow-sm space-y-8">
      <GeneralSettings tenant={tenant} />
      <BrandingSettings
        initialCompanyName={tenant.name}
        initialLogoUrl=""
        initialPrimaryColor="#16a34a"
      />
    </div>
  );
};

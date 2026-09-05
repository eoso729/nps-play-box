import React, { useState } from 'react';
import {
  LayoutDashboard,
  Building2,
  Layers,
  KeyRound,
  ShieldAlert,
} from 'lucide-react';
import { AppHeader } from '../../../components/layout/AppHeader';
import { PlatformMetrics } from '../components/Dashboard/PlatformMetrics';
import { TenantGrowthChart } from '../components/Dashboard/TenantGrowthChart';
import { SeatUtilizationChart } from '../components/Dashboard/SeatUtilizationChart';
import { RecentActivityFeed } from '../components/Dashboard/RecentActivityFeed';
import { TenantList } from '../components/TenantManagement/TenantList';
import { PendingSeatRequestsList } from '../components/SeatRequests/PendingSeatRequestsList';
import { ImpersonationRequestList } from '../components/Impersonation/ImpersonationRequestList';
import { StartImpersonationModal } from '../components/Impersonation/StartImpersonationModal';
import { AuditLogViewer } from '../components/AuditLogs/AuditLogViewer';
import { PlatformTenant } from '../types/platform-admin.types';
import { Button } from '../../shared/components/Button';

export type PlatformAdminTab =
  | 'overview'
  | 'tenants'
  | 'seat-requests'
  | 'impersonation'
  | 'audit';

export const PlatformAdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PlatformAdminTab>('overview');
  const [impersonationModalOpen, setImpersonationModalOpen] = useState(false);
  const [targetTenant, setTargetTenant] = useState<PlatformTenant | null>(null);

  const handleOpenImpersonation = (tenant?: PlatformTenant) => {
    setTargetTenant(tenant || null);
    setImpersonationModalOpen(true);
  };

  const tabs: { id: PlatformAdminTab; label: string; icon: React.FC<{ className?: string }> }[] = [
    { id: 'overview', label: 'Dashboard Overview', icon: LayoutDashboard },
    { id: 'tenants', label: 'Tenant Directory', icon: Building2 },
    { id: 'seat-requests', label: 'Seat Expansion Requests', icon: Layers },
    { id: 'impersonation', label: 'Support Impersonation', icon: KeyRound },
    { id: 'audit', label: 'System Audit Logs', icon: ShieldAlert },
  ];

  return (
    <div className="min-h-screen bg-[#f6f9f7] flex flex-col">
      <AppHeader />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Page Hero Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white border border-[#e4e9e6] rounded-2xl p-6 shadow-sm">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-800 bg-emerald-50 border border-emerald-200/80 px-2.5 py-0.5 rounded-full">
                Platform Control Plane
              </span>
            </div>
            <h1 className="text-2xl font-bold text-gray-900">Platform Administration</h1>
            <p className="text-xs text-gray-500 mt-1">
              Enterprise tenant lifecycle provisioning, capacity quotas, supervised support impersonation, and audit trails
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleOpenImpersonation()}
              className="text-purple-700 border-purple-200 hover:bg-purple-50"
            >
              <KeyRound className="w-4 h-4 mr-1.5" />
              Request Impersonation
            </Button>
          </div>
        </div>

        {/* Tab Navigation Navigation Bar */}
        <div className="flex items-center gap-1.5 overflow-x-auto bg-white p-1.5 border border-[#e4e9e6] rounded-xl shadow-sm">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isSelected = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                  isSelected
                    ? 'bg-[#15803d] text-white shadow-sm'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100/80'
                }`}
              >
                <Icon className={`w-4 h-4 ${isSelected ? 'text-white' : 'text-gray-500'}`} />
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Tab Content Panels */}
        <div className="pt-2">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              <PlatformMetrics />
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <TenantGrowthChart />
                <SeatUtilizationChart />
              </div>
              <RecentActivityFeed />
            </div>
          )}

          {activeTab === 'tenants' && (
            <TenantList
              onRequestImpersonation={(tenant) => handleOpenImpersonation(tenant)}
            />
          )}

          {activeTab === 'seat-requests' && (
            <PendingSeatRequestsList />
          )}

          {activeTab === 'impersonation' && (
            <ImpersonationRequestList />
          )}

          {activeTab === 'audit' && (
            <AuditLogViewer />
          )}
        </div>
      </main>

      {/* Start Impersonation Modal */}
      <StartImpersonationModal
        isOpen={impersonationModalOpen}
        onClose={() => {
          setImpersonationModalOpen(false);
          setTargetTenant(null);
        }}
        initialTenant={targetTenant}
      />
    </div>
  );
};

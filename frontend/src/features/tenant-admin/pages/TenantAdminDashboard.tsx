import React, { useState } from 'react';
import { SeatUsageCard } from '../components/SeatUsage/SeatUsageCard';
import { UserList } from '../components/UserManagement/UserList';
import { InvitationForm } from '../components/Invitations/InvitationForm';
import { InvitationList } from '../components/Invitations/InvitationList';
import { TenantSettingsForm } from '../components/Settings/TenantSettingsForm';
import { AuditLogTab } from '../components/Audit/AuditLogTab';
import { AppHeader } from '../../../components/layout/AppHeader';
import { ToastProvider } from '../../shared/hooks/useToast';
import { Users, Mail, Settings, ShieldCheck } from 'lucide-react';

type AdminTab = 'users' | 'invitations' | 'settings' | 'audit';

export const TenantAdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<AdminTab>('users');

  const tabs: { id: AdminTab; label: string; icon: React.ReactNode }[] = [
    { id: 'users', label: 'Team Members', icon: <Users className="w-4 h-4" /> },
    { id: 'invitations', label: 'Invitations', icon: <Mail className="w-4 h-4" /> },
    { id: 'settings', label: 'Settings', icon: <Settings className="w-4 h-4" /> },
    { id: 'audit', label: 'Audit Logs', icon: <ShieldCheck className="w-4 h-4" /> },
  ];

  return (
    <ToastProvider>
      <div className="min-h-screen bg-[#f6f9f7] flex flex-col text-[#0f3a22]">
        <AppHeader />

        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
          {/* Page Hero Header */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white border border-[#e4e9e6] rounded-2xl p-6 shadow-sm">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-blue-800 bg-blue-50 border border-blue-200/80 px-2.5 py-0.5 rounded-full">
                  Tenant Control Plane
                </span>
              </div>
              <h1 className="text-2xl font-bold text-gray-900">Tenant Administration</h1>
              <p className="text-xs text-gray-500 mt-1">
                Manage your organization's user lifecycle, seat allocation quotas, invitations, and compliance logs.
              </p>
            </div>
          </div>

          {/* Seat Capacity Card */}
          <section aria-label="Seat Utilization">
            <SeatUsageCard />
          </section>

          {/* Navigation Tabs */}
          <div className="space-y-6">
            <div className="flex items-center gap-1.5 overflow-x-auto bg-white p-1.5 border border-[#e4e9e6] rounded-xl shadow-sm">
              {tabs.map((tab) => {
                const isActive = activeTab === tab.id;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setActiveTab(tab.id)}
                    className={`flex items-center gap-2 px-4 py-2.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                      isActive
                        ? 'bg-[#15803d] text-white shadow-sm'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100/80'
                    }`}
                  >
                    <span className={isActive ? 'text-white' : 'text-gray-500'}>{tab.icon}</span>
                    {tab.label}
                  </button>
                );
              })}
            </div>

            {/* Tab Panels */}
            <div>
              {activeTab === 'users' && (
                <UserList onOpenInvite={() => setActiveTab('invitations')} />
              )}

              {activeTab === 'invitations' && (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  <div className="lg:col-span-1">
                    <InvitationForm onSuccess={() => {}} />
                  </div>
                  <div className="lg:col-span-2">
                    <InvitationList />
                  </div>
                </div>
              )}

              {activeTab === 'settings' && <TenantSettingsForm />}

              {activeTab === 'audit' && <AuditLogTab />}
            </div>
          </div>
        </main>
      </div>
    </ToastProvider>
  );
};

export default TenantAdminDashboard;

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
    { id: 'settings', label: 'Organization Settings', icon: <Settings className="w-4 h-4" /> },
    { id: 'audit', label: 'Compliance Audit', icon: <ShieldCheck className="w-4 h-4" /> },
  ];

  return (
    <ToastProvider>
      <div className="min-h-screen bg-[#f6f9f7] flex flex-col text-[#0f3a22]">
        <AppHeader />

        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
          {/* Page Title & Overview */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2.5">
                <div className="w-2.5 h-2.5 rounded-full bg-[#16a34a]" />
                <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                  Tenant Administration
                </h1>
              </div>
              <p className="mt-1.5 text-xs sm:text-sm text-gray-500">
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
            <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 shadow-inner overflow-x-auto">
              {tabs.map((tab) => {
                const isActive = activeTab === tab.id;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setActiveTab(tab.id)}
                    className={`flex-1 min-w-[140px] py-2.5 px-4 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-2 cursor-pointer ${
                      isActive
                        ? 'bg-white text-[#16a34a] shadow-sm'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
                    }`}
                  >
                    {tab.icon}
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

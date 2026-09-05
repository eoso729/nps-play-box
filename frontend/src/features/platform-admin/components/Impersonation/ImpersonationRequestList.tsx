import React, { useState } from 'react';
import { KeyRound, Plus, ShieldCheck, Clock, UserCheck } from 'lucide-react';
import {
  usePendingImpersonations,
  useMyImpersonations,
} from '../../hooks/usePlatformImpersonation';
import { ImpersonationRequestItem } from './ImpersonationRequestItem';
import { StartImpersonationModal } from './StartImpersonationModal';
import { Button } from '../../../shared/components/Button';

export const ImpersonationRequestList: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'pending' | 'my-sessions'>('pending');
  const [modalOpen, setModalOpen] = useState(false);

  const pendingQuery = usePendingImpersonations();
  const mySessionsQuery = useMyImpersonations();

  const currentList = activeTab === 'pending' ? pendingQuery.data || [] : mySessionsQuery.data || [];
  const isLoading = activeTab === 'pending' ? pendingQuery.isLoading : mySessionsQuery.isLoading;

  return (
    <div className="space-y-5">
      {/* Header with Dual Auth Notice & Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <KeyRound className="w-5 h-5 text-purple-600" />
            Supervised Support Impersonation
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Strict dual-authorization workflow for cross-tenant technical support and troubleshooting
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          onClick={() => setModalOpen(true)}
          className="bg-purple-600 hover:bg-purple-700 text-white"
        >
          <Plus className="w-4 h-4 mr-1.5" />
          Request Impersonation
        </Button>
      </div>

      {/* Tabs / Filter Pills */}
      <div className="flex items-center gap-2 border-b border-gray-200 pb-3">
        <button
          type="button"
          onClick={() => setActiveTab('pending')}
          className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
            activeTab === 'pending'
              ? 'bg-purple-50 text-purple-700 border border-purple-200'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          <Clock className="w-3.5 h-3.5" />
          Pending Approvals ({pendingQuery.data?.length ?? 0})
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('my-sessions')}
          className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
            activeTab === 'my-sessions'
              ? 'bg-purple-50 text-purple-700 border border-purple-200'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          <UserCheck className="w-3.5 h-3.5" />
          My Sessions ({mySessionsQuery.data?.length ?? 0})
        </button>
      </div>

      {/* Content Feed */}
      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-gray-200 p-5 animate-pulse space-y-3">
              <div className="h-5 bg-gray-200 rounded w-1/3"></div>
              <div className="h-4 bg-gray-100 rounded w-2/3"></div>
              <div className="h-8 bg-gray-100 rounded w-full"></div>
            </div>
          ))}
        </div>
      ) : currentList.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200/80 p-12 text-center text-gray-500">
          <ShieldCheck className="w-10 h-10 mx-auto text-emerald-500 mb-2" />
          <p className="font-semibold text-gray-800 text-sm">
            {activeTab === 'pending'
              ? 'No pending impersonation requests'
              : 'You have no requested impersonation sessions'}
          </p>
          <p className="text-xs text-gray-400 mt-1">
            {activeTab === 'pending'
              ? 'All cross-tenant support sessions have been reviewed and authorized'
              : 'Click "Request Impersonation" if you require supervised access to troubleshoot an issue'}
          </p>
        </div>
      ) : (
        <div className="space-y-3.5">
          {currentList.map((session) => (
            <ImpersonationRequestItem key={session.sessionUuid} session={session} />
          ))}
        </div>
      )}

      {/* Request Modal */}
      <StartImpersonationModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
      />
    </div>
  );
};

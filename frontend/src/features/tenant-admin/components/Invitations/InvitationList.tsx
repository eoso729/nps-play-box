import React, { useState, useMemo } from 'react';
import { useInvitations, useCancelInvitation } from '../../hooks/useInvitations';
import { InvitationListItem } from './InvitationListItem';
import { Input } from '../../../shared/components/Input';
import { Button } from '../../../shared/components/Button';
import { Mail, Search, RefreshCw } from 'lucide-react';

export const InvitationList: React.FC = () => {
  const [search, setSearch] = useState('');
  const { data: invitations, isLoading, error, refetch } = useInvitations();
  const cancelMutation = useCancelInvitation();

  const filteredInvitations = useMemo(() => {
    if (!invitations) return [];
    if (!search.trim()) return invitations;
    const q = search.toLowerCase();
    return invitations.filter((inv) => inv.email.toLowerCase().includes(q));
  }, [invitations, search]);

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] shadow-sm overflow-hidden">
      <div className="p-5 sm:p-6 border-b border-[#e4e9e6]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-gray-900 leading-tight">
                Pending Invitations
              </h3>
              <span className="text-xs font-semibold bg-purple-50 text-purple-700 px-2.5 py-0.5 rounded-full border border-purple-200">
                {filteredInvitations.length} pending
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Outstanding organization invites that have not yet been accepted.
            </p>
          </div>

          <Button
            size="sm"
            variant="outline"
            icon={<RefreshCw className="w-3.5 h-3.5 text-gray-500" />}
            onClick={() => refetch()}
          >
            Refresh
          </Button>
        </div>

        <div className="w-full sm:w-80">
          <Input
            type="text"
            placeholder="Search invites by email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            leftIcon={<Search className="w-4 h-4" />}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="p-6 space-y-4 animate-pulse">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="flex items-center gap-4">
              <div className="w-10 h-10 bg-gray-200 rounded-full flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-1/3" />
                <div className="h-3 bg-gray-100 rounded w-1/4" />
              </div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="p-8 text-center text-xs text-red-700 bg-red-50/50">
          <p className="font-semibold mb-2">Unable to load pending invitations.</p>
          <Button size="sm" variant="outline" onClick={() => refetch()}>
            Try Again
          </Button>
        </div>
      ) : filteredInvitations.length === 0 ? (
        <div className="p-12 text-center">
          <div className="w-12 h-12 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center mx-auto mb-3 border border-purple-200">
            <Mail className="w-6 h-6" />
          </div>
          <h4 className="text-sm font-bold text-gray-900 mb-1">No pending invitations</h4>
          <p className="text-xs text-gray-500 max-w-sm mx-auto">
            {search
              ? 'No invitations match your search filter.'
              : 'All dispatched invitations have been accepted or expired.'}
          </p>
        </div>
      ) : (
        <div className="divide-y divide-[#e4e9e6]">
          {filteredInvitations.map((inv) => (
            <InvitationListItem
              key={inv.id}
              invitation={inv}
              onRevoke={(id) => cancelMutation.mutate(id)}
              isRevoking={cancelMutation.isPending}
            />
          ))}
        </div>
      )}
    </div>
  );
};

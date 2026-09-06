import React, { useState } from 'react';
import { useCreateInvitation } from '../../hooks/useInvitations';
import { useQuotaStatus } from '../../hooks/useSeatUsage';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { Button } from '../../../shared/components/Button';
import { Mail, Shield, UserPlus, AlertOctagon } from 'lucide-react';

interface InvitationFormProps {
  onSuccess?: () => void;
  onRequestSeats?: () => void;
}

export const InvitationForm: React.FC<InvitationFormProps> = ({
  onSuccess,
  onRequestSeats,
}) => {
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('DEVELOPER');
  const [error, setError] = useState<string | null>(null);

  const createInvitationMutation = useCreateInvitation();
  const { data: quota } = useQuotaStatus();

  const availableSeats = quota ? quota.availableSeats ?? Math.max(0, quota.maxSeats - quota.usedSeats) : 1;
  const isBlocked = availableSeats <= 0;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isBlocked) {
      setError('Cannot invite new members: organization seat quota is full.');
      return;
    }

    if (!email.trim() || !email.includes('@')) {
      setError('Please provide a valid member email address.');
      return;
    }

    setError(null);
    createInvitationMutation.mutate(
      { email: email.trim(), role },
      {
        onSuccess: () => {
          setEmail('');
          setRole('DEVELOPER');
          if (onSuccess) onSuccess();
        },
      }
    );
  };

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] p-6 shadow-sm">
      <div className="flex items-center gap-2 mb-1">
        <h3 className="text-base font-bold text-gray-900 leading-tight">Invite Team Member</h3>
      </div>
      <p className="text-xs text-gray-500 mb-5">
        Send an email invitation link with preset role authorization.
      </p>

      {isBlocked ? (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl mb-4 text-xs text-red-900 space-y-2">
          <div className="flex items-center gap-2 font-bold text-red-700">
            <AlertOctagon className="w-4 h-4 flex-shrink-0" />
            Seat Limit Reached
          </div>
          <p className="text-red-800 leading-relaxed">
            Your organization has filled all allocated seats ({quota?.maxSeats} total). Request
            additional seats to invite more team members.
          </p>
          {onRequestSeats && (
            <Button size="sm" variant="danger" onClick={onRequestSeats} className="w-full mt-2">
              Request More Seats
            </Button>
          )}
        </div>
      ) : (
        <div className="p-3 bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl mb-4 text-xs text-gray-600 flex items-center justify-between">
          <span>Available seats remaining:</span>
          <span className="font-bold text-[#15803d]">{availableSeats} seat(s)</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 font-medium">
            {error}
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Member Email Address <span className="text-red-500">*</span>
          </label>
          <Input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="engineer@organization.com"
            leftIcon={<Mail className="w-4 h-4" />}
            disabled={isBlocked}
            required
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">Assigned Role</label>
          <Select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            leftIcon={<Shield className="w-3.5 h-3.5" />}
            disabled={isBlocked}
            options={[
              { value: 'DEVELOPER', label: 'Developer (Standard access)' },
              { value: 'VIEWER', label: 'Viewer (Read-only access)' },
              { value: 'TENANT_ADMIN', label: 'Tenant Administrator (Full team control)' },
            ]}
          />
        </div>

        <div className="pt-2">
          <Button
            type="submit"
            variant="primary"
            size="md"
            className="w-full"
            isLoading={createInvitationMutation.isPending}
            disabled={isBlocked}
            icon={<UserPlus className="w-4 h-4" />}
          >
            Send Invitation
          </Button>
        </div>
      </form>
    </div>
  );
};

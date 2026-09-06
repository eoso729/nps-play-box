import React from 'react';
import { TenantInvitation } from '../../types/tenant-admin.types';
import { Badge } from '../../../shared/components/Badge';
import { Button } from '../../../shared/components/Button';
import { ResendInvitationButton } from './ResendInvitationButton';
import { Mail, Clock, ShieldCheck, Code, Eye, XCircle } from 'lucide-react';

interface InvitationListItemProps {
  invitation: TenantInvitation;
  onRevoke: (id: number | string) => void;
  isRevoking?: boolean;
}

export const InvitationListItem: React.FC<InvitationListItemProps> = ({
  invitation,
  onRevoke,
  isRevoking = false,
}) => {
  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'TENANT_ADMIN':
        return (
          <Badge variant="purple" icon={<ShieldCheck className="w-3.5 h-3.5" />}>
            Tenant Admin
          </Badge>
        );
      case 'DEVELOPER':
        return (
          <Badge variant="success" icon={<Code className="w-3.5 h-3.5" />}>
            Developer
          </Badge>
        );
      default:
        return (
          <Badge variant="neutral" icon={<Eye className="w-3.5 h-3.5" />}>
            Viewer
          </Badge>
        );
    }
  };

  const isExpired =
    invitation.expired || (invitation.expiresAt && new Date(invitation.expiresAt) < new Date());

  const formattedExpiry = () => {
    if (!invitation.expiresAt) return 'No expiration set';
    try {
      const date = new Date(invitation.expiresAt);
      return `Expires: ${date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      })}`;
    } catch {
      return 'Expires soon';
    }
  };

  return (
    <div className="p-4 sm:px-6 hover:bg-[#fbfdfc] transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#e4e9e6] last:border-b-0">
      <div className="flex items-center gap-3.5 min-w-0">
        <div className="w-10 h-10 rounded-full bg-purple-50 text-purple-700 flex items-center justify-center flex-shrink-0 border border-purple-200/80">
          <Mail className="w-5 h-5" />
        </div>

        <div className="min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-0.5">
            <span className="text-sm font-bold text-gray-900 font-mono truncate">
              {invitation.email}
            </span>
            {getRoleBadge(String(invitation.role))}
            {isExpired ? (
              <Badge variant="danger">Expired</Badge>
            ) : (
              <Badge variant="warning" icon={<Clock className="w-3 h-3" />}>
                Pending
              </Badge>
            )}
          </div>
          <div className="flex items-center gap-3 text-xs text-gray-500">
            {invitation.invitedByName && (
              <>
                <span>Invited by {invitation.invitedByName}</span>
                <span className="text-gray-300">&bull;</span>
              </>
            )}
            <span>{formattedExpiry()}</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2 self-end sm:self-center flex-shrink-0">
        <ResendInvitationButton email={invitation.email} role={String(invitation.role)} />

        <Button
          size="sm"
          variant="ghost"
          className="text-red-600 hover:text-red-700 hover:bg-red-50"
          isLoading={isRevoking}
          icon={<XCircle className="w-3.5 h-3.5" />}
          onClick={() => onRevoke(invitation.id)}
        >
          Revoke
        </Button>
      </div>
    </div>
  );
};

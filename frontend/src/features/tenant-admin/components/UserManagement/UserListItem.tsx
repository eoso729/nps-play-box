import React from 'react';
import { TenantUser } from '../../types/tenant-admin.types';
import { Badge } from '../../../shared/components/Badge';
import { Button } from '../../../shared/components/Button';
import { ShieldCheck, Code, Eye, Shield, UserX, UserCheck, KeyRound } from 'lucide-react';

interface UserListItemProps {
  user: TenantUser;
  onRoleChange: (user: TenantUser) => void;
  onDelete: (user: TenantUser) => void;
  onToggleStatus: (user: TenantUser) => void;
  isStatusPending?: boolean;
}

export const UserListItem: React.FC<UserListItemProps> = ({
  user,
  onRoleChange,
  onDelete,
  onToggleStatus,
  isStatusPending = false,
}) => {
  const getInitials = () => {
    if (user.firstName || user.lastName) {
      return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
    }
    return (user.username || user.email || 'U')[0].toUpperCase();
  };

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
      case 'VIEWER':
        return (
          <Badge variant="neutral" icon={<Eye className="w-3.5 h-3.5" />}>
            Viewer
          </Badge>
        );
      case 'PLATFORM_ADMIN':
        return (
          <Badge variant="danger" icon={<Shield className="w-3.5 h-3.5" />}>
            Platform Admin
          </Badge>
        );
      default:
        return <Badge variant="neutral">{role}</Badge>;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge variant="success">Active</Badge>;
      case 'INACTIVE':
        return <Badge variant="neutral">Inactive</Badge>;
      case 'PENDING':
        return <Badge variant="warning">Pending</Badge>;
      case 'SUSPENDED':
        return <Badge variant="danger">Suspended</Badge>;
      default:
        return <Badge variant="neutral">{status}</Badge>;
    }
  };

  const formattedDate = (dateStr?: string) => {
    if (!dateStr) return 'Never logged in';
    try {
      return `Last active: ${new Date(dateStr).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      })}`;
    } catch {
      return 'Never logged in';
    }
  };

  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || user.email;

  return (
    <div className="p-4 sm:px-6 hover:bg-[#fbfdfc] transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#e4e9e6] last:border-b-0">
      <div className="flex items-center gap-3.5 min-w-0">
        <div className="w-10 h-10 rounded-full bg-[#e6f6ec] text-[#15803d] flex items-center justify-center font-bold text-xs flex-shrink-0 border border-[#d5e2d8]">
          {getInitials()}
        </div>

        <div className="min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-0.5">
            <span className="text-sm font-bold text-gray-900 truncate">{fullName}</span>
            {getRoleBadge(String(user.role))}
            {getStatusBadge(String(user.status))}
          </div>
          <div className="flex items-center gap-3 text-xs text-gray-500">
            <span className="font-mono text-gray-600 truncate">{user.email}</span>
            <span className="text-gray-300">&bull;</span>
            <span>{formattedDate(user.lastLoginAt)}</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2 self-end sm:self-center flex-shrink-0">
        <Button
          size="sm"
          variant="outline"
          icon={<KeyRound className="w-3.5 h-3.5 text-gray-500" />}
          onClick={() => onRoleChange(user)}
        >
          Change Role
        </Button>

        {user.status === 'ACTIVE' ? (
          <Button
            size="sm"
            variant="ghost"
            isLoading={isStatusPending}
            icon={<UserX className="w-3.5 h-3.5 text-amber-600" />}
            onClick={() => onToggleStatus(user)}
          >
            Deactivate
          </Button>
        ) : (
          <Button
            size="sm"
            variant="ghost"
            isLoading={isStatusPending}
            icon={<UserCheck className="w-3.5 h-3.5 text-emerald-600" />}
            onClick={() => onToggleStatus(user)}
          >
            Reactivate
          </Button>
        )}

        <Button
          size="sm"
          variant="ghost"
          className="text-red-600 hover:text-red-700 hover:bg-red-50"
          onClick={() => onDelete(user)}
        >
          Delete
        </Button>
      </div>
    </div>
  );
};

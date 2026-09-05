import React, { useState, useMemo } from 'react';
import { useUsers, useToggleUserStatus } from '../../hooks/useUsers';
import { TenantUser, UserFilters } from '../../types/tenant-admin.types';
import { UserFiltersComponent } from './UserFilters';
import { UserListItem } from './UserListItem';
import { RoleAssignmentModal } from './RoleAssignmentModal';
import { DeleteUserModal } from './DeleteUserModal';
import { Button } from '../../../shared/components/Button';
import { Users, UserPlus, ChevronLeft, ChevronRight } from 'lucide-react';

interface UserListProps {
  onOpenInvite?: () => void;
}

export const UserList: React.FC<UserListProps> = ({ onOpenInvite }) => {
  const [filters, setFilters] = useState<UserFilters>({
    page: 0,
    size: 15,
  });

  const [selectedUserForRole, setSelectedUserForRole] = useState<TenantUser | null>(null);
  const [selectedUserForDelete, setSelectedUserForDelete] = useState<TenantUser | null>(null);

  const { data: pageData, isLoading, error, refetch } = useUsers(filters);
  const toggleStatusMutation = useToggleUserStatus();

  // Local client-side filtering support if the backend page returns all records
  const users = useMemo(() => {
    let list = pageData?.content || [];
    if (filters.search) {
      const q = filters.search.toLowerCase();
      list = list.filter(
        (u) =>
          u.email.toLowerCase().includes(q) ||
          (u.firstName && u.firstName.toLowerCase().includes(q)) ||
          (u.lastName && u.lastName.toLowerCase().includes(q)) ||
          (u.username && u.username.toLowerCase().includes(q))
      );
    }
    if (filters.role) {
      list = list.filter((u) => u.role === filters.role);
    }
    if (filters.status) {
      list = list.filter((u) => u.status === filters.status);
    }
    return list;
  }, [pageData, filters]);

  const totalElements = pageData?.totalElements ?? users.length;
  const currentPage = filters.page ?? 0;
  const pageSize = filters.size ?? 15;
  const totalPages = pageData?.totalPages ?? Math.ceil(totalElements / pageSize);

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setFilters((prev) => ({ ...prev, page: currentPage - 1 }));
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setFilters((prev) => ({ ...prev, page: currentPage + 1 }));
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] shadow-sm overflow-hidden">
      {/* Header and Filters */}
      <div className="p-5 sm:p-6 border-b border-[#e4e9e6]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-gray-900 leading-tight">Team Members</h3>
              <span className="text-xs font-semibold bg-[#edf2ee] text-[#15803d] px-2.5 py-0.5 rounded-full border border-[#d5e2d8]">
                {totalElements} {totalElements === 1 ? 'member' : 'members'}
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Manage organization users, change authorization roles, and toggle access states.
            </p>
          </div>

          {onOpenInvite && (
            <Button
              size="sm"
              variant="primary"
              icon={<UserPlus className="w-4 h-4" />}
              onClick={onOpenInvite}
            >
              Invite Member
            </Button>
          )}
        </div>

        <UserFiltersComponent filters={filters} onFiltersChange={setFilters} />
      </div>

      {/* User Content */}
      {isLoading ? (
        <div className="p-6 space-y-4 animate-pulse">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="flex items-center gap-4">
              <div className="w-10 h-10 bg-gray-200 rounded-full flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-1/4" />
                <div className="h-3 bg-gray-100 rounded w-1/3" />
              </div>
              <div className="h-8 bg-gray-200 rounded w-20" />
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="p-8 text-center text-xs text-red-700 bg-red-50/50">
          <p className="font-semibold mb-2">Unable to load team members.</p>
          <Button size="sm" variant="outline" onClick={() => refetch()}>
            Try Again
          </Button>
        </div>
      ) : users.length === 0 ? (
        <div className="p-12 text-center">
          <div className="w-12 h-12 rounded-full bg-[#f6f9f7] text-[#15803d] flex items-center justify-center mx-auto mb-3 border border-[#e4e9e6]">
            <Users className="w-6 h-6" />
          </div>
          <h4 className="text-sm font-bold text-gray-900 mb-1">No team members found</h4>
          <p className="text-xs text-gray-500 max-w-sm mx-auto mb-4">
            {filters.search || filters.role || filters.status
              ? 'No members match the selected filters. Try clearing your search parameters.'
              : 'There are currently no additional members in this organization.'}
          </p>
          {onOpenInvite && (
            <Button
              size="sm"
              variant="secondary"
              icon={<UserPlus className="w-3.5 h-3.5" />}
              onClick={onOpenInvite}
            >
              Invite First Member
            </Button>
          )}
        </div>
      ) : (
        <div className="divide-y divide-[#e4e9e6]">
          {users.map((user) => (
            <UserListItem
              key={user.id}
              user={user}
              onRoleChange={(u) => setSelectedUserForRole(u)}
              onDelete={(u) => setSelectedUserForDelete(u)}
              onToggleStatus={(u) =>
                toggleStatusMutation.mutate({
                  userId: u.id,
                  currentStatus: String(u.status),
                })
              }
              isStatusPending={toggleStatusMutation.isPending}
            />
          ))}
        </div>
      )}

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <div className="p-4 border-t border-[#e4e9e6] bg-[#fbfdfc] flex items-center justify-between text-xs text-gray-600">
          <div>
            Showing page <span className="font-bold text-gray-900">{currentPage + 1}</span> of{' '}
            <span className="font-bold text-gray-900">{totalPages}</span>
          </div>

          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              icon={<ChevronLeft className="w-4 h-4" />}
              disabled={currentPage <= 0}
              onClick={handlePrevPage}
            >
              Previous
            </Button>
            <Button
              size="sm"
              variant="outline"
              disabled={currentPage >= totalPages - 1}
              onClick={handleNextPage}
            >
              Next <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* Modals */}
      {selectedUserForRole && (
        <RoleAssignmentModal
          isOpen={!!selectedUserForRole}
          onClose={() => setSelectedUserForRole(null)}
          user={selectedUserForRole}
        />
      )}

      {selectedUserForDelete && (
        <DeleteUserModal
          isOpen={!!selectedUserForDelete}
          onClose={() => setSelectedUserForDelete(null)}
          user={selectedUserForDelete}
        />
      )}
    </div>
  );
};

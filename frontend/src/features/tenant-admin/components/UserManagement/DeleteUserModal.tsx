import React from 'react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { TenantUser } from '../../types/tenant-admin.types';
import { useDeleteUser, useToggleUserStatus } from '../../hooks/useUsers';
import { AlertTriangle, UserX } from 'lucide-react';

interface DeleteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: TenantUser;
}

export const DeleteUserModal: React.FC<DeleteUserModalProps> = ({
  isOpen,
  onClose,
  user,
}) => {
  const deleteMutation = useDeleteUser();
  const toggleStatusMutation = useToggleUserStatus();

  const isActive = user.status === 'ACTIVE';

  const handleDeactivate = () => {
    toggleStatusMutation.mutate(
      { userId: user.id, currentStatus: user.status },
      {
        onSuccess: () => {
          onClose();
        },
      }
    );
  };

  const handleHardDelete = () => {
    deleteMutation.mutate(user.id, {
      onSuccess: () => {
        onClose();
      },
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isActive ? 'Deactivate Team Member' : 'Remove Team Member'}
      maxWidth="md"
    >
      <div className="space-y-4">
        <div className="flex items-start gap-3 p-3.5 bg-amber-50 border border-amber-200 rounded-xl text-amber-900 text-xs">
          <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
          <div>
            <span className="font-bold">Organization Seat Impact:</span>
            <p className="mt-0.5 text-amber-800 leading-relaxed">
              Deactivating or removing{' '}
              <span className="font-semibold text-gray-900">
                {user.firstName} {user.lastName} ({user.email})
              </span>{' '}
              will release 1 seat back to your organization's available quota immediately.
            </p>
          </div>
        </div>

        <p className="text-xs text-gray-600 leading-relaxed">
          {isActive
            ? 'Deactivating this member revokes their active sessions and login capabilities while preserving their audit history. You can reactivate them later if needed.'
            : 'This member is already inactive. Are you sure you want to permanently remove their association?'}
        </p>

        <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-[#e4e9e6]">
          <Button variant="outline" size="md" onClick={onClose}>
            Cancel
          </Button>

          {isActive ? (
            <Button
              variant="danger"
              size="md"
              isLoading={toggleStatusMutation.isPending}
              icon={<UserX className="w-4 h-4" />}
              onClick={handleDeactivate}
            >
              Deactivate Member
            </Button>
          ) : (
            <Button
              variant="danger"
              size="md"
              isLoading={deleteMutation.isPending}
              icon={<UserX className="w-4 h-4" />}
              onClick={handleHardDelete}
            >
              Remove Member
            </Button>
          )}
        </div>
      </div>
    </Modal>
  );
};

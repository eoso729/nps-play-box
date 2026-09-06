import React, { useState } from 'react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { TenantUser } from '../../types/tenant-admin.types';
import { useUpdateUserRole } from '../../hooks/useUsers';
import { ShieldCheck, Code, Eye, Check } from 'lucide-react';

interface RoleAssignmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: TenantUser;
}

export const RoleAssignmentModal: React.FC<RoleAssignmentModalProps> = ({
  isOpen,
  onClose,
  user,
}) => {
  const [selectedRole, setSelectedRole] = useState<string>(user.role || 'DEVELOPER');
  const updateRoleMutation = useUpdateUserRole();

  const handleSave = () => {
    updateRoleMutation.mutate(
      { userId: user.id, role: selectedRole },
      {
        onSuccess: () => {
          onClose();
        },
      }
    );
  };

  const roles = [
    {
      id: 'TENANT_ADMIN',
      name: 'Tenant Administrator',
      description:
        'Full control over team members, invitations, seat quotas, and organization settings.',
      icon: <ShieldCheck className="w-5 h-5 text-purple-600" />,
      color: 'border-purple-200 bg-purple-50/50',
    },
    {
      id: 'DEVELOPER',
      name: 'Developer',
      description:
        'Generate ISO 20022 messages, execute pipelines, run orchestrator flows, and use test diff tools.',
      icon: <Code className="w-5 h-5 text-emerald-600" />,
      color: 'border-emerald-200 bg-emerald-50/50',
    },
    {
      id: 'VIEWER',
      name: 'Viewer',
      description:
        'Read-only inspection of generated XML payloads, pipeline logs, and documentation.',
      icon: <Eye className="w-5 h-5 text-gray-600" />,
      color: 'border-gray-200 bg-gray-50/50',
    },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Change Member Role"
      description={`Update access privileges and role assignment for ${user.firstName || ''} ${
        user.lastName || ''
      } (${user.email}).`}
      maxWidth="lg"
    >
      <div className="space-y-3 my-2">
        {roles.map((role) => {
          const isSelected = selectedRole === role.id;
          return (
            <div
              key={role.id}
              onClick={() => setSelectedRole(role.id)}
              className={`p-3.5 rounded-xl border-2 transition-all cursor-pointer flex items-start justify-between gap-3 ${
                isSelected
                  ? 'border-[#16a34a] bg-emerald-50/40 shadow-sm'
                  : 'border-[#e4e9e6] hover:border-gray-300 hover:bg-gray-50/60'
              }`}
            >
              <div className="flex items-start gap-3">
                <div className="mt-0.5 p-2 rounded-lg bg-white border border-[#e4e9e6] shadow-2xs">
                  {role.icon}
                </div>
                <div>
                  <h4 className="text-sm font-bold text-gray-900">{role.name}</h4>
                  <p className="text-xs text-gray-500 mt-0.5 leading-relaxed">{role.description}</p>
                </div>
              </div>

              <div
                className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 mt-1 transition-all ${
                  isSelected
                    ? 'border-[#16a34a] bg-[#16a34a] text-white'
                    : 'border-gray-300 bg-white'
                }`}
              >
                {isSelected && <Check className="w-3 h-3 stroke-[3]" />}
              </div>
            </div>
          );
        })}
      </div>

      <div className="flex items-center justify-end gap-2.5 pt-5 border-t border-[#e4e9e6] mt-4">
        <Button variant="outline" size="md" onClick={onClose}>
          Cancel
        </Button>
        <Button
          variant="primary"
          size="md"
          isLoading={updateRoleMutation.isPending}
          disabled={selectedRole === user.role}
          onClick={handleSave}
        >
          Save Role Assignment
        </Button>
      </div>
    </Modal>
  );
};

import React from 'react';
import { Button } from '../../../shared/components/Button';
import { useResendInvitation } from '../../hooks/useInvitations';
import { RotateCw } from 'lucide-react';

interface ResendInvitationButtonProps {
  email: string;
  role: string;
}

export const ResendInvitationButton: React.FC<ResendInvitationButtonProps> = ({
  email,
  role,
}) => {
  const resendMutation = useResendInvitation();

  return (
    <Button
      size="sm"
      variant="ghost"
      isLoading={resendMutation.isPending}
      icon={<RotateCw className="w-3.5 h-3.5 text-gray-500" />}
      onClick={() => resendMutation.mutate({ email, role })}
    >
      Resend
    </Button>
  );
};

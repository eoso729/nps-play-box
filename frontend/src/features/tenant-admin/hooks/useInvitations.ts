import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { invitationsApi } from '../api/invitations.api';
import { useToast } from '../../shared/hooks/useToast';

export const useInvitations = () => {
  return useQuery({
    queryKey: ['tenant-invitations'],
    queryFn: () => invitationsApi.getInvitations(),
    staleTime: 15000,
  });
};

export const useCreateInvitation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (data: { email: string; role: string }) =>
      invitationsApi.createInvitation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-invitations'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-seats'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-quota-status'] });
      showToast('Invitation sent successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to send invitation', 'error');
    },
  });
};

export const useResendInvitation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (data: { email: string; role: string }) =>
      invitationsApi.resendInvitation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-invitations'] });
      showToast('Invitation re-sent successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to re-send invitation', 'error');
    },
  });
};

export const useCancelInvitation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (invitationId: number | string) =>
      invitationsApi.cancelInvitation(invitationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-invitations'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-seats'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-quota-status'] });
      showToast('Invitation revoked successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to revoke invitation', 'error');
    },
  });
};

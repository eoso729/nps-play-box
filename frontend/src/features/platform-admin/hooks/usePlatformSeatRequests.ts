import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { platformSeatRequestsApi } from '../api/platform-seat-requests.api';
import { useToast } from '../../shared/hooks/useToast';

export const usePlatformSeatRequests = () => {
  return useQuery({
    queryKey: ['platform-seat-requests'],
    queryFn: () => platformSeatRequestsApi.getAllPendingSeatRequests(),
    staleTime: 15000,
    refetchInterval: 30000,
  });
};

export const useApproveSeatRequest = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      requestId,
      reviewNote,
    }: {
      requestId: number | string;
      reviewNote?: string;
    }) => platformSeatRequestsApi.approveSeatRequest(requestId, { reviewNote }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-seat-requests'] });
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast('Seat expansion request approved and quota updated', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to approve seat request', 'error');
    },
  });
};

export const useDenySeatRequest = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      requestId,
      denialReason,
    }: {
      requestId: number | string;
      denialReason: string;
    }) => platformSeatRequestsApi.denySeatRequest(requestId, denialReason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-seat-requests'] });
      showToast('Seat expansion request rejected', 'info');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to reject seat request', 'error');
    },
  });
};

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { seatsApi } from '../api/seats.api';
import { SeatRequestPayload } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useSeatUsage = () => {
  return useQuery({
    queryKey: ['tenant-seats'],
    queryFn: () => seatsApi.getSeatUsage(),
    staleTime: 10000,
    refetchInterval: 30000,
  });
};

export const useQuotaStatus = () => {
  return useQuery({
    queryKey: ['tenant-quota-status'],
    queryFn: () => seatsApi.getQuotaStatus(),
    staleTime: 10000,
    refetchInterval: 30000,
  });
};

export const useTenantSeatRequests = () => {
  return useQuery({
    queryKey: ['tenant-seat-requests'],
    queryFn: () => seatsApi.getTenantSeatRequests(),
    staleTime: 15000,
  });
};

export const useRequestAdditionalSeats = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (data: SeatRequestPayload) => seatsApi.requestAdditionalSeats(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-seat-requests'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-quota-status'] });
      showToast('Seat increase request submitted for administrator review', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to submit seat request', 'error');
    },
  });
};

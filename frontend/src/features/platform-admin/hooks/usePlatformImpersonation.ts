import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { impersonationApi, ImpersonationSessionResponse } from '../../../api/impersonation';
import { setStoredToken } from '../../../api/client';
import { useToast } from '../../shared/hooks/useToast';

export const usePendingImpersonations = () => {
  return useQuery({
    queryKey: ['impersonation-pending'],
    queryFn: async () => {
      const res = await impersonationApi.getPendingSessions();
      return (res.data?.content || []) as ImpersonationSessionResponse[];
    },
    staleTime: 15000,
    refetchInterval: 30000,
  });
};

export const useMyImpersonations = () => {
  return useQuery({
    queryKey: ['impersonation-my-sessions'],
    queryFn: async () => {
      const res = await impersonationApi.getMySessions();
      return (res.data?.content || []) as ImpersonationSessionResponse[];
    },
    staleTime: 15000,
  });
};

export const useTenantImpersonations = (tenantId: number) => {
  return useQuery({
    queryKey: ['impersonation-tenant-sessions', tenantId],
    queryFn: async () => {
      const res = await impersonationApi.getTenantSessions(tenantId);
      return (res.data?.content || []) as ImpersonationSessionResponse[];
    },
    enabled: !!tenantId,
    staleTime: 15000,
  });
};

export const useRequestImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      targetUserId,
      reason,
      durationMinutes,
    }: {
      targetUserId: number;
      reason: string;
      durationMinutes?: number;
    }) => impersonationApi.requestSession(targetUserId, reason, durationMinutes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-pending'] });
      queryClient.invalidateQueries({ queryKey: ['impersonation-my-sessions'] });
      showToast('Support impersonation request submitted for dual-authorization', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to request impersonation', 'error');
    },
  });
};

export const useApproveImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      sessionUuid,
      approvalReason,
    }: {
      sessionUuid: string;
      approvalReason: string;
    }) => impersonationApi.approveSession(sessionUuid, approvalReason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-pending'] });
      queryClient.invalidateQueries({ queryKey: ['impersonation-my-sessions'] });
      showToast('Impersonation session approved', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Approval failed (dual authorization required)', 'error');
    },
  });
};

export const useRejectImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      sessionUuid,
      rejectionReason,
    }: {
      sessionUuid: string;
      rejectionReason: string;
    }) => impersonationApi.rejectSession(sessionUuid, rejectionReason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-pending'] });
      queryClient.invalidateQueries({ queryKey: ['impersonation-my-sessions'] });
      showToast('Impersonation session rejected', 'info');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Rejection failed', 'error');
    },
  });
};

export const useStartImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (sessionUuid: string) => impersonationApi.startSession(sessionUuid),
    onSuccess: (res) => {
      if (res.data.impersonationToken) {
        setStoredToken(res.data.impersonationToken);
        showToast('Entering supervised impersonation session...', 'success');
        setTimeout(() => {
          window.location.href = '/workbench';
        }, 500);
      }
      queryClient.invalidateQueries({ queryKey: ['impersonation-pending'] });
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to start impersonation session', 'error');
    },
  });
};

export const useTerminateImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ sessionUuid, reason }: { sessionUuid: string; reason?: string }) =>
      impersonationApi.terminateSession(sessionUuid, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-pending'] });
      queryClient.invalidateQueries({ queryKey: ['impersonation-my-sessions'] });
      showToast('Impersonation session terminated', 'info');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to terminate session', 'error');
    },
  });
};

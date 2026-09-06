import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi } from '../api/users.api';
import { UserFilters } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useUsers = (filters?: UserFilters) => {
  return useQuery({
    queryKey: ['tenant-users', filters],
    queryFn: () => usersApi.getUsers(filters),
    staleTime: 15000,
  });
};

export const useUser = (userId: number | string) => {
  return useQuery({
    queryKey: ['tenant-user', userId],
    queryFn: () => usersApi.getUser(userId),
    enabled: !!userId,
  });
};

export const useUpdateUser = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({
      userId,
      data,
    }: {
      userId: number | string;
      data: { firstName?: string; lastName?: string; email?: string };
    }) => usersApi.updateUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-users'] });
      showToast('User profile updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update user', 'error');
    },
  });
};

export const useUpdateUserRole = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ userId, role }: { userId: number | string; role: string }) =>
      usersApi.updateUserRole(userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-users'] });
      showToast('User role updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update user role', 'error');
    },
  });
};

export const useToggleUserStatus = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: async ({
      userId,
      currentStatus,
    }: {
      userId: number | string;
      currentStatus: string;
    }) => {
      if (currentStatus === 'ACTIVE') {
        return usersApi.deactivateUser(userId);
      } else {
        return usersApi.reactivateUser(userId);
      }
    },
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['tenant-users'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-seats'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-quota-status'] });
      showToast(
        variables.currentStatus === 'ACTIVE'
          ? 'User deactivated successfully'
          : 'User reactivated successfully',
        'success'
      );
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to toggle user status', 'error');
    },
  });
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (userId: number | string) => usersApi.deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-users'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-seats'] });
      queryClient.invalidateQueries({ queryKey: ['tenant-quota-status'] });
      showToast('User deleted successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to delete user', 'error');
    },
  });
};

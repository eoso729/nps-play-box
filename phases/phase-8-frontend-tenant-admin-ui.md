# Phase 8: Frontend - Tenant Administration UI

**Duration:** 6-8 days  
**Dependencies:** Phase 3 (User Management), Phase 4 (Invitation System), Phase 5 (Seat Management)  
**Tech Stack:** React, TypeScript, React Router, TanStack Query, React Hook Form, Tailwind CSS

## Overview

Build the tenant administrator interface for managing users, invitations, seat usage, and tenant settings. This phase provides tenant admins with full control over their organization's user lifecycle and configuration.

## Objectives

- Create intuitive user management interface with role assignment
- Implement invitation flow with email validation and tracking
- Visualize seat usage with warnings and limits
- Provide filtering and search for user lists
- Build tenant settings configuration page
- Integrate with backend APIs from phases 3, 4, and 5

## Architecture

### Component Structure

```
src/
├── features/
│   ├── tenant-admin/
│   │   ├── components/
│   │   │   ├── UserManagement/
│   │   │   │   ├── UserList.tsx
│   │   │   │   ├── UserListItem.tsx
│   │   │   │   ├── UserFilters.tsx
│   │   │   │   ├── RoleAssignmentModal.tsx
│   │   │   │   └── DeleteUserModal.tsx
│   │   │   ├── Invitations/
│   │   │   │   ├── InvitationForm.tsx
│   │   │   │   ├── InvitationList.tsx
│   │   │   │   ├── InvitationListItem.tsx
│   │   │   │   └── ResendInvitationButton.tsx
│   │   │   ├── SeatUsage/
│   │   │   │   ├── SeatUsageCard.tsx
│   │   │   │   ├── SeatUsageChart.tsx
│   │   │   │   └── SeatWarningBanner.tsx
│   │   │   └── Settings/
│   │   │       ├── TenantSettingsForm.tsx
│   │   │       ├── GeneralSettings.tsx
│   │   │       └── BrandingSettings.tsx
│   │   ├── hooks/
│   │   │   ├── useUsers.ts
│   │   │   ├── useInvitations.ts
│   │   │   ├── useSeatUsage.ts
│   │   │   └── useTenantSettings.ts
│   │   ├── api/
│   │   │   ├── users.api.ts
│   │   │   ├── invitations.api.ts
│   │   │   ├── seats.api.ts
│   │   │   └── settings.api.ts
│   │   └── types/
│   │       └── tenant-admin.types.ts
│   └── shared/
│       ├── components/
│       │   ├── Button.tsx
│       │   ├── Input.tsx
│       │   ├── Select.tsx
│       │   ├── Modal.tsx
│       │   └── ProgressBar.tsx
│       └── hooks/
│           └── useToast.ts
```

## Implementation

### 1. Type Definitions

```typescript
// src/features/tenant-admin/types/tenant-admin.types.ts

export enum UserRole {
  TENANT_ADMIN = 'TENANT_ADMIN',
  TENANT_USER = 'TENANT_USER',
  TENANT_VIEWER = 'TENANT_VIEWER',
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
}

export enum InvitationStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  EXPIRED = 'EXPIRED',
  REVOKED = 'REVOKED',
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  lastLoginAt?: string;
}

export interface Invitation {
  id: string;
  email: string;
  role: UserRole;
  status: InvitationStatus;
  invitedBy: string;
  invitedAt: string;
  expiresAt: string;
  acceptedAt?: string;
}

export interface SeatUsage {
  used: number;
  total: number;
  percentage: number;
  activeUsers: number;
  pendingInvitations: number;
}

export interface TenantSettings {
  tenantId: string;
  name: string;
  subdomain: string;
  allowSelfRegistration: boolean;
  requireEmailVerification: boolean;
  sessionTimeoutMinutes: number;
  branding?: {
    logoUrl?: string;
    primaryColor?: string;
    secondaryColor?: string;
  };
}

export interface UserFilters {
  search?: string;
  role?: UserRole;
  status?: UserStatus;
  sortBy?: 'name' | 'email' | 'createdAt' | 'lastLoginAt';
  sortOrder?: 'asc' | 'desc';
}

export interface InvitationFilters {
  search?: string;
  status?: InvitationStatus;
  sortBy?: 'email' | 'invitedAt' | 'expiresAt';
  sortOrder?: 'asc' | 'desc';
}
```

### 2. API Client

```typescript
// src/features/tenant-admin/api/users.api.ts

import axios from 'axios';
import { User, UserRole, UserStatus, UserFilters } from '../types/tenant-admin.types';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api/v1';

export const usersApi = {
  getUsers: async (filters?: UserFilters): Promise<User[]> => {
    const params = new URLSearchParams();
    if (filters?.search) params.append('search', filters.search);
    if (filters?.role) params.append('role', filters.role);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.sortBy) params.append('sortBy', filters.sortBy);
    if (filters?.sortOrder) params.append('sortOrder', filters.sortOrder);

    const response = await axios.get(`${API_BASE_URL}/tenant/users?${params.toString()}`);
    return response.data;
  },

  getUser: async (userId: string): Promise<User> => {
    const response = await axios.get(`${API_BASE_URL}/tenant/users/${userId}`);
    return response.data;
  },

  updateUserRole: async (userId: string, role: UserRole): Promise<User> => {
    const response = await axios.patch(`${API_BASE_URL}/tenant/users/${userId}/role`, { role });
    return response.data;
  },

  updateUserStatus: async (userId: string, status: UserStatus): Promise<User> => {
    const response = await axios.patch(`${API_BASE_URL}/tenant/users/${userId}/status`, { status });
    return response.data;
  },

  deleteUser: async (userId: string): Promise<void> => {
    await axios.delete(`${API_BASE_URL}/tenant/users/${userId}`);
  },
};

// src/features/tenant-admin/api/invitations.api.ts

import axios from 'axios';
import { Invitation, UserRole, InvitationFilters } from '../types/tenant-admin.types';

export const invitationsApi = {
  getInvitations: async (filters?: InvitationFilters): Promise<Invitation[]> => {
    const params = new URLSearchParams();
    if (filters?.search) params.append('search', filters.search);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.sortBy) params.append('sortBy', filters.sortBy);
    if (filters?.sortOrder) params.append('sortOrder', filters.sortOrder);

    const response = await axios.get(`${API_BASE_URL}/tenant/invitations?${params.toString()}`);
    return response.data;
  },

  createInvitation: async (email: string, role: UserRole): Promise<Invitation> => {
    const response = await axios.post(`${API_BASE_URL}/tenant/invitations`, { email, role });
    return response.data;
  },

  resendInvitation: async (invitationId: string): Promise<Invitation> => {
    const response = await axios.post(`${API_BASE_URL}/tenant/invitations/${invitationId}/resend`);
    return response.data;
  },

  revokeInvitation: async (invitationId: string): Promise<void> => {
    await axios.delete(`${API_BASE_URL}/tenant/invitations/${invitationId}`);
  },
};

// src/features/tenant-admin/api/seats.api.ts

import axios from 'axios';
import { SeatUsage } from '../types/tenant-admin.types';

export const seatsApi = {
  getSeatUsage: async (): Promise<SeatUsage> => {
    const response = await axios.get(`${API_BASE_URL}/tenant/seats/usage`);
    return response.data;
  },
};

// src/features/tenant-admin/api/settings.api.ts

import axios from 'axios';
import { TenantSettings } from '../types/tenant-admin.types';

export const settingsApi = {
  getSettings: async (): Promise<TenantSettings> => {
    const response = await axios.get(`${API_BASE_URL}/tenant/settings`);
    return response.data;
  },

  updateSettings: async (settings: Partial<TenantSettings>): Promise<TenantSettings> => {
    const response = await axios.patch(`${API_BASE_URL}/tenant/settings`, settings);
    return response.data;
  },
};
```

### 3. TanStack Query Hooks

```typescript
// src/features/tenant-admin/hooks/useUsers.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi } from '../api/users.api';
import { UserRole, UserStatus, UserFilters } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useUsers = (filters?: UserFilters) => {
  return useQuery({
    queryKey: ['users', filters],
    queryFn: () => usersApi.getUsers(filters),
    staleTime: 30000, // 30 seconds
  });
};

export const useUser = (userId: string) => {
  return useQuery({
    queryKey: ['users', userId],
    queryFn: () => usersApi.getUser(userId),
    enabled: !!userId,
  });
};

export const useUpdateUserRole = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ userId, role }: { userId: string; role: UserRole }) =>
      usersApi.updateUserRole(userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      showToast('User role updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update user role', 'error');
    },
  });
};

export const useUpdateUserStatus = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ userId, status }: { userId: string; status: UserStatus }) =>
      usersApi.updateUserStatus(userId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      showToast('User status updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update user status', 'error');
    },
  });
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (userId: string) => usersApi.deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['seatUsage'] });
      showToast('User deleted successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to delete user', 'error');
    },
  });
};

// src/features/tenant-admin/hooks/useInvitations.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { invitationsApi } from '../api/invitations.api';
import { UserRole, InvitationFilters } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useInvitations = (filters?: InvitationFilters) => {
  return useQuery({
    queryKey: ['invitations', filters],
    queryFn: () => invitationsApi.getInvitations(filters),
    staleTime: 30000,
  });
};

export const useCreateInvitation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ email, role }: { email: string; role: UserRole }) =>
      invitationsApi.createInvitation(email, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations'] });
      queryClient.invalidateQueries({ queryKey: ['seatUsage'] });
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
    mutationFn: (invitationId: string) => invitationsApi.resendInvitation(invitationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations'] });
      showToast('Invitation resent successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to resend invitation', 'error');
    },
  });
};

export const useRevokeInvitation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (invitationId: string) => invitationsApi.revokeInvitation(invitationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitations'] });
      queryClient.invalidateQueries({ queryKey: ['seatUsage'] });
      showToast('Invitation revoked successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to revoke invitation', 'error');
    },
  });
};

// src/features/tenant-admin/hooks/useSeatUsage.ts

import { useQuery } from '@tanstack/react-query';
import { seatsApi } from '../api/seats.api';

export const useSeatUsage = () => {
  return useQuery({
    queryKey: ['seatUsage'],
    queryFn: () => seatsApi.getSeatUsage(),
    staleTime: 10000, // 10 seconds
    refetchInterval: 60000, // Refetch every minute
  });
};

// src/features/tenant-admin/hooks/useTenantSettings.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { settingsApi } from '../api/settings.api';
import { TenantSettings } from '../types/tenant-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useTenantSettings = () => {
  return useQuery({
    queryKey: ['tenantSettings'],
    queryFn: () => settingsApi.getSettings(),
    staleTime: 300000, // 5 minutes
  });
};

export const useUpdateTenantSettings = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (settings: Partial<TenantSettings>) => settingsApi.updateSettings(settings),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenantSettings'] });
      showToast('Settings updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update settings', 'error');
    },
  });
};
```

### 4. Seat Usage Components

```typescript
// src/features/tenant-admin/components/SeatUsage/SeatUsageCard.tsx

import React from 'react';
import { useSeatUsage } from '../../hooks/useSeatUsage';
import { SeatUsageChart } from './SeatUsageChart';
import { SeatWarningBanner } from './SeatWarningBanner';

export const SeatUsageCard: React.FC = () => {
  const { data: seatUsage, isLoading, error } = useSeatUsage();

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6 animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="h-24 bg-gray-200 rounded"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6">
        <p className="text-red-800">Failed to load seat usage data</p>
      </div>
    );
  }

  if (!seatUsage) return null;

  const isWarningThreshold = seatUsage.percentage >= 80;
  const isCriticalThreshold = seatUsage.percentage >= 95;

  return (
    <div className="bg-white rounded-lg shadow">
      {isWarningThreshold && <SeatWarningBanner seatUsage={seatUsage} />}
      
      <div className="p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">Seat Usage</h2>
          <span className="text-sm text-gray-500">
            {seatUsage.used} / {seatUsage.total} seats
          </span>
        </div>

        <SeatUsageChart seatUsage={seatUsage} />

        <div className="mt-6 grid grid-cols-2 gap-4">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-600 mb-1">Active Users</p>
            <p className="text-2xl font-bold text-gray-900">{seatUsage.activeUsers}</p>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-600 mb-1">Pending Invitations</p>
            <p className="text-2xl font-bold text-gray-900">{seatUsage.pendingInvitations}</p>
          </div>
        </div>

        {isCriticalThreshold && (
          <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800 font-medium">
              Critical: You're approaching your seat limit. Contact support to upgrade.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

// src/features/tenant-admin/components/SeatUsage/SeatUsageChart.tsx

import React from 'react';
import { SeatUsage } from '../../types/tenant-admin.types';

interface SeatUsageChartProps {
  seatUsage: SeatUsage;
}

export const SeatUsageChart: React.FC<SeatUsageChartProps> = ({ seatUsage }) => {
  const getColorClass = () => {
    if (seatUsage.percentage >= 95) return 'bg-red-600';
    if (seatUsage.percentage >= 80) return 'bg-yellow-500';
    return 'bg-green-600';
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-medium text-gray-700">Usage</span>
        <span className="text-sm font-bold text-gray-900">{seatUsage.percentage.toFixed(1)}%</span>
      </div>
      
      <div className="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-500 ${getColorClass()}`}
          style={{ width: `${seatUsage.percentage}%` }}
        >
          <div className="h-full w-full bg-gradient-to-r from-transparent to-white opacity-20"></div>
        </div>
      </div>
    </div>
  );
};

// src/features/tenant-admin/components/SeatUsage/SeatWarningBanner.tsx

import React from 'react';
import { SeatUsage } from '../../types/tenant-admin.types';
import { ExclamationTriangleIcon } from '@heroicons/react/24/outline';

interface SeatWarningBannerProps {
  seatUsage: SeatUsage;
}

export const SeatWarningBanner: React.FC<SeatWarningBannerProps> = ({ seatUsage }) => {
  const isCritical = seatUsage.percentage >= 95;
  const remaining = seatUsage.total - seatUsage.used;

  return (
    <div
      className={`px-6 py-4 border-b ${
        isCritical
          ? 'bg-red-50 border-red-200'
          : 'bg-yellow-50 border-yellow-200'
      }`}
    >
      <div className="flex items-start gap-3">
        <ExclamationTriangleIcon
          className={`w-5 h-5 mt-0.5 flex-shrink-0 ${
            isCritical ? 'text-red-600' : 'text-yellow-600'
          }`}
        />
        <div>
          <h3
            className={`text-sm font-semibold ${
              isCritical ? 'text-red-900' : 'text-yellow-900'
            }`}
          >
            {isCritical ? 'Seat Limit Critical' : 'Seat Limit Warning'}
          </h3>
          <p
            className={`text-sm mt-1 ${
              isCritical ? 'text-red-800' : 'text-yellow-800'
            }`}
          >
            You have {remaining} seat{remaining !== 1 ? 's' : ''} remaining ({seatUsage.percentage.toFixed(1)}% used).
            {isCritical
              ? ' You cannot invite new users until seats are available.'
              : ' Consider upgrading your plan to add more users.'}
          </p>
        </div>
      </div>
    </div>
  );
};
```

### 5. User Management Components

```typescript
// src/features/tenant-admin/components/UserManagement/UserList.tsx

import React, { useState } from 'react';
import { useUsers } from '../../hooks/useUsers';
import { UserFilters } from '../../types/tenant-admin.types';
import { UserListItem } from './UserListItem';
import { UserFiltersComponent } from './UserFilters';
import { RoleAssignmentModal } from './RoleAssignmentModal';
import { DeleteUserModal } from './DeleteUserModal';

export const UserList: React.FC = () => {
  const [filters, setFilters] = useState<UserFilters>({});
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  const { data: users, isLoading, error } = useUsers(filters);

  const handleRoleChange = (userId: string) => {
    setSelectedUserId(userId);
    setRoleModalOpen(true);
  };

  const handleDelete = (userId: string) => {
    setSelectedUserId(userId);
    setDeleteModalOpen(true);
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="animate-pulse flex items-center gap-4">
              <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                <div className="h-3 bg-gray-200 rounded w-1/3"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6">
        <p className="text-red-800">Failed to load users</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Users</h2>
        <UserFiltersComponent filters={filters} onFiltersChange={setFilters} />
      </div>

      <div className="divide-y divide-gray-200">
        {users?.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-gray-500">No users found</p>
          </div>
        ) : (
          users?.map((user) => (
            <UserListItem
              key={user.id}
              user={user}
              onRoleChange={() => handleRoleChange(user.id)}
              onDelete={() => handleDelete(user.id)}
            />
          ))
        )}
      </div>

      {selectedUserId && (
        <>
          <RoleAssignmentModal
            isOpen={roleModalOpen}
            onClose={() => {
              setRoleModalOpen(false);
              setSelectedUserId(null);
            }}
            userId={selectedUserId}
          />
          <DeleteUserModal
            isOpen={deleteModalOpen}
            onClose={() => {
              setDeleteModalOpen(false);
              setSelectedUserId(null);
            }}
            userId={selectedUserId}
          />
        </>
      )}
    </div>
  );
};

// src/features/tenant-admin/components/UserManagement/UserListItem.tsx

import React from 'react';
import { User, UserStatus } from '../../types/tenant-admin.types';
import { useUpdateUserStatus } from '../../hooks/useUsers';
import { 
  UserCircleIcon, 
  EllipsisVerticalIcon,
  ShieldCheckIcon,
  UserIcon,
  EyeIcon
} from '@heroicons/react/24/outline';
import { Menu } from '@headlessui/react';

interface UserListItemProps {
  user: User;
  onRoleChange: () => void;
  onDelete: () => void;
}

export const UserListItem: React.FC<UserListItemProps> = ({ user, onRoleChange, onDelete }) => {
  const updateStatus = useUpdateUserStatus();

  const getRoleIcon = () => {
    switch (user.role) {
      case 'TENANT_ADMIN':
        return <ShieldCheckIcon className="w-5 h-5 text-purple-600" />;
      case 'TENANT_USER':
        return <UserIcon className="w-5 h-5 text-blue-600" />;
      case 'TENANT_VIEWER':
        return <EyeIcon className="w-5 h-5 text-gray-600" />;
    }
  };

  const getRoleBadgeClass = () => {
    switch (user.role) {
      case 'TENANT_ADMIN':
        return 'bg-purple-100 text-purple-800';
      case 'TENANT_USER':
        return 'bg-blue-100 text-blue-800';
      case 'TENANT_VIEWER':
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusBadgeClass = () => {
    switch (user.status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'INACTIVE':
        return 'bg-gray-100 text-gray-800';
      case 'SUSPENDED':
        return 'bg-red-100 text-red-800';
    }
  };

  const handleToggleStatus = () => {
    const newStatus = user.status === UserStatus.ACTIVE ? UserStatus.SUSPENDED : UserStatus.ACTIVE;
    updateStatus.mutate({ userId: user.id, status: newStatus });
  };

  return (
    <div className="p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-center gap-4">
        <div className="flex-shrink-0">
          <UserCircleIcon className="w-10 h-10 text-gray-400" />
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <p className="text-sm font-medium text-gray-900 truncate">
              {user.firstName} {user.lastName}
            </p>
            <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${getRoleBadgeClass()}`}>
              {getRoleIcon()}
              {user.role.replace('TENANT_', '')}
            </span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass()}`}>
              {user.status}
            </span>
          </div>
          <p className="text-sm text-gray-500 truncate">{user.email}</p>
          <p className="text-xs text-gray-400 mt-1">
            {user.lastLoginAt 
              ? `Last login: ${new Date(user.lastLoginAt).toLocaleDateString()}`
              : 'Never logged in'
            }
          </p>
        </div>

        <div className="flex-shrink-0">
          <Menu as="div" className="relative">
            <Menu.Button className="p-2 hover:bg-gray-100 rounded-lg transition-colors">
              <EllipsisVerticalIcon className="w-5 h-5 text-gray-500" />
            </Menu.Button>
            <Menu.Items className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-10">
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={onRoleChange}
                    className={`${
                      active ? 'bg-gray-50' : ''
                    } w-full text-left px-4 py-2 text-sm text-gray-700`}
                  >
                    Change Role
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={handleToggleStatus}
                    className={`${
                      active ? 'bg-gray-50' : ''
                    } w-full text-left px-4 py-2 text-sm text-gray-700`}
                  >
                    {user.status === UserStatus.ACTIVE ? 'Suspend' : 'Activate'}
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={onDelete}
                    className={`${
                      active ? 'bg-red-50' : ''
                    } w-full text-left px-4 py-2 text-sm text-red-700`}
                  >
                    Delete User
                  </button>
                )}
              </Menu.Item>
            </Menu.Items>
          </Menu>
        </div>
      </div>
    </div>
  );
};

// src/features/tenant-admin/components/UserManagement/UserFilters.tsx

import React from 'react';
import { UserFilters, UserRole, UserStatus } from '../../types/tenant-admin.types';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';

interface UserFiltersComponentProps {
  filters: UserFilters;
  onFiltersChange: (filters: UserFilters) => void;
}

export const UserFiltersComponent: React.FC<UserFiltersComponentProps> = ({
  filters,
  onFiltersChange,
}) => {
  const handleSearchChange = (search: string) => {
    onFiltersChange({ ...filters, search: search || undefined });
  };

  const handleRoleChange = (role: string) => {
    onFiltersChange({ ...filters, role: role ? (role as UserRole) : undefined });
  };

  const handleStatusChange = (status: string) => {
    onFiltersChange({ ...filters, status: status ? (status as UserStatus) : undefined });
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div className="relative">
        <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search users..."
          value={filters.search || ''}
          onChange={(e) => handleSearchChange(e.target.value)}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      <select
        value={filters.role || ''}
        onChange={(e) => handleRoleChange(e.target.value)}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option value="">All Roles</option>
        <option value={UserRole.TENANT_ADMIN}>Admin</option>
        <option value={UserRole.TENANT_USER}>User</option>
        <option value={UserRole.TENANT_VIEWER}>Viewer</option>
      </select>

      <select
        value={filters.status || ''}
        onChange={(e) => handleStatusChange(e.target.value)}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option value="">All Statuses</option>
        <option value={UserStatus.ACTIVE}>Active</option>
        <option value={UserStatus.INACTIVE}>Inactive</option>
        <option value={UserStatus.SUSPENDED}>Suspended</option>
      </select>
    </div>
  );
};

// src/features/tenant-admin/components/UserManagement/RoleAssignmentModal.tsx

import React from 'react';
import { useForm } from 'react-hook-form';
import { useUser, useUpdateUserRole } from '../../hooks/useUsers';
import { UserRole } from '../../types/tenant-admin.types';
import { Dialog } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';

interface RoleAssignmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: string;
}

interface RoleFormData {
  role: UserRole;
}

export const RoleAssignmentModal: React.FC<RoleAssignmentModalProps> = ({
  isOpen,
  onClose,
  userId,
}) => {
  const { data: user } = useUser(userId);
  const updateRole = useUpdateUserRole();

  const { register, handleSubmit, formState: { errors } } = useForm<RoleFormData>({
    defaultValues: {
      role: user?.role,
    },
  });

  const onSubmit = async (data: RoleFormData) => {
    await updateRole.mutateAsync({ userId, role: data.role });
    onClose();
  };

  if (!user) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="bg-white rounded-lg shadow-xl max-w-md w-full">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <Dialog.Title className="text-lg font-semibold text-gray-900">
              Change User Role
            </Dialog.Title>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <XMarkIcon className="w-5 h-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="p-6 space-y-4">
              <div>
                <p className="text-sm text-gray-600 mb-2">User</p>
                <p className="font-medium text-gray-900">
                  {user.firstName} {user.lastName}
                </p>
                <p className="text-sm text-gray-500">{user.email}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Role
                </label>
                <select
                  {...register('role', { required: 'Role is required' })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value={UserRole.TENANT_ADMIN}>Tenant Admin</option>
                  <option value={UserRole.TENANT_USER}>Tenant User</option>
                  <option value={UserRole.TENANT_VIEWER}>Tenant Viewer</option>
                </select>
                {errors.role && (
                  <p className="mt-1 text-sm text-red-600">{errors.role.message}</p>
                )}
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-sm text-blue-800">
                  <strong>Tenant Admin:</strong> Full access to manage users, settings, and data
                  <br />
                  <strong>Tenant User:</strong> Standard access to use the application
                  <br />
                  <strong>Tenant Viewer:</strong> Read-only access
                </p>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={updateRole.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {updateRole.isPending ? 'Updating...' : 'Update Role'}
              </button>
            </div>
          </form>
        </Dialog.Panel>
      </div>
    </Dialog>
  );
};

// src/features/tenant-admin/components/UserManagement/DeleteUserModal.tsx

import React from 'react';
import { useUser, useDeleteUser } from '../../hooks/useUsers';
import { Dialog } from '@headlessui/react';
import { ExclamationTriangleIcon, XMarkIcon } from '@heroicons/react/24/outline';

interface DeleteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: string;
}

export const DeleteUserModal: React.FC<DeleteUserModalProps> = ({
  isOpen,
  onClose,
  userId,
}) => {
  const { data: user } = useUser(userId);
  const deleteUser = useDeleteUser();

  const handleDelete = async () => {
    await deleteUser.mutateAsync(userId);
    onClose();
  };

  if (!user) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="bg-white rounded-lg shadow-xl max-w-md w-full">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <Dialog.Title className="text-lg font-semibold text-gray-900">
              Delete User
            </Dialog.Title>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <XMarkIcon className="w-5 h-5" />
            </button>
          </div>

          <div className="p-6">
            <div className="flex items-start gap-4 mb-4">
              <div className="flex-shrink-0">
                <ExclamationTriangleIcon className="w-12 h-12 text-red-600" />
              </div>
              <div>
                <p className="text-sm text-gray-700 mb-2">
                  Are you sure you want to delete this user? This action cannot be undone.
                </p>
                <div className="bg-gray-50 rounded-lg p-3">
                  <p className="font-medium text-gray-900">
                    {user.firstName} {user.lastName}
                  </p>
                  <p className="text-sm text-gray-500">{user.email}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleDelete}
              disabled={deleteUser.isPending}
              className="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {deleteUser.isPending ? 'Deleting...' : 'Delete User'}
            </button>
          </div>
        </Dialog.Panel>
      </div>
    </Dialog>
  );
};
```

### 6. Invitation Components

```typescript
// src/features/tenant-admin/components/Invitations/InvitationForm.tsx

import React from 'react';
import { useForm } from 'react-hook-form';
import { useCreateInvitation } from '../../hooks/useInvitations';
import { useSeatUsage } from '../../hooks/useSeatUsage';
import { UserRole } from '../../types/tenant-admin.types';
import { EnvelopeIcon } from '@heroicons/react/24/outline';

interface InvitationFormData {
  email: string;
  role: UserRole;
}

export const InvitationForm: React.FC = () => {
  const createInvitation = useCreateInvitation();
  const { data: seatUsage } = useSeatUsage();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InvitationFormData>({
    defaultValues: {
      role: UserRole.TENANT_USER,
    },
  });

  const hasAvailableSeats = seatUsage && seatUsage.used < seatUsage.total;

  const onSubmit = async (data: InvitationFormData) => {
    await createInvitation.mutateAsync(data);
    reset();
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center gap-3 mb-6">
        <EnvelopeIcon className="w-6 h-6 text-blue-600" />
        <h2 className="text-xl font-semibold text-gray-900">Invite User</h2>
      </div>

      {!hasAvailableSeats && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm text-red-800 font-medium">
            No available seats. Cannot send invitations until seats are freed.
          </p>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            Email Address
          </label>
          <input
            type="email"
            id="email"
            {...register('email', {
              required: 'Email is required',
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: 'Invalid email address',
              },
            })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="user@example.com"
            disabled={!hasAvailableSeats}
          />
          {errors.email && (
            <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-1">
            Role
          </label>
          <select
            id="role"
            {...register('role', { required: 'Role is required' })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            disabled={!hasAvailableSeats}
          >
            <option value={UserRole.TENANT_USER}>Tenant User</option>
            <option value={UserRole.TENANT_VIEWER}>Tenant Viewer</option>
            <option value={UserRole.TENANT_ADMIN}>Tenant Admin</option>
          </select>
          {errors.role && (
            <p className="mt-1 text-sm text-red-600">{errors.role.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={createInvitation.isPending || !hasAvailableSeats}
          className="w-full px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {createInvitation.isPending ? 'Sending...' : 'Send Invitation'}
        </button>
      </form>
    </div>
  );
};

// src/features/tenant-admin/components/Invitations/InvitationList.tsx

import React, { useState } from 'react';
import { useInvitations } from '../../hooks/useInvitations';
import { InvitationFilters } from '../../types/tenant-admin.types';
import { InvitationListItem } from './InvitationListItem';

export const InvitationList: React.FC = () => {
  const [filters, setFilters] = useState<InvitationFilters>({});
  const { data: invitations, isLoading, error } = useInvitations(filters);

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="animate-pulse flex items-center gap-4">
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                <div className="h-3 bg-gray-200 rounded w-1/4"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6">
        <p className="text-red-800">Failed to load invitations</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-xl font-semibold text-gray-900">Pending Invitations</h2>
      </div>

      <div className="divide-y divide-gray-200">
        {invitations?.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-gray-500">No pending invitations</p>
          </div>
        ) : (
          invitations?.map((invitation) => (
            <InvitationListItem key={invitation.id} invitation={invitation} />
          ))
        )}
      </div>
    </div>
  );
};

// src/features/tenant-admin/components/Invitations/InvitationListItem.tsx

import React from 'react';
import { Invitation, InvitationStatus } from '../../types/tenant-admin.types';
import { useResendInvitation, useRevokeInvitation } from '../../hooks/useInvitations';
import { EnvelopeIcon, ClockIcon, CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline';

interface InvitationListItemProps {
  invitation: Invitation;
}

export const InvitationListItem: React.FC<InvitationListItemProps> = ({ invitation }) => {
  const resendInvitation = useResendInvitation();
  const revokeInvitation = useRevokeInvitation();

  const getStatusIcon = () => {
    switch (invitation.status) {
      case InvitationStatus.PENDING:
        return <ClockIcon className="w-5 h-5 text-yellow-600" />;
      case InvitationStatus.ACCEPTED:
        return <CheckCircleIcon className="w-5 h-5 text-green-600" />;
      case InvitationStatus.EXPIRED:
        return <XCircleIcon className="w-5 h-5 text-red-600" />;
      case InvitationStatus.REVOKED:
        return <XCircleIcon className="w-5 h-5 text-gray-600" />;
    }
  };

  const getStatusBadgeClass = () => {
    switch (invitation.status) {
      case InvitationStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case InvitationStatus.ACCEPTED:
        return 'bg-green-100 text-green-800';
      case InvitationStatus.EXPIRED:
        return 'bg-red-100 text-red-800';
      case InvitationStatus.REVOKED:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const isExpiringSoon = () => {
    if (invitation.status !== InvitationStatus.PENDING) return false;
    const expiresAt = new Date(invitation.expiresAt);
    const now = new Date();
    const hoursUntilExpiry = (expiresAt.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursUntilExpiry < 24;
  };

  const handleResend = () => {
    resendInvitation.mutate(invitation.id);
  };

  const handleRevoke = () => {
    revokeInvitation.mutate(invitation.id);
  };

  return (
    <div className="p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-center gap-4">
        <div className="flex-shrink-0">
          <EnvelopeIcon className="w-10 h-10 text-gray-400" />
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <p className="text-sm font-medium text-gray-900 truncate">{invitation.email}</p>
            <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass()}`}>
              {getStatusIcon()}
              {invitation.status}
            </span>
            <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              {invitation.role.replace('TENANT_', '')}
            </span>
          </div>
          <p className="text-xs text-gray-500">
            Invited by {invitation.invitedBy} on {new Date(invitation.invitedAt).toLocaleDateString()}
          </p>
          <p className="text-xs text-gray-400 mt-0.5">
            {invitation.status === InvitationStatus.PENDING && (
              <>
                Expires: {new Date(invitation.expiresAt).toLocaleDateString()}
                {isExpiringSoon() && (
                  <span className="ml-2 text-yellow-600 font-medium">Expiring soon!</span>
                )}
              </>
            )}
            {invitation.status === InvitationStatus.ACCEPTED && invitation.acceptedAt && (
              <>Accepted on {new Date(invitation.acceptedAt).toLocaleDateString()}</>
            )}
          </p>
        </div>

        {invitation.status === InvitationStatus.PENDING && (
          <div className="flex items-center gap-2">
            <button
              onClick={handleResend}
              disabled={resendInvitation.isPending}
              className="px-3 py-1.5 text-sm font-medium text-blue-700 hover:bg-blue-50 rounded-lg transition-colors disabled:opacity-50"
            >
              Resend
            </button>
            <button
              onClick={handleRevoke}
              disabled={revokeInvitation.isPending}
              className="px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
            >
              Revoke
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
```

### 7. Tenant Settings Components

```typescript
// src/features/tenant-admin/components/Settings/TenantSettingsForm.tsx

import React from 'react';
import { useForm } from 'react-hook-form';
import { useTenantSettings, useUpdateTenantSettings } from '../../hooks/useTenantSettings';
import { TenantSettings } from '../../types/tenant-admin.types';

export const TenantSettingsForm: React.FC = () => {
  const { data: settings, isLoading } = useTenantSettings();
  const updateSettings = useUpdateTenantSettings();

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<TenantSettings>({
    values: settings,
  });

  const onSubmit = async (data: TenantSettings) => {
    await updateSettings.mutateAsync(data);
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6 animate-pulse">
        <div className="space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/3"></div>
          <div className="h-20 bg-gray-200 rounded"></div>
          <div className="h-20 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-xl font-semibold text-gray-900">Tenant Settings</h2>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-6">
        {/* General Settings */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">General</h3>
          <div className="space-y-4">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                Organization Name
              </label>
              <input
                type="text"
                id="name"
                {...register('name', { required: 'Organization name is required' })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="subdomain" className="block text-sm font-medium text-gray-700 mb-1">
                Subdomain
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  id="subdomain"
                  {...register('subdomain', {
                    required: 'Subdomain is required',
                    pattern: {
                      value: /^[a-z0-9-]+$/,
                      message: 'Only lowercase letters, numbers, and hyphens allowed',
                    },
                  })}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <span className="text-sm text-gray-500">.npsplaybox.com</span>
              </div>
              {errors.subdomain && (
                <p className="mt-1 text-sm text-red-600">{errors.subdomain.message}</p>
              )}
            </div>
          </div>
        </div>

        {/* Security Settings */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Security</h3>
          <div className="space-y-4">
            <div className="flex items-center">
              <input
                type="checkbox"
                id="allowSelfRegistration"
                {...register('allowSelfRegistration')}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <label htmlFor="allowSelfRegistration" className="ml-2 text-sm text-gray-700">
                Allow self-registration
              </label>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="requireEmailVerification"
                {...register('requireEmailVerification')}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <label htmlFor="requireEmailVerification" className="ml-2 text-sm text-gray-700">
                Require email verification
              </label>
            </div>

            <div>
              <label htmlFor="sessionTimeoutMinutes" className="block text-sm font-medium text-gray-700 mb-1">
                Session Timeout (minutes)
              </label>
              <input
                type="number"
                id="sessionTimeoutMinutes"
                {...register('sessionTimeoutMinutes', {
                  required: 'Session timeout is required',
                  min: { value: 5, message: 'Minimum 5 minutes' },
                  max: { value: 1440, message: 'Maximum 1440 minutes (24 hours)' },
                })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              {errors.sessionTimeoutMinutes && (
                <p className="mt-1 text-sm text-red-600">{errors.sessionTimeoutMinutes.message}</p>
              )}
            </div>
          </div>
        </div>

        {/* Branding Settings */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Branding</h3>
          <div className="space-y-4">
            <div>
              <label htmlFor="logoUrl" className="block text-sm font-medium text-gray-700 mb-1">
                Logo URL
              </label>
              <input
                type="url"
                id="logoUrl"
                {...register('branding.logoUrl')}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="https://example.com/logo.png"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="primaryColor" className="block text-sm font-medium text-gray-700 mb-1">
                  Primary Color
                </label>
                <input
                  type="color"
                  id="primaryColor"
                  {...register('branding.primaryColor')}
                  className="w-full h-10 px-1 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label htmlFor="secondaryColor" className="block text-sm font-medium text-gray-700 mb-1">
                  Secondary Color
                </label>
                <input
                  type="color"
                  id="secondaryColor"
                  {...register('branding.secondaryColor')}
                  className="w-full h-10 px-1 border border-gray-300 rounded-lg"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-6 border-t border-gray-200">
          <button
            type="submit"
            disabled={updateSettings.isPending || !isDirty}
            className="px-6 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {updateSettings.isPending ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
};
```

### 8. Main Page Component

```typescript
// src/features/tenant-admin/pages/TenantAdminDashboard.tsx

import React from 'react';
import { SeatUsageCard } from '../components/SeatUsage/SeatUsageCard';
import { InvitationForm } from '../components/Invitations/InvitationForm';
import { InvitationList } from '../components/Invitations/InvitationList';
import { UserList } from '../components/UserManagement/UserList';
import { TenantSettingsForm } from '../components/Settings/TenantSettingsForm';
import { Tab } from '@headlessui/react';
import { 
  UsersIcon, 
  EnvelopeIcon, 
  Cog6ToothIcon,
  ChartBarIcon 
} from '@heroicons/react/24/outline';

export const TenantAdminDashboard: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Tenant Administration</h1>
          <p className="mt-2 text-sm text-gray-600">
            Manage your organization's users, invitations, and settings
          </p>
        </div>

        <div className="mb-8">
          <SeatUsageCard />
        </div>

        <Tab.Group>
          <Tab.List className="flex space-x-1 rounded-xl bg-white p-1 shadow mb-6">
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected
                  ? 'bg-blue-600 text-white shadow'
                  : 'text-gray-700 hover:bg-gray-50'
                }`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <UsersIcon className="w-5 h-5" />
                Users
              </div>
            </Tab>
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected
                  ? 'bg-blue-600 text-white shadow'
                  : 'text-gray-700 hover:bg-gray-50'
                }`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <EnvelopeIcon className="w-5 h-5" />
                Invitations
              </div>
            </Tab>
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected
                  ? 'bg-blue-600 text-white shadow'
                  : 'text-gray-700 hover:bg-gray-50'
                }`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <Cog6ToothIcon className="w-5 h-5" />
                Settings
              </div>
            </Tab>
          </Tab.List>

          <Tab.Panels>
            <Tab.Panel>
              <UserList />
            </Tab.Panel>

            <Tab.Panel>
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-1">
                  <InvitationForm />
                </div>
                <div className="lg:col-span-2">
                  <InvitationList />
                </div>
              </div>
            </Tab.Panel>

            <Tab.Panel>
              <TenantSettingsForm />
            </Tab.Panel>
          </Tab.Panels>
        </Tab.Group>
      </div>
    </div>
  );
};
```

## Testing Strategy

### Unit Tests
- Component rendering and user interactions
- Form validation logic
- API client functions
- Custom hooks with TanStack Query

### Integration Tests
- Complete user management workflows
- Invitation flow from creation to acceptance
- Seat usage updates after user actions
- Settings persistence

### E2E Tests
- Full user lifecycle: invite → accept → assign role → delete
- Seat quota enforcement
- Multi-tab data synchronization
- Error handling and recovery

## Deployment Checklist

- [ ] Configure API base URL for production
- [ ] Set up error tracking (Sentry, etc.)
- [ ] Configure axios interceptors for auth tokens
- [ ] Test seat limit enforcement
- [ ] Verify email invitation delivery
- [ ] Test role permission boundaries
- [ ] Performance testing with large user lists
- [ ] Accessibility audit (WCAG 2.1 AA)
- [ ] Cross-browser testing
- [ ] Mobile responsiveness verification

## Performance Optimizations

1. **Query Caching**: TanStack Query handles caching automatically
2. **Optimistic Updates**: Update UI before API response for role changes
3. **Debounced Search**: 300ms delay on user search input
4. **Virtual Scrolling**: Implement for user lists > 100 items
5. **Image Optimization**: Lazy load user avatars
6. **Code Splitting**: Lazy load modals and settings components

## Dependencies

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.21.0",
    "@tanstack/react-query": "^5.17.0",
    "react-hook-form": "^7.49.0",
    "axios": "^1.6.5",
    "@headlessui/react": "^1.7.18",
    "@heroicons/react": "^2.1.1"
  },
  "devDependencies": {
    "@types/react": "^18.2.48",
    "@types/react-dom": "^18.2.18",
    "typescript": "^5.3.3",
    "tailwindcss": "^3.4.1",
    "autoprefixer": "^10.4.17",
    "postcss": "^8.4.33",
    "vite": "^5.0.11",
    "@vitejs/plugin-react": "^4.2.1"
  }
}
```

## Success Criteria

- ✅ Tenant admins can view and manage all users
- ✅ Seat usage displays accurately with color-coded warnings
- ✅ Warning shown at 80% capacity, blocking at 100%
- ✅ Invitation flow complete with resend and revoke
- ✅ Role assignment updates in real-time
- ✅ User filtering and search works smoothly
- ✅ Settings persist correctly
- ✅ All forms validate properly
- ✅ API errors display user-friendly messages
- ✅ Mobile responsive design
- ✅ Accessible to keyboard navigation and screen readers

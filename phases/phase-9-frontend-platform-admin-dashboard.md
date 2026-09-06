# Phase 9: Frontend - Platform Admin Dashboard

**Duration:** 5-6 days  
**Dependencies:** Phase 3 (User Management), Phase 5 (Seat Management), Phase 6 (Platform Admin Features), Phase 7 (Audit Logging)  
**Tech Stack:** React, TypeScript, React Router, TanStack Query, React Hook Form, Tailwind CSS, Recharts

## Overview

Build the platform administrator dashboard for managing all tenants, monitoring system-wide metrics, handling impersonation requests, and reviewing audit logs. This phase provides platform admins with complete visibility and control over the multi-tenant system.

## Objectives

- Create comprehensive tenant overview dashboard with metrics
- Implement seat quota management across all tenants
- Build audit log viewer with advanced filtering
- Provide impersonation workflow (request, approve, start, end)
- Enable tenant status management (active, suspended, trial)
- Visualize platform-wide metrics and trends
- Integrate with backend APIs from phases 3, 5, 6, and 7

## Architecture

### Component Structure

```
src/
├── features/
│   ├── platform-admin/
│   │   ├── components/
│   │   │   ├── Dashboard/
│   │   │   │   ├── TenantOverviewCard.tsx
│   │   │   │   ├── PlatformMetrics.tsx
│   │   │   │   ├── TenantGrowthChart.tsx
│   │   │   │   ├── SeatUtilizationChart.tsx
│   │   │   │   └── RecentActivityFeed.tsx
│   │   │   ├── TenantManagement/
│   │   │   │   ├── TenantList.tsx
│   │   │   │   ├── TenantListItem.tsx
│   │   │   │   ├── TenantFilters.tsx
│   │   │   │   ├── TenantDetailModal.tsx
│   │   │   │   └── SeatQuotaModal.tsx
│   │   │   ├── AuditLogs/
│   │   │   │   ├── AuditLogViewer.tsx
│   │   │   │   ├── AuditLogList.tsx
│   │   │   │   ├── AuditLogFilters.tsx
│   │   │   │   ├── AuditLogDetail.tsx
│   │   │   │   └── AuditLogExport.tsx
│   │   │   └── Impersonation/
│   │   │       ├── ImpersonationRequestList.tsx
│   │   │       ├── ImpersonationRequestItem.tsx
│   │   │       ├── StartImpersonationModal.tsx
│   │   │       ├── ImpersonationBanner.tsx
│   │   │       └── ImpersonationHistory.tsx
│   │   ├── hooks/
│   │   │   ├── useTenants.ts
│   │   │   ├── usePlatformMetrics.ts
│   │   │   ├── useAuditLogs.ts
│   │   │   └── useImpersonation.ts
│   │   ├── api/
│   │   │   ├── tenants.api.ts
│   │   │   ├── metrics.api.ts
│   │   │   ├── audit.api.ts
│   │   │   └── impersonation.api.ts
│   │   └── types/
│   │       └── platform-admin.types.ts
│   └── shared/
│       └── components/
│           ├── Chart.tsx
│           └── DataTable.tsx
```

## Implementation

### 1. Type Definitions

```typescript
// src/features/platform-admin/types/platform-admin.types.ts

export enum TenantStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  TRIAL = 'TRIAL',
  CHURNED = 'CHURNED',
}

export enum ImpersonationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  DENIED = 'DENIED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
}

export enum AuditEventType {
  USER_LOGIN = 'USER_LOGIN',
  USER_LOGOUT = 'USER_LOGOUT',
  USER_CREATED = 'USER_CREATED',
  USER_DELETED = 'USER_DELETED',
  ROLE_CHANGED = 'ROLE_CHANGED',
  INVITATION_SENT = 'INVITATION_SENT',
  SEAT_QUOTA_CHANGED = 'SEAT_QUOTA_CHANGED',
  TENANT_STATUS_CHANGED = 'TENANT_STATUS_CHANGED',
  IMPERSONATION_STARTED = 'IMPERSONATION_STARTED',
  IMPERSONATION_ENDED = 'IMPERSONATION_ENDED',
  SETTINGS_UPDATED = 'SETTINGS_UPDATED',
}

export interface Tenant {
  id: string;
  name: string;
  subdomain: string;
  status: TenantStatus;
  seatQuota: number;
  seatsUsed: number;
  userCount: number;
  adminEmail: string;
  createdAt: string;
  lastActivityAt?: string;
  trialEndsAt?: string;
}

export interface PlatformMetrics {
  totalTenants: number;
  activeTenants: number;
  trialTenants: number;
  suspendedTenants: number;
  totalUsers: number;
  totalSeatsAllocated: number;
  totalSeatsUsed: number;
  averageSeatUtilization: number;
  tenantGrowthRate: number;
  activeImpersonations: number;
}

export interface TenantGrowthData {
  date: string;
  newTenants: number;
  churnedTenants: number;
  totalTenants: number;
}

export interface SeatUtilizationData {
  tenantId: string;
  tenantName: string;
  quota: number;
  used: number;
  percentage: number;
}

export interface AuditLog {
  id: string;
  eventType: AuditEventType;
  tenantId: string;
  tenantName: string;
  userId: string;
  userEmail: string;
  ipAddress: string;
  userAgent: string;
  timestamp: string;
  details: Record<string, any>;
  metadata?: {
    impersonatorId?: string;
    impersonatorEmail?: string;
  };
}

export interface ImpersonationRequest {
  id: string;
  requesterId: string;
  requesterEmail: string;
  tenantId: string;
  tenantName: string;
  targetUserId?: string;
  targetUserEmail?: string;
  reason: string;
  status: ImpersonationStatus;
  requestedAt: string;
  reviewedAt?: string;
  reviewedBy?: string;
  startedAt?: string;
  endedAt?: string;
  duration?: number;
}

export interface TenantFilters {
  search?: string;
  status?: TenantStatus;
  seatUtilizationMin?: number;
  seatUtilizationMax?: number;
  sortBy?: 'name' | 'createdAt' | 'lastActivityAt' | 'userCount';
  sortOrder?: 'asc' | 'desc';
}

export interface AuditLogFilters {
  tenantId?: string;
  userId?: string;
  eventType?: AuditEventType;
  startDate?: string;
  endDate?: string;
  search?: string;
  page?: number;
  pageSize?: number;
}
```

### 2. API Client

```typescript
// src/features/platform-admin/api/tenants.api.ts

import axios from 'axios';
import { Tenant, TenantStatus, TenantFilters } from '../types/platform-admin.types';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api/v1';

export const tenantsApi = {
  getTenants: async (filters?: TenantFilters): Promise<Tenant[]> => {
    const params = new URLSearchParams();
    if (filters?.search) params.append('search', filters.search);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.seatUtilizationMin !== undefined) 
      params.append('seatUtilizationMin', filters.seatUtilizationMin.toString());
    if (filters?.seatUtilizationMax !== undefined) 
      params.append('seatUtilizationMax', filters.seatUtilizationMax.toString());
    if (filters?.sortBy) params.append('sortBy', filters.sortBy);
    if (filters?.sortOrder) params.append('sortOrder', filters.sortOrder);

    const response = await axios.get(`${API_BASE_URL}/platform/tenants?${params.toString()}`);
    return response.data;
  },

  getTenant: async (tenantId: string): Promise<Tenant> => {
    const response = await axios.get(`${API_BASE_URL}/platform/tenants/${tenantId}`);
    return response.data;
  },

  updateTenantStatus: async (tenantId: string, status: TenantStatus): Promise<Tenant> => {
    const response = await axios.patch(
      `${API_BASE_URL}/platform/tenants/${tenantId}/status`,
      { status }
    );
    return response.data;
  },

  updateSeatQuota: async (tenantId: string, quota: number): Promise<Tenant> => {
    const response = await axios.patch(
      `${API_BASE_URL}/platform/tenants/${tenantId}/seat-quota`,
      { quota }
    );
    return response.data;
  },
};

// src/features/platform-admin/api/metrics.api.ts

import axios from 'axios';
import { PlatformMetrics, TenantGrowthData, SeatUtilizationData } from '../types/platform-admin.types';

export const metricsApi = {
  getPlatformMetrics: async (): Promise<PlatformMetrics> => {
    const response = await axios.get(`${API_BASE_URL}/platform/metrics`);
    return response.data;
  },

  getTenantGrowth: async (days: number = 30): Promise<TenantGrowthData[]> => {
    const response = await axios.get(`${API_BASE_URL}/platform/metrics/tenant-growth?days=${days}`);
    return response.data;
  },

  getSeatUtilization: async (): Promise<SeatUtilizationData[]> => {
    const response = await axios.get(`${API_BASE_URL}/platform/metrics/seat-utilization`);
    return response.data;
  },
};

// src/features/platform-admin/api/audit.api.ts

import axios from 'axios';
import { AuditLog, AuditLogFilters } from '../types/platform-admin.types';

export const auditApi = {
  getAuditLogs: async (filters?: AuditLogFilters): Promise<{ logs: AuditLog[]; total: number }> => {
    const params = new URLSearchParams();
    if (filters?.tenantId) params.append('tenantId', filters.tenantId);
    if (filters?.userId) params.append('userId', filters.userId);
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.startDate) params.append('startDate', filters.startDate);
    if (filters?.endDate) params.append('endDate', filters.endDate);
    if (filters?.search) params.append('search', filters.search);
    if (filters?.page) params.append('page', filters.page.toString());
    if (filters?.pageSize) params.append('pageSize', filters.pageSize.toString());

    const response = await axios.get(`${API_BASE_URL}/platform/audit-logs?${params.toString()}`);
    return response.data;
  },

  exportAuditLogs: async (filters?: AuditLogFilters): Promise<Blob> => {
    const params = new URLSearchParams();
    if (filters?.tenantId) params.append('tenantId', filters.tenantId);
    if (filters?.eventType) params.append('eventType', filters.eventType);
    if (filters?.startDate) params.append('startDate', filters.startDate);
    if (filters?.endDate) params.append('endDate', filters.endDate);

    const response = await axios.get(
      `${API_BASE_URL}/platform/audit-logs/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },
};

// src/features/platform-admin/api/impersonation.api.ts

import axios from 'axios';
import { ImpersonationRequest, ImpersonationStatus } from '../types/platform-admin.types';

export const impersonationApi = {
  getImpersonationRequests: async (): Promise<ImpersonationRequest[]> => {
    const response = await axios.get(`${API_BASE_URL}/platform/impersonation/requests`);
    return response.data;
  },

  createImpersonationRequest: async (
    tenantId: string,
    targetUserId: string | undefined,
    reason: string
  ): Promise<ImpersonationRequest> => {
    const response = await axios.post(`${API_BASE_URL}/platform/impersonation/requests`, {
      tenantId,
      targetUserId,
      reason,
    });
    return response.data;
  },

  reviewImpersonationRequest: async (
    requestId: string,
    approved: boolean,
    reviewNote?: string
  ): Promise<ImpersonationRequest> => {
    const response = await axios.post(
      `${API_BASE_URL}/platform/impersonation/requests/${requestId}/review`,
      { approved, reviewNote }
    );
    return response.data;
  },

  startImpersonation: async (requestId: string): Promise<{ token: string; expiresAt: string }> => {
    const response = await axios.post(
      `${API_BASE_URL}/platform/impersonation/requests/${requestId}/start`
    );
    return response.data;
  },

  endImpersonation: async (): Promise<void> => {
    await axios.post(`${API_BASE_URL}/platform/impersonation/end`);
  },

  getActiveImpersonation: async (): Promise<ImpersonationRequest | null> => {
    const response = await axios.get(`${API_BASE_URL}/platform/impersonation/active`);
    return response.data;
  },
};
```

### 3. TanStack Query Hooks

```typescript
// src/features/platform-admin/hooks/useTenants.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tenantsApi } from '../api/tenants.api';
import { TenantStatus, TenantFilters } from '../types/platform-admin.types';
import { useToast } from '../../shared/hooks/useToast';

export const useTenants = (filters?: TenantFilters) => {
  return useQuery({
    queryKey: ['platform-tenants', filters],
    queryFn: () => tenantsApi.getTenants(filters),
    staleTime: 30000,
  });
};

export const useTenant = (tenantId: string) => {
  return useQuery({
    queryKey: ['platform-tenants', tenantId],
    queryFn: () => tenantsApi.getTenant(tenantId),
    enabled: !!tenantId,
  });
};

export const useUpdateTenantStatus = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ tenantId, status }: { tenantId: string; status: TenantStatus }) =>
      tenantsApi.updateTenantStatus(tenantId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast('Tenant status updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update tenant status', 'error');
    },
  });
};

export const useUpdateSeatQuota = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ tenantId, quota }: { tenantId: string; quota: number }) =>
      tenantsApi.updateSeatQuota(tenantId, quota),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-tenants'] });
      queryClient.invalidateQueries({ queryKey: ['platform-metrics'] });
      showToast('Seat quota updated successfully', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to update seat quota', 'error');
    },
  });
};

// src/features/platform-admin/hooks/usePlatformMetrics.ts

import { useQuery } from '@tanstack/react-query';
import { metricsApi } from '../api/metrics.api';

export const usePlatformMetrics = () => {
  return useQuery({
    queryKey: ['platform-metrics'],
    queryFn: () => metricsApi.getPlatformMetrics(),
    staleTime: 60000, // 1 minute
    refetchInterval: 300000, // 5 minutes
  });
};

export const useTenantGrowth = (days: number = 30) => {
  return useQuery({
    queryKey: ['tenant-growth', days],
    queryFn: () => metricsApi.getTenantGrowth(days),
    staleTime: 300000,
  });
};

export const useSeatUtilization = () => {
  return useQuery({
    queryKey: ['seat-utilization'],
    queryFn: () => metricsApi.getSeatUtilization(),
    staleTime: 60000,
  });
};

// src/features/platform-admin/hooks/useAuditLogs.ts

import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../api/audit.api';
import { AuditLogFilters } from '../types/platform-admin.types';

export const useAuditLogs = (filters?: AuditLogFilters) => {
  return useQuery({
    queryKey: ['audit-logs', filters],
    queryFn: () => auditApi.getAuditLogs(filters),
    staleTime: 10000,
  });
};

// src/features/platform-admin/hooks/useImpersonation.ts

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { impersonationApi } from '../api/impersonation.api';
import { useToast } from '../../shared/hooks/useToast';

export const useImpersonationRequests = () => {
  return useQuery({
    queryKey: ['impersonation-requests'],
    queryFn: () => impersonationApi.getImpersonationRequests(),
    staleTime: 30000,
  });
};

export const useCreateImpersonationRequest = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ tenantId, targetUserId, reason }: { 
      tenantId: string; 
      targetUserId?: string; 
      reason: string 
    }) => impersonationApi.createImpersonationRequest(tenantId, targetUserId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-requests'] });
      showToast('Impersonation request created', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to create request', 'error');
    },
  });
};

export const useReviewImpersonationRequest = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ requestId, approved, reviewNote }: { 
      requestId: string; 
      approved: boolean;
      reviewNote?: string;
    }) => impersonationApi.reviewImpersonationRequest(requestId, approved, reviewNote),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-requests'] });
      showToast(
        `Request ${variables.approved ? 'approved' : 'denied'}`,
        variables.approved ? 'success' : 'info'
      );
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to review request', 'error');
    },
  });
};

export const useStartImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (requestId: string) => impersonationApi.startImpersonation(requestId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['impersonation-requests'] });
      queryClient.invalidateQueries({ queryKey: ['active-impersonation'] });
      showToast('Impersonation started', 'success');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to start impersonation', 'error');
    },
  });
};

export const useEndImpersonation = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: () => impersonationApi.endImpersonation(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['active-impersonation'] });
      showToast('Impersonation ended', 'info');
    },
    onError: (error: any) => {
      showToast(error.response?.data?.message || 'Failed to end impersonation', 'error');
    },
  });
};

export const useActiveImpersonation = () => {
  return useQuery({
    queryKey: ['active-impersonation'],
    queryFn: () => impersonationApi.getActiveImpersonation(),
    staleTime: 10000,
    refetchInterval: 30000,
  });
};
```

### 4. Dashboard Components

```typescript
// src/features/platform-admin/components/Dashboard/PlatformMetrics.tsx

import React from 'react';
import { usePlatformMetrics } from '../../hooks/usePlatformMetrics';
import {
  BuildingOfficeIcon,
  UsersIcon,
  ChartBarIcon,
  UserGroupIcon,
} from '@heroicons/react/24/outline';

export const PlatformMetrics: React.FC = () => {
  const { data: metrics, isLoading } = usePlatformMetrics();

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="bg-white rounded-lg shadow p-6 animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div className="h-10 bg-gray-200 rounded w-1/2"></div>
          </div>
        ))}
      </div>
    );
  }

  if (!metrics) return null;

  const metricCards = [
    {
      title: 'Total Tenants',
      value: metrics.totalTenants,
      subValue: `${metrics.activeTenants} active`,
      icon: BuildingOfficeIcon,
      color: 'blue',
    },
    {
      title: 'Total Users',
      value: metrics.totalUsers,
      subValue: `Across all tenants`,
      icon: UsersIcon,
      color: 'green',
    },
    {
      title: 'Seat Utilization',
      value: `${metrics.averageSeatUtilization.toFixed(1)}%`,
      subValue: `${metrics.totalSeatsUsed} / ${metrics.totalSeatsAllocated}`,
      icon: ChartBarIcon,
      color: 'purple',
    },
    {
      title: 'Trial Tenants',
      value: metrics.trialTenants,
      subValue: `${((metrics.trialTenants / metrics.totalTenants) * 100).toFixed(1)}% of total`,
      icon: UserGroupIcon,
      color: 'yellow',
    },
  ];

  const getColorClasses = (color: string) => {
    const colors: Record<string, { bg: string; text: string; icon: string }> = {
      blue: { bg: 'bg-blue-50', text: 'text-blue-900', icon: 'text-blue-600' },
      green: { bg: 'bg-green-50', text: 'text-green-900', icon: 'text-green-600' },
      purple: { bg: 'bg-purple-50', text: 'text-purple-900', icon: 'text-purple-600' },
      yellow: { bg: 'bg-yellow-50', text: 'text-yellow-900', icon: 'text-yellow-600' },
    };
    return colors[color];
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {metricCards.map((card) => {
        const colors = getColorClasses(card.color);
        const Icon = card.icon;

        return (
          <div key={card.title} className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <div className={`p-3 rounded-lg ${colors.bg}`}>
                <Icon className={`w-6 h-6 ${colors.icon}`} />
              </div>
            </div>
            <h3 className="text-sm font-medium text-gray-600 mb-1">{card.title}</h3>
            <p className={`text-3xl font-bold ${colors.text} mb-1`}>{card.value}</p>
            <p className="text-xs text-gray-500">{card.subValue}</p>
          </div>
        );
      })}
    </div>
  );
};

// src/features/platform-admin/components/Dashboard/TenantGrowthChart.tsx

import React, { useState } from 'react';
import { useTenantGrowth } from '../../hooks/usePlatformMetrics';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

export const TenantGrowthChart: React.FC = () => {
  const [days, setDays] = useState(30);
  const { data: growthData, isLoading } = useTenantGrowth(days);

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="h-64 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold text-gray-900">Tenant Growth</h2>
        <select
          value={days}
          onChange={(e) => setDays(Number(e.target.value))}
          className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value={7}>Last 7 days</option>
          <option value={30}>Last 30 days</option>
          <option value={90}>Last 90 days</option>
        </select>
      </div>

      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={growthData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="date" 
            tickFormatter={(value) => new Date(value).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
          />
          <YAxis />
          <Tooltip 
            labelFormatter={(value) => new Date(value).toLocaleDateString()}
          />
          <Legend />
          <Line
            type="monotone"
            dataKey="totalTenants"
            stroke="#3b82f6"
            strokeWidth={2}
            name="Total Tenants"
          />
          <Line
            type="monotone"
            dataKey="newTenants"
            stroke="#10b981"
            strokeWidth={2}
            name="New Tenants"
          />
          <Line
            type="monotone"
            dataKey="churnedTenants"
            stroke="#ef4444"
            strokeWidth={2}
            name="Churned"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

// src/features/platform-admin/components/Dashboard/SeatUtilizationChart.tsx

import React from 'react';
import { useSeatUtilization } from '../../hooks/usePlatformMetrics';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
} from 'recharts';

export const SeatUtilizationChart: React.FC = () => {
  const { data: utilizationData, isLoading } = useSeatUtilization();

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="h-64 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  const getBarColor = (percentage: number) => {
    if (percentage >= 90) return '#ef4444'; // red
    if (percentage >= 80) return '#f59e0b'; // yellow
    return '#10b981'; // green
  };

  // Top 10 tenants by utilization
  const topTenants = utilizationData?.slice(0, 10) || [];

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-900">Seat Utilization by Tenant</h2>
        <p className="text-sm text-gray-600 mt-1">Top 10 tenants by usage</p>
      </div>

      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={topTenants} layout="vertical">
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis type="number" domain={[0, 100]} />
          <YAxis dataKey="tenantName" type="category" width={120} />
          <Tooltip 
            formatter={(value: number) => `${value.toFixed(1)}%`}
            labelFormatter={(label) => `Tenant: ${label}`}
          />
          <Bar dataKey="percentage" name="Utilization %">
            {topTenants.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={getBarColor(entry.percentage)} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>

      <div className="flex items-center justify-center gap-6 mt-4 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-green-500"></div>
          <span className="text-gray-600">&lt; 80%</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
          <span className="text-gray-600">80-90%</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-red-500"></div>
          <span className="text-gray-600">&gt; 90%</span>
        </div>
      </div>
    </div>
  );
};

// src/features/platform-admin/components/Dashboard/RecentActivityFeed.tsx

import React from 'react';
import { useAuditLogs } from '../../hooks/useAuditLogs';
import { AuditEventType } from '../../types/platform-admin.types';
import { 
  UserPlusIcon, 
  UserMinusIcon, 
  ShieldCheckIcon,
  EnvelopeIcon,
  BuildingOfficeIcon,
  ArrowPathIcon,
} from '@heroicons/react/24/outline';

export const RecentActivityFeed: React.FC = () => {
  const { data, isLoading } = useAuditLogs({ page: 1, pageSize: 10 });

  const getEventIcon = (eventType: AuditEventType) => {
    switch (eventType) {
      case AuditEventType.USER_CREATED:
        return <UserPlusIcon className="w-5 h-5 text-green-600" />;
      case AuditEventType.USER_DELETED:
        return <UserMinusIcon className="w-5 h-5 text-red-600" />;
      case AuditEventType.ROLE_CHANGED:
        return <ShieldCheckIcon className="w-5 h-5 text-purple-600" />;
      case AuditEventType.INVITATION_SENT:
        return <EnvelopeIcon className="w-5 h-5 text-blue-600" />;
      case AuditEventType.TENANT_STATUS_CHANGED:
        return <BuildingOfficeIcon className="w-5 h-5 text-yellow-600" />;
      default:
        return <ArrowPathIcon className="w-5 h-5 text-gray-600" />;
    }
  };

  const getEventDescription = (log: any) => {
    const eventDescriptions: Record<string, string> = {
      USER_CREATED: `created user ${log.details.targetEmail}`,
      USER_DELETED: `deleted user ${log.details.targetEmail}`,
      ROLE_CHANGED: `changed role to ${log.details.newRole}`,
      INVITATION_SENT: `sent invitation to ${log.details.inviteeEmail}`,
      TENANT_STATUS_CHANGED: `changed tenant status to ${log.details.newStatus}`,
      SEAT_QUOTA_CHANGED: `updated seat quota to ${log.details.newQuota}`,
    };
    return eventDescriptions[log.eventType] || log.eventType;
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse space-y-4">
          <div className="h-6 bg-gray-200 rounded w-1/3"></div>
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex gap-3">
              <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">Recent Activity</h2>

      <div className="space-y-4">
        {data?.logs.length === 0 ? (
          <p className="text-center text-gray-500 py-8">No recent activity</p>
        ) : (
          data?.logs.map((log) => (
            <div key={log.id} className="flex gap-3 pb-4 border-b border-gray-100 last:border-0">
              <div className="flex-shrink-0 w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
                {getEventIcon(log.eventType)}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-900">
                  <span className="font-medium">{log.userEmail}</span>{' '}
                  {getEventDescription(log)}
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  {log.tenantName} • {new Date(log.timestamp).toLocaleString()}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
```

### 5. Tenant Management Components

```typescript
// src/features/platform-admin/components/TenantManagement/TenantList.tsx

import React, { useState } from 'react';
import { useTenants } from '../../hooks/useTenants';
import { TenantFilters } from '../../types/platform-admin.types';
import { TenantListItem } from './TenantListItem';
import { TenantFiltersComponent } from './TenantFilters';
import { TenantDetailModal } from './TenantDetailModal';
import { SeatQuotaModal } from './SeatQuotaModal';

export const TenantList: React.FC = () => {
  const [filters, setFilters] = useState<TenantFilters>({});
  const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [quotaModalOpen, setQuotaModalOpen] = useState(false);

  const { data: tenants, isLoading, error } = useTenants(filters);

  const handleViewDetails = (tenantId: string) => {
    setSelectedTenantId(tenantId);
    setDetailModalOpen(true);
  };

  const handleManageQuota = (tenantId: string) => {
    setSelectedTenantId(tenantId);
    setQuotaModalOpen(true);
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="animate-pulse flex gap-4">
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
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
        <p className="text-red-800">Failed to load tenants</p>
      </div>
    );
  }

  return (
    <>
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">All Tenants</h2>
          <TenantFiltersComponent filters={filters} onFiltersChange={setFilters} />
        </div>

        <div className="divide-y divide-gray-200">
          {tenants?.length === 0 ? (
            <div className="p-12 text-center">
              <p className="text-gray-500">No tenants found</p>
            </div>
          ) : (
            tenants?.map((tenant) => (
              <TenantListItem
                key={tenant.id}
                tenant={tenant}
                onViewDetails={() => handleViewDetails(tenant.id)}
                onManageQuota={() => handleManageQuota(tenant.id)}
              />
            ))
          )}
        </div>
      </div>

      {selectedTenantId && (
        <>
          <TenantDetailModal
            isOpen={detailModalOpen}
            onClose={() => {
              setDetailModalOpen(false);
              setSelectedTenantId(null);
            }}
            tenantId={selectedTenantId}
          />
          <SeatQuotaModal
            isOpen={quotaModalOpen}
            onClose={() => {
              setQuotaModalOpen(false);
              setSelectedTenantId(null);
            }}
            tenantId={selectedTenantId}
          />
        </>
      )}
    </>
  );
};

// src/features/platform-admin/components/TenantManagement/TenantListItem.tsx

import React from 'react';
import { Tenant, TenantStatus } from '../../types/platform-admin.types';
import { useUpdateTenantStatus } from '../../hooks/useTenants';
import { Menu } from '@headlessui/react';
import { 
  BuildingOfficeIcon, 
  EllipsisVerticalIcon,
  ChartBarIcon,
  UsersIcon,
} from '@heroicons/react/24/outline';

interface TenantListItemProps {
  tenant: Tenant;
  onViewDetails: () => void;
  onManageQuota: () => void;
}

export const TenantListItem: React.FC<TenantListItemProps> = ({
  tenant,
  onViewDetails,
  onManageQuota,
}) => {
  const updateStatus = useUpdateTenantStatus();

  const getStatusBadgeClass = () => {
    switch (tenant.status) {
      case TenantStatus.ACTIVE:
        return 'bg-green-100 text-green-800';
      case TenantStatus.SUSPENDED:
        return 'bg-red-100 text-red-800';
      case TenantStatus.TRIAL:
        return 'bg-blue-100 text-blue-800';
      case TenantStatus.CHURNED:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const utilizationPercentage = (tenant.seatsUsed / tenant.seatQuota) * 100;
  const getUtilizationColor = () => {
    if (utilizationPercentage >= 90) return 'text-red-600';
    if (utilizationPercentage >= 80) return 'text-yellow-600';
    return 'text-green-600';
  };

  const handleStatusChange = (status: TenantStatus) => {
    updateStatus.mutate({ tenantId: tenant.id, status });
  };

  return (
    <div className="p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-center gap-4">
        <div className="flex-shrink-0">
          <BuildingOfficeIcon className="w-10 h-10 text-gray-400" />
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="text-sm font-medium text-gray-900 truncate">{tenant.name}</h3>
            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass()}`}>
              {tenant.status}
            </span>
          </div>
          <p className="text-sm text-gray-500">{tenant.subdomain}.npsplaybox.com</p>
          <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
            <div className="flex items-center gap-1">
              <UsersIcon className="w-4 h-4" />
              <span>{tenant.userCount} users</span>
            </div>
            <div className="flex items-center gap-1">
              <ChartBarIcon className="w-4 h-4" />
              <span className={getUtilizationColor()}>
                {tenant.seatsUsed}/{tenant.seatQuota} seats ({utilizationPercentage.toFixed(0)}%)
              </span>
            </div>
            {tenant.lastActivityAt && (
              <span>Last active: {new Date(tenant.lastActivityAt).toLocaleDateString()}</span>
            )}
          </div>
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
                    onClick={onViewDetails}
                    className={`${active ? 'bg-gray-50' : ''} w-full text-left px-4 py-2 text-sm text-gray-700`}
                  >
                    View Details
                  </button>
                )}
              </Menu.Item>
              <Menu.Item>
                {({ active }) => (
                  <button
                    onClick={onManageQuota}
                    className={`${active ? 'bg-gray-50' : ''} w-full text-left px-4 py-2 text-sm text-gray-700`}
                  >
                    Manage Seat Quota
                  </button>
                )}
              </Menu.Item>
              <div className="border-t border-gray-100 my-1"></div>
              {tenant.status !== TenantStatus.ACTIVE && (
                <Menu.Item>
                  {({ active }) => (
                    <button
                      onClick={() => handleStatusChange(TenantStatus.ACTIVE)}
                      className={`${active ? 'bg-green-50' : ''} w-full text-left px-4 py-2 text-sm text-green-700`}
                    >
                      Activate
                    </button>
                  )}
                </Menu.Item>
              )}
              {tenant.status !== TenantStatus.SUSPENDED && (
                <Menu.Item>
                  {({ active }) => (
                    <button
                      onClick={() => handleStatusChange(TenantStatus.SUSPENDED)}
                      className={`${active ? 'bg-red-50' : ''} w-full text-left px-4 py-2 text-sm text-red-700`}
                    >
                      Suspend
                    </button>
                  )}
                </Menu.Item>
              )}
            </Menu.Items>
          </Menu>
        </div>
      </div>
    </div>
  );
};

// src/features/platform-admin/components/TenantManagement/TenantFilters.tsx

import React from 'react';
import { TenantFilters, TenantStatus } from '../../types/platform-admin.types';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';

interface TenantFiltersComponentProps {
  filters: TenantFilters;
  onFiltersChange: (filters: TenantFilters) => void;
}

export const TenantFiltersComponent: React.FC<TenantFiltersComponentProps> = ({
  filters,
  onFiltersChange,
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div className="relative md:col-span-2">
        <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search tenants..."
          value={filters.search || ''}
          onChange={(e) => onFiltersChange({ ...filters, search: e.target.value || undefined })}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      <select
        value={filters.status || ''}
        onChange={(e) => onFiltersChange({ ...filters, status: e.target.value as TenantStatus || undefined })}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option value="">All Statuses</option>
        <option value={TenantStatus.ACTIVE}>Active</option>
        <option value={TenantStatus.TRIAL}>Trial</option>
        <option value={TenantStatus.SUSPENDED}>Suspended</option>
        <option value={TenantStatus.CHURNED}>Churned</option>
      </select>

      <select
        value={filters.sortBy || ''}
        onChange={(e) => onFiltersChange({ ...filters, sortBy: e.target.value as any || undefined })}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option value="">Sort by...</option>
        <option value="name">Name</option>
        <option value="createdAt">Created Date</option>
        <option value="lastActivityAt">Last Activity</option>
        <option value="userCount">User Count</option>
      </select>
    </div>
  );
};

// src/features/platform-admin/components/TenantManagement/SeatQuotaModal.tsx

import React from 'react';
import { useForm } from 'react-hook-form';
import { useTenant, useUpdateSeatQuota } from '../../hooks/useTenants';
import { Dialog } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';

interface SeatQuotaModalProps {
  isOpen: boolean;
  onClose: () => void;
  tenantId: string;
}

interface QuotaFormData {
  quota: number;
}

export const SeatQuotaModal: React.FC<SeatQuotaModalProps> = ({
  isOpen,
  onClose,
  tenantId,
}) => {
  const { data: tenant } = useTenant(tenantId);
  const updateQuota = useUpdateSeatQuota();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<QuotaFormData>({
    defaultValues: {
      quota: tenant?.seatQuota,
    },
  });

  const onSubmit = async (data: QuotaFormData) => {
    await updateQuota.mutateAsync({ tenantId, quota: data.quota });
    onClose();
  };

  if (!tenant) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="bg-white rounded-lg shadow-xl max-w-md w-full">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <Dialog.Title className="text-lg font-semibold text-gray-900">
              Manage Seat Quota
            </Dialog.Title>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <XMarkIcon className="w-5 h-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="p-6 space-y-4">
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Tenant</p>
                <p className="font-medium text-gray-900">{tenant.name}</p>
                <p className="text-sm text-gray-500">{tenant.subdomain}.npsplaybox.com</p>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-blue-900 font-medium">Current Usage</span>
                  <span className="text-blue-800">
                    {tenant.seatsUsed} / {tenant.seatQuota} seats
                  </span>
                </div>
                <div className="w-full bg-blue-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full"
                    style={{ width: `${(tenant.seatsUsed / tenant.seatQuota) * 100}%` }}
                  ></div>
                </div>
              </div>

              <div>
                <label htmlFor="quota" className="block text-sm font-medium text-gray-700 mb-1">
                  New Seat Quota
                </label>
                <input
                  type="number"
                  id="quota"
                  {...register('quota', {
                    required: 'Quota is required',
                    min: {
                      value: tenant.seatsUsed,
                      message: `Quota must be at least ${tenant.seatsUsed} (current usage)`,
                    },
                    max: { value: 10000, message: 'Maximum quota is 10,000 seats' },
                  })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                {errors.quota && (
                  <p className="mt-1 text-sm text-red-600">{errors.quota.message}</p>
                )}
                <p className="mt-1 text-xs text-gray-500">
                  Must be at least {tenant.seatsUsed} (current usage)
                </p>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={updateQuota.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg disabled:opacity-50"
              >
                {updateQuota.isPending ? 'Updating...' : 'Update Quota'}
              </button>
            </div>
          </form>
        </Dialog.Panel>
      </div>
    </Dialog>
  );
};
```

### 6. Audit Log Components

```typescript
// src/features/platform-admin/components/AuditLogs/AuditLogViewer.tsx

import React, { useState } from 'react';
import { useAuditLogs } from '../../hooks/useAuditLogs';
import { AuditLogFilters } from '../../types/platform-admin.types';
import { AuditLogList } from './AuditLogList';
import { AuditLogFiltersComponent } from './AuditLogFilters';
import { AuditLogExport } from './AuditLogExport';

export const AuditLogViewer: React.FC = () => {
  const [filters, setFilters] = useState<AuditLogFilters>({ page: 1, pageSize: 20 });
  const { data, isLoading, error } = useAuditLogs(filters);

  const handlePageChange = (page: number) => {
    setFilters({ ...filters, page });
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-gray-900">Audit Logs</h2>
          <AuditLogExport filters={filters} />
        </div>
        <AuditLogFiltersComponent filters={filters} onFiltersChange={setFilters} />
      </div>

      {isLoading ? (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="space-y-4">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="animate-pulse flex gap-4">
                <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                  <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : error ? (
        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
          <p className="text-red-800">Failed to load audit logs</p>
        </div>
      ) : (
        <AuditLogList
          logs={data?.logs || []}
          total={data?.total || 0}
          currentPage={filters.page || 1}
          pageSize={filters.pageSize || 20}
          onPageChange={handlePageChange}
        />
      )}
    </div>
  );
};

// src/features/platform-admin/components/AuditLogs/AuditLogFilters.tsx

import React from 'react';
import { AuditLogFilters, AuditEventType } from '../../types/platform-admin.types';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';

interface AuditLogFiltersComponentProps {
  filters: AuditLogFilters;
  onFiltersChange: (filters: AuditLogFilters) => void;
}

export const AuditLogFiltersComponent: React.FC<AuditLogFiltersComponentProps> = ({
  filters,
  onFiltersChange,
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="relative">
        <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search..."
          value={filters.search || ''}
          onChange={(e) => onFiltersChange({ ...filters, search: e.target.value || undefined })}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <select
        value={filters.eventType || ''}
        onChange={(e) =>
          onFiltersChange({ ...filters, eventType: e.target.value as AuditEventType || undefined })
        }
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
      >
        <option value="">All Event Types</option>
        {Object.values(AuditEventType).map((type) => (
          <option key={type} value={type}>
            {type.replace(/_/g, ' ')}
          </option>
        ))}
      </select>

      <input
        type="date"
        value={filters.startDate || ''}
        onChange={(e) => onFiltersChange({ ...filters, startDate: e.target.value || undefined })}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
        placeholder="Start Date"
      />

      <input
        type="date"
        value={filters.endDate || ''}
        onChange={(e) => onFiltersChange({ ...filters, endDate: e.target.value || undefined })}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
        placeholder="End Date"
      />
    </div>
  );
};

// src/features/platform-admin/components/AuditLogs/AuditLogList.tsx

import React from 'react';
import { AuditLog } from '../../types/platform-admin.types';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/outline';

interface AuditLogListProps {
  logs: AuditLog[];
  total: number;
  currentPage: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export const AuditLogList: React.FC<AuditLogListProps> = ({
  logs,
  total,
  currentPage,
  pageSize,
  onPageChange,
}) => {
  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Timestamp
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Event
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                User
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tenant
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                IP Address
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {logs.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                  No audit logs found
                </td>
              </tr>
            ) : (
              logs.map((log) => (
                <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {new Date(log.timestamp).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                      {log.eventType.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {log.userEmail}
                    {log.metadata?.impersonatorEmail && (
                      <span className="block text-xs text-purple-600">
                        (via {log.metadata.impersonatorEmail})
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {log.tenantName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                    {log.ipAddress}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-700">
            Showing {(currentPage - 1) * pageSize + 1} to {Math.min(currentPage * pageSize, total)} of{' '}
            {total} results
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeftIcon className="w-5 h-5" />
            </button>
            <span className="text-sm text-gray-700">
              Page {currentPage} of {totalPages}
            </span>
            <button
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronRightIcon className="w-5 h-5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

// src/features/platform-admin/components/AuditLogs/AuditLogExport.tsx

import React from 'react';
import { auditApi } from '../../api/audit.api';
import { AuditLogFilters } from '../../types/platform-admin.types';
import { ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import { useToast } from '../../../shared/hooks/useToast';

interface AuditLogExportProps {
  filters?: AuditLogFilters;
}

export const AuditLogExport: React.FC<AuditLogExportProps> = ({ filters }) => {
  const { showToast } = useToast();
  const [isExporting, setIsExporting] = React.useState(false);

  const handleExport = async () => {
    try {
      setIsExporting(true);
      const blob = await auditApi.exportAuditLogs(filters);
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      showToast('Audit logs exported successfully', 'success');
    } catch (error: any) {
      showToast(error.response?.data?.message || 'Failed to export audit logs', 'error');
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <button
      onClick={handleExport}
      disabled={isExporting}
      className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50"
    >
      <ArrowDownTrayIcon className="w-4 h-4" />
      {isExporting ? 'Exporting...' : 'Export CSV'}
    </button>
  );
};
```

### 7. Impersonation Components

```typescript
// src/features/platform-admin/components/Impersonation/StartImpersonationModal.tsx

import React from 'react';
import { useForm } from 'react-hook-form';
import { useCreateImpersonationRequest } from '../../hooks/useImpersonation';
import { useTenants } from '../../hooks/useTenants';
import { Dialog } from '@headlessui/react';
import { XMarkIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';

interface StartImpersonationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface ImpersonationFormData {
  tenantId: string;
  targetUserId?: string;
  reason: string;
}

export const StartImpersonationModal: React.FC<StartImpersonationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { data: tenants } = useTenants();
  const createRequest = useCreateImpersonationRequest();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ImpersonationFormData>();

  const onSubmit = async (data: ImpersonationFormData) => {
    await createRequest.mutateAsync(data);
    reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="bg-white rounded-lg shadow-xl max-w-md w-full">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <Dialog.Title className="text-lg font-semibold text-gray-900">
              Request Impersonation
            </Dialog.Title>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <XMarkIcon className="w-5 h-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="p-6 space-y-4">
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex gap-3">
                <ExclamationTriangleIcon className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm text-yellow-800 font-medium">Security Notice</p>
                  <p className="text-xs text-yellow-700 mt-1">
                    All impersonation sessions are logged and monitored. Provide a clear business
                    justification.
                  </p>
                </div>
              </div>

              <div>
                <label htmlFor="tenantId" className="block text-sm font-medium text-gray-700 mb-1">
                  Target Tenant *
                </label>
                <select
                  id="tenantId"
                  {...register('tenantId', { required: 'Tenant is required' })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select a tenant</option>
                  {tenants?.map((tenant) => (
                    <option key={tenant.id} value={tenant.id}>
                      {tenant.name} ({tenant.subdomain})
                    </option>
                  ))}
                </select>
                {errors.tenantId && (
                  <p className="mt-1 text-sm text-red-600">{errors.tenantId.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="targetUserId" className="block text-sm font-medium text-gray-700 mb-1">
                  Target User ID (Optional)
                </label>
                <input
                  type="text"
                  id="targetUserId"
                  {...register('targetUserId')}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="Leave empty to impersonate any tenant admin"
                />
                <p className="mt-1 text-xs text-gray-500">
                  If not specified, you'll be logged in as a tenant administrator
                </p>
              </div>

              <div>
                <label htmlFor="reason" className="block text-sm font-medium text-gray-700 mb-1">
                  Business Justification *
                </label>
                <textarea
                  id="reason"
                  rows={4}
                  {...register('reason', {
                    required: 'Justification is required',
                    minLength: {
                      value: 20,
                      message: 'Provide at least 20 characters explaining the reason',
                    },
                  })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="Explain why impersonation is needed..."
                />
                {errors.reason && (
                  <p className="mt-1 text-sm text-red-600">{errors.reason.message}</p>
                )}
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createRequest.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 rounded-lg disabled:opacity-50"
              >
                {createRequest.isPending ? 'Requesting...' : 'Request Access'}
              </button>
            </div>
          </form>
        </Dialog.Panel>
      </div>
    </Dialog>
  );
};

// src/features/platform-admin/components/Impersonation/ImpersonationBanner.tsx

import React from 'react';
import { useActiveImpersonation, useEndImpersonation } from '../../hooks/useImpersonation';
import { ExclamationTriangleIcon, XMarkIcon } from '@heroicons/react/24/outline';

export const ImpersonationBanner: React.FC = () => {
  const { data: activeImpersonation } = useActiveImpersonation();
  const endImpersonation = useEndImpersonation();

  if (!activeImpersonation) return null;

  return (
    <div className="bg-purple-600 text-white px-4 py-3 shadow-lg">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <div className="flex items-center gap-3">
          <ExclamationTriangleIcon className="w-6 h-6 flex-shrink-0" />
          <div>
            <p className="font-semibold">Impersonation Mode Active</p>
            <p className="text-sm text-purple-100">
              You are impersonating {activeImpersonation.tenantName}
              {activeImpersonation.targetUserEmail && ` as ${activeImpersonation.targetUserEmail}`}
            </p>
          </div>
        </div>
        <button
          onClick={() => endImpersonation.mutate()}
          disabled={endImpersonation.isPending}
          className="flex items-center gap-2 px-4 py-2 bg-white text-purple-600 rounded-lg hover:bg-purple-50 transition-colors font-medium disabled:opacity-50"
        >
          <XMarkIcon className="w-4 h-4" />
          End Session
        </button>
      </div>
    </div>
  );
};
```

### 8. Main Dashboard Page

```typescript
// src/features/platform-admin/pages/PlatformAdminDashboard.tsx

import React, { useState } from 'react';
import { Tab } from '@headlessui/react';
import {
  ChartBarIcon,
  BuildingOfficeIcon,
  DocumentTextIcon,
  UserIcon,
} from '@heroicons/react/24/outline';
import { PlatformMetrics } from '../components/Dashboard/PlatformMetrics';
import { TenantGrowthChart } from '../components/Dashboard/TenantGrowthChart';
import { SeatUtilizationChart } from '../components/Dashboard/SeatUtilizationChart';
import { RecentActivityFeed } from '../components/Dashboard/RecentActivityFeed';
import { TenantList } from '../components/TenantManagement/TenantList';
import { AuditLogViewer } from '../components/AuditLogs/AuditLogViewer';
import { ImpersonationRequestList } from '../components/Impersonation/ImpersonationRequestList';
import { StartImpersonationModal } from '../components/Impersonation/StartImpersonationModal';
import { ImpersonationBanner } from '../components/Impersonation/ImpersonationBanner';

export const PlatformAdminDashboard: React.FC = () => {
  const [impersonationModalOpen, setImpersonationModalOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50">
      <ImpersonationBanner />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Platform Administration</h1>
            <p className="mt-2 text-sm text-gray-600">
              Manage tenants, monitor metrics, and review system activity
            </p>
          </div>
          <button
            onClick={() => setImpersonationModalOpen(true)}
            className="px-4 py-2 text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 rounded-lg transition-colors"
          >
            Request Impersonation
          </button>
        </div>

        <Tab.Group>
          <Tab.List className="flex space-x-1 rounded-xl bg-white p-1 shadow mb-6">
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected ? 'bg-blue-600 text-white shadow' : 'text-gray-700 hover:bg-gray-50'}`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <ChartBarIcon className="w-5 h-5" />
                Dashboard
              </div>
            </Tab>
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected ? 'bg-blue-600 text-white shadow' : 'text-gray-700 hover:bg-gray-50'}`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <BuildingOfficeIcon className="w-5 h-5" />
                Tenants
              </div>
            </Tab>
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected ? 'bg-blue-600 text-white shadow' : 'text-gray-700 hover:bg-gray-50'}`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <DocumentTextIcon className="w-5 h-5" />
                Audit Logs
              </div>
            </Tab>
            <Tab
              className={({ selected }) =>
                `w-full rounded-lg py-2.5 text-sm font-medium leading-5 transition-colors
                ${selected ? 'bg-blue-600 text-white shadow' : 'text-gray-700 hover:bg-gray-50'}`
              }
            >
              <div className="flex items-center justify-center gap-2">
                <UserIcon className="w-5 h-5" />
                Impersonation
              </div>
            </Tab>
          </Tab.List>

          <Tab.Panels>
            <Tab.Panel className="space-y-6">
              <PlatformMetrics />
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <TenantGrowthChart />
                <SeatUtilizationChart />
              </div>
              <RecentActivityFeed />
            </Tab.Panel>

            <Tab.Panel>
              <TenantList />
            </Tab.Panel>

            <Tab.Panel>
              <AuditLogViewer />
            </Tab.Panel>

            <Tab.Panel>
              <ImpersonationRequestList />
            </Tab.Panel>
          </Tab.Panels>
        </Tab.Group>
      </div>

      <StartImpersonationModal
        isOpen={impersonationModalOpen}
        onClose={() => setImpersonationModalOpen(false)}
      />
    </div>
  );
};
```

## Testing Strategy

### Unit Tests
- Component rendering and interactions
- API client functions
- TanStack Query hooks
- Chart data transformations
- Filter logic

### Integration Tests
- Tenant management workflows
- Seat quota updates
- Audit log filtering and export
- Impersonation request lifecycle

### E2E Tests
- Platform admin dashboard navigation
- Tenant status management end-to-end
- Audit log search and filter
- Complete impersonation flow
- Metric chart interactions

## Deployment Checklist

- [ ] Configure API endpoints for production
- [ ] Set up error tracking and monitoring
- [ ] Configure authentication for platform admin role
- [ ] Test impersonation security controls
- [ ] Verify audit log retention policies
- [ ] Performance test with large datasets
- [ ] Accessibility audit (WCAG 2.1 AA)
- [ ] Cross-browser testing
- [ ] Mobile responsiveness
- [ ] Rate limiting on sensitive operations

## Performance Optimizations

1. **Virtual Scrolling**: For large tenant lists and audit logs
2. **Query Caching**: TanStack Query with appropriate stale times
3. **Chart Memoization**: Memoize expensive chart calculations
4. **Lazy Loading**: Code-split dashboard tabs
5. **Debounced Search**: 300ms delay on filter inputs
6. **Pagination**: Server-side pagination for audit logs

## Security Considerations

1. **Impersonation Audit**: All sessions logged with requester, reason, duration
2. **Role-Based Access**: Enforce PLATFORM_ADMIN role on all routes
3. **Audit Log Protection**: Read-only, immutable logs
4. **Sensitive Data**: Mask PII in audit log exports
5. **Session Timeout**: Automatic impersonation session expiry
6. **IP Tracking**: Log IP addresses for all platform admin actions

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
    "@heroicons/react": "^2.1.1",
    "recharts": "^2.10.3"
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

- ✅ Platform metrics display accurately and update in real-time
- ✅ Tenant list with filtering, sorting, and status management
- ✅ Seat quota management with validation
- ✅ Comprehensive audit log viewer with advanced filters
- ✅ Audit log CSV export functionality
- ✅ Complete impersonation workflow (request → approve → start → end)
- ✅ Impersonation sessions properly logged in audit trail
- ✅ Visual charts for tenant growth and seat utilization
- ✅ Recent activity feed on dashboard
- ✅ Mobile responsive design
- ✅ Accessible to keyboard navigation and screen readers
- ✅ All API errors handled gracefully with user-friendly messages

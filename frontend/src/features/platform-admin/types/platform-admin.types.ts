export enum TenantStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  TRIAL = 'TRIAL',
  DEACTIVATED = 'DEACTIVATED',
}

export enum SubscriptionTier {
  STANDARD = 'STANDARD',
  PROFESSIONAL = 'PROFESSIONAL',
  ENTERPRISE = 'ENTERPRISE',
}

export interface PlatformTenant {
  id: number;
  tenantUuid: string;
  name: string;
  slug: string;
  status: TenantStatus | string;
  maxSeats: number;
  usedSeats: number;
  subscriptionTier: string;
  createdAt: string;
  updatedAt: string;
  metadata?: string;
  invitationToken?: string;
  invitationUrl?: string;
}

export interface CreateTenantPayload {
  name: string;
  slug: string;
  adminEmail: string;
  adminFirstName?: string;
  adminLastName?: string;
  adminPassword?: string;
  maxSeats: number;
  subscriptionTier: string;
}

export interface UpdateTenantPayload {
  name: string;
  subscriptionTier?: string;
  metadata?: string;
}

export interface TenantFilters {
  search?: string;
  status?: string;
  subscriptionTier?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PlatformMetricsData {
  totalTenants: number;
  activeTenants: number;
  suspendedTenants: number;
  trialTenants: number;
  totalSeatsAllocated: number;
  totalSeatsUsed: number;
  averageUtilization: number;
  activeImpersonations: number;
  pendingSeatRequests: number;
}

export interface TenantGrowthPoint {
  date: string;
  newTenants: number;
  totalTenants: number;
}

export interface SeatUtilizationPoint {
  tenantId: number;
  tenantName: string;
  quota: number;
  used: number;
  percentage: number;
}

export interface PlatformSeatRequest {
  id: number;
  tenantId: number;
  tenantName?: string;
  requestedByUserId?: number;
  requestedByUserName?: string;
  requestedSeats: number;
  justification: string;
  expectedGrowth?: string;
  contactEmail?: string;
  status: 'PENDING' | 'APPROVED' | 'DENIED';
  createdAt: string;
  reviewedAt?: string;
  reviewedByUserId?: number;
  denialReason?: string;
}

export interface PlatformAuditItem {
  id: number | string;
  eventId: string;
  tenantId?: number;
  tenantName?: string;
  eventType: string;
  action: string;
  actorId?: string;
  actorUsername?: string;
  actorRole?: string;
  resourceType?: string;
  resourceId?: string;
  status: 'SUCCESS' | 'FAILURE' | 'WARNING';
  details?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface PlatformAuditFilters {
  tenantId?: number;
  eventType?: string;
  actorUsername?: string;
  status?: string;
  resourceType?: string;
  page?: number;
  size?: number;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

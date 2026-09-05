export enum UserRole {
  TENANT_ADMIN = 'TENANT_ADMIN',
  DEVELOPER = 'DEVELOPER',
  VIEWER = 'VIEWER',
  PLATFORM_ADMIN = 'PLATFORM_ADMIN',
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING = 'PENDING',
  SUSPENDED = 'SUSPENDED',
}

export enum InvitationStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  EXPIRED = 'EXPIRED',
  REVOKED = 'REVOKED',
}

export interface TenantUser {
  id: number | string;
  userUuid?: string;
  email: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  role: UserRole | string;
  status: UserStatus | string;
  createdAt: string;
  updatedAt?: string;
  lastLoginAt?: string;
  tenant?: {
    id: number;
    name: string;
    slug: string;
  };
}

export interface TenantInvitation {
  id: number | string;
  email: string;
  role: UserRole | string;
  invitationToken?: string;
  invitedByName?: string;
  invitedByEmail?: string;
  expiresAt: string;
  createdAt: string;
  expired: boolean;
  status?: InvitationStatus | string;
}

export interface SeatUtilization {
  maxSeats: number;
  usedSeats: number;
  availableSeats: number;
  activeUsers: number;
  inactiveUsers: number;
  pendingInvitations: number;
  utilizationPercentage: number;
}

export interface QuotaStatus {
  tenantId: number;
  tenantName: string;
  maxSeats: number;
  usedSeats: number;
  availableSeats: number;
  activeUsers: number;
  inactiveUsers: number;
  pendingInvitations: number;
  utilizationPercentage: number;
  subscriptionTier: string;
  quotaExceeded: boolean;
  nearingLimit: boolean;
  lastUpdated?: string;
}

export interface SeatRequestPayload {
  additionalSeats: number;
  justification: string;
  expectedGrowth?: string;
  contactEmail?: string;
}

export interface SeatRequestResponse {
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

export interface TenantDetails {
  id: number;
  tenantUuid: string;
  name: string;
  slug: string;
  status: string;
  maxSeats: number;
  usedSeats: number;
  subscriptionTier: string;
  createdAt: string;
  updatedAt: string;
  metadata?: string;
  branding?: {
    logoUrl?: string;
    primaryColor?: string;
    secondaryColor?: string;
    companyName?: string;
  };
}

export interface UserFilters {
  search?: string;
  role?: string;
  status?: string;
  sortBy?: 'name' | 'email' | 'createdAt' | 'lastLoginAt';
  sortOrder?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface InvitationFilters {
  search?: string;
  status?: string;
}

export interface AuditEventItem {
  id: number | string;
  eventId: string;
  tenantId: number;
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

export interface AuditFilters {
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

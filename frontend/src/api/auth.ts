import { apiClient } from './client';

export interface UserTenant {
  id: number;
  name: string;
  slug: string;
}

export interface User {
  id?: string | number;
  uuid?: string;
  username?: string;
  email: string;
  firstName?: string;
  lastName?: string;
  organization?: string;
  role?: 'PLATFORM_ADMIN' | 'TENANT_ADMIN' | 'DEVELOPER' | 'VIEWER' | 'ADMIN' | 'USER';
  authProvider?: 'LOCAL' | 'MICROSOFT';
  tenant?: UserTenant;
  createdAt?: string;
  lastLoginAt?: string;
}

/** Backend returns either `accessToken` or `token` – we normalise to `accessToken` */
export interface AuthResponse {
  accessToken: string;
  token?: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user: User;
}

export interface LoginRequest {
  email?: string;
  emailOrUsername?: string;
  password: string;
}

export interface RegisterRequest {
  firstName?: string;
  lastName?: string;
  username: string;
  email: string;
  organization?: string;
  password: string;
}

export const loginApi = async (data: LoginRequest): Promise<AuthResponse> => {
  const payload = {
    emailOrUsername: data.emailOrUsername || data.email || '',
    password: data.password,
  };
  const res = await apiClient.post<any>('/api/auth/login', payload);
  const raw = res.data;
  // Normalise: backend may return 'token' instead of 'accessToken'
  const normalised: AuthResponse = {
    ...raw,
    accessToken: raw.accessToken || raw.token || '',
    user: raw.user || {},
  };
  return normalised;
};

export const registerApi = async (data: RegisterRequest): Promise<AuthResponse> => {
  const res = await apiClient.post<any>('/api/auth/register', data);
  const raw = res.data;
  const normalised: AuthResponse = {
    ...raw,
    accessToken: raw.accessToken || raw.token || '',
    user: raw.user || {},
  };
  return normalised;
};

export const getMeApi = async (): Promise<User> => {
  const res = await apiClient.get<any>('/api/auth/me');
  const raw = res.data;
  // Response might be a User directly or wrapped in { user: ... }
  if (raw && typeof raw === 'object' && 'user' in raw && raw.user) {
    return raw.user as User;
  }
  return raw as User;
};


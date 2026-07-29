import { apiClient } from './client';

export interface User {
  id: string | number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  organization?: string;
  role?: 'ADMIN' | 'USER';
  authProvider?: 'LOCAL' | 'MICROSOFT';
  createdAt?: string;
  lastLoginAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType?: string;
  expiresIn?: number;
  user: User;
}

export interface LoginRequest {
  email: string;
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
  const res = await apiClient.post<AuthResponse>('/api/auth/login', data);
  return res.data;
};

export const registerApi = async (data: RegisterRequest): Promise<AuthResponse> => {
  const res = await apiClient.post<AuthResponse>('/api/auth/register', data);
  return res.data;
};

export const getMeApi = async (): Promise<User> => {
  const res = await apiClient.get<User | AuthResponse>('/api/auth/me');
  if ('user' in res.data) {
    return res.data.user;
  }
  return res.data as User;
};

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '../../../../shared/hooks/useToast';
import { InvitationForm } from '../InvitationForm';
import { invitationsApi } from '../../../api/invitations.api';
import * as seatHooks from '../../../hooks/useSeatUsage';

describe('InvitationForm Component', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    vi.restoreAllMocks();
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>{ui}</ToastProvider>
      </QueryClientProvider>
    );
  };

  it('renders available seats count and form fields', () => {
    vi.spyOn(seatHooks, 'useQuotaStatus').mockReturnValue({
      data: {
        tenantId: 1,
        tenantName: 'Acme Corp',
        maxSeats: 10,
        usedSeats: 4,
        availableSeats: 6,
        activeUsers: 4,
        inactiveUsers: 0,
        pendingInvitations: 0,
        utilizationPercentage: 40,
        subscriptionTier: 'ENTERPRISE',
        quotaExceeded: false,
        nearingLimit: false,
      },
      isLoading: false,
    } as any);

    renderWithProviders(<InvitationForm />);

    expect(screen.getByText(/Invite Team Member/i)).toBeInTheDocument();
    expect(screen.getByText(/6 seat\(s\)/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/engineer@organization.com/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Send Invitation/i })).toBeInTheDocument();
  });

  it('submits invitation with valid email and role', async () => {
    vi.spyOn(seatHooks, 'useQuotaStatus').mockReturnValue({
      data: {
        tenantId: 1,
        tenantName: 'Acme Corp',
        maxSeats: 10,
        usedSeats: 4,
        availableSeats: 6,
        activeUsers: 4,
        inactiveUsers: 0,
        pendingInvitations: 0,
        utilizationPercentage: 40,
        subscriptionTier: 'ENTERPRISE',
        quotaExceeded: false,
        nearingLimit: false,
      },
      isLoading: false,
    } as any);

    const createSpy = vi.spyOn(invitationsApi, 'createInvitation').mockResolvedValue({
      id: 1,
      email: 'colleague@example.com',
      role: 'DEVELOPER',
      createdAt: new Date().toISOString(),
      expiresAt: new Date().toISOString(),
      expired: false,
    });

    renderWithProviders(<InvitationForm />);

    const emailInput = screen.getByPlaceholderText(/engineer@organization.com/i);
    fireEvent.change(emailInput, { target: { value: 'colleague@example.com' } });

    const form = emailInput.closest('form')!;
    fireEvent.submit(form);

    await waitFor(() => {
      expect(createSpy).toHaveBeenCalledWith({
        email: 'colleague@example.com',
        role: 'DEVELOPER',
      });
    });
  });

  it('displays validation error if email does not contain @', async () => {
    vi.spyOn(seatHooks, 'useQuotaStatus').mockReturnValue({
      data: {
        tenantId: 1,
        tenantName: 'Acme Corp',
        maxSeats: 10,
        usedSeats: 4,
        availableSeats: 6,
        activeUsers: 4,
        inactiveUsers: 0,
        pendingInvitations: 0,
        utilizationPercentage: 40,
        subscriptionTier: 'ENTERPRISE',
        quotaExceeded: false,
        nearingLimit: false,
      },
      isLoading: false,
    } as any);

    renderWithProviders(<InvitationForm />);

    const emailInput = screen.getByPlaceholderText(/engineer@organization.com/i);
    fireEvent.change(emailInput, { target: { value: 'invalidemail' } });

    const form = emailInput.closest('form')!;
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/Please provide a valid member email address/i)).toBeInTheDocument();
    });
  });

  it('shows seat limit reached banner and disables inputs when available seats is 0', () => {
    vi.spyOn(seatHooks, 'useQuotaStatus').mockReturnValue({
      data: {
        tenantId: 1,
        tenantName: 'Acme Corp',
        maxSeats: 10,
        usedSeats: 10,
        availableSeats: 0,
        activeUsers: 10,
        inactiveUsers: 0,
        pendingInvitations: 0,
        utilizationPercentage: 100,
        subscriptionTier: 'ENTERPRISE',
        quotaExceeded: true,
        nearingLimit: true,
      },
      isLoading: false,
    } as any);

    renderWithProviders(<InvitationForm />);

    expect(screen.getByText(/Seat Limit Reached/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/engineer@organization.com/i)).toBeDisabled();
    expect(screen.getByRole('button', { name: /Send Invitation/i })).toBeDisabled();
  });
});

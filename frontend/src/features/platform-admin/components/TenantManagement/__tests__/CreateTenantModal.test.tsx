import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '../../../../shared/hooks/useToast';
import { CreateTenantModal } from '../CreateTenantModal';
import * as tenantHooks from '../../../hooks/usePlatformTenants';

describe('CreateTenantModal Component', () => {
  let queryClient: QueryClient;
  const mockMutateAsync = vi.fn();

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    vi.restoreAllMocks();

    vi.spyOn(tenantHooks, 'useCreateTenant').mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    } as any);
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>{ui}</ToastProvider>
      </QueryClientProvider>
    );
  };

  it('renders modal with organization, admin and optional password fields', () => {
    renderWithProviders(
      <CreateTenantModal isOpen={true} onClose={vi.fn()} />
    );

    expect(screen.getByText('Provision New Enterprise Tenant')).toBeInTheDocument();
    expect(screen.getByLabelText(/Organization Name \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Subdomain \/ Slug \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Admin Work Email \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Initial Password \(Optional\)/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Provision & Generate Invite/i })).toBeInTheDocument();
  });

  it('dispatches invitation when password is left empty', async () => {
    mockMutateAsync.mockResolvedValueOnce({
      id: 1,
      name: 'Zenith Bank',
      slug: 'zenith',
      subscriptionTier: 'PROFESSIONAL',
      maxSeats: 10,
      invitationToken: 'test-token-123',
      invitationUrl: 'http://localhost:3000/accept-invitation?token=test-token-123',
    });

    renderWithProviders(
      <CreateTenantModal isOpen={true} onClose={vi.fn()} />
    );

    fireEvent.change(screen.getByLabelText(/Organization Name \*/i), {
      target: { value: 'Zenith Bank' },
    });
    fireEvent.change(screen.getByLabelText(/Admin Work Email \*/i), {
      target: { value: 'admin@zenith.com' },
    });

    const submitBtn = screen.getByRole('button', { name: /Provision & Generate Invite/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockMutateAsync).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Zenith Bank',
          slug: 'zenith-bank',
          adminEmail: 'admin@zenith.com',
          adminPassword: undefined,
        })
      );
    });

    // Should transition to success screen
    await waitFor(() => {
      expect(screen.getByText(/Zenith Bank successfully provisioned/i)).toBeInTheDocument();
      expect(screen.getByText(/Administrator Setup Link/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Copy Link/i })).toBeInTheDocument();
    });
  });

  it('validates password length if password is provided', async () => {
    renderWithProviders(
      <CreateTenantModal isOpen={true} onClose={vi.fn()} />
    );

    fireEvent.change(screen.getByLabelText(/Organization Name \*/i), {
      target: { value: 'Zenith Bank' },
    });
    fireEvent.change(screen.getByLabelText(/Admin Work Email \*/i), {
      target: { value: 'admin@zenith.com' },
    });
    fireEvent.change(screen.getByLabelText(/Initial Password \(Optional\)/i), {
      target: { value: 'short' },
    });

    // Button text updates to reflect direct user provisioning
    expect(screen.getByRole('button', { name: /Provision Tenant & User/i })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /Provision Tenant & User/i }));

    await waitFor(() => {
      expect(screen.getByText(/Password must be at least 8 characters if provided/i)).toBeInTheDocument();
      expect(mockMutateAsync).not.toHaveBeenCalled();
    });
  });
});

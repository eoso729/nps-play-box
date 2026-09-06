import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '../../../../shared/hooks/useToast';
import { StartImpersonationModal } from '../StartImpersonationModal';
import { impersonationApi } from '../../../../../api/impersonation';
import * as tenantHooks from '../../../hooks/usePlatformTenants';

describe('StartImpersonationModal Component', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    vi.restoreAllMocks();

    vi.spyOn(tenantHooks, 'usePlatformTenants').mockReturnValue({
      data: {
        content: [
          { id: 1, name: 'Acme Global', slug: 'acme-global' },
          { id: 2, name: 'Zenith Payments', slug: 'zenith-payments' },
        ],
      },
      isLoading: false,
    } as any);
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>{ui}</ToastProvider>
      </QueryClientProvider>
    );
  };

  it('renders modal with dual-auth notice and input fields when open', () => {
    renderWithProviders(
      <StartImpersonationModal
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    expect(screen.getByText(/Request Support Impersonation/i)).toBeInTheDocument();
    expect(screen.getByText(/Dual Authorization & Audit Protocol/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Target User ID \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Business Justification \*/i)).toBeInTheDocument();
  });

  it('validates required fields before submission', async () => {
    renderWithProviders(
      <StartImpersonationModal
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    const submitBtn = screen.getByRole('button', { name: /Submit Request/i });
    fireEvent.click(submitBtn);

    expect(await screen.findByText(/Target user ID must be a positive integer/i)).toBeInTheDocument();
    expect(await screen.findByText(/Business justification must be at least 20 characters/i)).toBeInTheDocument();
  });

  it('submits request successfully when inputs are valid', async () => {
    const requestSpy = vi.spyOn(impersonationApi, 'requestSession').mockResolvedValue({
      data: {
        sessionUuid: 'test-uuid-1234',
        status: 'PENDING',
      },
    } as any);

    const onClose = vi.fn();

    renderWithProviders(
      <StartImpersonationModal
        isOpen={true}
        onClose={onClose}
      />
    );

    const userIdInput = screen.getByLabelText(/Target User ID \*/i);
    fireEvent.change(userIdInput, { target: { value: '42' } });

    const reasonInput = screen.getByLabelText(/Business Justification \*/i);
    fireEvent.change(reasonInput, {
      target: {
        value: 'Troubleshooting payment routing failure reported in support ticket #987654',
      },
    });

    const submitBtn = screen.getByRole('button', { name: /Submit Request/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(requestSpy).toHaveBeenCalledWith(
        42,
        'Troubleshooting payment routing failure reported in support ticket #987654',
        60
      );
      expect(onClose).toHaveBeenCalled();
    });
  });
});

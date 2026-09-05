import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PlatformMetrics } from '../PlatformMetrics';
import * as metricsHooks from '../../../hooks/usePlatformMetrics';

describe('PlatformMetrics Component', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    vi.restoreAllMocks();
  });

  const renderWithQuery = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        {ui}
      </QueryClientProvider>
    );
  };

  it('renders loading skeleton when loading', () => {
    vi.spyOn(metricsHooks, 'usePlatformMetrics').mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as any);

    const { container } = renderWithQuery(<PlatformMetrics />);
    expect(container.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('renders error message when query fails', () => {
    vi.spyOn(metricsHooks, 'usePlatformMetrics').mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error('Network error'),
    } as any);

    renderWithQuery(<PlatformMetrics />);
    expect(screen.getByText(/Failed to load platform metrics/i)).toBeInTheDocument();
  });

  it('renders KPI metrics cards correctly', () => {
    vi.spyOn(metricsHooks, 'usePlatformMetrics').mockReturnValue({
      data: {
        metrics: {
          totalTenants: 15,
          activeTenants: 12,
          suspendedTenants: 1,
          trialTenants: 2,
          totalSeatsAllocated: 250,
          totalSeatsUsed: 175,
          averageUtilization: 70.0,
          activeImpersonations: 1,
          pendingSeatRequests: 3,
        },
      },
      isLoading: false,
      error: null,
    } as any);

    renderWithQuery(<PlatformMetrics />);

    // Total Tenants
    expect(screen.getByText('Total Tenants')).toBeInTheDocument();
    expect(screen.getByText('15')).toBeInTheDocument();
    expect(screen.getByText(/12 active · 2 trial/i)).toBeInTheDocument();
    expect(screen.getByText(/1 suspended/i)).toBeInTheDocument();

    // Seats Allocated
    expect(screen.getByText('Seats Allocated')).toBeInTheDocument();
    expect(screen.getByText('250')).toBeInTheDocument();
    expect(screen.getByText(/175 seats currently claimed/i)).toBeInTheDocument();

    // System Seat Utilization
    expect(screen.getByText('System Seat Utilization')).toBeInTheDocument();
    expect(screen.getByText('70.0%')).toBeInTheDocument();
    expect(screen.getByText(/75 remaining seats available/i)).toBeInTheDocument();

    // Pending Seat Requests
    expect(screen.getByText('Pending Seat Requests')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText(/Requires admin review/i)).toBeInTheDocument();

    // Active Impersonations
    expect(screen.getByText('Active Impersonations')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();
  });
});

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { TenantFiltersComponent } from '../TenantFilters';
import { TenantStatus, SubscriptionTier } from '../../../types/platform-admin.types';

describe('TenantFiltersComponent', () => {
  it('triggers onFiltersChange when search input changes', () => {
    const onFiltersChange = vi.fn();
    const onCreateTenant = vi.fn();

    render(
      <TenantFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
        onCreateTenant={onCreateTenant}
      />
    );

    const searchInput = screen.getByPlaceholderText(/Search by tenant name or slug.../i);
    fireEvent.change(searchInput, { target: { value: 'apex' } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        search: 'apex',
        page: 0,
      })
    );
  });

  it('triggers onFiltersChange when status select changes', () => {
    const onFiltersChange = vi.fn();
    const onCreateTenant = vi.fn();

    render(
      <TenantFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
        onCreateTenant={onCreateTenant}
      />
    );

    const statusSelect = screen.getByDisplayValue('All Statuses');
    fireEvent.change(statusSelect, { target: { value: TenantStatus.ACTIVE } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        status: TenantStatus.ACTIVE,
        page: 0,
      })
    );
  });

  it('triggers onFiltersChange when subscription tier changes', () => {
    const onFiltersChange = vi.fn();
    const onCreateTenant = vi.fn();

    render(
      <TenantFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
        onCreateTenant={onCreateTenant}
      />
    );

    const tierSelect = screen.getByDisplayValue('All Tiers');
    fireEvent.change(tierSelect, { target: { value: SubscriptionTier.ENTERPRISE } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        subscriptionTier: SubscriptionTier.ENTERPRISE,
        page: 0,
      })
    );
  });

  it('shows reset button when filters active and resets on click', () => {
    const onFiltersChange = vi.fn();
    const onCreateTenant = vi.fn();

    render(
      <TenantFiltersComponent
        filters={{ search: 'corp' }}
        onFiltersChange={onFiltersChange}
        onCreateTenant={onCreateTenant}
      />
    );

    const resetButton = screen.getByRole('button', { name: /Reset/i });
    expect(resetButton).toBeInTheDocument();

    fireEvent.click(resetButton);
    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0,
      })
    );
  });

  it('calls onCreateTenant when Provision Tenant button is clicked', () => {
    const onFiltersChange = vi.fn();
    const onCreateTenant = vi.fn();

    render(
      <TenantFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
        onCreateTenant={onCreateTenant}
      />
    );

    const createButton = screen.getByRole('button', { name: /Provision Tenant/i });
    fireEvent.click(createButton);

    expect(onCreateTenant).toHaveBeenCalledTimes(1);
  });
});

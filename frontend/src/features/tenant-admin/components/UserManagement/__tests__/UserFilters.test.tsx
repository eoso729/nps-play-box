import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { UserFiltersComponent } from '../UserFilters';

describe('UserFiltersComponent', () => {
  it('triggers onFiltersChange when search input changes', () => {
    const onFiltersChange = vi.fn();
    render(
      <UserFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );

    const searchInput = screen.getByPlaceholderText(/Search by name, username, or email.../i);
    fireEvent.change(searchInput, { target: { value: 'alice' } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        search: 'alice',
        page: 0,
      })
    );
  });

  it('triggers onFiltersChange when role select changes', () => {
    const onFiltersChange = vi.fn();
    render(
      <UserFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );

    const roleSelect = screen.getByDisplayValue('All Roles');
    fireEvent.change(roleSelect, { target: { value: 'TENANT_ADMIN' } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        role: 'TENANT_ADMIN',
        page: 0,
      })
    );
  });

  it('triggers onFiltersChange when status select changes', () => {
    const onFiltersChange = vi.fn();
    render(
      <UserFiltersComponent
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );

    const statusSelect = screen.getByDisplayValue('All Statuses');
    fireEvent.change(statusSelect, { target: { value: 'ACTIVE' } });

    expect(onFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        status: 'ACTIVE',
        page: 0,
      })
    );
  });
});

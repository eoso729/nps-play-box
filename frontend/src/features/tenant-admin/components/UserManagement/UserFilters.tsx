import React from 'react';
import { UserFilters } from '../../types/tenant-admin.types';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { Search, Filter } from 'lucide-react';

interface UserFiltersProps {
  filters: UserFilters;
  onFiltersChange: (filters: UserFilters) => void;
}

export const UserFiltersComponent: React.FC<UserFiltersProps> = ({
  filters,
  onFiltersChange,
}) => {
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onFiltersChange({
      ...filters,
      search: e.target.value,
      page: 0,
    });
  };

  const handleRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onFiltersChange({
      ...filters,
      role: e.target.value || undefined,
      page: 0,
    });
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onFiltersChange({
      ...filters,
      status: e.target.value || undefined,
      page: 0,
    });
  };

  return (
    <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
      <div className="w-full sm:w-80">
        <Input
          type="text"
          placeholder="Search by name, username, or email..."
          value={filters.search || ''}
          onChange={handleSearchChange}
          leftIcon={<Search className="w-4 h-4" />}
        />
      </div>

      <div className="flex items-center gap-2.5 w-full sm:w-auto">
        <div className="w-full sm:w-44">
          <Select
            value={filters.role || ''}
            onChange={handleRoleChange}
            leftIcon={<Filter className="w-3.5 h-3.5" />}
            options={[
              { value: '', label: 'All Roles' },
              { value: 'TENANT_ADMIN', label: 'Tenant Admin' },
              { value: 'DEVELOPER', label: 'Developer' },
              { value: 'VIEWER', label: 'Viewer' },
            ]}
          />
        </div>

        <div className="w-full sm:w-40">
          <Select
            value={filters.status || ''}
            onChange={handleStatusChange}
            options={[
              { value: '', label: 'All Statuses' },
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' },
              { value: 'PENDING', label: 'Pending' },
            ]}
          />
        </div>
      </div>
    </div>
  );
};

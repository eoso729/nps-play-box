import { useQuery } from '@tanstack/react-query';
import { settingsApi } from '../api/settings.api';

export const useTenantSettings = () => {
  return useQuery({
    queryKey: ['tenant-settings'],
    queryFn: () => settingsApi.getCurrentTenant(),
    staleTime: 60000,
  });
};

import { useQuery } from '@tanstack/react-query';
import { platformTenantsApi } from '../api/platform-tenants.api';
import { platformSeatRequestsApi } from '../api/platform-seat-requests.api';
import { impersonationApi } from '../../../api/impersonation';
import {
  PlatformMetricsData,
  SeatUtilizationPoint,
  TenantGrowthPoint,
} from '../types/platform-admin.types';

export const usePlatformMetrics = () => {
  return useQuery({
    queryKey: ['platform-metrics'],
    queryFn: async () => {
      const [tenantsPage, seatRequests, pendingImpersonations] = await Promise.all([
        platformTenantsApi.getTenants({ page: 0, size: 500 }),
        platformSeatRequestsApi.getAllPendingSeatRequests().catch(() => []),
        impersonationApi.getPendingSessions().catch(() => ({ data: { content: [] } })),
      ]);

      const tenants = tenantsPage.content || [];

      const totalTenants = tenantsPage.totalElements ?? tenants.length;
      const activeTenants = tenants.filter((t) => t.status === 'ACTIVE').length;
      const suspendedTenants = tenants.filter((t) => t.status === 'SUSPENDED').length;
      const trialTenants = tenants.filter((t) => t.status === 'TRIAL').length;

      const totalSeatsAllocated = tenants.reduce((acc, t) => acc + (t.maxSeats || 0), 0);
      const totalSeatsUsed = tenants.reduce((acc, t) => acc + (t.usedSeats || 0), 0);
      const averageUtilization =
        totalSeatsAllocated > 0 ? (totalSeatsUsed / totalSeatsAllocated) * 100 : 0;

      const pendingSessions = (pendingImpersonations.data?.content || []) as any[];
      const activeImpersonations = pendingSessions.filter((s: any) => s.status === 'ACTIVE').length;

      const metrics: PlatformMetricsData = {
        totalTenants,
        activeTenants,
        suspendedTenants,
        trialTenants,
        totalSeatsAllocated,
        totalSeatsUsed,
        averageUtilization,
        activeImpersonations,
        pendingSeatRequests: seatRequests.length,
      };

      // Derive tenant growth points sorted chronologically
      const sortedByDate = [...tenants].sort(
        (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );

      const growthMap = new Map<string, number>();
      sortedByDate.forEach((t) => {
        try {
          const dateKey = new Date(t.createdAt).toISOString().split('T')[0];
          growthMap.set(dateKey, (growthMap.get(dateKey) || 0) + 1);
        } catch {
          // ignore date parse errors
        }
      });

      let cumulative = 0;
      const growthPoints: TenantGrowthPoint[] = Array.from(growthMap.entries()).map(
        ([date, count]) => {
          cumulative += count;
          return {
            date,
            newTenants: count,
            totalTenants: cumulative,
          };
        }
      );

      // If fewer than 2 points, generate fallback baseline
      if (growthPoints.length === 0) {
        growthPoints.push({
          date: new Date().toISOString().split('T')[0],
          newTenants: totalTenants,
          totalTenants,
        });
      }

      // Top tenants by seat quota percentage
      const utilizationPoints: SeatUtilizationPoint[] = tenants
        .map((t) => {
          const quota = t.maxSeats || 1;
          const used = t.usedSeats || 0;
          return {
            tenantId: t.id,
            tenantName: t.name,
            quota,
            used,
            percentage: Math.min(100, Math.round((used / quota) * 100)),
          };
        })
        .sort((a, b) => b.percentage - a.percentage)
        .slice(0, 8);

      return {
        metrics,
        growthPoints,
        utilizationPoints,
        tenants,
      };
    },
    staleTime: 30000,
    refetchInterval: 60000,
  });
};

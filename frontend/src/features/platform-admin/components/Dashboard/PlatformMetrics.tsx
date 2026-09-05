import React from 'react';
import { Building2, Users, PieChart, Clock, KeyRound } from 'lucide-react';
import { usePlatformMetrics } from '../../hooks/usePlatformMetrics';
import { ProgressBar } from '../../../shared/components/ProgressBar';

export const PlatformMetrics: React.FC = () => {
  const { data, isLoading, error } = usePlatformMetrics();

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="bg-white rounded-xl border border-gray-200/80 p-5 shadow-sm animate-pulse">
            <div className="flex items-center justify-between mb-3">
              <div className="h-4 bg-gray-200 rounded w-24"></div>
              <div className="w-9 h-9 bg-gray-200 rounded-lg"></div>
            </div>
            <div className="h-8 bg-gray-200 rounded w-16 mb-2"></div>
            <div className="h-3 bg-gray-200 rounded w-32"></div>
          </div>
        ))}
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-red-700 text-sm">
        Failed to load platform metrics. Please try refreshing the dashboard.
      </div>
    );
  }

  const { metrics } = data;

  const cards = [
    {
      title: 'Total Tenants',
      value: metrics.totalTenants.toLocaleString(),
      subValue: `${metrics.activeTenants} active · ${metrics.trialTenants} trial`,
      icon: Building2,
      iconBg: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      badge: `${metrics.suspendedTenants} suspended`,
      badgeColor: metrics.suspendedTenants > 0 ? 'text-amber-700 bg-amber-50' : 'text-gray-500 bg-gray-50',
    },
    {
      title: 'Seats Allocated',
      value: metrics.totalSeatsAllocated.toLocaleString(),
      subValue: `${metrics.totalSeatsUsed.toLocaleString()} seats currently claimed`,
      icon: Users,
      iconBg: 'bg-blue-50 text-blue-700 border-blue-200',
      progress: metrics.averageUtilization,
    },
    {
      title: 'System Seat Utilization',
      value: `${metrics.averageUtilization.toFixed(1)}%`,
      subValue: `${(metrics.totalSeatsAllocated - metrics.totalSeatsUsed).toLocaleString()} remaining seats available`,
      icon: PieChart,
      iconBg: 'bg-indigo-50 text-indigo-700 border-indigo-200',
      progress: metrics.averageUtilization,
    },
    {
      title: 'Pending Seat Requests',
      value: metrics.pendingSeatRequests.toString(),
      subValue: metrics.pendingSeatRequests > 0 ? 'Requires admin review' : 'All requests addressed',
      icon: Clock,
      iconBg: metrics.pendingSeatRequests > 0 ? 'bg-amber-50 text-amber-700 border-amber-200' : 'bg-gray-50 text-gray-700 border-gray-200',
      highlight: metrics.pendingSeatRequests > 0,
    },
    {
      title: 'Active Impersonations',
      value: metrics.activeImpersonations.toString(),
      subValue: metrics.activeImpersonations > 0 ? 'Supervised support sessions' : 'No active sessions',
      icon: KeyRound,
      iconBg: metrics.activeImpersonations > 0 ? 'bg-purple-50 text-purple-700 border-purple-200' : 'bg-gray-50 text-gray-700 border-gray-200',
      highlight: metrics.activeImpersonations > 0,
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div
            key={card.title}
            className={`bg-white rounded-xl border p-5 shadow-sm flex flex-col justify-between transition-all duration-200 hover:shadow-md ${
              card.highlight ? 'border-amber-300 ring-1 ring-amber-200/50' : 'border-gray-200/80'
            }`}
          >
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                  {card.title}
                </span>
                <div className={`w-9 h-9 rounded-lg border flex items-center justify-center ${card.iconBg}`}>
                  <Icon className="w-5 h-5" />
                </div>
              </div>
              <div className="text-2xl font-bold text-gray-900 mb-1">{card.value}</div>
              <p className="text-xs text-gray-500">{card.subValue}</p>
            </div>

            {typeof card.progress === 'number' && (
              <div className="mt-3 pt-3 border-t border-gray-100">
                <ProgressBar value={card.progress} size="sm" />
              </div>
            )}

            {card.badge && (
              <div className="mt-3 pt-3 border-t border-gray-100 flex items-center justify-between">
                <span className={`text-[11px] font-medium px-2 py-0.5 rounded-full border border-gray-200/60 ${card.badgeColor}`}>
                  {card.badge}
                </span>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};

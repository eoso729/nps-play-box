import React from 'react';
import { BarChart3, AlertCircle } from 'lucide-react';
import { usePlatformMetrics } from '../../hooks/usePlatformMetrics';

export const SeatUtilizationChart: React.FC = () => {
  const { data, isLoading } = usePlatformMetrics();

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-10 bg-gray-100 rounded-lg"></div>
          ))}
        </div>
      </div>
    );
  }

  const utilizationPoints = data?.utilizationPoints || [];

  const getBarColor = (percentage: number) => {
    if (percentage >= 90) return 'bg-rose-500';
    if (percentage >= 80) return 'bg-amber-500';
    return 'bg-emerald-500';
  };

  const getBadgeClass = (percentage: number) => {
    if (percentage >= 90) return 'text-rose-700 bg-rose-50 border-rose-200/60';
    if (percentage >= 80) return 'text-amber-700 bg-amber-50 border-amber-200/60';
    return 'text-emerald-700 bg-emerald-50 border-emerald-200/60';
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-4">
          <div>
            <div className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-indigo-600" />
              <h3 className="text-base font-semibold text-gray-900">Seat Utilization by Tenant</h3>
            </div>
            <p className="text-xs text-gray-500 mt-0.5">
              Top enterprise tenants ranked by quota saturation
            </p>
          </div>
        </div>

        {utilizationPoints.length === 0 ? (
          <div className="py-12 text-center text-gray-500 text-sm">
            <AlertCircle className="w-8 h-8 mx-auto text-gray-400 mb-2" />
            No tenant seat data available.
          </div>
        ) : (
          <div className="space-y-3.5">
            {utilizationPoints.map((item) => (
              <div key={item.tenantId} className="group">
                <div className="flex items-center justify-between text-xs mb-1.5">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-900 group-hover:text-emerald-700 transition-colors">
                      {item.tenantName}
                    </span>
                    <span className="text-[11px] text-gray-500">
                      ({item.used} / {item.quota} seats)
                    </span>
                  </div>
                  <span
                    className={`text-[11px] font-bold px-1.5 py-0.5 rounded border ${getBadgeClass(
                      item.percentage
                    )}`}
                  >
                    {item.percentage}%
                  </span>
                </div>

                <div className="w-full bg-gray-100 rounded-full h-2.5 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ease-out ${getBarColor(
                      item.percentage
                    )}`}
                    style={{ width: `${Math.min(100, item.percentage)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Threshold Legend */}
      <div className="flex items-center justify-center gap-6 mt-6 pt-4 border-t border-gray-100 text-xs text-gray-600">
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
          <span>&lt; 80% Normal</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
          <span>80% - 90% Warning</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-rose-500"></span>
          <span>&ge; 90% Critical</span>
        </div>
      </div>
    </div>
  );
};

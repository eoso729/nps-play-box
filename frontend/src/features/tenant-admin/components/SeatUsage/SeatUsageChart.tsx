import React from 'react';

interface SeatUsageChartProps {
  percentage: number;
  usedSeats: number;
  maxSeats: number;
  className?: string;
}

export const SeatUsageChart: React.FC<SeatUsageChartProps> = ({
  percentage,
  usedSeats,
  maxSeats,
  className = '',
}) => {
  const clamped = Math.min(100, Math.max(0, percentage));

  const getColor = () => {
    if (clamped >= 95) return 'bg-red-600';
    if (clamped >= 80) return 'bg-amber-500';
    return 'bg-[#16a34a]';
  };

  const getTextColor = () => {
    if (clamped >= 95) return 'text-red-600';
    if (clamped >= 80) return 'text-amber-600';
    return 'text-[#16a34a]';
  };

  return (
    <div className={`w-full ${className}`}>
      <div className="flex items-baseline justify-between mb-2">
        <div className="flex items-center gap-2">
          <span className="text-2xl font-extrabold text-gray-900 tracking-tight">
            {usedSeats}{' '}
            <span className="text-sm font-semibold text-gray-500">/ {maxSeats} seats</span>
          </span>
        </div>
        <span className={`text-base font-bold ${getTextColor()}`}>{clamped.toFixed(1)}%</span>
      </div>

      <div className="w-full h-3 bg-gray-100 rounded-full overflow-hidden p-0.5 border border-gray-200/60">
        <div
          className={`h-full rounded-full transition-all duration-500 ease-out ${getColor()}`}
          style={{ width: `${clamped}%` }}
          role="progressbar"
          aria-valuenow={clamped}
          aria-valuemin={0}
          aria-valuemax={100}
        />
      </div>
    </div>
  );
};

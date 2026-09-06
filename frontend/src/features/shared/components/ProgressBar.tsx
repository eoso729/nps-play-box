import React from 'react';

export interface ProgressBarProps {
  value: number; // percentage 0 - 100
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
  customColor?: string;
  className?: string;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({
  value,
  size = 'md',
  showLabel = false,
  customColor,
  className = '',
}) => {
  const clampedValue = Math.min(100, Math.max(0, value));

  const getColorClass = () => {
    if (customColor) return customColor;
    if (clampedValue >= 95) return 'bg-red-600';
    if (clampedValue >= 80) return 'bg-amber-500';
    return 'bg-[#16a34a]';
  };

  const heightStyles = {
    sm: 'h-1.5',
    md: 'h-2.5',
    lg: 'h-4',
  };

  return (
    <div className={`w-full ${className}`}>
      {showLabel && (
        <div className="flex justify-between items-center text-xs font-semibold text-gray-700 mb-1.5">
          <span>Utilization</span>
          <span
            className={
              clampedValue >= 95
                ? 'text-red-600 font-bold'
                : clampedValue >= 80
                ? 'text-amber-600 font-bold'
                : 'text-gray-900 font-bold'
            }
          >
            {clampedValue.toFixed(1)}%
          </span>
        </div>
      )}
      <div className={`w-full bg-gray-200/80 rounded-full overflow-hidden ${heightStyles[size]}`}>
        <div
          className={`h-full rounded-full transition-all duration-500 ease-out ${getColorClass()}`}
          style={{ width: `${clampedValue}%` }}
          role="progressbar"
          aria-valuenow={clampedValue}
          aria-valuemin={0}
          aria-valuemax={100}
        />
      </div>
    </div>
  );
};

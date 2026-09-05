import React, { useState, useMemo } from 'react';
import { TrendingUp, Calendar } from 'lucide-react';
import { usePlatformMetrics } from '../../hooks/usePlatformMetrics';

export const TenantGrowthChart: React.FC = () => {
  const { data, isLoading } = usePlatformMetrics();
  const [rangeDays, setRangeDays] = useState<number>(30); // 7, 30, 90, 0 (all)
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  const filteredPoints = useMemo(() => {
    if (!data?.growthPoints || data.growthPoints.length === 0) return [];
    if (rangeDays === 0) return data.growthPoints;

    const cutoff = new Date();
    cutoff.setDate(cutoff.getDate() - rangeDays);
    const cutoffStr = cutoff.toISOString().split('T')[0];

    const points = data.growthPoints.filter((p) => p.date >= cutoffStr);
    return points.length > 0 ? points : data.growthPoints;
  }, [data?.growthPoints, rangeDays]);

  // SVG Chart Geometry
  const width = 640;
  const height = 220;
  const padding = { top: 20, right: 30, bottom: 35, left: 45 };
  const chartWidth = width - padding.left - padding.right;
  const chartHeight = height - padding.top - padding.bottom;

  const { pointsCoords, pathD, areaD, yTicks, maxY } = useMemo(() => {
    if (filteredPoints.length === 0) {
      return { pointsCoords: [], pathD: '', areaD: '', yTicks: [0, 5, 10], maxY: 10 };
    }

    const maxVal = Math.max(...filteredPoints.map((p) => p.totalTenants), 5);
    // Round maxVal up to a nice number
    const ceilingY = Math.ceil(maxVal / 5) * 5 || 5;

    const coords = filteredPoints.map((point, index) => {
      const x =
        filteredPoints.length === 1
          ? padding.left + chartWidth / 2
          : padding.left + (index / (filteredPoints.length - 1)) * chartWidth;
      const y = padding.top + chartHeight - (point.totalTenants / ceilingY) * chartHeight;
      return { x, y, point };
    });

    const pathString = coords.reduce(
      (acc, c, idx) => (idx === 0 ? `M ${c.x} ${c.y}` : `${acc} L ${c.x} ${c.y}`),
      ''
    );

    const firstX = coords[0]?.x ?? padding.left;
    const lastX = coords[coords.length - 1]?.x ?? padding.left + chartWidth;
    const baselineY = padding.top + chartHeight;
    const areaString = `${pathString} L ${lastX} ${baselineY} L ${firstX} ${baselineY} Z`;

    const ticks = [0, Math.round(ceilingY / 2), ceilingY];

    return { pointsCoords: coords, pathD: pathString, areaD: areaString, yTicks: ticks, maxY: ceilingY };
  }, [filteredPoints, chartWidth, chartHeight, padding.left, padding.top]);

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="h-56 bg-gray-100 rounded-lg"></div>
      </div>
    );
  }

  const latestCount = filteredPoints[filteredPoints.length - 1]?.totalTenants || 0;
  const firstCount = filteredPoints[0]?.totalTenants || 0;
  const netGrowth = latestCount - firstCount;

  return (
    <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm flex flex-col justify-between">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
        <div>
          <div className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-emerald-600" />
            <h3 className="text-base font-semibold text-gray-900">Tenant Growth Timeline</h3>
          </div>
          <p className="text-xs text-gray-500 mt-0.5">
            Cumulative enterprise tenant onboarding over time
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-gray-400" />
          <select
            value={rangeDays}
            onChange={(e) => setRangeDays(Number(e.target.value))}
            className="text-xs font-medium border border-gray-300 rounded-lg px-2.5 py-1.5 bg-white text-gray-700 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
          >
            <option value={7}>Last 7 days</option>
            <option value={30}>Last 30 days</option>
            <option value={90}>Last 90 days</option>
            <option value={0}>All time</option>
          </select>
        </div>
      </div>

      <div className="flex items-baseline gap-3 mb-2">
        <span className="text-2xl font-bold text-gray-900">{latestCount}</span>
        <span className="text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200/60">
          +{netGrowth} in selected range
        </span>
      </div>

      <div className="relative w-full overflow-hidden">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto overflow-visible select-none"
        >
          <defs>
            <linearGradient id="growthGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.35" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Grid lines */}
          {yTicks.map((tick) => {
            const y = padding.top + chartHeight - (tick / maxY) * chartHeight;
            return (
              <g key={tick}>
                <line
                  x1={padding.left}
                  y1={y}
                  x2={padding.left + chartWidth}
                  y2={y}
                  stroke="#f1f5f9"
                  strokeWidth="1"
                  strokeDasharray="4 4"
                />
                <text
                  x={padding.left - 8}
                  y={y + 3}
                  textAnchor="end"
                  className="text-[10px] fill-gray-400"
                >
                  {tick}
                </text>
              </g>
            );
          })}

          {/* Area fill */}
          {areaD && <path d={areaD} fill="url(#growthGradient)" />}

          {/* Polyline */}
          {pathD && (
            <path
              d={pathD}
              fill="none"
              stroke="#059669"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          )}

          {/* Data Points */}
          {pointsCoords.map((pt, idx) => (
            <g key={idx}>
              <circle
                cx={pt.x}
                cy={pt.y}
                r={hoveredIndex === idx ? 5 : 3.5}
                fill="#ffffff"
                stroke="#059669"
                strokeWidth={hoveredIndex === idx ? 2.5 : 2}
                className="cursor-pointer transition-all duration-150"
                onMouseEnter={() => setHoveredIndex(idx)}
                onMouseLeave={() => setHoveredIndex(null)}
              />
            </g>
          ))}

          {/* Bottom X-axis labels (first and last date) */}
          {filteredPoints.length > 0 && (
            <>
              <text
                x={padding.left}
                y={height - 10}
                textAnchor="start"
                className="text-[10px] fill-gray-400"
              >
                {filteredPoints[0].date}
              </text>
              <text
                x={padding.left + chartWidth}
                y={height - 10}
                textAnchor="end"
                className="text-[10px] fill-gray-400"
              >
                {filteredPoints[filteredPoints.length - 1].date}
              </text>
            </>
          )}
        </svg>

        {/* Floating Tooltip */}
        {hoveredIndex !== null && pointsCoords[hoveredIndex] && (
          <div
            className="absolute z-10 -translate-x-1/2 -translate-y-full pointer-events-none bg-gray-900 text-white text-[11px] rounded px-2.5 py-1 shadow-lg"
            style={{
              left: `${(pointsCoords[hoveredIndex].x / width) * 100}%`,
              top: `${(pointsCoords[hoveredIndex].y / height) * 100 - 4}%`,
            }}
          >
            <div className="font-semibold">{pointsCoords[hoveredIndex].point.date}</div>
            <div className="text-gray-300">
              Total: <span className="text-emerald-400 font-bold">{pointsCoords[hoveredIndex].point.totalTenants}</span> tenants
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

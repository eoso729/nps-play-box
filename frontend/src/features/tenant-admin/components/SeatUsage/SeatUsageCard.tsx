import React, { useState } from 'react';
import { useQuotaStatus } from '../../hooks/useSeatUsage';
import { SeatUsageChart } from './SeatUsageChart';
import { SeatWarningBanner } from './SeatWarningBanner';
import { SeatRequestModal } from './SeatRequestModal';
import { Button } from '../../../shared/components/Button';
import { Users, Mail, UserCheck, ShieldAlert, PlusCircle } from 'lucide-react';

export const SeatUsageCard: React.FC = () => {
  const [requestModalOpen, setRequestModalOpen] = useState(false);
  const { data: quota, isLoading, error } = useQuotaStatus();

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl border border-[#e4e9e6] p-6 shadow-sm animate-pulse space-y-4">
        <div className="flex justify-between items-center">
          <div className="h-5 bg-gray-200 rounded w-1/4" />
          <div className="h-8 bg-gray-200 rounded w-24" />
        </div>
        <div className="h-10 bg-gray-200 rounded" />
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="h-16 bg-gray-100 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  if (error || !quota) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-2xl p-5 text-red-800 text-xs flex items-center justify-between">
        <div className="flex items-center gap-2 font-medium">
          <ShieldAlert className="w-4 h-4 text-red-600" />
          Failed to load organization seat quota information.
        </div>
        <Button size="sm" variant="outline" onClick={() => window.location.reload()}>
          Retry
        </Button>
      </div>
    );
  }

  const percentage = quota.utilizationPercentage ?? 0;
  const maxSeats = quota.maxSeats ?? 0;
  const usedSeats = quota.usedSeats ?? 0;
  const availableSeats = quota.availableSeats ?? Math.max(0, maxSeats - usedSeats);

  return (
    <div className="bg-white rounded-2xl border border-[#e4e9e6] shadow-sm overflow-hidden">
      {/* Warning Banner at >=80% or critical */}
      <SeatWarningBanner
        percentage={percentage}
        availableSeats={availableSeats}
        totalSeats={maxSeats}
        onRequestSeats={() => setRequestModalOpen(true)}
      />

      <div className="p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-gray-900 leading-tight">
                Seat Capacity & Utilization
              </h3>
              {quota.subscriptionTier && (
                <span className="text-[11px] font-semibold bg-[#edf2ee] text-[#15803d] px-2.5 py-0.5 rounded-full border border-[#d5e2d8]">
                  {quota.subscriptionTier} Tier
                </span>
              )}
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Active team members and reserved invitation slots contributing to subscription quota.
            </p>
          </div>

          <Button
            size="sm"
            variant="outline"
            icon={<PlusCircle className="w-4 h-4 text-[#16a34a]" />}
            onClick={() => setRequestModalOpen(true)}
          >
            Request More Seats
          </Button>
        </div>

        {/* Visual Progress Chart */}
        <div className="mb-6 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl p-4">
          <SeatUsageChart
            percentage={percentage}
            usedSeats={usedSeats}
            maxSeats={maxSeats}
          />
        </div>

        {/* Metric Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl p-3.5">
            <div className="flex items-center gap-1.5 text-xs text-gray-600 mb-1">
              <Users className="w-3.5 h-3.5 text-[#16a34a]" />
              <span>Available Seats</span>
            </div>
            <div
              className={`text-xl font-extrabold ${
                availableSeats <= 0
                  ? 'text-red-600'
                  : availableSeats <= 2
                  ? 'text-amber-600'
                  : 'text-gray-900'
              }`}
            >
              {availableSeats}
            </div>
          </div>

          <div className="bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl p-3.5">
            <div className="flex items-center gap-1.5 text-xs text-gray-600 mb-1">
              <UserCheck className="w-3.5 h-3.5 text-blue-600" />
              <span>Active Users</span>
            </div>
            <div className="text-xl font-extrabold text-gray-900">{quota.activeUsers}</div>
          </div>

          <div className="bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl p-3.5">
            <div className="flex items-center gap-1.5 text-xs text-gray-600 mb-1">
              <Mail className="w-3.5 h-3.5 text-purple-600" />
              <span>Pending Invites</span>
            </div>
            <div className="text-xl font-extrabold text-gray-900">{quota.pendingInvitations}</div>
          </div>

          <div className="bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl p-3.5">
            <div className="flex items-center gap-1.5 text-xs text-gray-600 mb-1">
              <Users className="w-3.5 h-3.5 text-gray-500" />
              <span>Total Plan Quota</span>
            </div>
            <div className="text-xl font-extrabold text-gray-900">{maxSeats}</div>
          </div>
        </div>
      </div>

      <SeatRequestModal
        isOpen={requestModalOpen}
        onClose={() => setRequestModalOpen(false)}
        currentTotal={maxSeats}
      />
    </div>
  );
};

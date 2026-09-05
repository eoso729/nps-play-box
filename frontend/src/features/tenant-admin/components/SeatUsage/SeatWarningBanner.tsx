import React from 'react';
import { AlertTriangle, AlertOctagon, PlusCircle } from 'lucide-react';
import { Button } from '../../../shared/components/Button';

interface SeatWarningBannerProps {
  percentage: number;
  availableSeats: number;
  totalSeats: number;
  onRequestSeats?: () => void;
}

export const SeatWarningBanner: React.FC<SeatWarningBannerProps> = ({
  percentage,
  availableSeats,
  totalSeats,
  onRequestSeats,
}) => {
  if (percentage < 80) return null;

  const isCritical = percentage >= 95 || availableSeats <= 0;

  return (
    <div
      role="alert"
      className={`px-5 py-4 rounded-xl border flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all ${
        isCritical
          ? 'bg-red-50/90 border-red-200 text-red-950'
          : 'bg-amber-50/90 border-amber-200 text-amber-950'
      }`}
    >
      <div className="flex items-start gap-3">
        {isCritical ? (
          <AlertOctagon className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
        ) : (
          <AlertTriangle className="w-5 h-5 text-amber-600 mt-0.5 flex-shrink-0" />
        )}
        <div>
          <h4 className="text-sm font-bold leading-tight">
            {isCritical ? 'Seat Quota Critical - Invitations Restricted' : 'Seat Quota Warning'}
          </h4>
          <p className="text-xs mt-1 leading-relaxed opacity-90">
            {isCritical
              ? `You have ${availableSeats} of ${totalSeats} seat(s) remaining (${percentage.toFixed(
                  1
                )}% allocated). You cannot invite new members until seats are upgraded.`
              : `Your organization has utilized ${percentage.toFixed(
                  1
                )}% of allocated seats (${availableSeats} seat(s) remaining). Consider requesting additional seats.`}
          </p>
        </div>
      </div>

      {onRequestSeats && (
        <div className="flex-shrink-0 self-start sm:self-center">
          <Button
            size="sm"
            variant={isCritical ? 'danger' : 'secondary'}
            icon={<PlusCircle className="w-3.5 h-3.5" />}
            onClick={onRequestSeats}
          >
            Request Seats
          </Button>
        </div>
      )}
    </div>
  );
};

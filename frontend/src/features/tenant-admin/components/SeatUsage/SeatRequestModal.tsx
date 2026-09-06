import React, { useState } from 'react';
import { Modal } from '../../../shared/components/Modal';
import { Input } from '../../../shared/components/Input';
import { Button } from '../../../shared/components/Button';
import { useRequestAdditionalSeats } from '../../hooks/useSeatUsage';
import { Users, Send } from 'lucide-react';

interface SeatRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentTotal: number;
}

export const SeatRequestModal: React.FC<SeatRequestModalProps> = ({
  isOpen,
  onClose,
  currentTotal,
}) => {
  const [additionalSeats, setAdditionalSeats] = useState<number>(5);
  const [justification, setJustification] = useState<string>('');
  const [expectedGrowth, setExpectedGrowth] = useState<string>('');
  const [contactEmail, setContactEmail] = useState<string>('');
  const [formError, setFormError] = useState<string | null>(null);

  const requestSeatsMutation = useRequestAdditionalSeats();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (additionalSeats < 1) {
      setFormError('Please request at least 1 additional seat.');
      return;
    }
    if (!justification.trim()) {
      setFormError('Business justification is required for seat expansion requests.');
      return;
    }

    setFormError(null);
    requestSeatsMutation.mutate(
      {
        additionalSeats,
        justification: justification.trim(),
        expectedGrowth: expectedGrowth.trim() || undefined,
        contactEmail: contactEmail.trim() || undefined,
      },
      {
        onSuccess: () => {
          onClose();
          setJustification('');
          setExpectedGrowth('');
          setContactEmail('');
          setAdditionalSeats(5);
        },
      }
    );
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Request Additional Seats"
      description="Submit a seat quota increase request for approval by system administrators."
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {formError && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 font-medium">
            {formError}
          </div>
        )}

        <div className="bg-[#f6f9f7] border border-[#e4e9e6] rounded-xl p-3.5 flex items-center justify-between text-xs text-gray-700">
          <span>Current Organization Capacity:</span>
          <span className="font-bold text-[#0f3a22] text-sm">{currentTotal} seats</span>
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Number of Additional Seats
          </label>
          <Input
            type="number"
            min={1}
            max={500}
            value={additionalSeats}
            onChange={(e) => setAdditionalSeats(parseInt(e.target.value, 10) || 1)}
            leftIcon={<Users className="w-4 h-4" />}
            helperText={`New total requested capacity will be ${currentTotal + (additionalSeats || 0)} seats.`}
            required
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Business Justification <span className="text-red-500">*</span>
          </label>
          <textarea
            rows={3}
            value={justification}
            onChange={(e) => setJustification(e.target.value)}
            placeholder="Explain why your team requires additional seats (e.g., onboarding 5 new backend integration engineers)..."
            className="block w-full rounded-lg border border-[#e4e9e6] p-3 text-sm transition-all placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:border-[#16a34a] focus:ring-[#16a34a]/20"
            required
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Expected Growth / Timeline (Optional)
          </label>
          <Input
            type="text"
            value={expectedGrowth}
            onChange={(e) => setExpectedGrowth(e.target.value)}
            placeholder="e.g., Q3 Payment Pipeline Rollout"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Notification Contact Email (Optional)
          </label>
          <Input
            type="email"
            value={contactEmail}
            onChange={(e) => setContactEmail(e.target.value)}
            placeholder="billing@organization.com"
          />
        </div>

        <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-[#e4e9e6]">
          <Button type="button" variant="outline" size="md" onClick={onClose}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            size="md"
            isLoading={requestSeatsMutation.isPending}
            icon={<Send className="w-4 h-4" />}
          >
            Submit Request
          </Button>
        </div>
      </form>
    </Modal>
  );
};

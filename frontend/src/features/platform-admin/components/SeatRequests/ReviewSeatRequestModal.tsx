import React, { useState } from 'react';
import { CheckCircle2, XCircle, Building2 } from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import {
  useApproveSeatRequest,
  useDenySeatRequest,
} from '../../hooks/usePlatformSeatRequests';
import { PlatformSeatRequest } from '../../types/platform-admin.types';

interface ReviewSeatRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  request: PlatformSeatRequest | null;
}

export const ReviewSeatRequestModal: React.FC<ReviewSeatRequestModalProps> = ({
  isOpen,
  onClose,
  request,
}) => {
  const approve = useApproveSeatRequest();
  const deny = useDenySeatRequest();

  const [mode, setMode] = useState<'decision' | 'deny'>('decision');
  const [reviewNote, setReviewNote] = useState('');
  const [denialReason, setDenialReason] = useState('');
  const [denyError, setDenyError] = useState<string | null>(null);

  if (!request) return null;

  const handleApprove = async () => {
    try {
      await approve.mutateAsync({
        requestId: request.id,
        reviewNote: reviewNote.trim() || undefined,
      });
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  const handleDenySubmit = async () => {
    if (!denialReason.trim() || denialReason.trim().length < 5) {
      setDenyError('Please provide a reason for denying the request (min 5 characters)');
      return;
    }

    try {
      await deny.mutateAsync({
        requestId: request.id,
        denialReason: denialReason.trim(),
      });
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Review Seat Expansion Request"
      description={`Evaluate commercial seat quota request for Tenant #${request.tenantId}`}
      maxWidth="md"
    >
      <div className="p-5 space-y-4">
        {/* Request Details Box */}
        <div className="bg-gray-50 border border-gray-200/80 rounded-xl p-4 space-y-3 text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Building2 className="w-4 h-4 text-gray-500" />
              <span className="font-bold text-gray-900 text-sm">
                {request.tenantName || `Tenant Organization #${request.tenantId}`}
              </span>
            </div>
            <span className="font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200">
              +{request.requestedSeats} seats requested
            </span>
          </div>

          <div className="grid grid-cols-2 gap-2 text-gray-600 pt-2 border-t border-gray-200/60">
            <div>
              <span className="text-gray-400 block text-[10px] uppercase">Requested By</span>
              <span className="font-medium text-gray-800">
                {request.requestedByUserName || `User #${request.requestedByUserId || 'Admin'}`}
              </span>
              {request.contactEmail && (
                <span className="block text-gray-500 text-[11px]">{request.contactEmail}</span>
              )}
            </div>
            <div>
              <span className="text-gray-400 block text-[10px] uppercase">Submitted On</span>
              <span className="font-medium text-gray-800">
                {new Date(request.createdAt).toLocaleString()}
              </span>
            </div>
          </div>

          <div>
            <span className="text-gray-400 block text-[10px] uppercase mb-1">
              Business Justification
            </span>
            <p className="italic text-gray-800 bg-white p-2.5 rounded border border-gray-200">
              "{request.justification}"
            </p>
          </div>

          {request.expectedGrowth && (
            <div>
              <span className="text-gray-400 block text-[10px] uppercase mb-0.5">
                Expected Team Growth
              </span>
              <p className="text-gray-700">{request.expectedGrowth}</p>
            </div>
          )}
        </div>

        {/* Decision Mode */}
        {mode === 'decision' ? (
          <div className="space-y-3">
            <div>
              <label htmlFor="review-notes" className="block text-xs font-semibold text-gray-700 mb-1">
                Optional Admin Note / Reference
              </label>
              <input
                id="review-notes"
                type="text"
                placeholder="e.g. Approved under annual contract expansion"
                value={reviewNote}
                onChange={(e) => setReviewNote(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
              />
            </div>

            <div className="flex items-center justify-between gap-3 pt-3 border-t border-gray-100">
              <Button
                type="button"
                variant="outline"
                onClick={() => setMode('deny')}
                className="text-rose-700 border-rose-200 hover:bg-rose-50"
              >
                <XCircle className="w-4 h-4 mr-1.5" />
                Deny Request
              </Button>

              <div className="flex items-center gap-2">
                <Button variant="ghost" onClick={onClose}>
                  Cancel
                </Button>
                <Button
                  variant="primary"
                  onClick={handleApprove}
                  isLoading={approve.isPending}
                  className="bg-emerald-600 hover:bg-emerald-700 text-white"
                >
                  <CheckCircle2 className="w-4 h-4 mr-1.5" />
                  Approve Expansion
                </Button>
              </div>
            </div>
          </div>
        ) : (
          /* Deny Form */
          <div className="space-y-3">
            <div className="bg-rose-50 border border-rose-200 rounded-lg p-3 text-xs text-rose-900">
              <p className="font-semibold mb-1">Denial Justification Required</p>
              <p className="text-[11px] text-rose-700">
                Please provide feedback for the tenant administrator explaining why this request cannot
                be fulfilled at this time.
              </p>
            </div>

            <div>
              <label htmlFor="denial-reason" className="block text-xs font-semibold text-gray-700 mb-1">
                Reason for Denial *
              </label>
              <textarea
                id="denial-reason"
                rows={3}
                value={denialReason}
                onChange={(e) => {
                  setDenialReason(e.target.value);
                  setDenyError(null);
                }}
                placeholder="e.g. Current contract tier does not allow further expansion without account review..."
                className="w-full px-3 py-2 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
              />
              {denyError && <p className="text-xs text-rose-600 mt-1">{denyError}</p>}
            </div>

            <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
              <Button
                variant="ghost"
                onClick={() => {
                  setMode('decision');
                  setDenyError(null);
                }}
              >
                Back
              </Button>
              <Button
                variant="danger"
                onClick={handleDenySubmit}
                isLoading={deny.isPending}
              >
                Confirm Denial
              </Button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};

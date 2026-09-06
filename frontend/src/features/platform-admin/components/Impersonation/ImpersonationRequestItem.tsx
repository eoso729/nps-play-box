import React, { useState } from 'react';
import {
  KeyRound,
  ShieldCheck,
  XCircle,
  Play,
  StopCircle,
} from 'lucide-react';
import { ImpersonationSessionResponse } from '../../../../api/impersonation';
import {
  useApproveImpersonation,
  useRejectImpersonation,
  useStartImpersonation,
  useTerminateImpersonation,
} from '../../hooks/usePlatformImpersonation';
import { Badge } from '../../../shared/components/Badge';
import { Button } from '../../../shared/components/Button';

interface ImpersonationRequestItemProps {
  session: ImpersonationSessionResponse;
}

export const ImpersonationRequestItem: React.FC<ImpersonationRequestItemProps> = ({
  session,
}) => {
  const approve = useApproveImpersonation();
  const reject = useRejectImpersonation();
  const startSession = useStartImpersonation();
  const terminateSession = useTerminateImpersonation();

  const [isApproving, setIsApproving] = useState(false);
  const [isRejecting, setIsRejecting] = useState(false);
  const [reasonInput, setReasonInput] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="warning" size="sm">Pending Approval</Badge>;
      case 'APPROVED':
        return <Badge variant="success" size="sm">Approved</Badge>;
      case 'ACTIVE':
        return <Badge variant="purple" size="sm">Active Session</Badge>;
      case 'REJECTED':
        return <Badge variant="danger" size="sm">Rejected</Badge>;
      case 'EXPIRED':
        return <Badge variant="neutral" size="sm">Expired</Badge>;
      case 'TERMINATED':
        return <Badge variant="neutral" size="sm">Terminated</Badge>;
      default:
        return <Badge variant="neutral" size="sm">{status}</Badge>;
    }
  };

  const handleApproveSubmit = async () => {
    if (!reasonInput.trim() || reasonInput.trim().length < 5) {
      setActionError('Approval justification must be at least 5 characters');
      return;
    }
    try {
      await approve.mutateAsync({
        sessionUuid: session.sessionUuid,
        approvalReason: reasonInput.trim(),
      });
      setIsApproving(false);
      setReasonInput('');
      setActionError(null);
    } catch {
      // Handled by mutation toast
    }
  };

  const handleRejectSubmit = async () => {
    if (!reasonInput.trim() || reasonInput.trim().length < 5) {
      setActionError('Rejection reason must be at least 5 characters');
      return;
    }
    try {
      await reject.mutateAsync({
        sessionUuid: session.sessionUuid,
        rejectionReason: reasonInput.trim(),
      });
      setIsRejecting(false);
      setReasonInput('');
      setActionError(null);
    } catch {
      // Handled by mutation toast
    }
  };

  const handleStart = async () => {
    try {
      await startSession.mutateAsync(session.sessionUuid);
    } catch {
      // Handled by mutation toast
    }
  };

  const handleTerminate = async () => {
    try {
      await terminateSession.mutateAsync({
        sessionUuid: session.sessionUuid,
        reason: 'Manually terminated by platform administrator',
      });
    } catch {
      // Handled by mutation toast
    }
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200/80 p-5 shadow-sm space-y-3 transition-all hover:border-gray-300">
      {/* Header Info */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-gray-100 pb-3">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-purple-50 border border-purple-100 flex items-center justify-center text-purple-700">
            <KeyRound className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-sm text-gray-900">
                {session.targetTenantName || `Tenant #${session.targetTenantId}`}
              </span>
              {getStatusBadge(session.status)}
            </div>
            <p className="text-xs text-gray-500 font-mono mt-0.5">
              Target: {session.targetUserEmail || `User #${session.targetUserId}`}
            </p>
          </div>
        </div>

        <div className="text-right text-xs text-gray-500">
          <div>Requested by: <span className="font-medium text-gray-700">{session.supportUserEmail}</span></div>
          <div className="text-[11px] text-gray-400 mt-0.5">
            {new Date(session.createdAt).toLocaleString()} · {session.maxDurationMinutes}m max
          </div>
        </div>
      </div>

      {/* Justification Details */}
      <div className="bg-gray-50 rounded-lg p-3 text-xs text-gray-700">
        <span className="font-semibold text-gray-500 uppercase tracking-wider text-[10px] block mb-1">
          Business Justification
        </span>
        <p className="italic text-gray-800">"{session.reason}"</p>
      </div>

      {/* Approval / Rejection details if present */}
      {session.approvalReason && (
        <div className="bg-emerald-50/70 border border-emerald-100 rounded-lg p-3 text-xs text-emerald-900">
          <span className="font-semibold text-emerald-800 text-[10px] uppercase tracking-wider block mb-0.5">
            Approved by {session.approvedByEmail || 'Secondary Admin'}
          </span>
          <p>{session.approvalReason}</p>
        </div>
      )}

      {/* Inline Approve/Reject Input Form */}
      {(isApproving || isRejecting) && (
        <div className="p-3.5 bg-gray-50 border border-gray-200 rounded-lg space-y-2 text-xs">
          <label className="block font-semibold text-gray-700">
            {isApproving ? 'Enter Approval Justification:' : 'Enter Rejection Reason:'}
          </label>
          <input
            type="text"
            placeholder={isApproving ? 'e.g. Verified customer support ticket #1234' : 'e.g. Insufficient justification provided'}
            value={reasonInput}
            onChange={(e) => {
              setReasonInput(e.target.value);
              setActionError(null);
            }}
            className="w-full px-3 py-1.5 text-xs border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-purple-500/20"
          />
          {actionError && <p className="text-[11px] text-rose-600">{actionError}</p>}
          <div className="flex justify-end gap-2 pt-1">
            <Button
              size="sm"
              variant="ghost"
              onClick={() => {
                setIsApproving(false);
                setIsRejecting(false);
                setReasonInput('');
                setActionError(null);
              }}
            >
              Cancel
            </Button>
            {isApproving && (
              <Button
                size="sm"
                variant="primary"
                onClick={handleApproveSubmit}
                isLoading={approve.isPending}
                className="bg-emerald-600 hover:bg-emerald-700"
              >
                Confirm Approval
              </Button>
            )}
            {isRejecting && (
              <Button
                size="sm"
                variant="danger"
                onClick={handleRejectSubmit}
                isLoading={reject.isPending}
              >
                Confirm Rejection
              </Button>
            )}
          </div>
        </div>
      )}

      {/* Action Buttons */}
      {!isApproving && !isRejecting && (
        <div className="flex items-center justify-end gap-2 pt-1">
          {session.status === 'PENDING' && (
            <>
              <Button
                size="sm"
                variant="outline"
                onClick={() => {
                  setIsRejecting(true);
                  setIsApproving(false);
                }}
                className="text-rose-700 border-rose-200 hover:bg-rose-50"
              >
                <XCircle className="w-3.5 h-3.5 mr-1.5" />
                Reject
              </Button>
              <Button
                size="sm"
                variant="primary"
                onClick={() => {
                  setIsApproving(true);
                  setIsRejecting(false);
                }}
                className="bg-emerald-600 hover:bg-emerald-700"
              >
                <ShieldCheck className="w-3.5 h-3.5 mr-1.5" />
                Approve (Dual Auth)
              </Button>
            </>
          )}

          {session.status === 'APPROVED' && (
            <Button
              size="sm"
              variant="primary"
              onClick={handleStart}
              isLoading={startSession.isPending}
              className="bg-purple-600 hover:bg-purple-700"
            >
              <Play className="w-3.5 h-3.5 mr-1.5" />
              Start Impersonation Session
            </Button>
          )}

          {session.status === 'ACTIVE' && (
            <Button
              size="sm"
              variant="danger"
              onClick={handleTerminate}
              isLoading={terminateSession.isPending}
            >
              <StopCircle className="w-3.5 h-3.5 mr-1.5" />
              Terminate Active Session
            </Button>
          )}
        </div>
      )}
    </div>
  );
};

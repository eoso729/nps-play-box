import React, { useState } from 'react';
import { Layers, CheckCircle2, User, Building2, AlertCircle } from 'lucide-react';
import { usePlatformSeatRequests } from '../../hooks/usePlatformSeatRequests';
import { PlatformSeatRequest } from '../../types/platform-admin.types';
import { ReviewSeatRequestModal } from './ReviewSeatRequestModal';
import { Badge } from '../../../shared/components/Badge';
import { Button } from '../../../shared/components/Button';

export const PendingSeatRequestsList: React.FC = () => {
  const { data: requests, isLoading, error } = usePlatformSeatRequests();
  const [selectedRequest, setSelectedRequest] = useState<PlatformSeatRequest | null>(null);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="warning" size="sm">Pending Review</Badge>;
      case 'APPROVED':
        return <Badge variant="success" size="sm">Approved</Badge>;
      case 'DENIED':
        return <Badge variant="danger" size="sm">Denied</Badge>;
      default:
        return <Badge variant="neutral" size="sm">{status}</Badge>;
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <Layers className="w-5 h-5 text-indigo-600" />
            Tenant Seat Expansion Requests
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Incoming commercial license expansion requests awaiting platform administrator review
          </p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="flex items-center gap-4 animate-pulse">
                <div className="w-10 h-10 rounded-xl bg-gray-200"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                  <div className="h-3 bg-gray-100 rounded w-1/3"></div>
                </div>
                <div className="h-8 bg-gray-200 rounded w-20"></div>
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="p-8 text-center text-rose-700 text-sm">
            <AlertCircle className="w-8 h-8 mx-auto text-rose-500 mb-2" />
            Failed to load seat expansion requests.
          </div>
        ) : !requests || requests.length === 0 ? (
          <div className="py-16 text-center text-gray-500 text-sm">
            <CheckCircle2 className="w-10 h-10 mx-auto text-emerald-500 mb-2" />
            <p className="font-semibold text-gray-700">All Seat Requests Reviewed</p>
            <p className="text-xs text-gray-400 mt-1">
              There are no pending seat expansion requests across any enterprise tenants
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200/80 text-[11px] font-semibold uppercase tracking-wider text-gray-500">
                  <th className="px-5 py-3.5">Tenant</th>
                  <th className="px-5 py-3.5">Requested Addition</th>
                  <th className="px-5 py-3.5">Justification</th>
                  <th className="px-5 py-3.5">Requested By</th>
                  <th className="px-5 py-3.5">Submitted</th>
                  <th className="px-5 py-3.5">Status</th>
                  <th className="px-5 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {requests.map((req) => (
                  <tr key={req.id} className="hover:bg-gray-50/80 transition-colors">
                    <td className="px-5 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2.5">
                        <Building2 className="w-4 h-4 text-gray-400" />
                        <span className="font-semibold text-sm text-gray-900">
                          {req.tenantName || `Tenant #${req.tenantId}`}
                        </span>
                      </div>
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap">
                      <span className="font-bold text-xs text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
                        +{req.requestedSeats} seats
                      </span>
                    </td>

                    <td className="px-5 py-4 max-w-xs">
                      <p className="text-xs text-gray-700 line-clamp-2 italic" title={req.justification}>
                        "{req.justification}"
                      </p>
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5 text-gray-400" />
                        <span>{req.requestedByUserName || `User #${req.requestedByUserId || 'Admin'}`}</span>
                      </div>
                      {req.contactEmail && (
                        <span className="text-[11px] text-gray-400 block">{req.contactEmail}</span>
                      )}
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-500">
                      {new Date(req.createdAt).toLocaleDateString()}
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap">
                      {getStatusBadge(req.status)}
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap text-right">
                      {req.status === 'PENDING' ? (
                        <Button
                          size="sm"
                          variant="primary"
                          onClick={() => setSelectedRequest(req)}
                          className="bg-emerald-600 hover:bg-emerald-700 text-white"
                        >
                          Review Request
                        </Button>
                      ) : (
                        <span className="text-xs text-gray-400 font-medium">Completed</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ReviewSeatRequestModal
        isOpen={Boolean(selectedRequest)}
        onClose={() => setSelectedRequest(null)}
        request={selectedRequest}
      />
    </div>
  );
};

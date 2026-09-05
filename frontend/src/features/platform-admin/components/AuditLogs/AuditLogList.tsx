import React, { useState } from 'react';
import { ChevronLeft, ChevronRight, FileText, ChevronDown, ChevronUp } from 'lucide-react';
import { PlatformAuditItem } from '../../types/platform-admin.types';
import { Badge } from '../../../shared/components/Badge';

interface AuditLogListProps {
  logs: PlatformAuditItem[];
  total: number;
  currentPage: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export const AuditLogList: React.FC<AuditLogListProps> = ({
  logs,
  total,
  currentPage,
  pageSize,
  onPageChange,
}) => {
  const [expandedId, setExpandedId] = useState<string | number | null>(null);
  const totalPages = Math.ceil(total / pageSize) || 1;

  const getStatusBadge = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'SUCCESS':
        return <Badge variant="success" size="sm">SUCCESS</Badge>;
      case 'FAILURE':
      case 'ERROR':
        return <Badge variant="danger" size="sm">FAILURE</Badge>;
      case 'WARNING':
        return <Badge variant="warning" size="sm">WARNING</Badge>;
      default:
        return <Badge variant="neutral" size="sm">{status || 'INFO'}</Badge>;
    }
  };

  const toggleExpand = (id: string | number) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50/80 border-b border-gray-200/80 text-[11px] font-semibold uppercase tracking-wider text-gray-500">
              <th className="px-5 py-3.5">Timestamp</th>
              <th className="px-5 py-3.5">Event / Action</th>
              <th className="px-5 py-3.5">Actor</th>
              <th className="px-5 py-3.5">Tenant Scope</th>
              <th className="px-5 py-3.5">Status</th>
              <th className="px-5 py-3.5">IP Address</th>
              <th className="px-5 py-3.5 text-right">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-xs">
            {logs.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-12 text-center text-gray-400">
                  <FileText className="w-8 h-8 mx-auto text-gray-300 mb-2" />
                  No audit trail records matched the filter criteria
                </td>
              </tr>
            ) : (
              logs.map((log) => {
                const isExpanded = expandedId === (log.id || log.eventId);
                return (
                  <React.Fragment key={log.id || log.eventId}>
                    <tr
                      onClick={() => toggleExpand(log.id || log.eventId)}
                      className="hover:bg-gray-50/80 transition-colors cursor-pointer"
                    >
                      <td className="px-5 py-3.5 whitespace-nowrap text-gray-600 font-mono text-[11px]">
                        {new Date(log.createdAt).toLocaleString()}
                      </td>

                      <td className="px-5 py-3.5">
                        <span className="font-semibold text-gray-900 block">
                          {log.eventType?.replace(/_/g, ' ')}
                        </span>
                        {log.action && log.action !== log.eventType && (
                          <span className="text-[11px] text-gray-500">{log.action}</span>
                        )}
                      </td>

                      <td className="px-5 py-3.5 whitespace-nowrap">
                        <span className="font-medium text-gray-900">
                          {log.actorUsername || log.actorId || 'System'}
                        </span>
                        {log.actorRole && (
                          <span className="block text-[11px] text-gray-500 font-mono">
                            {log.actorRole}
                          </span>
                        )}
                      </td>

                      <td className="px-5 py-3.5 whitespace-nowrap text-gray-600">
                        {log.tenantName || (log.tenantId ? `Tenant #${log.tenantId}` : 'System-wide')}
                      </td>

                      <td className="px-5 py-3.5 whitespace-nowrap">
                        {getStatusBadge(log.status)}
                      </td>

                      <td className="px-5 py-3.5 whitespace-nowrap text-gray-500 font-mono text-[11px]">
                        {log.ipAddress || '—'}
                      </td>

                      <td className="px-5 py-3.5 whitespace-nowrap text-right">
                        <button
                          type="button"
                          className="text-gray-400 hover:text-gray-600 p-1"
                          aria-label={isExpanded ? 'Collapse' : 'Expand'}
                        >
                          {isExpanded ? (
                            <ChevronUp className="w-4 h-4" />
                          ) : (
                            <ChevronDown className="w-4 h-4" />
                          )}
                        </button>
                      </td>
                    </tr>

                    {/* Expandable row */}
                    {isExpanded && (
                      <tr className="bg-gray-50/70 border-b border-gray-100">
                        <td colSpan={7} className="px-6 py-3 text-xs text-gray-700">
                          <div className="bg-white border border-gray-200 rounded-lg p-3 space-y-1 font-mono text-[11px]">
                            <div>
                              <span className="text-gray-400 select-none">Event ID: </span>
                              <span className="text-gray-800">{log.eventId}</span>
                            </div>
                            {log.resourceType && (
                              <div>
                                <span className="text-gray-400 select-none">Resource: </span>
                                <span className="text-gray-800">
                                  {log.resourceType}
                                  {log.resourceId ? ` (ID: ${log.resourceId})` : ''}
                                </span>
                              </div>
                            )}
                            {log.details && (
                              <div>
                                <span className="text-gray-400 select-none">Details / Payload: </span>
                                <span className="text-gray-800 break-all">{log.details}</span>
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="px-5 py-3.5 border-t border-gray-100 flex items-center justify-between text-xs text-gray-600 bg-gray-50/40">
          <div>
            Showing <span className="font-semibold">{currentPage * pageSize + 1}</span> to{' '}
            <span className="font-semibold">{Math.min((currentPage + 1) * pageSize, total)}</span> of{' '}
            <span className="font-semibold">{total}</span> audit records
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              title="Previous Page"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-medium text-gray-700">
              Page {currentPage + 1} of {totalPages}
            </span>
            <button
              type="button"
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
              className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              title="Next Page"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

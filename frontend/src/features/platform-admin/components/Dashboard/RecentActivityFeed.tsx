import React from 'react';
import {
  Activity,
  UserPlus,
  Building2,
  KeyRound,
  ShieldCheck,
  Clock,
  Layers,
} from 'lucide-react';
import { usePlatformAuditLogs } from '../../hooks/usePlatformAudit';
import { Badge } from '../../../shared/components/Badge';

export const RecentActivityFeed: React.FC = () => {
  const { data, isLoading } = usePlatformAuditLogs({ page: 0, size: 8 });

  const getEventIcon = (eventType: string) => {
    const type = eventType.toUpperCase();
    if (type.includes('USER')) {
      return <UserPlus className="w-4 h-4 text-emerald-600" />;
    }
    if (type.includes('TENANT')) {
      return <Building2 className="w-4 h-4 text-blue-600" />;
    }
    if (type.includes('IMPERSONAT')) {
      return <KeyRound className="w-4 h-4 text-purple-600" />;
    }
    if (type.includes('QUOTA') || type.includes('SEAT')) {
      return <Layers className="w-4 h-4 text-amber-600" />;
    }
    if (type.includes('SECURITY') || type.includes('AUTH')) {
      return <ShieldCheck className="w-4 h-4 text-indigo-600" />;
    }
    return <Activity className="w-4 h-4 text-gray-500" />;
  };

  const getStatusBadge = (status: string) => {
    const s = status?.toUpperCase();
    if (s === 'SUCCESS') return <Badge variant="success" size="sm">Success</Badge>;
    if (s === 'FAILURE' || s === 'ERROR') return <Badge variant="danger" size="sm">Failed</Badge>;
    if (s === 'WARNING') return <Badge variant="warning" size="sm">Warning</Badge>;
    return <Badge variant="neutral" size="sm">{status}</Badge>;
  };

  const formatTimestamp = (dateStr: string) => {
    try {
      const d = new Date(dateStr);
      return d.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gray-200"></div>
              <div className="flex-1 space-y-1.5">
                <div className="h-3.5 bg-gray-200 rounded w-3/4"></div>
                <div className="h-2.5 bg-gray-100 rounded w-1/2"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const logs = data?.content || [];

  return (
    <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-sm">
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-emerald-600" />
          <h3 className="text-base font-semibold text-gray-900">Recent Platform Activity</h3>
        </div>
        <span className="text-xs text-gray-500">Live operational events</span>
      </div>

      {logs.length === 0 ? (
        <div className="py-8 text-center text-gray-500 text-sm">
          <Clock className="w-8 h-8 mx-auto text-gray-400 mb-2" />
          No recent platform administrative events recorded.
        </div>
      ) : (
        <div className="divide-y divide-gray-100">
          {logs.map((log) => (
            <div key={log.id || log.eventId} className="py-3 flex items-start gap-3 first:pt-0 last:pb-0">
              <div className="p-2 rounded-lg bg-gray-50 border border-gray-100 flex-shrink-0 mt-0.5">
                {getEventIcon(log.eventType)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-xs font-semibold text-gray-900 truncate">
                    <span className="text-emerald-800">{log.actorUsername || 'System'}</span>
                    {' · '}
                    <span className="font-medium text-gray-700">{log.action || log.eventType}</span>
                  </p>
                  {getStatusBadge(log.status)}
                </div>
                <p className="text-[11px] text-gray-500 mt-0.5 truncate">
                  {log.tenantName ? `Tenant: ${log.tenantName}` : 'System-level'}
                  {log.details ? ` · ${log.details}` : ''}
                </p>
                <span className="text-[10px] text-gray-400 mt-0.5 block">
                  {formatTimestamp(log.createdAt)}
                  {log.ipAddress ? ` · ${log.ipAddress}` : ''}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

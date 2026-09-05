import React, { useState, useCallback, useMemo } from 'react';
import { ServicePushResult } from '../../../types/workbench';

interface ResponsePaneProps {
  serviceResponse: ServicePushResult | null;
  isLoading: boolean;
}

export const ResponsePane: React.FC<ResponsePaneProps> = ({ serviceResponse, isLoading }) => {
  const [activeTab, setActiveTab] = useState<'body' | 'headers'>('body');
  const [copied, setCopied] = useState(false);

  const statusCode = serviceResponse?.statusCode;
  const isSuccess = serviceResponse?.success;
  const latency = serviceResponse?.executionTimeMs;
  const timestamp = serviceResponse?.timestamp;
  const body = serviceResponse?.rawResponseBody ?? '';

  const statusChipStyle = !serviceResponse
    ? { bg: '#f3f4f6', color: '#6b7280', text: 'Awaiting send' }
    : isSuccess
    ? { bg: '#e6f6ec', color: '#15803d', text: `${statusCode} OK` }
    : { bg: '#fee2e2', color: '#dc2626', text: `${statusCode} Error` };

  const handleCopy = useCallback(() => {
    if (body) {
      navigator.clipboard.writeText(body).then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1200);
      });
    }
  }, [body]);

  const handleExport = useCallback(() => {
    if (!body) return;
    const blob = new Blob([body], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'nps-gateway-response.xml';
    a.click();
    URL.revokeObjectURL(url);
  }, [body]);

  const formatTimestamp = (ts?: string) => {
    if (!ts) return '--';
    try {
      return new Date(ts).toLocaleString('en-NG', { timeZone: 'Africa/Lagos', hour12: false }) + ' WAT';
    } catch {
      return ts;
    }
  };

  const bodyLines = body ? body.split('\n') : [];

  const requestId = useMemo(() => {
    return serviceResponse ? `req-${(timestamp ? new Date(timestamp).getTime() : Date.now()).toString(36)}` : '--';
  }, [serviceResponse, timestamp]);

  return (
    <div className="flex flex-col min-w-0 overflow-hidden" style={{ flex: 1 }}>
      {/* Pane Header */}
      <div className="px-3.5 py-3 border-b border-[#e4e9e6] bg-white flex-shrink-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-[12px] font-bold text-[#111827]">
            <span
              className="w-[17px] h-[17px] rounded-full flex items-center justify-center text-white flex-shrink-0"
              style={{ background: '#16a34a', fontSize: 9.5, fontWeight: 700 }}
            >
              3
            </span>
            Gateway Service Response
          </div>
          <span
            className="text-[10px] font-bold px-2 py-0.5 rounded-[5px]"
            style={{ background: statusChipStyle.bg, color: statusChipStyle.color }}
          >
            {statusChipStyle.text}
          </span>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-2.5 p-3 flex-shrink-0 bg-white border-b border-[#e4e9e6]">
        <StatCard label="HTTP STATUS" value={isLoading ? '...' : statusCode ? `${statusCode}` : '--'} highlight={isSuccess} />
        <StatCard label="LATENCY" value={isLoading ? '...' : latency != null ? `${latency}ms` : '--'} highlight={isSuccess} />
        <StatCard label="TIMESTAMP" value={isLoading ? '...' : formatTimestamp(timestamp)} plain />
        <StatCard
          label="REQUEST ID"
          value={isLoading ? '...' : requestId}
          plain
          mono
        />
      </div>

      {/* Tabs */}
      <div className="flex gap-4 px-3 border-b border-[#e4e9e6] bg-white flex-shrink-0">
        <button
          type="button"
          onClick={() => setActiveTab('body')}
          className={`py-2 text-[12px] font-semibold border-b-2 transition-colors cursor-pointer bg-transparent ${
            activeTab === 'body'
              ? 'text-[#15803d] border-[#16a34a]'
              : 'text-[#6b7280] border-transparent hover:text-[#111827]'
          }`}
        >
          Response Body
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('headers')}
          className={`py-2 text-[12px] font-semibold border-b-2 transition-colors cursor-pointer bg-transparent ${
            activeTab === 'headers'
              ? 'text-[#15803d] border-[#16a34a]'
              : 'text-[#6b7280] border-transparent hover:text-[#111827]'
          }`}
        >
          Headers
        </button>
      </div>

      {/* Code Body */}
      <div
        className="flex-1 overflow-auto"
        style={{
          background: '#0b1a12',
          fontFamily: "'JetBrains Mono', 'Courier New', monospace",
          fontSize: 11,
          lineHeight: 1.65,
          padding: '10px 0',
        }}
      >
        {isLoading && (
          <div className="px-4 py-6 space-y-2">
            {[45, 78, 62, 85, 53, 70, 90, 48, 65, 80].map((width, i) => (
              <div
                key={i}
                className="h-3 rounded animate-pulse"
                style={{ background: 'rgba(255,255,255,0.06)', width: `${width}%` }}
              />
            ))}
          </div>
        )}

        {!isLoading && !serviceResponse && (
          <div className="flex flex-col items-center justify-center h-full px-4 py-10 text-center">
            <p style={{ color: '#3f6a52', fontSize: 12 }}>
              Use &ldquo;Execute Request Pipeline&rdquo; to send and view the gateway response here.
            </p>
          </div>
        )}

        {!isLoading && serviceResponse && activeTab === 'body' && bodyLines.map((line, i) => (
          <div
            key={i}
            className="flex"
            style={{ padding: '0 12px' }}
            onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.03)'; }}
            onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
          >
            <span className="select-none mr-3 text-right" style={{ width: 22, flexShrink: 0, color: '#3f6a52' }}>
              {i + 1}
            </span>
            <span style={{ color: '#cfe8db' }}>{line}</span>
          </div>
        ))}

        {!isLoading && serviceResponse && activeTab === 'headers' && (
          <div className="px-4 py-4 space-y-1.5">
            <HeaderRow label="Content-Type" value="application/xml; charset=UTF-8" />
            <HeaderRow label="X-Request-ID" value={requestId} />
            <HeaderRow label="X-Response-Time" value={latency != null ? `${latency}ms` : '--'} />
            <HeaderRow label="Server" value="NIBSS-Gateway/2.4" />
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex gap-2 px-3 py-2.5 border-t border-[#e4e9e6] bg-white flex-shrink-0">
        <button
          type="button"
          onClick={handleCopy}
          className="flex items-center gap-1.5 border border-[#e4e9e6] bg-white text-[#111827] px-2.5 py-1.5 rounded-[6px] text-[11px] font-semibold cursor-pointer hover:border-[#22a05a] hover:text-[#15803d] transition-colors"
        >
          {copied ? 'Copied' : 'Copy'}
        </button>
        <button
          type="button"
          onClick={handleExport}
          className="flex items-center gap-1.5 border border-[#e4e9e6] bg-white text-[#111827] px-2.5 py-1.5 rounded-[6px] text-[11px] font-semibold cursor-pointer hover:border-[#22a05a] hover:text-[#15803d] transition-colors"
        >
          Export Response
        </button>
      </div>
    </div>
  );
};

const StatCard: React.FC<{
  label: string;
  value: string;
  highlight?: boolean | null;
  plain?: boolean;
  mono?: boolean;
}> = ({ label, value, highlight, plain, mono }) => (
  <div className="border border-[#e4e9e6] rounded-lg px-3 py-2.5 bg-white">
    <div className="text-[10px] text-[#6b7280] font-semibold mb-1">{label}</div>
    <div
      className={`font-bold ${plain ? 'text-[13px] text-[#111827]' : 'text-[16px]'}`}
      style={{
        color: plain ? '#111827' : highlight == null ? '#111827' : highlight ? '#15803d' : '#dc2626',
        fontFamily: mono ? "'JetBrains Mono', monospace" : undefined,
        fontSize: mono ? 11 : undefined,
        wordBreak: 'break-all',
      }}
    >
      {value}
    </div>
  </div>
);

const HeaderRow: React.FC<{ label: string; value: string }> = ({ label, value }) => (
  <div className="flex gap-3 text-[11px]" style={{ fontFamily: "'JetBrains Mono', monospace" }}>
    <span style={{ color: '#8fd4b8', minWidth: 140 }}>{label}:</span>
    <span style={{ color: '#cfe8db' }}>{value}</span>
  </div>
);

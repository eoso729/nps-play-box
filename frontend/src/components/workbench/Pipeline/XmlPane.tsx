import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

interface XmlPaneProps {
  title: string;
  stageNum: number;
  stageColor?: string;
  statusText: string;
  statusVariant: 'gen' | 'signed' | 'ok' | 'error' | 'idle';
  xml: string | null;
  isLoading: boolean;
  footer?: React.ReactNode;
}



function renderXmlLine(line: string, lineNum: number) {
  // Simple colorization by pattern matching
  const renderContent = () => {
    if (line.includes('<?xml')) {
      return <span style={{ color: '#9aa5a0' }}>{line}</span>;
    }
    // Replace tags with colored spans
    const parts: React.ReactNode[] = [];
    const tagRegex = /(<\/?)([\w:.-]+)((?:\s+[\w:.-]+(?:=(?:"[^"]*"|'[^']*'|[^\s>]*))?)*)(\/?>)/g;
    let last = 0;
    let m: RegExpExecArray | null;

    while ((m = tagRegex.exec(line)) !== null) {
      if (m.index > last) {
        const txt = line.slice(last, m.index);
        parts.push(<span key={`txt-${last}`} style={{ color: '#cfe8db' }}>{txt}</span>);
      }
      parts.push(<span key={`lt-${m.index}`} style={{ color: '#5fe0a0' }}>{m[1]}</span>);
      parts.push(<span key={`tn-${m.index}`} style={{ color: '#5fe0a0' }}>{m[2]}</span>);
      if (m[3]) {
        // Attributes
        const attrStr = m[3];
        const attrParts = attrStr.split(/(\s+[\w:.-]+=(?:"[^"]*"|'[^']*'|[^\s>]*))/g);
        attrParts.forEach((ap, i) => {
          if (ap.includes('=')) {
            const [attrName, ...attrValParts] = ap.split('=');
            const attrVal = attrValParts.join('=');
            parts.push(<span key={`an-${m!.index}-${i}`} style={{ color: '#8fd4b8' }}>{attrName}</span>);
            parts.push(<span key={`eq-${m!.index}-${i}`} style={{ color: '#9aa5a0' }}>=</span>);
            parts.push(<span key={`av-${m!.index}-${i}`} style={{ color: '#e7c26a' }}>{attrVal}</span>);
          } else {
            parts.push(<span key={`ap-${m!.index}-${i}`} style={{ color: '#8fd4b8' }}>{ap}</span>);
          }
        });
      }
      parts.push(<span key={`gt-${m.index}`} style={{ color: '#5fe0a0' }}>{m[4]}</span>);
      last = m.index + m[0].length;
    }
    if (last < line.length) {
      parts.push(<span key={`tail-${last}`} style={{ color: '#cfe8db' }}>{line.slice(last)}</span>);
    }
    return parts.length > 0 ? parts : <span style={{ color: '#cfe8db' }}>{line}</span>;
  };

  return (
    <div
      key={lineNum}
      className="flex"
      style={{ padding: '0 12px' }}
      onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.03)'; }}
      onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
    >
      <span className="select-none mr-3 text-right" style={{ width: 22, flexShrink: 0, color: '#3f6a52', userSelect: 'none' }}>
        {lineNum}
      </span>
      <span style={{ whiteSpace: 'pre' }}>{renderContent()}</span>
    </div>
  );
}

const STATUS_CHIP_STYLES: Record<string, { bg: string; color: string; text: string }> = {
  gen: { bg: '#e6f6ec', color: '#15803d', text: 'Generated' },
  signed: { bg: '#eef0fe', color: '#6366f1', text: 'Signed' },
  ok: { bg: '#e6f6ec', color: '#15803d', text: '200 OK' },
  error: { bg: '#fee2e2', color: '#dc2626', text: 'Error' },
  idle: { bg: '#f3f4f6', color: '#6b7280', text: 'Awaiting input' },
};

export const XmlPane: React.FC<XmlPaneProps> = ({
  title,
  stageNum,
  stageColor = '#16a34a',
  statusText,
  statusVariant,
  xml,
  isLoading,
  footer,
}) => {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const chip = STATUS_CHIP_STYLES[statusVariant] || STATUS_CHIP_STYLES.idle;
  const displayStatusText = statusText || chip.text;

  const handleCopy = useCallback(() => {
    if (xml) {
      navigator.clipboard.writeText(xml).then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1200);
      });
    }
  }, [xml]);

  const handleExport = useCallback(() => {
    if (!xml) return;
    const blob = new Blob([xml], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `nps-${title.toLowerCase().replace(/\s+/g, '-')}.xml`;
    a.click();
    URL.revokeObjectURL(url);
  }, [xml, title]);

  const handleHealthCheck = useCallback(() => {
    if (xml) {
      localStorage.setItem('nps_inspector_xml', xml);
      localStorage.setItem('nps_inspector_title', title);
      navigate('/inspector', { state: { xml, title } });
    }
  }, [xml, title, navigate]);

  const handleCompare = useCallback(() => {
    if (xml) {
      localStorage.setItem('nps_diff_source_xml', xml);
      localStorage.setItem('nps_diff_source_title', title);
      navigate('/diff', { state: { xml, title } });
    }
  }, [xml, title, navigate]);

  const lines = xml ? xml.split('\n') : [];

  return (
    <div className="flex flex-col border-r border-[#e4e9e6] min-w-0 overflow-hidden" style={{ flex: 1 }}>
      {/* Pane Header */}
      <div className="px-3.5 py-3 border-b border-[#e4e9e6] bg-white flex-shrink-0">
        <div className="flex items-center justify-between mb-0.5">
          <div className="flex items-center gap-1.5 text-[12px] font-bold text-[#111827]">
            <span
              className="w-[17px] h-[17px] rounded-full flex items-center justify-center text-white flex-shrink-0"
              style={{ background: stageColor, fontSize: 9.5, fontWeight: 700 }}
            >
              {stageNum}
            </span>
            {title}
          </div>
          <span
            className="text-[10px] font-bold px-2 py-0.5 rounded-[5px]"
            style={{ background: chip.bg, color: chip.color }}
          >
            {displayStatusText}
          </span>
        </div>
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
            {Array.from({ length: 12 }).map((_, i) => (
              <div
                key={i}
                className="h-3 rounded animate-pulse"
                style={{
                  background: 'rgba(255,255,255,0.06)',
                  width: `${40 + Math.random() * 50}%`,
                }}
              />
            ))}
          </div>
        )}

        {!isLoading && !xml && (
          <div className="flex flex-col items-center justify-center h-full px-4 py-10 text-center">
            <div className="w-8 h-8 rounded-lg mb-3 flex items-center justify-center" style={{ background: 'rgba(255,255,255,0.06)' }}>
              <span style={{ color: '#3f6a52', fontSize: 16 }}>&#60;/&#62;</span>
            </div>
            <p style={{ color: '#3f6a52', fontSize: 12 }}>Fill the form and run a pipeline to see output here.</p>
          </div>
        )}

        {!isLoading && xml && lines.map((line, i) => renderXmlLine(line, i + 1))}
      </div>

      {/* Footer Slot (for signature box etc.) */}
      {footer}

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
          Export XML
        </button>
        {xml && (
          <>
            <button
              type="button"
              onClick={handleHealthCheck}
              className="flex items-center gap-1.5 border border-[#c4ebd3] bg-[#e6f6ec] text-[#15803d] px-2.5 py-1.5 rounded-[6px] text-[11px] font-bold cursor-pointer hover:bg-[#d2efe0] transition-colors"
            >
              <span>🔍</span> Health Check
            </button>
            <button
              type="button"
              onClick={handleCompare}
              className="flex items-center gap-1.5 border border-[#e4e9e6] bg-white text-[#111827] px-2.5 py-1.5 rounded-[6px] text-[11px] font-semibold cursor-pointer hover:border-[#22a05a] hover:text-[#15803d] transition-colors"
            >
              Compare XML
            </button>
          </>
        )}
      </div>
    </div>
  );
};

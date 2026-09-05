import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor, { Monaco } from '@monaco-editor/react';

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

const SKELETON_WIDTHS = [45, 78, 62, 85, 53, 70, 90, 48, 65, 80, 58, 72];

const STATUS_CHIP_STYLES: Record<string, { bg: string; color: string; text: string }> = {
  gen: { bg: '#e6f6ec', color: '#15803d', text: 'Generated' },
  signed: { bg: '#eef0fe', color: '#6366f1', text: 'Signed' },
  ok: { bg: '#e6f6ec', color: '#15803d', text: '200 OK' },
  error: { bg: '#fee2e2', color: '#dc2626', text: 'Error' },
  idle: { bg: '#f3f4f6', color: '#6b7280', text: 'Awaiting input' },
};

const handleBeforeMount = (monaco: Monaco) => {
  monaco.editor.defineTheme('nps-emerald-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'tag', foreground: '5fe0a0' },
      { token: 'tag.xml', foreground: '5fe0a0' },
      { token: 'attribute.name', foreground: '8fd4b8' },
      { token: 'attribute.value', foreground: 'e7c26a' },
      { token: 'string', foreground: 'e7c26a' },
      { token: 'comment', foreground: '4f7a60', fontStyle: 'italic' },
      { token: 'delimiter', foreground: '5fe0a0' },
    ],
    colors: {
      'editor.background': '#0b1a12',
      'editor.foreground': '#cfe8db',
      'editorLineNumber.foreground': '#3f6a52',
      'editorLineNumber.activeForeground': '#8fd4b8',
      'editor.lineHighlightBackground': '#0f2419',
      'editorCursor.foreground': '#22c55e',
    },
  });
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
      <div className="flex-1 min-h-0 relative bg-[#0b1a12] overflow-hidden">
        {isLoading && (
          <div className="px-4 py-6 space-y-2">
            {SKELETON_WIDTHS.map((width, i) => (
              <div
                key={i}
                className="h-3 rounded animate-pulse"
                style={{
                  background: 'rgba(255,255,255,0.06)',
                  width: `${width}%`,
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

        {!isLoading && xml && (
          <Editor
            height="100%"
            language="xml"
            theme="nps-emerald-dark"
            value={xml}
            beforeMount={handleBeforeMount}
            options={{
              readOnly: true,
              domReadOnly: true,
              fontSize: 11,
              lineHeight: 20,
              fontFamily: "'JetBrains Mono', 'Courier New', monospace",
              lineNumbers: 'on',
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              wordWrap: 'on',
              automaticLayout: true,
              folding: true,
              renderLineHighlight: 'all',
              scrollbar: {
                vertical: 'visible',
                horizontal: 'auto',
                verticalScrollbarSize: 8,
                horizontalScrollbarSize: 8,
              },
            }}
            loading={
              <div className="px-4 py-6 space-y-2">
                {SKELETON_WIDTHS.map((width, i) => (
                  <div
                    key={i}
                    className="h-3 rounded animate-pulse"
                    style={{
                      background: 'rgba(255,255,255,0.06)',
                      width: `${width}%`,
                    }}
                  />
                ))}
              </div>
            }
          />
        )}
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

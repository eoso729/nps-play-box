import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { diffLines, Change } from 'diff';
import { useAuth } from '../../context/AuthContext';
import { canonicalizeXml } from '../../utils/xmlCanonicalizer';

export interface DiffLine {
  type: 'added' | 'removed' | 'unchanged';
  value: string;
  lineNumber?: number;
}

// Line-by-line Diff algorithm using Myers Diff algorithm (from 'diff')
export function computeMyersDiff(
  original: string,
  modified: string,
  canonicalize: boolean = false
): { left: DiffLine[]; right: DiffLine[] } {
  const text1 = canonicalize ? canonicalizeXml(original) : original;
  const text2 = canonicalize ? canonicalizeXml(modified) : modified;

  const changes: Change[] = diffLines(text1, text2);
  const left: DiffLine[] = [];
  const right: DiffLine[] = [];

  let origLineNum = 1;
  let modLineNum = 1;

  for (let i = 0; i < changes.length; i++) {
    const change = changes[i];
    const lines = change.value.split('\n');
    if (lines.length > 0 && lines[lines.length - 1] === '') {
      lines.pop();
    }

    if (change.removed) {
      const nextChange = changes[i + 1];
      if (nextChange && nextChange.added) {
        const addedLines = nextChange.value.split('\n');
        if (addedLines.length > 0 && addedLines[addedLines.length - 1] === '') {
          addedLines.pop();
        }

        const maxLen = Math.max(lines.length, addedLines.length);
        for (let j = 0; j < maxLen; j++) {
          if (j < lines.length) {
            left.push({ type: 'removed', value: lines[j], lineNumber: origLineNum++ });
          } else {
            left.push({ type: 'removed', value: '', lineNumber: undefined });
          }

          if (j < addedLines.length) {
            right.push({ type: 'added', value: addedLines[j], lineNumber: modLineNum++ });
          } else {
            right.push({ type: 'added', value: '', lineNumber: undefined });
          }
        }
        i++; // consumed nextChange
      } else {
        for (const line of lines) {
          left.push({ type: 'removed', value: line, lineNumber: origLineNum++ });
          right.push({ type: 'added', value: '', lineNumber: undefined });
        }
      }
    } else if (change.added) {
      for (const line of lines) {
        left.push({ type: 'removed', value: '', lineNumber: undefined });
        right.push({ type: 'added', value: line, lineNumber: modLineNum++ });
      }
    } else {
      for (const line of lines) {
        left.push({ type: 'unchanged', value: line, lineNumber: origLineNum++ });
        right.push({ type: 'unchanged', value: line, lineNumber: modLineNum++ });
      }
    }
  }

  return { left, right };
}

export const XmlDiffChecker: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [originalXml, setOriginalXml] = useState(() => {
    const saved = localStorage.getItem('nps_diff_source_xml');
    if (saved) localStorage.removeItem('nps_diff_source_xml');
    return saved || '';
  });
  const [modifiedXml, setModifiedXml] = useState('');
  const [originalTitle, setOriginalTitle] = useState(() => {
    const saved = localStorage.getItem('nps_diff_source_title');
    if (saved) localStorage.removeItem('nps_diff_source_title');
    return saved || 'Workbench XML';
  });
  const [canonicalize, setCanonicalize] = useState(true);
  
  const [diffResult, setDiffResult] = useState<{ left: DiffLine[]; right: DiffLine[] } | null>(null);
  const [isComparing, setIsComparing] = useState(false);

  const handleCompare = () => {
    if (!originalXml.trim() && !modifiedXml.trim()) return;
    setIsComparing(true);
    // Myers diff is fast and non-blocking
    setTimeout(() => {
      try {
        const diffs = computeMyersDiff(originalXml, modifiedXml, canonicalize);
        setDiffResult(diffs);
      } catch (err) {
        console.error(err);
      } finally {
        setIsComparing(false);
      }
    }, 20);
  };

  const handleFileUpload = (side: 'original' | 'modified', e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      if (side === 'original') {
        setOriginalXml(text);
        setOriginalTitle(file.name);
      } else {
        setModifiedXml(text);
      }
    };
    reader.readAsText(file);
  };

  const handleClear = () => {
    setOriginalXml('');
    setModifiedXml('');
    setDiffResult(null);
  };

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username
    : 'Developer';

  return (
    <div className="flex flex-col h-screen overflow-hidden bg-[#f6f9f7] font-sans">
      {/* Header */}
      <header className="h-16 flex-shrink-0 bg-white border-b border-[#e4e9e6] flex items-center justify-between px-6 z-10">
        <div className="flex items-center gap-3.5">
          <div
            className="w-[38px] h-[38px] rounded-[9px] flex items-center justify-center text-white font-bold text-[12px] flex-shrink-0 cursor-pointer"
            style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)', boxShadow: '0 4px 12px rgba(21,128,61,0.3)' }}
            onClick={() => navigate('/workbench')}
          >
            NPS
          </div>
          <div>
            <h1 className="text-[16px] font-bold text-[#0f3a22] m-0 leading-tight">XML Diff Checker</h1>
            <p className="text-[11.5px] text-[#6b7280] m-0 mt-[1px]">Compare generated XML with external reference payload</p>
          </div>
        </div>

        {/* Top Nav Mode Switcher Tabs */}
        <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 shadow-inner">
          <button
            type="button"
            onClick={() => navigate('/workbench')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Message Workbench
          </button>
          <button
            type="button"
            onClick={() => navigate('/orchestrator')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Flow Orchestrator
          </button>
          <button
            type="button"
            onClick={() => navigate('/inspector')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer bg-transparent border-0"
          >
            Fix My XML & Health Check
          </button>
          <button
            type="button"
            className="px-3.5 py-1.5 text-[12px] font-bold rounded-lg bg-white text-[#16a34a] shadow-[0_1px_3px_rgba(0,0,0,0.08)] flex items-center gap-1.5 cursor-default border-0"
          >
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            Diff Checker
          </button>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 border border-[#e4e9e6] rounded-lg px-3 py-1.5 bg-gray-50">
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            <span className="text-[12.5px] font-semibold text-gray-700">{displayName}</span>
          </div>
        </div>
      </header>

      {/* Main Diff Area */}
      <main className="flex-1 flex flex-col min-h-0 overflow-hidden p-6 gap-4">
        {/* Controls Bar */}
        <div className="flex justify-between items-center bg-white border border-[#e4e9e6] rounded-xl p-4 flex-shrink-0 shadow-2xs">
          <div className="flex gap-3">
            <button
              type="button"
              onClick={handleCompare}
              disabled={isComparing || (!originalXml.trim() && !modifiedXml.trim())}
              className="px-5 py-2 rounded-lg text-white font-bold text-[13px] transition-all bg-gradient-to-b from-[#16a34a] to-[#15803d] shadow-[0_3px_10px_rgba(22,163,74,0.25)] hover:from-[#15803d] hover:to-[#116932] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              {isComparing ? 'Comparing...' : 'Compare XML Payloads'}
            </button>
            {diffResult && (
              <button
                type="button"
                onClick={handleClear}
                className="px-4 py-2 border border-[#e4e9e6] bg-white rounded-lg text-[13px] font-semibold text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
              >
                Clear Payloads
              </button>
            )}
            <label className="flex items-center gap-2 text-[12.5px] font-medium text-gray-700 cursor-pointer select-none bg-[#f6f9f7] px-3 py-1.5 rounded-lg border border-[#e4e9e6]">
              <input
                type="checkbox"
                checked={canonicalize}
                onChange={(e) => setCanonicalize(e.target.checked)}
                className="w-4 h-4 rounded text-[#16a34a] focus:ring-[#16a34a] accent-[#16a34a] cursor-pointer"
              />
              <span>Canonicalize XML (normalize whitespace &amp; attribute order)</span>
            </label>
          </div>
          <div className="text-[12px] text-gray-500 font-medium">
            Myers diff algorithm active. Normalized comparison ignores formatting noise.
          </div>
        </div>

        {/* Diff Result / Inputs */}
        <div className="flex-1 flex min-h-0 gap-4">
          {!diffResult ? (
            /* Input View */
            <div className="flex-1 flex gap-4 min-h-0 w-full h-full">
              {/* Left Input */}
              <div className="flex-1 flex flex-col bg-white border border-[#e4e9e6] rounded-xl overflow-hidden shadow-2xs min-h-0">
                <div className="px-4 py-3 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
                  <span className="text-[13px] font-bold text-[#0f3a22]">{originalTitle}</span>
                  <label className="text-[11px] font-semibold text-[#16a34a] bg-white border border-[#d2efe0] px-2.5 py-1 rounded-md shadow-2xs cursor-pointer hover:bg-[#e8f6ed] transition-colors">
                    Upload XML
                    <input
                      type="file"
                      accept=".xml"
                      onChange={(e) => handleFileUpload('original', e)}
                      className="hidden"
                    />
                  </label>
                </div>
                <textarea
                  value={originalXml}
                  onChange={(e) => setOriginalXml(e.target.value)}
                  placeholder="Paste your source ISO 20022 XML document here..."
                  className="flex-grow p-4 resize-none outline-none font-mono text-[12px] leading-relaxed border-none text-gray-800"
                  style={{ fontFamily: "'JetBrains Mono', monospace" }}
                />
              </div>

              {/* Right Input */}
              <div className="flex-1 flex flex-col bg-white border border-[#e4e9e6] rounded-xl overflow-hidden shadow-2xs min-h-0">
                <div className="px-4 py-3 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
                  <span className="text-[13px] font-bold text-[#0f3a22]">External Reference XML</span>
                  <label className="text-[11px] font-semibold text-[#16a34a] bg-white border border-[#d2efe0] px-2.5 py-1 rounded-md shadow-2xs cursor-pointer hover:bg-[#e8f6ed] transition-colors">
                    Upload XML
                    <input
                      type="file"
                      accept=".xml"
                      onChange={(e) => handleFileUpload('modified', e)}
                      className="hidden"
                    />
                  </label>
                </div>
                <textarea
                  value={modifiedXml}
                  onChange={(e) => setModifiedXml(e.target.value)}
                  placeholder="Paste the external reference/target XML document to compare..."
                  className="flex-grow p-4 resize-none outline-none font-mono text-[12px] leading-relaxed border-none text-gray-800"
                  style={{ fontFamily: "'JetBrains Mono', monospace" }}
                />
              </div>
            </div>
          ) : (
            /* Visual Diff View */
            <div className="flex-1 flex gap-4 min-h-0 w-full h-full">
              {/* Left Diff Pane */}
              <div className="flex-1 flex flex-col bg-[#0b1a12] border border-[#e4e9e6] rounded-xl overflow-hidden shadow-inner min-h-0">
                <div className="px-4 py-3 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
                  <span className="text-[13px] font-bold text-[#0f3a22]">{originalTitle} (Original)</span>
                  <button
                    type="button"
                    onClick={() => setDiffResult(null)}
                    className="text-[11px] font-semibold text-gray-600 hover:text-gray-900 cursor-pointer"
                  >
                    Edit Input
                  </button>
                </div>
                <div className="flex-grow overflow-auto py-3 font-mono text-[11px] leading-relaxed text-gray-300">
                  {diffResult.left.map((line, idx) => (
                    <div
                      key={`left-${idx}`}
                      className="flex select-none"
                      style={{
                        background: line.type === 'removed' ? 'rgba(239,68,68,0.15)' : 'transparent',
                        borderLeft: line.type === 'removed' ? '3px solid #ef4444' : '3px solid transparent',
                        padding: '0 12px 0 0',
                      }}
                    >
                      <span className="text-right mr-3 text-[#3f6a52] flex-shrink-0" style={{ width: 32 }}>
                        {line.lineNumber || ''}
                      </span>
                      <span style={{ whiteSpace: 'pre', color: line.type === 'removed' ? '#fca5a5' : '#cfe8db' }}>
                        {line.value || (line.type === 'removed' ? ' ' : '')}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Right Diff Pane */}
              <div className="flex-1 flex flex-col bg-[#0b1a12] border border-[#e4e9e6] rounded-xl overflow-hidden shadow-inner min-h-0">
                <div className="px-4 py-3 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
                  <span className="text-[13px] font-bold text-[#0f3a22]">External Reference (Modified)</span>
                  <button
                    type="button"
                    onClick={() => setDiffResult(null)}
                    className="text-[11px] font-semibold text-gray-600 hover:text-gray-900 cursor-pointer"
                  >
                    Edit Input
                  </button>
                </div>
                <div className="flex-grow overflow-auto py-3 font-mono text-[11px] leading-relaxed text-gray-300">
                  {diffResult.right.map((line, idx) => (
                    <div
                      key={`right-${idx}`}
                      className="flex select-none"
                      style={{
                        background: line.type === 'added' ? 'rgba(34,197,94,0.15)' : 'transparent',
                        borderLeft: line.type === 'added' ? '3px solid #22c55e' : '3px solid transparent',
                        padding: '0 12px 0 0',
                      }}
                    >
                      <span className="text-right mr-3 text-[#3f6a52] flex-shrink-0" style={{ width: 32 }}>
                        {line.lineNumber || ''}
                      </span>
                      <span style={{ whiteSpace: 'pre', color: line.type === 'added' ? '#86efac' : '#cfe8db' }}>
                        {line.value || (line.type === 'added' ? ' ' : '')}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

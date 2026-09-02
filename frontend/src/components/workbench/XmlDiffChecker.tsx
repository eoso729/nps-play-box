import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface DiffLine {
  type: 'added' | 'removed' | 'unchanged';
  value: string;
  lineNumber?: number;
}

// Line-by-line Diff algorithm using Longest Common Subsequence (LCS)
function computeDiff(original: string, modified: string): { left: DiffLine[]; right: DiffLine[] } {
  const originalLines = original.split('\n');
  const modifiedLines = modified.split('\n');

  const m = originalLines.length;
  const n = modifiedLines.length;

  // DP table for LCS length
  const dp: number[][] = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));

  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (originalLines[i - 1].trim() === modifiedLines[j - 1].trim()) {
        dp[i][j] = dp[i - 1][j - 1] + 1;
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
      }
    }
  }

  const left: DiffLine[] = [];
  const right: DiffLine[] = [];

  let i = m;
  let j = n;

  // Backtrack to build diff lists
  while (i > 0 || j > 0) {
    if (i > 0 && j > 0 && originalLines[i - 1].trim() === modifiedLines[j - 1].trim()) {
      left.unshift({ type: 'unchanged', value: originalLines[i - 1], lineNumber: i });
      right.unshift({ type: 'unchanged', value: modifiedLines[j - 1], lineNumber: j });
      i--;
      j--;
    } else if (j > 0 && (i === 0 || dp[i][j - 1] >= dp[i - 1][j])) {
      // Added line in modified
      left.unshift({ type: 'removed', value: '', lineNumber: undefined }); // Spacer line
      right.unshift({ type: 'added', value: modifiedLines[j - 1], lineNumber: j });
      j--;
    } else {
      // Removed line from original
      left.unshift({ type: 'removed', value: originalLines[i - 1], lineNumber: i });
      right.unshift({ type: 'added', value: '', lineNumber: undefined }); // Spacer line
      i--;
    }
  }

  return { left, right };
}

export const XmlDiffChecker: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [originalXml, setOriginalXml] = useState('');
  const [modifiedXml, setModifiedXml] = useState('');
  const [originalTitle, setOriginalTitle] = useState('Workbench XML');
  
  const [diffResult, setDiffResult] = useState<{ left: DiffLine[]; right: DiffLine[] } | null>(null);
  const [isComparing, setIsComparing] = useState(false);

  // Load from localStorage if pre-populated from workbench
  useEffect(() => {
    const savedXml = localStorage.getItem('nps_diff_source_xml');
    const savedTitle = localStorage.getItem('nps_diff_source_title');
    if (savedXml) {
      setOriginalXml(savedXml);
      if (savedTitle) {
        setOriginalTitle(savedTitle);
      }
      // Clear storage so it doesn't linger
      localStorage.removeItem('nps_diff_source_xml');
      localStorage.removeItem('nps_diff_source_title');
    }
  }, []);

  const handleCompare = () => {
    if (!originalXml.trim() && !modifiedXml.trim()) return;
    setIsComparing(true);
    // Use setTimeout to avoid UI thread blocking on large XMLs
    setTimeout(() => {
      try {
        const diffs = computeDiff(originalXml, modifiedXml);
        setDiffResult(diffs);
      } catch (err) {
        console.error(err);
      } finally {
        setIsComparing(false);
      }
    }, 50);
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
          </div>
          <div className="text-[12px] text-gray-500 font-medium">
            Paste or upload files below to see line-by-line highlights.
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

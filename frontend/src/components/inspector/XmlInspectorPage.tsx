import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { inspectXml, autoFixXml, getAllSamples } from '../../api/validation';
import { ValidationReport, MessageSample } from '../../types/validation';

export const XmlInspectorPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [xmlContent, setXmlContent] = useState<string>('');
  const [selectedMessageType, setSelectedMessageType] = useState<string>('auto');
  const [report, setReport] = useState<ValidationReport | null>(null);
  const [isInspecting, setIsInspecting] = useState(false);
  const [isFixing, setIsFixing] = useState(false);
  const [activeTab, setActiveTab] = useState<'ALL' | 'ERRORS' | 'WARNINGS' | 'PASSED' | 'FIXES'>('ALL');
  const [highlightedLine, setHighlightedLine] = useState<number | null>(null);
  const [fixesApplied, setFixesApplied] = useState<string[]>([]);
  const [samples, setSamples] = useState<MessageSample[]>([]);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null);

  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const lineNumbersRef = useRef<HTMLDivElement>(null);

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const handleInspect = async (contentToInspect = xmlContent, msgType = selectedMessageType) => {
    if (!contentToInspect.trim()) {
      showToast('Please enter or paste XML to inspect', 'error');
      return;
    }
    setIsInspecting(true);
    try {
      const result = await inspectXml(contentToInspect, msgType === 'auto' ? undefined : msgType);
      setReport(result);
      if (result.valid) {
        showToast(`XML Health Check Passed: 100% Compliant (${result.detectedMessageType || 'ISO 20022'})`, 'success');
      } else {
        const errorCount = result.summary.totalErrors;
        showToast(`Inspection Complete: ${errorCount} issue(s) detected.`, 'error');
      }
    } catch (err: any) {
      console.error(err);
      showToast('Failed to validate XML: ' + (err?.response?.data?.message || err?.message), 'error');
    } finally {
      setIsInspecting(false);
    }
  };

  // Load passed XML on mount if navigated from workbench or diff
  useEffect(() => {
    // 1. Check if XML was passed via navigation state or localStorage
    const navState = location.state as { xml?: string; title?: string } | undefined;
    const passedXml = navState?.xml || localStorage.getItem('nps_inspector_xml') || localStorage.getItem('nps_diff_source_xml');
    const passedTitle = navState?.title || localStorage.getItem('nps_inspector_title') || localStorage.getItem('nps_diff_source_title');

    if (passedXml && passedXml.trim()) {
      setXmlContent(passedXml);
      handleInspect(passedXml, 'auto');
      showToast(`Loaded ${passedTitle || 'generated XML'} for Health Check inspection`, 'info');
      // Clean up storage key so it doesn't linger
      localStorage.removeItem('nps_inspector_xml');
      localStorage.removeItem('nps_inspector_title');
    }

    getAllSamples()
      .then(data => {
        setSamples(data);
      })
      .catch(err => console.error('Failed to load message samples', err));
  }, []);

  const handleAutoFix = async () => {
    if (!xmlContent.trim()) return;
    setIsFixing(true);
    try {
      const originalXml = xmlContent;
      const res = await autoFixXml({
        xmlContent,
        messageType: selectedMessageType === 'auto' ? undefined : selectedMessageType,
        fixDates: true,
        fixIds: true,
        fixSupplementaryData: true,
        truncateOversized: true,
      });

      if (res.fixedXml) {
        setXmlContent(res.fixedXml);
        setFixesApplied(res.fixesApplied || []);
        setReport(res.validationReport);
        setActiveTab('FIXES');
        showToast(`Auto-Fix applied ${res.fixesApplied?.length || 0} repairs successfully!`, 'success');

        // Store original & fixed for diffing if user desires
        localStorage.setItem('nps_diff_source_xml', originalXml);
        localStorage.setItem('nps_diff_source_title', 'Original XML (Before Fix)');
      }
    } catch (err: any) {
      console.error(err);
      showToast('Auto-Fix failed: ' + (err?.response?.data?.message || err?.message), 'error');
    } finally {
      setIsFixing(false);
    }
  };

  const handleFormatOnly = async () => {
    if (!xmlContent.trim()) return;
    setIsFixing(true);
    try {
      const res = await autoFixXml({
        xmlContent,
        messageType: selectedMessageType === 'auto' ? undefined : selectedMessageType,
        formatOnly: true,
      });

      if (res.fixedXml) {
        setXmlContent(res.fixedXml);
        setFixesApplied(res.fixesApplied || []);
        setReport(res.validationReport);
        showToast('XML formatted cleanly with 4-space indentation.', 'success');
      }
    } catch (err: any) {
      console.error(err);
      showToast('Formatting failed: ' + (err?.response?.data?.message || err?.message), 'error');
    } finally {
      setIsFixing(false);
    }
  };

  const handleLoadSample = (key: string) => {
    const sample = samples.find(s => s.key === key);
    if (sample) {
      setXmlContent(sample.sampleXml);
      setSelectedMessageType(sample.key);
      setFixesApplied([]);
      handleInspect(sample.sampleXml, sample.key);
      showToast(`Loaded official sample: ${sample.name} (${sample.isoCode})`, 'info');
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      setXmlContent(text);
      setFixesApplied([]);
      handleInspect(text, selectedMessageType);
      showToast(`Uploaded ${file.name}`, 'info');
    };
    reader.readAsText(file);
  };

  const handleCompareInDiff = () => {
    if (!xmlContent.trim()) return;
    localStorage.setItem('nps_diff_source_xml', xmlContent);
    localStorage.setItem('nps_diff_source_title', report?.messageName || 'Health Check XML');
    navigate('/diff');
  };

  const handleCopyXml = () => {
    navigator.clipboard.writeText(xmlContent);
    showToast('XML copied to clipboard!', 'success');
  };

  const handleDownload = () => {
    const blob = new Blob([xmlContent], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${report?.detectedMessageType || 'iso20022'}-health-checked.xml`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleEditorScroll = (e: React.UIEvent<HTMLTextAreaElement>) => {
    if (lineNumbersRef.current) {
      lineNumbersRef.current.scrollTop = e.currentTarget.scrollTop;
    }
  };

  const handleGutterWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (textareaRef.current) {
      textareaRef.current.scrollTop += e.deltaY;
    }
  };

  const jumpToLine = (lineNumber: number) => {
    setHighlightedLine(lineNumber);
    if (!textareaRef.current) return;
    const lines = xmlContent.split('\n');
    let charIndex = 0;
    for (let i = 0; i < Math.min(lineNumber - 1, lines.length); i++) {
      charIndex += lines[i].length + 1;
    }
    textareaRef.current.focus();
    textareaRef.current.setSelectionRange(charIndex, charIndex + (lines[lineNumber - 1]?.length || 0));

    // Synchronize vertical scroll to the target line
    const lineHeight = 20; // 20px per line (leading-5)
    const targetScrollTop = Math.max(0, (lineNumber - 1) * lineHeight - 60);
    textareaRef.current.scrollTop = targetScrollTop;
    if (lineNumbersRef.current) {
      lineNumbersRef.current.scrollTop = targetScrollTop;
    }
  };

  const copyXPath = (xpath: string) => {
    navigator.clipboard.writeText(xpath);
    showToast(`Copied XPath: ${xpath}`, 'info');
  };

  const filteredIssues = (report?.issues || []).filter(issue => {
    if (activeTab === 'ERRORS') return issue.severity === 'ERROR';
    if (activeTab === 'WARNINGS') return issue.severity === 'WARNING';
    return true;
  });

  const lines = xmlContent.split('\n');
  const errorLines = new Set((report?.issues || []).filter(i => i.severity === 'ERROR').map(i => i.lineNumber));
  const warningLines = new Set((report?.issues || []).filter(i => i.severity === 'WARNING').map(i => i.lineNumber));

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username
    : 'Developer';

  return (
    <div className="flex flex-col h-screen overflow-hidden bg-[#f6f9f7] font-sans text-gray-800">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-4 right-6 z-50 px-4 py-3 rounded-xl shadow-xl flex items-center gap-3 text-[13px] font-semibold transition-all animate-in fade-in slide-in-from-top-2 border ${
            toast.type === 'error'
              ? 'bg-red-50 text-red-700 border-red-200'
              : toast.type === 'info'
              ? 'bg-blue-50 text-blue-700 border-blue-200'
              : 'bg-emerald-50 text-emerald-800 border-emerald-200'
          }`}
        >
          <span className="text-[16px]">
            {toast.type === 'error' ? '⚠️' : toast.type === 'info' ? 'ℹ️' : '✅'}
          </span>
          <span>{toast.message}</span>
        </div>
      )}

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
            <div className="flex items-center gap-2">
              <h1 className="text-[16px] font-bold text-[#0f3a22] m-0 leading-tight">Fix My XML & Health Check</h1>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#e6f6ec] text-[#15803d] border border-[#c4ebd3]">
                ISO 20022 & NIBSS
              </span>
            </div>
            <p className="text-[11.5px] text-[#6b7280] m-0 mt-[1px]">Field-by-field schema validation, NIBSS business rules & 1-click auto-repair</p>
          </div>
        </div>

        {/* Mode Switcher */}
        <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 shadow-inner">
          <button
            type="button"
            onClick={() => navigate('/workbench')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer"
          >
            Message Workbench
          </button>
          <button
            type="button"
            onClick={() => navigate('/orchestrator')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer"
          >
            Flow Orchestrator
          </button>
          <button
            type="button"
            className="px-3.5 py-1.5 text-[12px] font-bold rounded-lg bg-white text-[#16a34a] shadow-[0_1px_3px_rgba(0,0,0,0.08)] flex items-center gap-1.5 cursor-default"
          >
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            XML Health Check & Fixer
          </button>
          <button
            type="button"
            onClick={() => navigate('/diff')}
            className="px-3.5 py-1.5 text-[12px] font-semibold rounded-lg text-gray-600 hover:text-gray-900 transition-all cursor-pointer"
          >
            Diff Checker
          </button>
        </div>

        {/* User Info */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 border border-[#e4e9e6] rounded-lg px-3 py-1.5 bg-gray-50">
            <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
            <span className="text-[12.5px] font-semibold text-gray-700">{displayName}</span>
          </div>
        </div>
      </header>

      {/* Action Toolbar */}
      <div className="bg-white border-b border-[#e4e9e6] px-6 py-3 flex items-center justify-between flex-shrink-0 gap-4">
        <div className="flex items-center gap-3 flex-wrap">
          {/* Message Type Selector */}
          <div className="flex items-center gap-2 bg-[#f6f9f7] border border-[#e1e9e3] rounded-lg px-3 py-1.5">
            <span className="text-[11.5px] font-semibold text-gray-500">Message Type:</span>
            <select
              value={selectedMessageType}
              onChange={(e) => setSelectedMessageType(e.target.value)}
              className="bg-transparent text-[12.5px] font-bold text-[#0f3a22] outline-none cursor-pointer"
            >
              <option value="auto">⚡ Auto-Detect Message Type</option>
              <optgroup label="Credit Transfer">
                <option value="pacs.008">pacs.008 (Customer Direct Credit)</option>
                <option value="pacs.002">pacs.002 (Payment Status Report)</option>
                <option value="pacs.028">pacs.028 (Payment Status Request)</option>
                <option value="pacs.004">pacs.004 (Payment Return)</option>
              </optgroup>
              <optgroup label="Payment Activation & Initiation">
                <option value="pain.013">pain.013 (Payment Activation)</option>
                <option value="pain.014">pain.014 (Payment Activation Status)</option>
                <option value="pain.001">pain.001 (Payment Initiation)</option>
                <option value="pain.002">pain.002 (Customer Status Report)</option>
              </optgroup>
              <optgroup label="Direct Debit Operations">
                <option value="pacs.003">pacs.003 (Direct Debit Transfer)</option>
                <option value="pain.008">pain.008 (Direct Debit Initiation)</option>
              </optgroup>
              <optgroup label="Mandate Management">
                <option value="pain.009">pain.009 (Mandate Initiation)</option>
                <option value="pain.010">pain.010 (Mandate Amendment)</option>
                <option value="pain.011">pain.011 (Mandate Cancellation)</option>
                <option value="pain.012">pain.012 (Mandate Acceptance Report)</option>
              </optgroup>
              <optgroup label="Account Services & Statements">
                <option value="acmt.023">acmt.023 (Name Verification Request)</option>
                <option value="acmt.024">acmt.024 (Name Verification Report)</option>
                <option value="camt.060">camt.060 (Account Reporting Request)</option>
                <option value="camt.052">camt.052 (Bank Account Report)</option>
                <option value="camt.053">camt.053 (Bank Statement)</option>
              </optgroup>
            </select>
          </div>

          {/* Quick Sample Selector */}
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              onClick={() => handleLoadSample(selectedMessageType === 'auto' ? 'pacs.008' : selectedMessageType)}
              className="text-[12px] font-semibold text-[#15803d] bg-[#e6f6ec] border border-[#bce3cb] hover:bg-[#d5eedf] px-3 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            >
              <span>📄</span> Load Sample XML
            </button>
          </div>

          {/* File Upload */}
          <label className="text-[12px] font-semibold text-gray-700 bg-white border border-[#e4e9e6] hover:bg-gray-50 px-3 py-1.5 rounded-lg cursor-pointer transition-colors flex items-center gap-1.5">
            <span>📁</span> Upload File
            <input type="file" accept=".xml" onChange={handleFileUpload} className="hidden" />
          </label>

          <button
            type="button"
            onClick={() => { setXmlContent(''); setReport(null); setFixesApplied([]); }}
            className="text-[12px] font-semibold text-gray-500 hover:text-gray-800 px-2 py-1.5 cursor-pointer"
          >
            Clear
          </button>
        </div>

        {/* Main Action Buttons */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => handleInspect()}
            disabled={isInspecting || !xmlContent.trim()}
            className="px-5 py-2 rounded-lg text-white font-bold text-[13px] transition-all bg-gradient-to-b from-[#16a34a] to-[#15803d] shadow-[0_3px_10px_rgba(22,163,74,0.25)] hover:from-[#15803d] hover:to-[#116932] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center gap-2"
          >
            {isInspecting ? (
              <>
                <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                Inspecting...
              </>
            ) : (
              <>
                <span>🔍</span> Inspect & Validate
              </>
            )}
          </button>

          <button
            type="button"
            onClick={handleFormatOnly}
            disabled={isFixing || !xmlContent.trim()}
            className="px-3.5 py-2 border border-[#bce3cb] bg-[#e6f6ec] hover:bg-[#d5eedf] rounded-lg text-[12.5px] font-bold text-[#15803d] transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center gap-1.5"
            title="Clean format XML with 4-space indentation without mutating field values"
          >
            <span>🧹</span> Format XML
          </button>

          <button
            type="button"
            onClick={handleAutoFix}
            disabled={isFixing || !xmlContent.trim()}
            className="px-4 py-2 rounded-lg text-white font-bold text-[13px] transition-all bg-gradient-to-r from-purple-600 to-indigo-600 shadow-[0_3px_10px_rgba(124,58,237,0.25)] hover:from-purple-700 hover:to-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center gap-1.5"
          >
            {isFixing ? (
              <>
                <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                Fixing...
              </>
            ) : (
              <>
                <span>✨</span> One-Click Auto-Fix & Format
              </>
            )}
          </button>

          <button
            type="button"
            onClick={handleCompareInDiff}
            className="px-3.5 py-2 border border-[#e4e9e6] bg-white rounded-lg text-[12.5px] font-semibold text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer flex items-center gap-1.5"
          >
            <span>⚖️</span> Compare in Diff
          </button>
        </div>
      </div>

      {/* Health Metrics & Status Banner */}
      {report && (
        <div className="bg-white border-b border-[#e4e9e6] px-6 py-3 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-4">
            {/* Score Badge */}
            <div
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl border font-bold text-[13px] ${
                report.valid
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                  : report.summary.totalErrors > 0
                  ? 'bg-red-50 border-red-200 text-red-700'
                  : 'bg-amber-50 border-amber-200 text-amber-800'
              }`}
            >
              <span className="text-[16px]">{report.valid ? '🛡️' : report.summary.totalErrors > 0 ? '⚠️' : '⚡'}</span>
              <span>Health Score: {report.healthScore}% {report.valid ? '(Compliant)' : `(${report.summary.totalErrors} errors)`}</span>
            </div>

            {/* Detected Message Info */}
            <div className="flex items-center gap-2 text-[12.5px]">
              <span className="text-gray-500">Detected Schema:</span>
              <span className="font-bold text-[#0f3a22] font-mono bg-gray-100 px-2 py-0.5 rounded border border-gray-200">
                {report.detectedMessageType || 'Unknown'}
              </span>
              {report.messageName && (
                <span className="text-gray-600 font-medium">({report.messageName})</span>
              )}
              {report.isoCode && (
                <span className="text-gray-400 font-mono text-[11px]">[{report.isoCode}]</span>
              )}
            </div>
          </div>

          {/* Issue Counts Summary Badges */}
          <div className="flex items-center gap-2.5 text-[12px]">
            <button
              onClick={() => setActiveTab('ERRORS')}
              className={`px-2.5 py-1 rounded-md font-bold transition-all ${
                report.summary.totalErrors > 0
                  ? 'bg-red-100 text-red-800 hover:bg-red-200 cursor-pointer'
                  : 'bg-gray-100 text-gray-400'
              }`}
            >
              🔴 {report.summary.totalErrors} Errors
            </button>
            <button
              onClick={() => setActiveTab('WARNINGS')}
              className={`px-2.5 py-1 rounded-md font-bold transition-all ${
                report.summary.totalWarnings > 0
                  ? 'bg-amber-100 text-amber-800 hover:bg-amber-200 cursor-pointer'
                  : 'bg-gray-100 text-gray-400'
              }`}
            >
              🟡 {report.summary.totalWarnings} Warnings
            </button>
            <button
              onClick={() => setActiveTab('PASSED')}
              className="px-2.5 py-1 rounded-md font-bold bg-emerald-100 text-emerald-800 hover:bg-emerald-200 cursor-pointer transition-all"
            >
              🟢 {report.passedRules.length} Rules Passed
            </button>
            {fixesApplied.length > 0 && (
              <button
                onClick={() => setActiveTab('FIXES')}
                className="px-2.5 py-1 rounded-md font-bold bg-purple-100 text-purple-800 hover:bg-purple-200 cursor-pointer transition-all"
              >
                ✨ {fixesApplied.length} Fixes Applied
              </button>
            )}
          </div>
        </div>
      )}

      {/* Main Dual-Pane Workspace */}
      <main className="flex-1 flex min-h-0 overflow-hidden p-6 gap-4">
        {/* Left Pane: XML Editor */}
        <div className="flex-1 flex flex-col bg-white border border-[#e4e9e6] rounded-xl overflow-hidden shadow-2xs min-h-0">
          <div className="px-4 py-2.5 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
            <div className="flex items-center gap-2">
              <span className="text-[13px] font-bold text-[#0f3a22]">XML Payload Editor</span>
              <span className="text-[11px] text-gray-400 font-mono">({lines.length} lines, {xmlContent.length} chars)</span>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleCopyXml}
                className="text-[11.5px] font-semibold text-gray-600 hover:text-gray-900 bg-white border border-gray-200 px-2.5 py-1 rounded-md transition-colors cursor-pointer"
              >
                Copy
              </button>
              <button
                type="button"
                onClick={handleDownload}
                className="text-[11.5px] font-semibold text-[#16a34a] hover:text-[#15803d] bg-white border border-[#d2efe0] px-2.5 py-1 rounded-md transition-colors cursor-pointer"
              >
                Download
              </button>
            </div>
          </div>

          {/* Editor Body with Gutter */}
          <div className="flex-1 flex min-h-0 overflow-hidden relative font-mono text-[12px]">
            {/* Line Numbers Gutter */}
            <div
              ref={lineNumbersRef}
              onWheel={handleGutterWheel}
              className="w-12 bg-[#f8faf9] border-r border-[#e4e9e6] py-3 select-none flex flex-col items-end pr-2 text-gray-400 text-[11px] overflow-hidden flex-shrink-0"
            >
              {lines.map((_, idx) => {
                const lineNum = idx + 1;
                const hasError = errorLines.has(lineNum);
                const hasWarning = warningLines.has(lineNum);
                const isCurrent = highlightedLine === lineNum;
                return (
                  <div
                    key={`line-${lineNum}`}
                    className={`h-5 leading-5 flex items-center justify-end w-full gap-1 flex-shrink-0 ${
                      isCurrent ? 'font-bold text-emerald-600 bg-emerald-50' : ''
                    }`}
                  >
                    {hasError && <span className="text-[8px] text-red-500">●</span>}
                    {hasWarning && !hasError && <span className="text-[8px] text-amber-500">●</span>}
                    <span>{lineNum}</span>
                  </div>
                );
              })}
            </div>

            {/* Textarea */}
            <textarea
              ref={textareaRef}
              value={xmlContent}
              onChange={(e) => setXmlContent(e.target.value)}
              onScroll={handleEditorScroll}
              placeholder="Paste or edit ISO 20022 XML payload here..."
              spellCheck={false}
              className="flex-1 p-3 resize-none outline-none leading-5 border-none text-gray-800 bg-white overflow-auto font-mono text-[12px] whitespace-pre"
              style={{ fontFamily: "'JetBrains Mono', 'Fira Code', monospace" }}
            />
          </div>
        </div>

        {/* Right Pane: Inspector & Validation Diagnostics */}
        <div className="flex-1 flex flex-col bg-white border border-[#e4e9e6] rounded-xl overflow-hidden shadow-2xs min-h-0">
          {/* Tabs */}
          <div className="px-4 py-2.5 bg-[#f6f9f7] border-b border-[#e4e9e6] flex items-center justify-between flex-shrink-0">
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => setActiveTab('ALL')}
                className={`px-3 py-1 text-[12px] font-bold rounded-lg transition-colors cursor-pointer ${
                  activeTab === 'ALL' ? 'bg-white text-[#16a34a] shadow-xs' : 'text-gray-500 hover:text-gray-800'
                }`}
              >
                All Issues ({report?.issues.length || 0})
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('ERRORS')}
                className={`px-3 py-1 text-[12px] font-bold rounded-lg transition-colors cursor-pointer ${
                  activeTab === 'ERRORS' ? 'bg-white text-red-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
                }`}
              >
                Errors ({report?.summary.totalErrors || 0})
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('WARNINGS')}
                className={`px-3 py-1 text-[12px] font-bold rounded-lg transition-colors cursor-pointer ${
                  activeTab === 'WARNINGS' ? 'bg-white text-amber-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
                }`}
              >
                Warnings ({report?.summary.totalWarnings || 0})
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('PASSED')}
                className={`px-3 py-1 text-[12px] font-bold rounded-lg transition-colors cursor-pointer ${
                  activeTab === 'PASSED' ? 'bg-white text-emerald-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
                }`}
              >
                Passed Rules ({report?.passedRules.length || 0})
              </button>
              {fixesApplied.length > 0 && (
                <button
                  type="button"
                  onClick={() => setActiveTab('FIXES')}
                  className={`px-3 py-1 text-[12px] font-bold rounded-lg transition-colors cursor-pointer ${
                    activeTab === 'FIXES' ? 'bg-white text-purple-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
                  }`}
                >
                  Repairs Log ({fixesApplied.length})
                </button>
              )}
            </div>

            <span className="text-[11.5px] font-semibold text-gray-500">
              {report ? (report.valid ? '✅ All checks passed' : `⚠️ ${report.summary.totalErrors} errors to fix`) : 'Ready to inspect'}
            </span>
          </div>

          {/* Tab Content */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {!report ? (
              <div className="flex flex-col items-center justify-center h-full text-center text-gray-400 py-12">
                <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-[22px] mb-3">
                  🔍
                </div>
                <h3 className="text-[14px] font-bold text-gray-700 m-0">No Inspection Performed Yet</h3>
                <p className="text-[12px] text-gray-500 max-w-sm mt-1 mb-4">
                  Paste your ISO 20022 XML on the left and click <strong>"Inspect & Validate"</strong> to run real-time field-by-field verification.
                </p>
                <button
                  type="button"
                  onClick={() => handleInspect()}
                  className="px-4 py-2 bg-[#16a34a] hover:bg-[#15803d] text-white text-[12.5px] font-bold rounded-lg transition-colors cursor-pointer"
                >
                  Inspect Current XML
                </button>
              </div>
            ) : activeTab === 'FIXES' ? (
              /* Repairs Log */
              <div className="space-y-2">
                <div className="bg-purple-50 border border-purple-200 rounded-xl p-3.5">
                  <h4 className="text-[13px] font-bold text-purple-900 m-0 flex items-center gap-2">
                    <span>✨</span> Automated Repairs Summary
                  </h4>
                  <p className="text-[12px] text-purple-700 m-0 mt-1">
                    The engine applied {fixesApplied.length} automated corrections to normalize timestamps, repair 35-char NPS IDs, and satisfy NIBSS ISO 20022 standards.
                  </p>
                </div>
                <div className="space-y-2 mt-3">
                  {fixesApplied.map((fix, idx) => (
                    <div
                      key={`fix-${idx}`}
                      className="p-3 bg-white border border-[#e1e9e3] rounded-lg text-[12.5px] flex items-start gap-2.5 shadow-2xs"
                    >
                      <span className="w-5 h-5 rounded-full bg-purple-100 text-purple-700 flex items-center justify-center font-bold text-[11px] flex-shrink-0 mt-0.5">
                        {idx + 1}
                      </span>
                      <span className="text-gray-700 font-medium leading-snug">{fix}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : activeTab === 'PASSED' ? (
              /* Passed Rules */
              <div className="space-y-2">
                <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-3.5">
                  <h4 className="text-[13px] font-bold text-emerald-900 m-0 flex items-center gap-2">
                    <span>🛡️</span> Verified NIBSS Rules & Schema Elements
                  </h4>
                  <p className="text-[12px] text-emerald-700 m-0 mt-1">
                    {report.passedRules.length} mandatory constraints and specifications verified successfully.
                  </p>
                </div>
                <div className="grid grid-cols-1 gap-2 mt-3">
                  {report.passedRules.map((rule, idx) => (
                    <div
                      key={`passed-${idx}`}
                      className="p-2.5 bg-white border border-[#d2efe0] rounded-lg text-[12px] flex items-center gap-2.5 text-gray-700 font-medium shadow-2xs"
                    >
                      <span className="text-emerald-600 font-bold text-[14px]">✓</span>
                      <span>{rule}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : filteredIssues.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-center text-gray-400 py-12">
                <div className="w-12 h-12 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center text-[22px] mb-3">
                  🎉
                </div>
                <h3 className="text-[14px] font-bold text-gray-800 m-0">No Issues Found in this Category!</h3>
                <p className="text-[12px] text-gray-500 max-w-sm mt-1">
                  All checked fields and business rules comply with NIBSS ISO 20022 standards.
                </p>
              </div>
            ) : (
              /* Issues List */
              <div className="space-y-3">
                {filteredIssues.map((issue) => (
                  <div
                    key={issue.id}
                    onClick={() => jumpToLine(issue.lineNumber)}
                    className={`p-4 rounded-xl border transition-all cursor-pointer hover:shadow-md ${
                      issue.severity === 'ERROR'
                        ? 'bg-red-50/40 border-red-200 hover:border-red-400'
                        : issue.severity === 'WARNING'
                        ? 'bg-amber-50/40 border-amber-200 hover:border-amber-400'
                        : 'bg-blue-50/40 border-blue-200 hover:border-blue-400'
                    }`}
                  >
                    {/* Header Row */}
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span
                          className={`text-[10px] font-bold uppercase px-2 py-0.5 rounded-full ${
                            issue.severity === 'ERROR'
                              ? 'bg-red-100 text-red-800 border border-red-300'
                              : issue.severity === 'WARNING'
                              ? 'bg-amber-100 text-amber-800 border border-amber-300'
                              : 'bg-blue-100 text-blue-800 border border-blue-300'
                          }`}
                        >
                          {issue.severity}
                        </span>

                        <span className="text-[11px] font-bold font-mono bg-white border border-gray-300 px-2 py-0.5 rounded text-gray-700">
                          Line {issue.lineNumber} : Col {issue.columnNumber}
                        </span>

                        <span className="text-[10.5px] font-mono text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">
                          {issue.ruleCode}
                        </span>
                      </div>

                      {issue.autoFixable && (
                        <span className="text-[10.5px] font-semibold text-purple-700 bg-purple-100 px-2 py-0.5 rounded-full flex items-center gap-1">
                          <span>✨</span> Auto-Fixable
                        </span>
                      )}
                    </div>

                    {/* Field & Message */}
                    <div className="mb-2">
                      <div className="text-[13px] font-bold text-gray-900 leading-tight">
                        {issue.fieldName ? `${issue.fieldName} ` : ''}
                        <span className="font-mono text-[12px] font-normal text-gray-600">({issue.fieldPath || issue.xpath})</span>
                      </div>
                      <p className="text-[12px] text-gray-700 m-0 mt-1 leading-snug">
                        {issue.message}
                      </p>
                    </div>

                    {/* Values comparison */}
                    <div className="grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-gray-200/70 text-[11.5px] font-mono">
                      <div className="bg-white/80 p-2 rounded border border-gray-200">
                        <span className="text-gray-400 block text-[10px] uppercase font-sans font-bold">Found Value</span>
                        <span className="text-red-700 break-all font-semibold">{issue.currentValue || '""'}</span>
                      </div>
                      <div className="bg-white/80 p-2 rounded border border-gray-200">
                        <span className="text-gray-400 block text-[10px] uppercase font-sans font-bold">Expected</span>
                        <span className="text-emerald-700 break-all font-semibold">{issue.expected}</span>
                      </div>
                    </div>

                    {/* XPath Footer */}
                    <div className="mt-2 pt-1.5 flex items-center justify-between text-[11px] text-gray-400 font-mono">
                      <span className="truncate max-w-xs">{issue.xpath}</span>
                      <button
                        type="button"
                        onClick={(e) => { e.stopPropagation(); copyXPath(issue.xpath); }}
                        className="text-gray-500 hover:text-gray-800 text-[10.5px] font-sans font-semibold underline cursor-pointer"
                      >
                        Copy XPath
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default XmlInspectorPage;

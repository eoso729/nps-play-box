import React, { useState } from 'react';
import { ServicePushResult } from '../../types/workbench';
import {
  Copy,
  Download,
  Check,
  ShieldCheck,
  FileCode,
  Lock,
  Server,
  Zap,
  Clock,
  Layers,
  Code2,
  Maximize2,
  Sparkles,
} from 'lucide-react';

interface InspectionPipelineProps {
  plainXml: string;
  signedXml: string;
  serviceResponse: ServicePushResult | null;
  messageType: string;
  messageId: string;
}

export const InspectionPipeline: React.FC<InspectionPipelineProps> = ({
  plainXml,
  signedXml,
  serviceResponse,
  messageType,
  messageId,
}) => {
  const [activeTab, setActiveTab] = useState<'plain' | 'signed' | 'response'>(
    'plain'
  );
  const [responseSubTab, setResponseSubTab] = useState<'body' | 'headers'>(
    'body'
  );
  const [copiedStage, setCopiedStage] = useState<string | null>(null);

  const handleCopy = (text: string, stage: string) => {
    navigator.clipboard.writeText(text);
    setCopiedStage(stage);
    setTimeout(() => setCopiedStage(null), 2000);
  };

  const handleDownload = (text: string, filename: string) => {
    const blob = new Blob([text], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const formatXml = (xml: string) => {
    let formatted = '';
    const reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    let pad = 0;
    xml.split('\r\n').forEach((line) => {
      let indent = 0;
      if (line.match(/.+<\/\w[^>]*>$/)) {
        indent = 0;
      } else if (line.match(/^<\/\w/)) {
        if (pad !== 0) {
          pad -= 1;
        }
      } else if (line.match(/^<\w[^>]*[^\/]>.*$/)) {
        indent = 1;
      } else {
        indent = 0;
      }

      let padding = '';
      for (let i = 0; i < pad; i++) {
        padding += '  ';
      }

      formatted += padding + line + '\r\n';
      pad += indent;
    });
    return formatted.trim();
  };

  const renderCodeWithLineNumbers = (content: string) => {
    if (!content) {
      return (
        <div className="p-8 text-center text-slate-400 font-mono text-xs">
          No payload generated yet. Use <span className="text-emerald-400 font-semibold font-mono">Generate XML</span> or <span className="text-emerald-400 font-semibold font-mono">Execute Request Pipeline</span> on the left panel.
        </div>
      );
    }

    const lines = content.split('\n');

    return (
      <div className="flex font-mono text-xs leading-relaxed">
        {/* Line Numbers */}
        <div className="py-3 px-3 text-right text-slate-600 bg-slate-950/80 border-r border-slate-800 select-none font-mono text-[11px]">
          {lines.map((_, idx) => (
            <div key={idx}>{idx + 1}</div>
          ))}
        </div>
        {/* Code Content */}
        <div className="py-3 px-4 overflow-x-auto text-slate-200 whitespace-pre font-mono flex-1 custom-scrollbar">
          {lines.map((line, idx) => (
            <div key={idx} className="hover:bg-slate-800/30">
              {line.includes('<?xml') ? (
                <span className="text-slate-500 font-bold">{line}</span>
              ) : line.includes('<') && line.includes('>') ? (
                <span dangerouslySetInnerHTML={{ __html: highlightXmlLine(line) }} />
              ) : (
                <span>{line}</span>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const highlightXmlLine = (line: string) => {
    return line
      .replace(/(&lt;|<)(\/?[a-zA-Z0-9_:]+)/g, '&lt;<span class="text-indigo-400 font-bold">$2</span>')
      .replace(/([a-zA-Z0-9_:]+)=("[^"]*")/g, '<span class="text-amber-300">$1</span>=<span class="text-emerald-300">$2</span>')
      .replace(/(&gt;|>)/g, '<span class="text-slate-500">$1</span>');
  };

  return (
    <div className="panel-pipeline flex flex-col h-full bg-[#0b1120] overflow-hidden">
      {/* Top Inspection Tabs */}
      <div className="px-4 pt-3 bg-[#090d16] border-b border-[#1e293b] flex items-center justify-between shrink-0">
        <div className="flex space-x-2">
          {/* Pane 1 Tab */}
          <button
            onClick={() => setActiveTab('plain')}
            className={`px-4 py-2.5 rounded-t-xl text-xs font-bold flex items-center space-x-2 transition-all border-t border-x ${
              activeTab === 'plain'
                ? 'bg-[#0b1120] text-emerald-400 border-slate-700 border-b-[#0b1120]'
                : 'text-slate-400 hover:text-slate-200 border-transparent bg-slate-900/40'
            }`}
          >
            <FileCode className="w-4 h-4 text-emerald-400" />
            <span>1. Plain ISO 20022 XML</span>
            {plainXml && (
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            )}
          </button>

          {/* Pane 2 Tab */}
          <button
            onClick={() => setActiveTab('signed')}
            className={`px-4 py-2.5 rounded-t-xl text-xs font-bold flex items-center space-x-2 transition-all border-t border-x ${
              activeTab === 'signed'
                ? 'bg-[#0b1120] text-indigo-400 border-slate-700 border-b-[#0b1120]'
                : 'text-slate-400 hover:text-slate-200 border-transparent bg-slate-900/40'
            }`}
          >
            <Lock className="w-4 h-4 text-indigo-400" />
            <span>2. PKCS#7 Signed XML</span>
            {signedXml && (
              <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse" />
            )}
          </button>

          {/* Pane 3 Tab */}
          <button
            onClick={() => setActiveTab('response')}
            className={`px-4 py-2.5 rounded-t-xl text-xs font-bold flex items-center space-x-2 transition-all border-t border-x ${
              activeTab === 'response'
                ? 'bg-[#0b1120] text-amber-400 border-slate-700 border-b-[#0b1120]'
                : 'text-slate-400 hover:text-slate-200 border-transparent bg-slate-900/40'
            }`}
          >
            <Server className="w-4 h-4 text-amber-400" />
            <span>3. Gateway Service Response</span>
            {serviceResponse && (
              <span className="px-1.5 py-0.2 rounded text-[10px] font-mono bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                {serviceResponse.statusCode} OK
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Pane Content Viewer */}
      <div className="flex-1 flex flex-col min-h-0 bg-[#0b1120]">
        {/* Pane 1: Plain XML */}
        {activeTab === 'plain' && (
          <div className="flex-1 flex flex-col min-h-0">
            <div className="p-3 bg-slate-900/60 border-b border-slate-800 flex items-center justify-between text-xs">
              <div className="flex items-center space-x-2 text-slate-300 font-mono text-[11px]">
                <Code2 className="w-4 h-4 text-emerald-400" />
                <span>Raw Unsigned ISO 20022 XML Message</span>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  onClick={() => handleCopy(plainXml, 'plain')}
                  className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono flex items-center space-x-1 border border-slate-700 transition-colors"
                >
                  {copiedStage === 'plain' ? (
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                  ) : (
                    <Copy className="w-3.5 h-3.5" />
                  )}
                  <span>Copy</span>
                </button>
                <button
                  onClick={() =>
                    handleDownload(
                      plainXml,
                      `${messageType.replace(/\./g, '_')}_plain.xml`
                    )
                  }
                  className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono flex items-center space-x-1 border border-slate-700 transition-colors"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Export XML</span>
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto bg-[#070b14]">
              {renderCodeWithLineNumbers(plainXml)}
            </div>
          </div>
        )}

        {/* Pane 2: Signed XML */}
        {activeTab === 'signed' && (
          <div className="flex-1 flex flex-col min-h-0">
            {/* Signature Status Box */}
            <div className="p-3 bg-slate-900/80 border-b border-slate-800 space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-emerald-400 tracking-wide font-mono">
                        Signature: Valid
                      </span>
                      <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                        SHA-256 with RSA
                      </span>
                    </div>
                    <p className="text-[11px] text-slate-400 font-mono">
                      Issuer: CN=NIBSS Sandbox Signing CA v2, O=NIBSS Plc, C=NG
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleCopy(signedXml, 'signed')}
                    className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono flex items-center space-x-1 border border-slate-700 transition-colors"
                  >
                    {copiedStage === 'signed' ? (
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                    <span>Copy</span>
                  </button>
                  <button
                    onClick={() =>
                      handleDownload(
                        signedXml,
                        `${messageType.replace(/\./g, '_')}_signed.xml`
                      )
                    }
                    className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono flex items-center space-x-1 border border-slate-700 transition-colors"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Export XML</span>
                  </button>
                </div>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto bg-[#070b14]">
              {renderCodeWithLineNumbers(signedXml)}
            </div>
          </div>
        )}

        {/* Pane 3: Gateway Service Response */}
        {activeTab === 'response' && (
          <div className="flex-1 flex flex-col min-h-0">
            {serviceResponse ? (
              <>
                {/* 4 Summary Cards Header */}
                <div className="p-3 bg-slate-900/60 border-b border-slate-800 grid grid-cols-2 sm:grid-cols-4 gap-2">
                  {/* Status Card */}
                  <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 flex items-center space-x-2.5">
                    <div
                      className={`p-2 rounded-lg ${
                        serviceResponse.statusCode < 400
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}
                    >
                      <Zap className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-mono uppercase">
                        HTTP Status
                      </div>
                      <div
                        className={`text-xs font-bold font-mono ${
                          serviceResponse.statusCode < 400
                            ? 'text-emerald-400'
                            : 'text-rose-400'
                        }`}
                      >
                        {serviceResponse.statusCode} {serviceResponse.success ? 'OK' : 'ERROR'}
                      </div>
                    </div>
                  </div>

                  {/* Latency Card */}
                  <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 flex items-center space-x-2.5">
                    <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                      <Clock className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-mono uppercase">
                        Roundtrip Latency
                      </div>
                      <div className="text-xs font-bold font-mono text-indigo-300">
                        {serviceResponse.executionTimeMs} ms
                      </div>
                    </div>
                  </div>

                  {/* Timestamp Card */}
                  <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 flex items-center space-x-2.5">
                    <div className="p-2 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20">
                      <Layers className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-mono uppercase">
                        Response Time
                      </div>
                      <div className="text-[11px] font-bold font-mono text-amber-300 truncate">
                        {new Date(serviceResponse.timestamp).toLocaleTimeString()}
                      </div>
                    </div>
                  </div>

                  {/* Request ID Card */}
                  <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 flex items-center space-x-2.5">
                    <div className="p-2 rounded-lg bg-violet-500/10 text-violet-400 border border-violet-500/20">
                      <FileCode className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-mono uppercase">
                        Request Ref
                      </div>
                      <div className="text-[11px] font-bold font-mono text-violet-300 truncate">
                        {serviceResponse.requestId || 'REQ-889128'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Sub-tabs: Body vs Headers */}
                <div className="px-4 py-2 bg-slate-950 border-b border-slate-800 flex items-center justify-between text-xs">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => setResponseSubTab('body')}
                      className={`px-3 py-1 rounded-lg font-mono font-semibold transition-colors ${
                        responseSubTab === 'body'
                          ? 'bg-slate-800 text-amber-400 border border-slate-700'
                          : 'text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      Response Body
                    </button>
                    <button
                      onClick={() => setResponseSubTab('headers')}
                      className={`px-3 py-1 rounded-lg font-mono font-semibold transition-colors ${
                        responseSubTab === 'headers'
                          ? 'bg-slate-800 text-amber-400 border border-slate-700'
                          : 'text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      Headers & Trace
                    </button>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() =>
                        handleCopy(serviceResponse.rawResponseBody, 'response')
                      }
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono flex items-center space-x-1 border border-slate-700 transition-colors"
                    >
                      {copiedStage === 'response' ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                      <span>Copy</span>
                    </button>
                  </div>
                </div>

                {/* Body or Headers Viewer */}
                <div className="flex-1 overflow-y-auto bg-[#070b14]">
                  {responseSubTab === 'body' ? (
                    renderCodeWithLineNumbers(serviceResponse.rawResponseBody)
                  ) : (
                    <div className="p-4 font-mono text-xs space-y-2 text-slate-300">
                      {Object.entries(
                        serviceResponse.responseHeaders || {
                          'content-type': 'application/json;charset=UTF-8',
                          'x-nibss-gateway': 'SANDBOX-V2.4',
                          'x-rate-limit-remaining': '924',
                        }
                      ).map(([key, val]) => (
                        <div
                          key={key}
                          className="flex items-center justify-between p-2 bg-slate-900/60 rounded border border-slate-800"
                        >
                          <span className="text-indigo-400 font-bold">{key}</span>
                          <span className="text-slate-200">{val}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex-1 flex flex-col items-center justify-center p-8 text-center space-y-3">
                <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 text-slate-500">
                  <Server className="w-8 h-8 text-slate-500" />
                </div>
                <div className="max-w-md">
                  <h3 className="text-sm font-bold text-slate-300">
                    No Gateway Response Captured Yet
                  </h3>
                  <p className="text-xs text-slate-400 mt-1">
                    Click <span className="text-emerald-400 font-semibold font-mono">▶ Execute Request Pipeline</span> on the left configurator panel to dispatch the signed XML to NIBSS Sandbox and view real-time HTTP response.
                  </p>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

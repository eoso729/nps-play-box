import React, { useState } from 'react';
import { Header } from './Header';
import { StatusBar } from './StatusBar';
import {
  GitCompare,
  Play,
  RotateCcw,
  FileCode,
  Check,
  Copy,
  Sparkles,
  ArrowRightLeft,
  FileDiff,
  Download,
} from 'lucide-react';

interface DiffLine {
  type: 'added' | 'removed' | 'modified' | 'unchanged';
  leftLineNumber?: number;
  rightLineNumber?: number;
  leftContent?: string;
  rightContent?: string;
}

export const XmlDiffChecker: React.FC = () => {
  const [leftXml, setLeftXml] = useState<string>(() => {
    return `<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.11">
  <GrpHdr>
    <MsgId>MSG/20260729/PAIN013/00109</MsgId>
    <CreDtTm>2026-07-29T21:30:00.000Z</CreDtTm>
    <NbOfTxs>1</NbOfTxs>
    <InitgPty>
      <Nm>NIBSS FINTECH GATEWAY LTD</Nm>
      <Id>
        <OrgId>
          <Othr>
            <Id>NIBSS001928</Id>
          </Othr>
        </OrgId>
      </Id>
    </InitgPty>
  </GrpHdr>
  <PmtInf>
    <PmtInfId>PMT/INF/2026/08912</PmtInfId>
    <PmtMtd>TRF</PmtMtd>
    <ReqdExctnDt>2026-07-30</ReqdExctnDt>
    <Dbtr>
      <Nm>Adebayo Ogunlesi</Nm>
      <Id>
        <PrvtId>
          <Othr>
            <Id>22198765432</Id>
            <SchmeNm>
              <Prtry>BVN</Prtry>
            </SchmeNm>
          </Othr>
        </PrvtId>
      </Id>
    </Dbtr>
    <DbtrAcct>
      <Id>
        <IBAN>NG12NIBSS011000998827361</IBAN>
      </Id>
    </DbtrAcct>
  </PmtInf>
</Document>`;
  });

  const [rightXml, setRightXml] = useState<string>(() => {
    return `<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.11">
  <GrpHdr>
    <MsgId>MSG/20260729/PAIN013/00109</MsgId>
    <CreDtTm>2026-07-29T22:00:00.000Z</CreDtTm>
    <NbOfTxs>1</NbOfTxs>
    <InitgPty>
      <Nm>NIBSS FINTECH GATEWAY LTD</Nm>
      <Id>
        <OrgId>
          <Othr>
            <Id>NIBSS001928-PROD</Id>
          </Othr>
        </OrgId>
      </Id>
    </InitgPty>
  </GrpHdr>
  <PmtInf>
    <PmtInfId>PMT/INF/2026/08912</PmtInfId>
    <PmtMtd>TRF</PmtMtd>
    <ReqdExctnDt>2026-08-01</ReqdExctnDt>
    <Dbtr>
      <Nm>Adebayo Ogunlesi</Nm>
      <Id>
        <PrvtId>
          <Othr>
            <Id>22198765432</Id>
            <SchmeNm>
              <Prtry>NIN</Prtry>
            </SchmeNm>
          </Othr>
        </PrvtId>
      </Id>
    </Dbtr>
    <DbtrAcct>
      <Id>
        <IBAN>NG12NIBSS011000998827361</IBAN>
      </Id>
    </DbtrAcct>
  </PmtInf>
</Document>`;
  });

  const [diffResults, setDiffResults] = useState<DiffLine[] | null>(null);
  const [stats, setStats] = useState({ added: 0, removed: 0, modified: 0 });

  const computeDiff = () => {
    const leftLines = leftXml.split('\n');
    const rightLines = rightXml.split('\n');

    const maxLen = Math.max(leftLines.length, rightLines.length);
    const results: DiffLine[] = [];
    let addedCount = 0;
    let removedCount = 0;
    let modifiedCount = 0;

    let i = 0;
    let j = 0;

    while (i < leftLines.length || j < rightLines.length) {
      const left = leftLines[i];
      const right = rightLines[j];

      if (left !== undefined && right !== undefined) {
        if (left.trim() === right.trim()) {
          results.push({
            type: 'unchanged',
            leftLineNumber: i + 1,
            rightLineNumber: j + 1,
            leftContent: left,
            rightContent: right,
          });
          i++;
          j++;
        } else {
          // Lines differ
          results.push({
            type: 'modified',
            leftLineNumber: i + 1,
            rightLineNumber: j + 1,
            leftContent: left,
            rightContent: right,
          });
          modifiedCount++;
          i++;
          j++;
        }
      } else if (left !== undefined) {
        results.push({
          type: 'removed',
          leftLineNumber: i + 1,
          leftContent: left,
        });
        removedCount++;
        i++;
      } else {
        results.push({
          type: 'added',
          rightLineNumber: j + 1,
          rightContent: right,
        });
        addedCount++;
        j++;
      }
    }

    setDiffResults(results);
    setStats({ added: addedCount, removed: removedCount, modified: modifiedCount });
  };

  const handleClearRight = () => {
    setRightXml('');
    setDiffResults(null);
  };

  return (
    <div className="min-h-screen bg-[#0b0f19] text-slate-100 flex flex-col font-sans">
      <Header />

      <main className="flex-1 flex flex-col p-4 sm:p-6 space-y-4 max-w-7xl w-full mx-auto">
        {/* Title Bar */}
        <div className="flex items-center justify-between bg-[#0f172a] p-4 rounded-2xl border border-[#1e293b] shadow-sm">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <GitCompare className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-base font-bold text-white tracking-tight">
                ISO 20022 XML Diff Checker
              </h1>
              <p className="text-xs text-slate-400">
                Compare generated plain ISO 20022 XML against external sample reference XML with line-level diff highlighting.
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={handleClearRight}
              className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold flex items-center space-x-1.5 border border-slate-700 transition-colors"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Clear Reference</span>
            </button>

            <button
              onClick={computeDiff}
              className="px-4 py-2 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white text-xs font-bold shadow-lg shadow-emerald-950/40 flex items-center space-x-2 transition-all"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>Run Diff Analysis</span>
            </button>
          </div>
        </div>

        {/* Inputs Split Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 flex-1">
          {/* Left Editor: Generated Plain XML */}
          <div className="bg-[#0f172a] border border-[#1e293b] rounded-2xl flex flex-col overflow-hidden">
            <div className="p-3 bg-[#090d16] border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center space-x-2 text-xs font-bold text-slate-200">
                <FileCode className="w-4 h-4 text-emerald-400" />
                <span>Generated Plain XML (Left Pane)</span>
              </div>
              <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-800 text-slate-400">
                Source Document
              </span>
            </div>
            <textarea
              value={leftXml}
              onChange={(e) => setLeftXml(e.target.value)}
              className="w-full flex-1 p-4 bg-[#070b14] text-xs font-mono text-slate-200 focus:outline-none resize-none custom-scrollbar min-h-[250px]"
              placeholder="Paste or generate XML here..."
            />
          </div>

          {/* Right Editor: Reference XML */}
          <div className="bg-[#0f172a] border border-[#1e293b] rounded-2xl flex flex-col overflow-hidden">
            <div className="p-3 bg-[#090d16] border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center space-x-2 text-xs font-bold text-slate-200">
                <FileDiff className="w-4 h-4 text-indigo-400" />
                <span>External Reference XML (Right Pane - Paste Here)</span>
              </div>
              <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-800 text-slate-400">
                Target / Spec Reference
              </span>
            </div>
            <textarea
              value={rightXml}
              onChange={(e) => setRightXml(e.target.value)}
              className="w-full flex-1 p-4 bg-[#070b14] text-xs font-mono text-slate-200 focus:outline-none resize-none custom-scrollbar min-h-[250px]"
              placeholder="Paste external reference XML here..."
            />
          </div>
        </div>

        {/* Diff Results Output */}
        {diffResults && (
          <div className="bg-[#0f172a] border border-[#1e293b] rounded-2xl overflow-hidden shadow-lg space-y-0">
            {/* Diff Header Stats */}
            <div className="p-4 bg-[#090d16] border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <h3 className="text-sm font-bold text-white tracking-tight">
                  Line-by-Line Diff Analysis
                </h3>
                <div className="flex items-center space-x-2 text-xs font-mono">
                  <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-bold">
                    +{stats.added} added
                  </span>
                  <span className="px-2 py-0.5 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20 font-bold">
                    -{stats.removed} removed
                  </span>
                  <span className="px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 font-bold">
                    ~{stats.modified} modified
                  </span>
                </div>
              </div>
            </div>

            {/* Line Diff List */}
            <div className="overflow-x-auto max-h-[450px] overflow-y-auto font-mono text-xs custom-scrollbar bg-[#070b14]">
              {diffResults.map((line, idx) => {
                if (line.type === 'unchanged') {
                  return (
                    <div
                      key={idx}
                      className="flex items-center px-4 py-1 hover:bg-slate-900/50 text-slate-400 border-b border-slate-900/40"
                    >
                      <div className="w-12 text-right pr-4 text-slate-600 select-none">
                        {line.leftLineNumber}
                      </div>
                      <div className="w-12 text-right pr-4 text-slate-600 select-none">
                        {line.rightLineNumber}
                      </div>
                      <div className="flex-1 whitespace-pre">{line.leftContent}</div>
                    </div>
                  );
                }

                if (line.type === 'modified') {
                  return (
                    <div
                      key={idx}
                      className="flex flex-col bg-amber-950/20 border-l-4 border-amber-500 text-amber-200 border-b border-amber-900/30"
                    >
                      <div className="flex items-center px-4 py-1 bg-rose-950/30 text-rose-300">
                        <div className="w-12 text-right pr-4 text-rose-500 select-none">
                          {line.leftLineNumber}
                        </div>
                        <div className="w-12 text-right pr-4 text-slate-700 select-none">
                          -
                        </div>
                        <div className="flex-1 whitespace-pre font-bold">
                          - {line.leftContent}
                        </div>
                      </div>
                      <div className="flex items-center px-4 py-1 bg-emerald-950/30 text-emerald-300">
                        <div className="w-12 text-right pr-4 text-slate-700 select-none">
                          -
                        </div>
                        <div className="w-12 text-right pr-4 text-emerald-500 select-none">
                          {line.rightLineNumber}
                        </div>
                        <div className="flex-1 whitespace-pre font-bold">
                          + {line.rightContent}
                        </div>
                      </div>
                    </div>
                  );
                }

                if (line.type === 'removed') {
                  return (
                    <div
                      key={idx}
                      className="flex items-center px-4 py-1 bg-rose-950/30 text-rose-300 border-l-4 border-rose-500 border-b border-rose-900/30"
                    >
                      <div className="w-12 text-right pr-4 text-rose-400 select-none">
                        {line.leftLineNumber}
                      </div>
                      <div className="w-12 text-right pr-4 text-slate-700 select-none">
                        -
                      </div>
                      <div className="flex-1 whitespace-pre font-bold">
                        - {line.leftContent}
                      </div>
                    </div>
                  );
                }

                if (line.type === 'added') {
                  return (
                    <div
                      key={idx}
                      className="flex items-center px-4 py-1 bg-emerald-950/30 text-emerald-300 border-l-4 border-emerald-500 border-b border-emerald-900/30"
                    >
                      <div className="w-12 text-right pr-4 text-slate-700 select-none">
                        -
                      </div>
                      <div className="w-12 text-right pr-4 text-emerald-400 select-none">
                        {line.rightLineNumber}
                      </div>
                      <div className="flex-1 whitespace-pre font-bold">
                        + {line.rightContent}
                      </div>
                    </div>
                  );
                }

                return null;
              })}
            </div>
          </div>
        )}
      </main>

      <StatusBar />
    </div>
  );
};

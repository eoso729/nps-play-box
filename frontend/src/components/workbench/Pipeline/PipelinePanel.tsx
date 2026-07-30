import React, { useState } from 'react';
import { XmlPane } from './XmlPane';
import { PipelineResult } from '../../../types/workbench';

interface PipelinePanelProps {
  result: PipelineResult;
}

export const PipelinePanel: React.FC<PipelinePanelProps> = ({ result }) => {
  const { plainXml, signedXml, isLoading, error } = result;
  const [activeTab, setActiveTab] = useState<'plain' | 'signed'>('plain');

  const plainStatus = isLoading ? 'idle' : plainXml ? 'gen' : error ? 'error' : 'idle';
  const signedStatus = isLoading ? 'idle' : signedXml ? 'signed' : error ? 'error' : 'idle';

  return (
    <div className="flex flex-col flex-1 min-w-0 overflow-hidden bg-white">
      {/* Sleek Tab Bar */}
      <div className="h-[48px] border-b border-[#e4e9e6] flex items-center justify-between px-6 bg-white flex-shrink-0">
        <div className="flex gap-6 h-full">
          <button
            type="button"
            onClick={() => setActiveTab('plain')}
            className={`h-full border-b-2 text-[13px] font-semibold transition-all px-1 cursor-pointer flex items-center gap-2 outline-none ${
              activeTab === 'plain'
                ? 'border-[#22a05a] text-[#15803d]'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <span className={`w-5 h-5 rounded-full flex items-center justify-center font-bold text-[10.5px] transition-colors ${
              activeTab === 'plain' ? 'bg-[#e6f6ec] text-[#15803d]' : 'bg-gray-100 text-gray-500'
            }`}>
              1
            </span>
            Plaine XML
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('signed')}
            className={`h-full border-b-2 text-[13px] font-semibold transition-all px-1 cursor-pointer flex items-center gap-2 outline-none ${
              activeTab === 'signed'
                ? 'border-[#6366f1] text-[#6366f1]'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <span className={`w-5 h-5 rounded-full flex items-center justify-center font-bold text-[10.5px] transition-colors ${
              activeTab === 'signed' ? 'bg-[#eef0fe] text-[#6366f1]' : 'bg-gray-100 text-gray-500'
            }`}>
              2
            </span>
            Signened XML
          </button>
        </div>

        {/* Tab Status Badge */}
        <div className="text-[12px] font-medium text-gray-500 flex items-center">
          {activeTab === 'plain' ? (
            <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11.5px] font-semibold ${
              plainStatus === 'gen' ? 'bg-[#e6f6ec] text-[#15803d]' : 'bg-gray-100 text-gray-500'
            }`}>
              {plainStatus === 'gen' ? 'Generated' : 'Awaiting input'}
            </span>
          ) : (
            <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11.5px] font-semibold ${
              signedStatus === 'signed' ? 'bg-[#eef0fe] text-[#6366f1]' : 'bg-gray-100 text-gray-500'
            }`}>
              {signedStatus === 'signed' ? 'Signed' : 'Awaiting input'}
            </span>
          )}
        </div>
      </div>

      {/* Pane Content */}
      <div className="flex-1 min-h-0 overflow-hidden flex flex-col">
        {activeTab === 'plain' ? (
          <XmlPane
            title="Plaine XML"
            stageNum={1}
            stageColor="#16a34a"
            statusText={plainStatus === 'gen' ? 'Generated' : plainStatus === 'error' ? 'Error' : 'Awaiting input'}
            statusVariant={plainStatus as any}
            xml={plainXml}
            isLoading={isLoading}
          />
        ) : (
          <XmlPane
            title="Signened XML"
            stageNum={2}
            stageColor="#6366f1"
            statusText={signedStatus === 'signed' ? 'Signed' : signedStatus === 'error' ? 'Error' : 'Awaiting input'}
            statusVariant={signedStatus as any}
            xml={signedXml}
            isLoading={isLoading}
          />
        )}
      </div>
    </div>
  );
};

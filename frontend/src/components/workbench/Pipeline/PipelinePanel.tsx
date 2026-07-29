import React from 'react';
import { XmlPane } from './XmlPane';
import { SignatureBox } from './SignatureBox';
import { ResponsePane } from './ResponsePane';
import { PipelineResult } from '../../../types/workbench';

interface PipelinePanelProps {
  result: PipelineResult;
}

export const PipelinePanel: React.FC<PipelinePanelProps> = ({ result }) => {
  const { plainXml, signedXml, generatedAt, serviceResponse, isLoading, error } = result;

  const plainStatus = isLoading ? 'idle' : plainXml ? 'gen' : error ? 'error' : 'idle';
  const signedStatus = isLoading ? 'idle' : signedXml ? 'signed' : error ? 'error' : 'idle';

  return (
    <div className="flex flex-row flex-1 min-w-0 overflow-hidden">
      {/* PANE 1: Plain XML */}
      <XmlPane
        title="Plain ISO 20022 XML"
        stageNum={1}
        stageColor="#16a34a"
        statusText={plainStatus === 'gen' ? 'Generated' : plainStatus === 'error' ? 'Error' : 'Awaiting input'}
        statusVariant={plainStatus as any}
        xml={plainXml}
        isLoading={isLoading}
      />

      {/* PANE 2: Signed XML */}
      <XmlPane
        title="PKCS#7 Signed XML"
        stageNum={2}
        stageColor="#6366f1"
        statusText={signedStatus === 'signed' ? 'Signed' : signedStatus === 'error' ? 'Error' : 'Awaiting input'}
        statusVariant={signedStatus as any}
        xml={signedXml}
        isLoading={isLoading}
        footer={
          signedXml ? (
            <SignatureBox signedAt={generatedAt} visible={!!signedXml} />
          ) : undefined
        }
      />

      {/* PANE 3: Gateway Response */}
      <ResponsePane
        serviceResponse={serviceResponse ?? null}
        isLoading={isLoading && !plainXml}
      />
    </div>
  );
};

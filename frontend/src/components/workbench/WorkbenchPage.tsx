import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { StatusBar } from './StatusBar';
import { MessageConfigurator } from './MessageConfigurator';
import { InspectionPipeline } from './InspectionPipeline';
import {
  MESSAGE_SPECS,
  MessageFormData,
  MessageTypeSpec,
  ServicePushResult,
} from '../../types/workbench';
import { generateMessageXml, sendRequestPipeline } from '../../api/workbench';

export const WorkbenchPage: React.FC = () => {
  const { messageId } = useParams<{ messageId?: string }>();
  const navigate = useNavigate();

  // Find spec by URL parameter, default to pain013
  const activeSpec: MessageTypeSpec =
    MESSAGE_SPECS.find((s) => s.id === messageId) || MESSAGE_SPECS[0];

  // State for form data
  const [formData, setFormData] = useState<MessageFormData>(
    activeSpec.defaultValues
  );

  // State for inspection pipeline outputs
  const [plainXml, setPlainXml] = useState<string>('');
  const [signedXml, setSignedXml] = useState<string>('');
  const [serviceResponse, setServiceResponse] =
    useState<ServicePushResult | null>(null);

  // Loading states
  const [isGenerating, setIsGenerating] = useState(false);
  const [isExecuting, setIsExecuting] = useState(false);

  // Sync form defaults when active message specification changes via URL route
  useEffect(() => {
    setFormData(activeSpec.defaultValues);
    setPlainXml('');
    setSignedXml('');
    setServiceResponse(null);
  }, [activeSpec.id]);

  const handleGenerateXml = async () => {
    setIsGenerating(true);
    try {
      const res = await generateMessageXml(activeSpec, formData);
      setPlainXml(res.plainXml);
      setSignedXml(res.signedXml);
    } catch (err) {
      console.error('Generation error', err);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleExecutePipeline = async () => {
    setIsExecuting(true);
    try {
      const res = await sendRequestPipeline(activeSpec, formData);
      setPlainXml(res.plainXml);
      setSignedXml(res.signedXml);
      setServiceResponse(res.serviceResponse);
    } catch (err) {
      console.error('Execution pipeline error', err);
    } finally {
      setIsExecuting(false);
    }
  };

  const handleClearForm = () => {
    setFormData({
      msgId: '',
      creDtTm: '',
      initgPtyNm: '',
      initgPtyId: '',
      pmtInfId: '',
      reqdExctnDt: '',
      dbtrNm: '',
      dbtrAcctIban: '',
      dbtrAgentBic: '',
      endToEndId: '',
      instdAmt: '',
      cdtrNm: '',
      cdtrAcctIban: '',
      cdtrAgentBic: '',
      purpCd: '',
      acctDesgn: '',
      idType: 'BVN',
      idValue: '',
      acctTierLevel: '',
      channelCode: '',
      latitude: '',
      longitude: '',
    });
  };

  const handleLoadPreFilled = () => {
    setFormData({
      ...activeSpec.defaultValues,
      msgId: `MSG/${new Date().toISOString().slice(0, 10).replace(/-/g, '')}/${activeSpec.id.toUpperCase()}/${Math.floor(1000 + Math.random() * 9000)}`,
      creDtTm: new Date().toISOString(),
    });
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-[#0b0f19] text-slate-100 overflow-hidden font-sans">
      <Header />

      {/* Main Body Layout: Flex Row containing Sidebar, Main Workbench, and Statusbar at bottom */}
      <div className="flex-1 flex flex-row overflow-hidden relative">
        <Sidebar />

        {/* Main Content (Split View: Panel 1 Configurator | Panel 2 Inspection Pipeline) */}
        <main className="flex-1 grid grid-cols-1 lg:grid-cols-2 overflow-hidden bg-[#0b0f19]">
          <MessageConfigurator
            spec={activeSpec}
            formData={formData}
            setFormData={setFormData}
            onGenerateXml={handleGenerateXml}
            onExecutePipeline={handleExecutePipeline}
            onClearForm={handleClearForm}
            onLoadPreFilled={handleLoadPreFilled}
            isGenerating={isGenerating}
            isExecuting={isExecuting}
          />

          <InspectionPipeline
            plainXml={plainXml}
            signedXml={signedXml}
            serviceResponse={serviceResponse}
            messageType={activeSpec.code}
            messageId={formData.msgId || activeSpec.id}
          />
        </main>
      </div>

      <StatusBar />
    </div>
  );
};

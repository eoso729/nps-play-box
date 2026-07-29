import React from 'react';
import { MessageFormData, MessageTypeSpec } from '../../types/workbench';
import {
  RotateCcw,
  Sparkles,
  Play,
  FileCode,
  Building,
  CreditCard,
  UserCheck,
  MapPin,
  Clock,
  ShieldAlert,
} from 'lucide-react';

interface MessageConfiguratorProps {
  spec: MessageTypeSpec;
  formData: MessageFormData;
  setFormData: React.Dispatch<React.SetStateAction<MessageFormData>>;
  onGenerateXml: () => void;
  onExecutePipeline: () => void;
  onClearForm: () => void;
  onLoadPreFilled: () => void;
  isGenerating: boolean;
  isExecuting: boolean;
}

export const MessageConfigurator: React.FC<MessageConfiguratorProps> = ({
  spec,
  formData,
  setFormData,
  onGenerateXml,
  onExecutePipeline,
  onClearForm,
  onLoadPreFilled,
  isGenerating,
  isExecuting,
}) => {
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="panel-configurator flex flex-col h-full bg-[#0f172a] border-r border-[#1e293b] overflow-hidden">
      {/* Header */}
      <div className="p-4 border-b border-[#1e293b] bg-[#090d16] flex items-center justify-between shrink-0">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <FileCode className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-sm font-bold text-white tracking-tight">
                {spec.name} Configurator
              </h2>
              <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                ISO:{spec.code.toUpperCase()}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-medium">
              {spec.description}
            </p>
          </div>
        </div>

        <button
          onClick={onClearForm}
          className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium flex items-center space-x-1.5 transition-colors border border-slate-700"
          title="Clear all fields"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Clear Form</span>
        </button>
      </div>

      {/* Form Body with 4 Fieldset Sections */}
      <div className="flex-1 overflow-y-auto p-4 space-y-6 custom-scrollbar">
        {/* Section 1: Message Header */}
        <fieldset className="border border-slate-800 rounded-xl p-4 bg-slate-900/40 space-y-4">
          <legend className="px-2 text-xs font-bold text-emerald-400 uppercase tracking-wider flex items-center space-x-1.5 font-mono">
            <Clock className="w-3.5 h-3.5" />
            <span>1. Message Header (GrpHdr)</span>
          </legend>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                MsgId (Message ID) *
              </label>
              <input
                type="text"
                name="msgId"
                value={formData.msgId}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
                placeholder="MSG/20260729/PAIN013/001"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                CreDtTm (Creation Timestamp) *
              </label>
              <input
                type="text"
                name="creDtTm"
                value={formData.creDtTm}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                InitgPty Nm (Initiating Party Name) *
              </label>
              <input
                type="text"
                name="initgPtyNm"
                value={formData.initgPtyNm}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                InitgPty Id (Initiating Party ID) *
              </label>
              <input
                type="text"
                name="initgPtyId"
                value={formData.initgPtyId}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
          </div>
        </fieldset>

        {/* Section 2: Payment Information */}
        <fieldset className="border border-slate-800 rounded-xl p-4 bg-slate-900/40 space-y-4">
          <legend className="px-2 text-xs font-bold text-indigo-400 uppercase tracking-wider flex items-center space-x-1.5 font-mono">
            <Building className="w-3.5 h-3.5" />
            <span>2. Payment Information (PmtInf)</span>
          </legend>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                PmtInfId (Payment Info ID) *
              </label>
              <input
                type="text"
                name="pmtInfId"
                value={formData.pmtInfId}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                ReqdExctnDt (Execution Date) *
              </label>
              <input
                type="date"
                name="reqdExctnDt"
                value={formData.reqdExctnDt}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                Dbtr Nm (Debtor Name) *
              </label>
              <input
                type="text"
                name="dbtrNm"
                value={formData.dbtrNm}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Dbtr Acct IBAN / NUBAN *
              </label>
              <input
                type="text"
                name="dbtrAcctIban"
                value={formData.dbtrAcctIban}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Dbtr Agent BIC / Member ID *
              </label>
              <input
                type="text"
                name="dbtrAgentBic"
                value={formData.dbtrAgentBic}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
          </div>
        </fieldset>

        {/* Section 3: Transaction & Creditor Details */}
        <fieldset className="border border-slate-800 rounded-xl p-4 bg-slate-900/40 space-y-4">
          <legend className="px-2 text-xs font-bold text-amber-400 uppercase tracking-wider flex items-center space-x-1.5 font-mono">
            <CreditCard className="w-3.5 h-3.5" />
            <span>3. Transaction & Creditor Details (CdtTrfTxInf)</span>
          </legend>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                EndToEndId (End to End Reference) *
              </label>
              <input
                type="text"
                name="endToEndId"
                value={formData.endToEndId}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                InstdAmt (Instructed Amount - NGN) *
              </label>
              <input
                type="text"
                name="instdAmt"
                value={formData.instdAmt}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                Cdtr Nm (Creditor Name) *
              </label>
              <input
                type="text"
                name="cdtrNm"
                value={formData.cdtrNm}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Cdtr Acct IBAN / NUBAN *
              </label>
              <input
                type="text"
                name="cdtrAcctIban"
                value={formData.cdtrAcctIban}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Cdtr Agent BIC / Member ID *
              </label>
              <input
                type="text"
                name="cdtrAgentBic"
                value={formData.cdtrAgentBic}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Purp Cd (Purpose Code) *
              </label>
              <input
                type="text"
                name="purpCd"
                value={formData.purpCd}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
          </div>
        </fieldset>

        {/* Section 4: Supplementary Data */}
        <fieldset className="border border-slate-800 rounded-xl p-4 bg-slate-900/40 space-y-4">
          <legend className="px-2 text-xs font-bold text-violet-400 uppercase tracking-wider flex items-center space-x-1.5 font-mono">
            <UserCheck className="w-3.5 h-3.5" />
            <span>4. Supplementary Data (NIBSS Verification)</span>
          </legend>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Acct Desgn (Account Designation)
              </label>
              <input
                type="text"
                name="acctDesgn"
                value={formData.acctDesgn}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Id Type (Identity Type)
              </label>
              <select
                name="idType"
                value={formData.idType}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              >
                <option value="BVN">BVN (Bank Verification Number)</option>
                <option value="NIN">NIN (National Identity Number)</option>
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Id Value ({formData.idType} Value)
              </label>
              <input
                type="text"
                name="idValue"
                value={formData.idValue}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Acct Tier Level
              </label>
              <input
                type="text"
                name="acctTierLevel"
                value={formData.acctTierLevel}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono">
                Channel Code
              </label>
              <input
                type="text"
                name="channelCode"
                value={formData.channelCode}
                onChange={handleChange}
                className="w-full bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-300 mb-1 font-mono flex items-center space-x-1">
                <MapPin className="w-3 h-3 text-slate-400" />
                <span>Coordinates (Lat / Long)</span>
              </label>
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="text"
                  name="latitude"
                  value={formData.latitude}
                  onChange={handleChange}
                  placeholder="Lat"
                  className="bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-2 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
                />
                <input
                  type="text"
                  name="longitude"
                  value={formData.longitude}
                  onChange={handleChange}
                  placeholder="Long"
                  className="bg-slate-950 border border-slate-800 focus:border-violet-500 rounded-lg px-2 py-1.5 text-xs text-slate-100 font-mono focus:outline-none transition-colors"
                />
              </div>
            </div>
          </div>
        </fieldset>
      </div>

      {/* Action Bar Footer */}
      <div className="p-4 border-t border-[#1e293b] bg-[#090d16] space-y-2 shrink-0">
        <button
          type="button"
          onClick={onLoadPreFilled}
          className="w-full py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold flex items-center justify-center space-x-2 border border-slate-700 transition-colors shadow-sm"
        >
          <Sparkles className="w-4 h-4 text-emerald-400" />
          <span>⤓ Load Pre-filled Spec Data</span>
        </button>

        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={onGenerateXml}
            disabled={isGenerating}
            className="py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-emerald-400 hover:text-emerald-300 text-xs font-bold border border-emerald-500/30 flex items-center justify-center space-x-2 transition-all disabled:opacity-50"
          >
            {isGenerating ? (
              <div className="w-4 h-4 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin" />
            ) : (
              <FileCode className="w-4 h-4" />
            )}
            <span>Generate XML</span>
          </button>

          <button
            type="button"
            onClick={onExecutePipeline}
            disabled={isExecuting}
            className="py-2.5 px-3 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white text-xs font-bold shadow-lg shadow-emerald-950/50 flex items-center justify-center space-x-2 transition-all disabled:opacity-50"
          >
            {isExecuting ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Play className="w-4 h-4 fill-white" />
            )}
            <span>▶ Execute Request Pipeline</span>
          </button>
        </div>
      </div>
    </div>
  );
};

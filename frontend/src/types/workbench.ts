export type MessageCategory =
  | 'Payment Activation'
  | 'Payment Initiation'
  | 'Mandate Management'
  | 'Direct Debit Operations'
  | 'Credit Transfer'
  | 'Account Services';

export interface MessageTypeSpec {
  id: string; // e.g. 'pain013'
  code: string; // e.g. 'pain.013.001.11'
  name: string; // e.g. 'Payment Activation'
  category: MessageCategory;
  description: string;
  generateEndpoint: string;
  sendEndpoint: string;
  defaultValues: MessageFormData;
}

export interface MessageFormData {
  // Message Header
  msgId: string;
  creDtTm: string;
  initgPtyNm: string;
  initgPtyId: string;

  // Payment Information
  pmtInfId: string;
  reqdExctnDt: string;
  dbtrNm: string;
  dbtrAcctIban: string;
  dbtrAgentBic: string;

  // Transaction & Creditor Details
  endToEndId: string;
  instdAmt: string;
  cdtrNm: string;
  cdtrAcctIban: string;
  cdtrAgentBic: string;
  purpCd: string;

  // Supplementary Data
  acctDesgn: string;
  idType: 'BVN' | 'NIN';
  idValue: string;
  acctTierLevel: string;
  channelCode: string;
  latitude: string;
  longitude: string;
}

export interface XmlGenerationResult {
  messageType: string;
  messageId: string;
  plainXml: string;
  signedXml: string;
  generatedAt: string;
}

export interface ServicePushResult {
  statusCode: number;
  rawResponseBody: string;
  executionTimeMs: number;
  success: boolean;
  timestamp: string;
  requestId?: string;
  responseHeaders?: Record<string, string>;
}

export interface MessageSendResult {
  messageType: string;
  messageId: string;
  plainXml: string;
  signedXml: string;
  serviceResponse: ServicePushResult;
}

export const MESSAGE_SPECS: MessageTypeSpec[] = [
  {
    id: 'pain013',
    code: 'pain.013.001.11',
    name: 'Payment Activation',
    category: 'Payment Activation',
    description: 'ISO 20022 Payment Activation Request (NIBSS Sandbox)',
    generateEndpoint: '/api/generate/payment-activation-pain013',
    sendEndpoint: '/api/payment-activation-pain013',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN013/00109',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'NIBSS FINTECH GATEWAY LTD',
      initgPtyId: 'NIBSS001928',
      pmtInfId: 'PMT/INF/2026/08912',
      reqdExctnDt: '2026-07-30',
      dbtrNm: 'Adebayo Ogunlesi',
      dbtrAcctIban: 'NG12NIBSS011000998827361',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'E2E/NIBSS/20260729/99182',
      instdAmt: '250000.00',
      cdtrNm: 'Zenith Pay Merchant',
      cdtrAcctIban: 'NG88NIBSS057001122334455',
      cdtrAgentBic: 'ZEIBNGLAXXX',
      purpCd: 'CASH',
      acctDesgn: 'CORPORATE_PREMIUM',
      idType: 'BVN',
      idValue: '22198765432',
      acctTierLevel: 'TIER_3',
      channelCode: 'MOBILE_APP',
      latitude: '6.5244',
      longitude: '3.3792',
    },
  },
  {
    id: 'pain001',
    code: 'pain.001.001.11',
    name: 'Payment Initiation',
    category: 'Payment Initiation',
    description: 'ISO 20022 Customer Credit Transfer Initiation',
    generateEndpoint: '/api/generate/payment-initiation-pain001',
    sendEndpoint: '/api/payment-initiation-pain001',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN001/00881',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'PAYMENT HUB SERVICES',
      initgPtyId: 'HUB99201',
      pmtInfId: 'PMT/INF/INIT/00192',
      reqdExctnDt: '2026-07-30',
      dbtrNm: 'Funke Akindele',
      dbtrAcctIban: 'NG44NIBSS033009988776655',
      dbtrAgentBic: 'UBNANGXX',
      endToEndId: 'E2E/INIT/20260729/77621',
      instdAmt: '120000.00',
      cdtrNm: 'Konga Retail Online',
      cdtrAcctIban: 'NG99NIBSS044002233445566',
      cdtrAgentBic: 'GTBINGLAXXX',
      purpCd: 'SUPP',
      acctDesgn: 'INDIVIDUAL_SAVINGS',
      idType: 'NIN',
      idValue: '10987654321',
      acctTierLevel: 'TIER_2',
      channelCode: 'WEB_PORTAL',
      latitude: '9.0765',
      longitude: '7.3986',
    },
  },
  {
    id: 'pain009',
    code: 'pain.009.001.07',
    name: 'Mandate Creation',
    category: 'Mandate Management',
    description: 'ISO 20022 Mandate Initiation Request',
    generateEndpoint: '/api/generate/mandate-creation-pain009',
    sendEndpoint: '/api/mandate-creation-pain009',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN009/00012',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'MICROFINANCE CREDIT ORG',
      initgPtyId: 'MFI882910',
      pmtInfId: 'MND/INF/00112',
      reqdExctnDt: '2026-08-01',
      dbtrNm: 'Chidi Mokeme',
      dbtrAcctIban: 'NG55NIBSS011005544332211',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'MND/REF/20260729/00099',
      instdAmt: '45000.00',
      cdtrNm: 'Utility Power Co.',
      cdtrAcctIban: 'NG11NIBSS022007788990011',
      cdtrAgentBic: 'ACBNNGLAXXX',
      purpCd: 'UTIL',
      acctDesgn: 'CURRENT_ACCOUNT',
      idType: 'BVN',
      idValue: '22887766554',
      acctTierLevel: 'TIER_3',
      channelCode: 'USSD',
      latitude: '6.4531',
      longitude: '3.3958',
    },
  },
  {
    id: 'pain010',
    code: 'pain.010.001.07',
    name: 'Mandate Amendment',
    category: 'Mandate Management',
    description: 'ISO 20022 Mandate Amendment Request',
    generateEndpoint: '/api/generate/mandate-amendment-pain010',
    sendEndpoint: '/api/mandate-amendment-pain010',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN010/00045',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'MICROFINANCE CREDIT ORG',
      initgPtyId: 'MFI882910',
      pmtInfId: 'MND/AMD/INF/0045',
      reqdExctnDt: '2026-08-05',
      dbtrNm: 'Chidi Mokeme',
      dbtrAcctIban: 'NG55NIBSS011005544332211',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'MND/REF/20260729/00099',
      instdAmt: '60000.00',
      cdtrNm: 'Utility Power Co. (Updated)',
      cdtrAcctIban: 'NG11NIBSS022007788990011',
      cdtrAgentBic: 'ACBNNGLAXXX',
      purpCd: 'UTIL',
      acctDesgn: 'CURRENT_ACCOUNT',
      idType: 'BVN',
      idValue: '22887766554',
      acctTierLevel: 'TIER_3',
      channelCode: 'WEB_PORTAL',
      latitude: '6.4531',
      longitude: '3.3958',
    },
  },
  {
    id: 'pain011',
    code: 'pain.011.001.07',
    name: 'Mandate Cancellation',
    category: 'Mandate Management',
    description: 'ISO 20022 Mandate Cancellation Request',
    generateEndpoint: '/api/generate/mandate-cancellation-pain011',
    sendEndpoint: '/api/mandate-cancellation-pain011',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN011/00089',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'MICROFINANCE CREDIT ORG',
      initgPtyId: 'MFI882910',
      pmtInfId: 'MND/CNC/INF/0089',
      reqdExctnDt: '2026-08-10',
      dbtrNm: 'Chidi Mokeme',
      dbtrAcctIban: 'NG55NIBSS011005544332211',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'MND/REF/20260729/00099',
      instdAmt: '0.00',
      cdtrNm: 'Utility Power Co.',
      cdtrAcctIban: 'NG11NIBSS022007788990011',
      cdtrAgentBic: 'ACBNNGLAXXX',
      purpCd: 'CNCL',
      acctDesgn: 'CURRENT_ACCOUNT',
      idType: 'BVN',
      idValue: '22887766554',
      acctTierLevel: 'TIER_3',
      channelCode: 'MOBILE_APP',
      latitude: '6.4531',
      longitude: '3.3958',
    },
  },
  {
    id: 'pain008',
    code: 'pain.008.001.10',
    name: 'Direct Debit Request',
    category: 'Direct Debit Operations',
    description: 'ISO 20022 Customer Direct Debit Initiation',
    generateEndpoint: '/api/generate/direct-debit-pain008',
    sendEndpoint: '/api/direct-debit-pain008',
    defaultValues: {
      msgId: 'MSG/20260729/PAIN008/00312',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'TELECOM BILLING AG',
      initgPtyId: 'TEL99281',
      pmtInfId: 'DD/INF/2026/00312',
      reqdExctnDt: '2026-07-31',
      dbtrNm: 'Bisi Alimi',
      dbtrAcctIban: 'NG66NIBSS022009988771122',
      dbtrAgentBic: 'STANGBXX',
      endToEndId: 'DD/E2E/20260729/5512',
      instdAmt: '18500.00',
      cdtrNm: 'MTN Nigeria Corp',
      cdtrAcctIban: 'NG77NIBSS058004455667788',
      cdtrAgentBic: 'GTBINGLAXXX',
      purpCd: 'TELC',
      acctDesgn: 'INDIVIDUAL_CHECKING',
      idType: 'NIN',
      idValue: '99887766554',
      acctTierLevel: 'TIER_2',
      channelCode: 'ATM_POS',
      latitude: '7.3775',
      longitude: '3.9470',
    },
  },
  {
    id: 'pacs003',
    code: 'pacs.003.001.09',
    name: 'Customer Direct Debit',
    category: 'Direct Debit Operations',
    description: 'ISO 20022 Interbank Financial Institution Direct Debit',
    generateEndpoint: '/api/generate/customer-direct-debit-pacs003',
    sendEndpoint: '/api/customer-direct-debit-pacs003',
    defaultValues: {
      msgId: 'MSG/20260729/PACS003/00741',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'NIBSS CLEARING SYSTEM',
      initgPtyId: 'CLR00912',
      pmtInfId: 'PACS003/PMT/00741',
      reqdExctnDt: '2026-07-30',
      dbtrNm: 'Oluwaseun Thorne',
      dbtrAcctIban: 'NG33NIBSS011003344556677',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'PACS003/E2E/20260729/1192',
      instdAmt: '500000.00',
      cdtrNm: 'Standard Chartered Bank',
      cdtrAcctIban: 'NG22NIBSS068001122334455',
      cdtrAgentBic: 'SCBLNGLAXXX',
      purpCd: 'LOAN',
      acctDesgn: 'CORPORATE_TRUST',
      idType: 'BVN',
      idValue: '22110099887',
      acctTierLevel: 'TIER_3',
      channelCode: 'HOST_TO_HOST',
      latitude: '6.5244',
      longitude: '3.3792',
    },
  },
  {
    id: 'pacs008',
    code: 'pacs.008.001.10',
    name: 'Credit Transfer',
    category: 'Credit Transfer',
    description: 'ISO 20022 Financial Institution Credit Transfer',
    generateEndpoint: '/api/generate/transfer-pacs008',
    sendEndpoint: '/api/transfer',
    defaultValues: {
      msgId: 'MSG/20260729/PACS008/00992',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'INTERBANK CLEARING HOUSE',
      initgPtyId: 'ICH90011',
      pmtInfId: 'PACS008/INF/00992',
      reqdExctnDt: '2026-07-30',
      dbtrNm: 'Folake Solanke',
      dbtrAcctIban: 'NG00NIBSS057001234567890',
      dbtrAgentBic: 'ZEIBNGLAXXX',
      endToEndId: 'PACS008/E2E/20260729/8821',
      instdAmt: '1500000.00',
      cdtrNm: 'Dangote Industries Corp',
      cdtrAcctIban: 'NG88NIBSS033009876543210',
      cdtrAgentBic: 'UBNANGXX',
      purpCd: 'COMM',
      acctDesgn: 'CORPORATE_PRIME',
      idType: 'BVN',
      idValue: '22998877112',
      acctTierLevel: 'TIER_3',
      channelCode: 'API_GATEWAY',
      latitude: '6.4531',
      longitude: '3.3958',
    },
  },
  {
    id: 'acmt023',
    code: 'acmt.023.001.03',
    name: 'Name Verification',
    category: 'Account Services',
    description: 'ISO 20022 Identification Verification Request',
    generateEndpoint: '/api/generate/name-verification-acmt023',
    sendEndpoint: '/api/gateway-name-verification-acmt023',
    defaultValues: {
      msgId: 'MSG/20260729/ACMT023/00101',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'NIBSS VERIFICATION ENGINE',
      initgPtyId: 'NIBSS_VERIF',
      pmtInfId: 'VERIF/INF/00101',
      reqdExctnDt: '2026-07-29',
      dbtrNm: 'Query Initiator',
      dbtrAcctIban: 'NG00NIBSS000000000000000',
      dbtrAgentBic: 'NIBSSNGLAXXX',
      endToEndId: 'VERIF/E2E/20260729/001',
      instdAmt: '0.00',
      cdtrNm: 'Target Account Holder',
      cdtrAcctIban: 'NG12NIBSS057009988776655',
      cdtrAgentBic: 'ZEIBNGLAXXX',
      purpCd: 'INFO',
      acctDesgn: 'INDIVIDUAL_SAVINGS',
      idType: 'BVN',
      idValue: '22233445566',
      acctTierLevel: 'TIER_1',
      channelCode: 'MOBILE_APP',
      latitude: '6.5244',
      longitude: '3.3792',
    },
  },
  {
    id: 'camt060',
    code: 'camt.060.001.05',
    name: 'Balance Enquiry',
    category: 'Account Services',
    description: 'ISO 20022 Account Reporting Request (Balance / Statement)',
    generateEndpoint: '/api/generate/balance-enquiry-camt060',
    sendEndpoint: '/api/balance-enquiry-camt060',
    defaultValues: {
      msgId: 'MSG/20260729/CAMT060/00404',
      creDtTm: '2026-07-29T21:30:00.000Z',
      initgPtyNm: 'TREASURY REPORTING PORTAL',
      initgPtyId: 'TRSY_PORTAL',
      pmtInfId: 'ENQ/INF/00404',
      reqdExctnDt: '2026-07-29',
      dbtrNm: 'Account Owner',
      dbtrAcctIban: 'NG77NIBSS011008877665544',
      dbtrAgentBic: 'FBNINGLAXXX',
      endToEndId: 'CAMT/E2E/20260729/404',
      instdAmt: '0.00',
      cdtrNm: 'NIBSS Reporting Engine',
      cdtrAcctIban: 'NG77NIBSS011008877665544',
      cdtrAgentBic: 'NIBSSNGLAXXX',
      purpCd: 'BALC',
      acctDesgn: 'CORPORATE_TREASURY',
      idType: 'BVN',
      idValue: '22114477889',
      acctTierLevel: 'TIER_3',
      channelCode: 'WEB_PORTAL',
      latitude: '9.0765',
      longitude: '7.3986',
    },
  },
];

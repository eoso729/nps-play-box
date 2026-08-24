import { MessageConfig } from '../../types/workbench';

export const MESSAGE_CONFIGS: Record<string, MessageConfig> = {
  'pain.013': {
    key: 'pain.013',
    label: 'Payment Activation',
    isoCode: 'pain.013.001.11',
    category: 'Payment Activation',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
        ],
      },
      {
        title: '2. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: 'NG56000110000012345678' },
          { key: 'sourceName', label: 'Initiating Party Name', type: 'text', required: true, placeholder: 'ACME FINANCIAL SERVICES' },
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorAccountNumber', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: 'NG29005800000098765432' },
        ],
      },
      {
        title: '4. Transaction',
        fields: [
          { key: 'amount', label: 'Amount', type: 'number', required: true, placeholder: '1250000' },
          { key: 'requestedExecutionDate', label: 'Requested Execution Date', type: 'date', placeholder: '' },
          { key: 'purpose', label: 'Purpose Code', type: 'text', placeholder: 'SALA' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997', amount: 1250000,
      sourceName: 'ACME FINANCIAL SERVICES', debtorName: 'ACME TECHNOLOGIES LTD',
      debtorAccountNumber: 'NG56000110000012345678',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: 'NG29005800000098765432',
      requestedExecutionDate: new Date().toISOString().split('T')[0],
      purpose: 'SALA',
    },
  },

  'pain.001': {
    key: 'pain.001',
    label: 'Payment Initiation',
    isoCode: 'pain.001.001.11',
    category: 'Payment Initiation',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'initiatorId', label: 'Initiator ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'debtorId', label: 'Debtor ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'creditorId', label: 'Creditor ID', type: 'text', required: true, placeholder: '000058' },
        ],
      },
      {
        title: '2. Amount',
        fields: [
          { key: 'amount', label: 'Amount (NGN)', type: 'number', placeholder: '120.51' },
        ],
      },
    ],
    prefill: {
      initiatorId: '999998', debtorId: '999997', creditorId: '000058', amount: 120.51,
    },
  },

  'pain.008': {
    key: 'pain.008',
    label: 'Direct Debit',
    isoCode: 'pain.008.001.10',
    category: 'Direct Debit Operations',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'initiatorId', label: 'Initiator ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'debtorId', label: 'Debtor ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'creditorId', label: 'Creditor ID', type: 'text', required: true, placeholder: '000058' },
        ],
      },
      {
        title: '2. Mandate Info',
        fields: [
          { key: 'mandateId', label: 'Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-123456' },
          { key: 'dtOfSgntr', label: 'Date of Signature', type: 'date' },
          { key: 'frstColltnDt', label: 'First Collection Date', type: 'date' },
          { key: 'fnlColltnDt', label: 'Final Collection Date', type: 'date' },
          { key: 'freqTp', label: 'Frequency Type', type: 'select', options: [
            { value: 'DAIL', label: 'Daily' },
            { value: 'WEEK', label: 'Weekly' },
            { value: 'MNTH', label: 'Monthly' },
            { value: 'YEAR', label: 'Annual' },
          ]},
        ],
      },
      {
        title: '3. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorIban', label: 'Debtor IBAN', type: 'text', required: true, placeholder: 'NG56000110000012345678' },
        ],
      },
      {
        title: '4. Creditor & Amount',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorIban', label: 'Creditor IBAN', type: 'text', required: true, placeholder: 'NG29005800000098765432' },
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '2200.00' },
          { key: 'remittanceInfo', label: 'Remittance Info', type: 'text', placeholder: 'Monthly subscription' },
          { key: 'nameEnquiryMsgId', label: 'Name Enquiry Msg ID', type: 'text', placeholder: 'NE20260725001' },
        ],
      },
    ],
    prefill: {
      initiatorId: '999998', debtorId: '999997', creditorId: '000058',
      mandateId: 'MNDT-RCUR-123456',
      dtOfSgntr: '2026-01-01', frstColltnDt: '2026-02-01', fnlColltnDt: '2027-01-31',
      freqTp: 'MNTH', debtorName: 'ACME TECHNOLOGIES LTD',
      debtorIban: 'NG56000110000012345678', creditorName: 'INNOVATECH SOLUTIONS',
      creditorIban: 'NG29005800000098765432', amount: 2200.00,
      remittanceInfo: 'Monthly subscription fee',
    },
  },

  'pain.009': {
    key: 'pain.009',
    label: 'Mandate Creation',
    isoCode: 'pain.009.001.07',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
        ],
      },
      {
        title: '2. Mandate Terms',
        fields: [
          { key: 'sequenceType', label: 'Sequence Type', type: 'select', options: [
            { value: 'FRST', label: 'First (FRST)' },
            { value: 'RCUR', label: 'Recurring (RCUR)' },
            { value: 'FNAL', label: 'Final (FNAL)' },
            { value: 'OOFF', label: 'One-off (OOFF)' },
          ]},
          { key: 'frequencyType', label: 'Frequency Type', type: 'select', options: [
            { value: 'DAIL', label: 'Daily' },
            { value: 'WEEK', label: 'Weekly' },
            { value: 'MNTH', label: 'Monthly' },
            { value: 'YEAR', label: 'Annual' },
          ]},
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date' },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
          { key: 'collectionAmount', label: 'Collection Amount', type: 'number', placeholder: '5000.00' },
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: 'NG29005800000098765432' },
          { key: 'creditorAgentBIC', label: 'Creditor Agent BIC', type: 'text', placeholder: 'GTBINGLA' },
          { key: 'creditorAgentMemberId', label: 'Creditor Member ID', type: 'text', placeholder: '000058' },
        ],
      },
      {
        title: '4. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: 'NG56000110000012345678' },
          { key: 'debtorAgentBIC', label: 'Debtor Agent BIC', type: 'text', placeholder: 'NIBSSNGXXX' },
          { key: 'debtorAgentMemberId', label: 'Debtor Member ID', type: 'text', placeholder: '999998' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997',
      sequenceType: 'RCUR', frequencyType: 'MNTH',
      firstCollectionDate: '2026-02-01', finalCollectionDate: '2027-01-31',
      currency: 'NGN', collectionAmount: 5000.00,
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: 'NG29005800000098765432',
      creditorAgentBIC: 'GTBINGLA', creditorAgentMemberId: '000058',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: 'NG56000110000012345678',
      debtorAgentBIC: 'NIBSSNGXXX', debtorAgentMemberId: '999998',
    },
  },

  'pain.010': {
    key: 'pain.010',
    label: 'Mandate Amendment',
    isoCode: 'pain.010.001.07',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
        ],
      },
      {
        title: '2. Original Mandate Reference',
        fields: [
          { key: 'orgnlMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: 'MSG009_ORIGINAL', fullWidth: true },
          { key: 'orgnlMsgNmId', label: 'Original Msg Name ID', type: 'text', placeholder: 'pain.009.001.07' },
          { key: 'orgnlCreDtTm', label: 'Original Creation DateTime', type: 'text', placeholder: '2026-01-15T10:30:00+01:00' },
          { key: 'orgnlMndtId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-123456' },
        ],
      },
      {
        title: '3. Amendment Reason',
        fields: [
          { key: 'amdmntRsnCode', label: 'Amendment Reason Code', type: 'text', placeholder: 'A001' },
          { key: 'amdmntRsnProprietary', label: 'Amendment Reason (Proprietary)', type: 'text', placeholder: 'Amount Change' },
          { key: 'initiatingPartyName', label: 'Initiating Party Name', type: 'text', placeholder: 'ACME FINANCIAL SERVICES', fullWidth: true },
        ],
      },
      {
        title: '4. Updated Mandate Terms',
        fields: [
          { key: 'sequenceType', label: 'Sequence Type', type: 'select', options: [
            { value: 'FRST', label: 'First (FRST)' },
            { value: 'RCUR', label: 'Recurring (RCUR)' },
            { value: 'FNAL', label: 'Final (FNAL)' },
            { value: 'OOFF', label: 'One-off (OOFF)' },
          ]},
          { key: 'frequencyType', label: 'Frequency Type', type: 'select', options: [
            { value: 'DAIL', label: 'Daily' },
            { value: 'WEEK', label: 'Weekly' },
            { value: 'MNTH', label: 'Monthly' },
            { value: 'YEAR', label: 'Annual' },
          ]},
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date' },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: 'NG29005800000098765432' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: 'NG56000110000012345678' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997',
      orgnlMsgId: 'MSG009_ORIGINAL', orgnlMsgNmId: 'pain.009.001.07',
      orgnlCreDtTm: '2026-01-15T10:30:00+01:00', orgnlMndtId: 'MNDT-RCUR-123456',
      amdmntRsnCode: 'A001', amdmntRsnProprietary: 'Amount Change',
      initiatingPartyName: 'ACME FINANCIAL SERVICES',
      sequenceType: 'RCUR', frequencyType: 'MNTH',
      firstCollectionDate: '2026-03-01', finalCollectionDate: '2027-01-31',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: 'NG29005800000098765432',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: 'NG56000110000012345678',
    },
  },

  'pain.011': {
    key: 'pain.011',
    label: 'Mandate Cancellation',
    isoCode: 'pain.011.001.07',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'sourceName', label: 'Source Name', type: 'text', placeholder: 'ACME FINANCIAL SERVICES', fullWidth: true },
        ],
      },
      {
        title: '2. Original Mandate Reference',
        fields: [
          { key: 'originalMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: 'MSG009_ORIGINAL', fullWidth: true },
          { key: 'originalCreDtTm', label: 'Original Creation DateTime', type: 'text', placeholder: '2026-01-15T10:30:00+01:00' },
          { key: 'originalMandateId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-123456' },
        ],
      },
      {
        title: '3. Cancellation Reason',
        fields: [
          { key: 'cancellationReasonCode', label: 'Cancellation Reason Code', type: 'text', placeholder: 'CUST' },
          { key: 'cancellationReasonDescription', label: 'Reason Description', type: 'text', placeholder: 'Customer request', fullWidth: true },
        ],
      },
      {
        title: '4. Mandate Details',
        fields: [
          { key: 'sequenceType', label: 'Sequence Type', type: 'select', options: [
            { value: 'FRST', label: 'First (FRST)' },
            { value: 'RCUR', label: 'Recurring (RCUR)' },
            { value: 'FNAL', label: 'Final (FNAL)' },
            { value: 'OOFF', label: 'One-off (OOFF)' },
          ]},
          { key: 'frequencyType', label: 'Frequency Type', type: 'select', options: [
            { value: 'DAIL', label: 'Daily' },
            { value: 'WEEK', label: 'Weekly' },
            { value: 'MNTH', label: 'Monthly' },
          ]},
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date' },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', fullWidth: true, placeholder: 'INNOVATECH SOLUTIONS' },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: 'NG29005800000098765432' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', fullWidth: true, placeholder: 'ACME TECHNOLOGIES LTD' },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: 'NG56000110000012345678' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997', sourceName: 'ACME FINANCIAL SERVICES',
      originalMsgId: 'MSG009_ORIGINAL', originalCreDtTm: '2026-01-15T10:30:00+01:00',
      originalMandateId: 'MNDT-RCUR-123456', cancellationReasonCode: 'CUST',
      cancellationReasonDescription: 'Customer request to cancel mandate',
      sequenceType: 'RCUR', frequencyType: 'MNTH',
      firstCollectionDate: '2026-02-01', finalCollectionDate: '2027-01-31',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: 'NG29005800000098765432',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: 'NG56000110000012345678',
    },
  },

  'pacs.008': {
    key: 'pacs.008',
    label: 'Credit Transfer',
    isoCode: 'pacs.008.001.10',
    category: 'Credit Transfer',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '991040' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
        ],
      },
      {
        title: '2. Sender',
        fields: [
          { key: 'senderName', label: 'Sender Name', type: 'text', required: true, placeholder: 'Ponmile Joy', fullWidth: true },
          { key: 'senderAccountNumber', label: 'Sender Account', type: 'text', required: true, placeholder: '3157417712' },
          { key: 'senderAccountName', label: 'Sender Account Name', type: 'text', placeholder: 'Ponmile Joy' },
        ],
      },
      {
        title: '3. Beneficiary',
        fields: [
          { key: 'beneficiaryName', label: 'Beneficiary Name', type: 'text', required: true, placeholder: 'SAMUEL ADEBOLA', fullWidth: true },
          { key: 'beneficiaryAccountNumber', label: 'Beneficiary Account', type: 'text', required: true, placeholder: '5000002101' },
          { key: 'beneficiaryAccountName', label: 'Beneficiary Account Name', type: 'text', placeholder: 'SAMUEL ADEBOLA' },
        ],
      },
      {
        title: '4. Transaction',
        fields: [
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '200.00' },
          { key: 'narration', label: 'Narration', type: 'text', placeholder: 'Thanks.', fullWidth: true },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: '1' },
          { key: 'nameEnquiryMsgId', label: 'Name Enquiry Msg ID', type: 'text', placeholder: '99999720260820104556011402881687033' },
        ],
      },
      {
        title: '5. Supplementary Data',
        fields: [
          { key: 'creditorIdType', label: 'Creditor ID Type', type: 'text', placeholder: 'BVN' },
          { key: 'creditorIdValue', label: 'Creditor ID Value', type: 'text', placeholder: '22383706153' },
          { key: 'debtorAccountDesignation', label: 'Debtor Account Designation', type: 'text', placeholder: '1' },
          { key: 'debtorIdType', label: 'Debtor ID Type', type: 'text', placeholder: 'BVN' },
          { key: 'debtorIdValue', label: 'Debtor ID Value', type: 'text', placeholder: '22383706153' },
          { key: 'debtorAccountTier', label: 'Debtor Account Tier', type: 'text', placeholder: '1' },
          { key: 'transactionLocation', label: 'Transaction Location (Lat,Lng)', type: 'text', placeholder: '6.5244,3.3792' },
        ],
      },
    ],
    prefill: {
      sourceId: '999997',
      destinationId: '991040',
      currency: 'NGN',
      senderName: 'Ponmile Joy',
      senderAccountNumber: '3157417712',
      senderAccountName: 'Ponmile Joy',
      beneficiaryName: 'SAMUEL ADEBOLA',
      beneficiaryAccountNumber: '5000002101',
      beneficiaryAccountName: 'SAMUEL ADEBOLA',
      amount: 200,
      narration: 'Thanks.',
      channelCode: '1',
      nameEnquiryMsgId: '99999720260820104556011402881687033',
      creditorIdType: 'BVN',
      creditorIdValue: '22383706153',
      debtorAccountDesignation: '1',
      debtorIdType: 'BVN',
      debtorIdValue: '22383706153',
      debtorAccountTier: '1',
      transactionLocation: '6.5244,3.3792',
    },
  },

  'pacs.003': {
    key: 'pacs.003',
    label: 'Customer Direct Debit',
    isoCode: 'pacs.003.001.09',
    category: 'Direct Debit Operations',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
        ],
      },
      {
        title: '2. Mandate Info',
        fields: [
          { key: 'mandateId', label: 'Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-123456' },
          { key: 'dateOfSignature', label: 'Date of Signature', type: 'date' },
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date' },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date' },
          { key: 'frequencyType', label: 'Frequency Type', type: 'select', options: [
            { value: 'DAIL', label: 'Daily' },
            { value: 'WEEK', label: 'Weekly' },
            { value: 'MNTH', label: 'Monthly' },
          ]},
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: 'NG29005800000098765432' },
        ],
      },
      {
        title: '4. Debtor & Amount',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: 'NG56000110000012345678' },
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '2200.00' },
          { key: 'narration', label: 'Narration', type: 'text', placeholder: 'Monthly subscription', fullWidth: true },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: 'MOB' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997', currency: 'NGN',
      mandateId: 'MNDT-RCUR-123456', dateOfSignature: '2026-01-01',
      firstCollectionDate: '2026-02-01', finalCollectionDate: '2027-01-31',
      frequencyType: 'MNTH', creditorName: 'INNOVATECH SOLUTIONS',
      creditorAccountNumber: 'NG29005800000098765432',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: 'NG56000110000012345678',
      amount: 2200.00, narration: 'Monthly subscription fee', channelCode: 'MOB',
    },
  },

  'pacs.004': {
    key: 'pacs.004',
    label: 'Payment Return',
    isoCode: 'pacs.004.001.11',
    category: 'Credit Transfer',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999057' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'bicfi', label: 'BICFI', type: 'text', placeholder: '999057' },
        ],
      },
      {
        title: '2. Original Transaction Reference',
        fields: [
          { key: 'originalMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: 'ORIG_MSG_ID', fullWidth: true },
          { key: 'originalMsgNameId', label: 'Original Msg Name ID', type: 'text', placeholder: 'pacs.008.001.12' },
          { key: 'originalCreDtTm', label: 'Original Creation DateTime', type: 'text', placeholder: '2026-07-25T10:00:00+01:00' },
          { key: 'originalInstrId', label: 'Original Instruction ID', type: 'text', required: true, placeholder: 'INSTR_ORIG_001' },
          { key: 'originalEndToEndId', label: 'Original E2E ID', type: 'text', required: true, placeholder: 'E2E_ORIG_001' },
          { key: 'originalTxId', label: 'Original Tx ID', type: 'text', required: true, placeholder: 'TX_ORIG_001' },
          { key: 'originalIntrBkSttlmDt', label: 'Original Settlement Date', type: 'text', placeholder: '2026-07-25Z' },
        ],
      },
      {
        title: '3. Return Details',
        fields: [
          { key: 'returnedAmount', label: 'Returned Amount', type: 'number', required: true, placeholder: '1250000' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
          { key: 'intrBkSttlmDt', label: 'Settlement Date', type: 'text', placeholder: '2026-07-26Z' },
          { key: 'returnReasonCode', label: 'Return Reason Code', type: 'text', required: true, placeholder: 'AC04' },
          { key: 'returnReasonInfo', label: 'Additional Return Info', type: 'text', placeholder: 'Account closed', fullWidth: true },
          { key: 'clearingChannel', label: 'Clearing Channel', type: 'text', placeholder: 'RTNS' },
          { key: 'localInstrument', label: 'Local Instrument', type: 'text', placeholder: 'CTAA' },
        ],
      },
      {
        title: '4. Debtor & Creditor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', fullWidth: true, placeholder: 'ACME TECHNOLOGIES LTD' },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: 'NG56000110000012345678' },
          { key: 'debtorAccountName', label: 'Debtor Account Name', type: 'text', placeholder: 'ACME TECH CURRENT' },
          { key: 'debtorAgentMmbId', label: 'Debtor Agent Member ID', type: 'text', placeholder: '999998' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', fullWidth: true, placeholder: 'INNOVATECH SOLUTIONS' },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: 'NG29005800000098765432' },
          { key: 'creditorAgentMmbId', label: 'Creditor Agent Member ID', type: 'text', placeholder: '000058' },
        ],
      },
    ],
    prefill: {
      sourceId: '999057', destinationId: '999998', bicfi: '999057',
      originalMsgId: 'ORIG_MSG_20260725001', originalMsgNameId: 'pacs.008.001.12',
      originalCreDtTm: '2026-07-25T10:00:00+01:00',
      originalInstrId: 'INSTR_ORIG_001', originalEndToEndId: 'E2E_ORIG_001',
      originalTxId: 'TX_ORIG_001', originalIntrBkSttlmDt: '2026-07-25Z',
      returnedAmount: 1250000, currency: 'NGN', intrBkSttlmDt: '2026-07-26Z',
      returnReasonCode: 'AC04', returnReasonInfo: 'Account closed',
      clearingChannel: 'RTNS', localInstrument: 'CTAA',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: 'NG56000110000012345678',
      debtorAccountName: 'ACME TECH CURRENT', debtorAgentMmbId: '999998',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: 'NG29005800000098765432',
      creditorAgentMmbId: '000058',
    },
  },

  'acmt.023': {
    key: 'acmt.023',
    label: 'Name Verification',
    isoCode: 'acmt.023.001.03',
    category: 'Account Services',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'beneficiaryId', label: 'Beneficiary ID', type: 'text', required: true, placeholder: '991040' },
        ],
      },
      {
        title: '2. Verification Details',
        fields: [
          { key: 'partyToBeVerifiedName', label: 'Party Name to Verify', type: 'text', required: true, placeholder: 'SAMUEL ADEBOLA', fullWidth: true },
          { key: 'partyToBeVerifiedAccountNumber', label: 'Account Number', type: 'text', required: true, placeholder: '5000002100' },
          { key: 'sendingPartyName', label: 'Sending Party Name', type: 'text', required: true, placeholder: 'test' },
        ],
      },
    ],
    prefill: {
      sourceId: '999997',
      beneficiaryId: '991040',
      partyToBeVerifiedName: 'SAMUEL ADEBOLA',
      partyToBeVerifiedAccountNumber: '5000002100',
      sendingPartyName: 'test',
    },
  },

  'acmt.024': {
    key: 'acmt.024',
    label: 'Name Verification Report',
    isoCode: 'acmt.024.001.04',
    category: 'Account Services',
    sections: [
      {
        title: '1. Assignment & Institutions',
        fields: [
          { key: 'sendingInstitutionId', label: 'Sending Inst. ID', type: 'text', required: true, placeholder: '999012' },
          { key: 'receivingInstitutionId', label: 'Receiving Inst. ID', type: 'text', required: true, placeholder: '999999' },
          { key: 'receiverName', label: 'Receiver Name', type: 'text', required: true, placeholder: 'Oso International Bank', fullWidth: true },
        ],
      },
      {
        title: '2. Original Request Reference',
        fields: [
          { key: 'originalMsgId', label: 'Original Msg ID (acmt.023)', type: 'text', required: true, placeholder: '99999920250829150504887742643314693', fullWidth: true },
          { key: 'originalCreDtTm', label: 'Original Creation Date-Time', type: 'text', required: true, placeholder: '2025-08-29T15:05:04.347Z', fullWidth: true },
        ],
      },
      {
        title: '3. Verification Result',
        fields: [
          {
            key: 'verificationResponse',
            label: 'Verification Status',
            type: 'select',
            required: true,
            options: [
              { value: 'true', label: 'True (Success)' },
              { value: 'false', label: 'False (Failure)' },
            ],
          },
          { key: 'verifiedAccountNumber', label: 'Verified Account Number', type: 'text', required: true, placeholder: '1029384756' },
          { key: 'verifiedAccountName', label: 'Verified Account Name', type: 'text', placeholder: 'Emmanuel Oso' },
          { key: 'reasonCode', label: 'Failure Reason Code', type: 'text', placeholder: '33' },
          { key: 'reasonProprietary', label: 'Failure Reason Text', type: 'text', placeholder: 'Account number mismatch' },
        ],
      },
      {
        title: '4. Supplementary Details',
        fields: [
          { key: 'creditorAccountDesignation', label: 'Account Designation', type: 'text', placeholder: '1' },
          { key: 'creditorIdType', label: 'ID Type', type: 'text', placeholder: 'BVN' },
          { key: 'creditorIdValue', label: 'ID Value', type: 'text', placeholder: '2211232346' },
          { key: 'creditorAccountTier', label: 'Account Tier', type: 'text', placeholder: '1' },
          { key: 'transactionRiskRating', label: 'Risk Rating', type: 'text', placeholder: 'R000000000000000000B9', fullWidth: true },
        ],
      },
    ],
    prefill: {
      sendingInstitutionId: '999012',
      receivingInstitutionId: '999999',
      receiverName: 'Oso International Bank',
      originalMsgId: '99999920250829150504887742643314693',
      originalCreDtTm: '2025-08-29T15:05:04.347Z',
      verificationResponse: 'true',
      verifiedAccountNumber: '1029384756',
      verifiedAccountName: 'Emmanuel Oso',
      reasonCode: '33',
      reasonProprietary: 'Account number mismatch',
      creditorAccountDesignation: '1',
      creditorIdType: 'BVN',
      creditorIdValue: '2211232346',
      creditorAccountTier: '1',
      transactionRiskRating: 'R000000000000000000B9',
    },
  },

  'camt.060': {
    key: 'camt.060',
    label: 'Balance Enquiry',
    isoCode: 'camt.060.001.05',
    category: 'Account Services',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997' },
        ],
      },
      {
        title: '2. Account Info',
        fields: [
          { key: 'accountNumber', label: 'Account Number', type: 'text', required: true, placeholder: 'NG56000110000012345678' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
          { key: 'requestedMessageType', label: 'Report Type', type: 'select', options: [
            { value: 'BALANCE', label: 'Balance' },
            { value: 'STATEMENT', label: 'Statement' },
          ]},
        ],
      },
      {
        title: '3. Reporting Period',
        fields: [
          { key: 'fromDate', label: 'From Date', type: 'date' },
          { key: 'toDate', label: 'To Date', type: 'date' },
          { key: 'reportingPeriodType', label: 'Reporting Period Type', type: 'text', placeholder: 'DAILY' },
        ],
      },
      {
        title: '4. Supplementary Data',
        fields: [
          { key: 'accountDesignation', label: 'Account Designation', type: 'text', placeholder: 'Personal' },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: 'MOB' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997',
      accountNumber: 'NG56000110000012345678', currency: 'NGN',
      requestedMessageType: 'BALANCE',
      fromDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      toDate: new Date().toISOString().split('T')[0],
      reportingPeriodType: 'DAILY', accountDesignation: 'Personal', channelCode: 'MOB',
    },
  },
};

export const SIDEBAR_GROUPS = [
  {
    label: 'Payment Activation',
    items: [{ key: 'pain.013', isoCode: 'pain.013.001.11' }],
  },
  {
    label: 'Payment Initiation',
    items: [{ key: 'pain.001', isoCode: 'pain.001.001.11' }],
  },
  {
    label: 'Mandate Management',
    items: [
      { key: 'pain.009', isoCode: 'pain.009.001.07 (Creation)' },
      { key: 'pain.010', isoCode: 'pain.010.001.07 (Amendment)' },
      { key: 'pain.011', isoCode: 'pain.011.001.07 (Cancellation)' },
    ],
  },
  {
    label: 'Direct Debit Operations',
    items: [
      { key: 'pain.008', isoCode: 'pain.008.001.10' },
      { key: 'pacs.003', isoCode: 'pacs.003.001.09' },
    ],
  },
  {
    label: 'Credit Transfer',
    items: [
      { key: 'pacs.008', isoCode: 'pacs.008.001.10' },
      { key: 'pacs.004', isoCode: 'pacs.004.001.11 (Return)' },
    ],
  },
  {
    label: 'Account Services',
    items: [
      { key: 'acmt.023', isoCode: 'acmt.023.001.03 (Name Verification)' },
      { key: 'acmt.024', isoCode: 'acmt.024.001.04 (Name Verification Report)' },
      { key: 'camt.060', isoCode: 'camt.060.001.05 (Balance Enquiry)' },
    ],
  },
];

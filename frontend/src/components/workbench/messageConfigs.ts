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
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999997' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '991015' },
        ],
      },
      {
        title: '2. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Tunde Fabiyi', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: '0000002110' },
          { key: 'sourceName', label: 'Initiating Party Name', type: 'text', required: true, placeholder: 'Ponmile Joy' },
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'Ponmile Joy', fullWidth: true },
          { key: 'creditorAccountNumber', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: '3157417712' },
        ],
      },
      {
        title: '4. Transaction',
        fields: [
          { key: 'amount', label: 'Amount', type: 'number', required: true, placeholder: '1000' },
          { key: 'requestedExecutionDate', label: 'Requested Execution Date', type: 'date', placeholder: '' },
          { key: 'purpose', label: 'Purpose Code', type: 'text', placeholder: 'Testing 013' },
        ],
      },
    ],
    prefill: {
      sourceId: '999997',
      destinationId: '991015',
      debtorName: 'Tunde Fabiyi',
      debtorAccountNumber: '0000002110',
      sourceName: 'Ponmile Joy',
      creditorName: 'Ponmile Joy',
      creditorAccountNumber: '3157417712',
      amount: 1000,
      requestedExecutionDate: '2026-08-24',
      purpose: 'Testing 013',
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
          { key: 'debtorIban', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: '0123456789' },
        ],
      },
      {
        title: '4. Creditor & Amount',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true },
          { key: 'creditorIban', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: '0987654321' },
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '2200.00' },
          { key: 'remittanceInfo', label: 'Remittance Info', type: 'text', placeholder: 'Monthly subscription' },
          { key: 'nameEnquiryMsgId', label: 'Name Enquiry Msg ID', type: 'text', placeholder: '99999820260101100000123456789012345' },
        ],
      },
    ],
    prefill: {
      initiatorId: '999998', debtorId: '999997', creditorId: '000058',
      mandateId: 'MNDT-RCUR-123456',
      dtOfSgntr: '2026-01-01', frstColltnDt: '2026-02-01', fnlColltnDt: '2027-01-31',
      freqTp: 'MNTH', debtorName: 'ACME TECHNOLOGIES LTD',
      debtorIban: '0123456789', creditorName: 'INNOVATECH SOLUTIONS',
      creditorIban: '0987654321', amount: 2200.00,
      remittanceInfo: 'Monthly subscription fee',
      nameEnquiryMsgId: '99999820260101100000123456789012345',
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
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '0987654321' },
          { key: 'creditorAgentBIC', label: 'Creditor Agent BIC', type: 'text', placeholder: 'GTBINGLA' },
          { key: 'creditorAgentMemberId', label: 'Creditor Member ID', type: 'text', placeholder: '000058' },
        ],
      },
      {
        title: '4. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '0123456789' },
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
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: '0987654321',
      creditorAgentBIC: 'GTBINGLA', creditorAgentMemberId: '000058',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: '0123456789',
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
          { key: 'orgnlMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: '99999820260115103000123456789012345', fullWidth: true },
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
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: '0987654321' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: '0123456789' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997',
      orgnlMsgId: '99999820260115103000123456789012345', orgnlMsgNmId: 'pain.009.001.07',
      orgnlCreDtTm: '2026-01-15T10:30:00+01:00', orgnlMndtId: 'MNDT-RCUR-123456',
      amdmntRsnCode: 'A001', amdmntRsnProprietary: 'Amount Change',
      initiatingPartyName: 'ACME FINANCIAL SERVICES',
      sequenceType: 'RCUR', frequencyType: 'MNTH',
      firstCollectionDate: '2026-03-01', finalCollectionDate: '2027-01-31',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: '0987654321',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: '0123456789',
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
          { key: 'originalMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: '99999820260115103000123456789012345', fullWidth: true },
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
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: '0987654321' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', fullWidth: true, placeholder: 'ACME TECHNOLOGIES LTD' },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: '0123456789' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997', sourceName: 'ACME FINANCIAL SERVICES',
      originalMsgId: '99999820260115103000123456789012345', originalCreDtTm: '2026-01-15T10:30:00+01:00',
      originalMandateId: 'MNDT-RCUR-123456', cancellationReasonCode: 'CUST',
      cancellationReasonDescription: 'Customer request to cancel mandate',
      sequenceType: 'RCUR', frequencyType: 'MNTH',
      firstCollectionDate: '2026-02-01', finalCollectionDate: '2027-01-31',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: '0987654321',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: '0123456789',
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
          { key: 'transactionLocation', label: 'Transaction Location', type: 'text', placeholder: '01080652440N020900337921E' },
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
      transactionLocation: '01080652440N020900337921E',
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
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '0987654321' },
        ],
      },
      {
        title: '4. Debtor & Amount',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '0123456789' },
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '2200.00' },
          { key: 'narration', label: 'Narration', type: 'text', placeholder: 'Monthly subscription', fullWidth: true },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: '3' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997', currency: 'NGN',
      mandateId: 'MNDT-RCUR-123456', dateOfSignature: '2026-01-01',
      firstCollectionDate: '2026-02-01', finalCollectionDate: '2027-01-31',
      frequencyType: 'MNTH', creditorName: 'INNOVATECH SOLUTIONS',
      creditorAccountNumber: '0987654321',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: '0123456789',
      amount: 2200.00, narration: 'Monthly subscription fee', channelCode: '3',
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
          { key: 'originalMsgId', label: 'Original Msg ID', type: 'text', required: true, placeholder: '99905720260725100000123456789012345', fullWidth: true },
          { key: 'originalMsgNameId', label: 'Original Msg Name ID', type: 'text', placeholder: 'pacs.008.001.12' },
          { key: 'originalCreDtTm', label: 'Original Creation DateTime', type: 'text', placeholder: '2026-07-25T10:00:00+01:00' },
          { key: 'originalInstrId', label: 'Original Instruction ID', type: 'text', required: true, placeholder: '99905799999820260725100000123456789' },
          { key: 'originalEndToEndId', label: 'Original E2E ID', type: 'text', required: true, placeholder: '99905720260725100000987654321012345' },
          { key: 'originalTxId', label: 'Original Tx ID', type: 'text', required: true, placeholder: '99905720260725100000123456789012345' },
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
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', placeholder: '0123456789' },
          { key: 'debtorAccountName', label: 'Debtor Account Name', type: 'text', placeholder: 'ACME TECH CURRENT' },
          { key: 'debtorAgentMmbId', label: 'Debtor Agent Member ID', type: 'text', placeholder: '999998' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', fullWidth: true, placeholder: 'INNOVATECH SOLUTIONS' },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', placeholder: '0987654321' },
          { key: 'creditorAgentMmbId', label: 'Creditor Agent Member ID', type: 'text', placeholder: '000058' },
        ],
      },
    ],
    prefill: {
      sourceId: '999057', destinationId: '999998', bicfi: '999057',
      originalMsgId: '99905720260725100000123456789012345', originalMsgNameId: 'pacs.008.001.12',
      originalCreDtTm: '2026-07-25T10:00:00+01:00',
      originalInstrId: '99905799999820260725100000123456789', originalEndToEndId: '99905720260725100000987654321012345',
      originalTxId: '99905720260725100000123456789012345', originalIntrBkSttlmDt: '2026-07-25Z',
      returnedAmount: 1250000, currency: 'NGN', intrBkSttlmDt: '2026-07-26Z',
      returnReasonCode: 'AC04', returnReasonInfo: 'Account closed',
      clearingChannel: 'RTNS', localInstrument: 'CTAA',
      debtorName: 'ACME TECHNOLOGIES LTD', debtorAccountNumber: '0123456789',
      debtorAccountName: 'ACME TECH CURRENT', debtorAgentMmbId: '999998',
      creditorName: 'INNOVATECH SOLUTIONS', creditorAccountNumber: '0987654321',
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
          { key: 'originalCreDtTm', label: 'Original Creation Date-Time', type: 'text', placeholder: '2025-08-29T15:05:04.347Z', fullWidth: true },
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
          { key: 'creditorIdValue', label: 'ID Value', type: 'text', placeholder: '22112323460' },
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
      creditorIdValue: '22112323460',
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
          { key: 'accountNumber', label: 'Account Number', type: 'text', required: true, placeholder: '0123456789' },
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
          { key: 'accountDesignation', label: 'Account Designation (1-6)', type: 'text', placeholder: '2' },
          { key: 'channelCode', label: 'Channel Code (1-11)', type: 'text', placeholder: '3' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998', destinationId: '999997',
      accountNumber: '0123456789', currency: 'NGN',
      requestedMessageType: 'BALANCE',
      fromDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      toDate: new Date().toISOString().split('T')[0],
      reportingPeriodType: 'DAILY', accountDesignation: '2', channelCode: '3',
    },
  },

  'pacs.002': {
    key: 'pacs.002',
    label: 'Payment Status Report',
    isoCode: 'pacs.002.001.12',
    category: 'Credit Transfer & Returns',
    sections: [
      {
        title: '1. Header & Agents',
        fields: [
          { key: 'sourceId', label: 'Instructing Agent ID', type: 'text', required: true, placeholder: '090004' },
          { key: 'destinationId', label: 'Instructed Agent ID', type: 'text', required: true, placeholder: '100022' },
        ],
      },
      {
        title: '2. Original Message Details',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '10002220260402170095982371426577881', fullWidth: true },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pacs.008.001.12' },
          { key: 'groupStatus', label: 'Group Status', type: 'select', options: [
            { value: 'ACSC', label: 'ACSC - Accepted Settlement Completed' },
            { value: 'RJCT', label: 'RJCT - Rejected' },
            { value: 'ACCP', label: 'ACCP - Accepted Customer Profile' },
          ]},
        ],
      },
      {
        title: '3. Transaction Information',
        fields: [
          { key: 'originalTxId', label: 'Original Transaction ID', type: 'text', placeholder: '10002220260331170095982371426577885', fullWidth: true },
          { key: 'originalEndToEndId', label: 'Original End-to-End ID', type: 'text', placeholder: '10002221234519115702293163242525113', fullWidth: true },
        ],
      },
    ],
    prefill: {
      sourceId: '090004',
      destinationId: '100022',
      originalMsgId: '10002220260402170095982371426577881',
      originalMsgNmId: 'pacs.008.001.12',
      groupStatus: 'ACSC',
      originalTxId: '10002220260331170095982371426577885',
      originalEndToEndId: '10002221234519115702293163242525113',
    },
  },

  'pacs.028': {
    key: 'pacs.028',
    label: 'Payment Status Request',
    isoCode: 'pacs.028.001.05',
    category: 'Credit Transfer & Returns',
    sections: [
      {
        title: '1. Header & Agent',
        fields: [
          { key: 'sourceId', label: 'Instructing Agent ID', type: 'text', required: true, placeholder: '999057' },
          { key: 'destinationId', label: 'Instructed Agent ID', type: 'text', required: true, placeholder: '999012' },
        ],
      },
      {
        title: '2. Original Message References',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pacs.008.001.12' },
          { key: 'originalTxId', label: 'Original Transaction ID', type: 'text', placeholder: '99905820250802112346977904433112345', fullWidth: true },
        ],
      },
    ],
    prefill: {
      sourceId: '999057',
      destinationId: '999012',
      originalMsgId: '99905820250802112346977904433112345',
      originalMsgNmId: 'pacs.008.001.12',
      originalTxId: '99905820250802112346977904433112345',
    },
  },

  'pain.012': {
    key: 'pain.012',
    label: 'Mandate Acceptance Report',
    isoCode: 'pain.012.001.08',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Original Mandate Details',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820251211112346125578725905163', fullWidth: true },
          { key: 'originalMandateId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-00001' },
          { key: 'accepted', label: 'Accepted Result', type: 'select', options: [
            { value: 'true', label: 'Accepted (True)' },
            { value: 'false', label: 'Rejected (False)' },
          ]},
        ],
      },
      {
        title: '2. Parties',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', placeholder: 'CreditorCorp' },
          { key: 'creditorAccountNumber', label: 'Creditor Account (IBAN)', type: 'text', placeholder: '5555544443' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', placeholder: 'Debtor Customer' },
          { key: 'debtorAccountNumber', label: 'Debtor Account (IBAN)', type: 'text', placeholder: '8888899999' },
        ],
      },
    ],
    prefill: {
      originalMsgId: '99905820251211112346125578725905163',
      originalMandateId: 'MNDT-RCUR-00001',
      accepted: 'true',
      creditorName: 'CreditorCorp',
      creditorAccountNumber: '5555544443',
      debtorName: 'Debtor Customer',
      debtorAccountNumber: '8888899999',
    },
  },

  'pain.014': {
    key: 'pain.014',
    label: 'Payment Activation Status Report',
    isoCode: 'pain.014.001.11',
    category: 'Payment Activation',
    sections: [
      {
        title: '1. Original Activation References',
        fields: [
          { key: 'originalMsgId', label: 'Original pain.013 Msg ID', type: 'text', required: true, placeholder: '99905820260105102349998878725905163', fullWidth: true },
          { key: 'originalPmtInfId', label: 'Original PmtInfId', type: 'text', placeholder: 'GSFPMTINF035985837' },
          { key: 'groupStatus', label: 'Group Status', type: 'select', options: [
            { value: 'ACCP', label: 'ACCP - Accepted' },
            { value: 'RJCT', label: 'RJCT - Rejected' },
          ]},
        ],
      },
    ],
    prefill: {
      originalMsgId: '99905820260105102349998878725905163',
      originalPmtInfId: 'GSFPMTINF035985837',
      groupStatus: 'ACCP',
    },
  },

  'camt.052': {
    key: 'camt.052',
    label: 'Bank To Customer Account Report',
    isoCode: 'camt.052.001.08',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Account & Balance',
        fields: [
          { key: 'accountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '4488447166' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
          { key: 'balanceAmount', label: 'Balance Amount', type: 'number', placeholder: '500000.00' },
          { key: 'creditDebitIndicator', label: 'Credit/Debit Indicator', type: 'select', options: [
            { value: 'CRDT', label: 'CRDT - Credit' },
            { value: 'DBIT', label: 'DBIT - Debit' },
          ]},
        ],
      },
    ],
    prefill: {
      accountNumber: '4488447166',
      currency: 'NGN',
      balanceAmount: 500000.00,
      creditDebitIndicator: 'CRDT',
    },
  },

  'camt.053': {
    key: 'camt.053',
    label: 'Bank To Customer Statement',
    isoCode: 'camt.053.001.08',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Statement Details',
        fields: [
          { key: 'accountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '8887788778' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN' },
          { key: 'openingBalance', label: 'Opening Balance', type: 'number', placeholder: '482000.00' },
        ],
      },
    ],
    prefill: {
      accountNumber: '8887788778',
      currency: 'NGN',
      openingBalance: 482000.00,
    },
  },

  'pain.002': {
    key: 'pain.002',
    label: 'Payment Status Report (Customer)',
    isoCode: 'pain.002.001.12',
    category: 'Payment Initiation',
    sections: [
      {
        title: '1. Original Initiation References',
        fields: [
          { key: 'originalMsgId', label: 'Original pain.001 Msg ID', type: 'text', required: true, placeholder: '99905720260225192650869851166984847', fullWidth: true },
          { key: 'groupStatus', label: 'Group Status', type: 'select', options: [
            { value: 'ACSC', label: 'ACSC - Accepted Settlement Completed' },
            { value: 'RJCT', label: 'RJCT - Rejected' },
          ]},
        ],
      },
    ],
    prefill: {
      originalMsgId: '99905720260225192650869851166984847',
      groupStatus: 'ACSC',
    },
  },
};

export const SIDEBAR_GROUPS = [
  {
    label: 'Payment Activation',
    items: [
      { key: 'pain.013', isoCode: 'pain.013 (Payment Activation)' },
      { key: 'pain.014', isoCode: 'pain.014 (Activation Status Report)' },
    ],
  },
  {
    label: 'Payment Initiation',
    items: [
      { key: 'pain.001', isoCode: 'pain.001 (Credit Transfer Initiation)' },
      { key: 'pain.002', isoCode: 'pain.002 (Payment Status Report)' },
    ],
  },
  {
    label: 'Mandate Management',
    items: [
      { key: 'pain.009', isoCode: 'pain.009 (Mandate Creation)' },
      { key: 'pain.010', isoCode: 'pain.010 (Mandate Amendment)' },
      { key: 'pain.011', isoCode: 'pain.011 (Mandate Cancellation)' },
      { key: 'pain.012', isoCode: 'pain.012 (Mandate Acceptance)' },
    ],
  },
  {
    label: 'Direct Debit Operations',
    items: [
      { key: 'pain.008', isoCode: 'pain.008 (Direct Debit Initiation)' },
      { key: 'pacs.003', isoCode: 'pacs.003 (Direct Debit Transfer)' },
    ],
  },
  {
    label: 'Credit Transfer & Returns',
    items: [
      { key: 'pacs.008', isoCode: 'pacs.008 (Customer Direct Credit)' },
      { key: 'pacs.004', isoCode: 'pacs.004 (Payment Return)' },
      { key: 'pacs.002', isoCode: 'pacs.002 (Payment Status Report)' },
      { key: 'pacs.028', isoCode: 'pacs.028 (Payment Status Request)' },
    ],
  },
  {
    label: 'Account Services & Statements',
    items: [
      { key: 'acmt.023', isoCode: 'acmt.023 (Name Verification Request)' },
      { key: 'acmt.024', isoCode: 'acmt.024 (Name Verification Report)' },
      { key: 'camt.060', isoCode: 'camt.060 (Balance Enquiry Request)' },
      { key: 'camt.052', isoCode: 'camt.052 (Bank Account Report)' },
      { key: 'camt.053', isoCode: 'camt.053 (Bank Statement)' },
    ],
  },
];

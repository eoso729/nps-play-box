import { MessageConfig } from '../../types/workbench';

export const MESSAGE_CONFIGS: Record<string, MessageConfig> = {
  'pain.013': {
    key: 'pain.013',
    label: 'Payment Activation',
    isoCode: 'pain.013.001.11',
    category: 'Payment Initiation & Activation',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Instructing Agent Member ID (6 digits)' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '991015', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Instructed Agent Member ID (6 digits)' },
        ],
      },
      {
        title: '2. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Tunde Fabiyi', fullWidth: true, maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: '0000002110', maxLength: 10, ruleType: 'NUBAN', helperText: '10-digit NUBAN account number' },
          { key: 'sourceName', label: 'Initiating Party Name', type: 'text', required: true, placeholder: 'Ponmile Joy', maxLength: 100 },
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'Ponmile Joy', fullWidth: true, maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: '3157417712', maxLength: 10, ruleType: 'NUBAN', helperText: '10-digit NUBAN account number' },
        ],
      },
      {
        title: '4. Transaction',
        fields: [
          { key: 'amount', label: 'Amount', type: 'number', required: true, placeholder: '1000', ruleType: 'AMOUNT', helperText: 'Positive amount in NGN' },
          { key: 'requestedExecutionDate', label: 'Requested Execution Date', type: 'date', helperText: 'ISO Date (YYYY-MM-DD)' },
          { key: 'purpose', label: 'Purpose Code', type: 'text', placeholder: 'Testing 013', maxLength: 35 },
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
    isoCode: 'pain.001.001.12',
    category: 'Payment Initiation & Activation',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'initiatorId', label: 'Initiator ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Initiating participant code' },
          { key: 'debtorId', label: 'Debtor Member ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Debtor Agent Member ID' },
          { key: 'creditorId', label: 'Creditor Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Creditor Agent Member ID' },
        ],
      },
      {
        title: '2. Amount',
        fields: [
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '120.51', ruleType: 'AMOUNT', helperText: 'Transaction amount in NGN' },
        ],
      },
    ],
    prefill: {
      initiatorId: '999057',
      debtorId: '999057',
      creditorId: '999058',
      amount: 120.51,
    },
  },

  'pain.008': {
    key: 'pain.008',
    label: 'Customer Direct Debit Initiation',
    isoCode: 'pain.008.001.10',
    category: 'Direct Debit Operations',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'initiatorId', label: 'Initiator ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'debtorId', label: 'Debtor Member ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'creditorId', label: 'Creditor Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Mandate Info',
        fields: [
          { key: 'mandateId', label: 'Mandate ID', type: 'text', required: true, placeholder: '0000004/001/0000070986', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'dtOfSgntr', label: 'Date of Signature', type: 'date', required: true },
          { key: 'frstColltnDt', label: 'First Collection Date', type: 'date', required: true },
          { key: 'fnlColltnDt', label: 'Final Collection Date', type: 'date', required: true },
          {
            key: 'freqTp',
            label: 'Frequency',
            type: 'select',
            required: true,
            ruleType: 'FREQUENCY_TYPE',
            options: [
              { value: 'MNTH', label: 'MNTH - Monthly' },
              { value: 'WEEK', label: 'WEEK - Weekly' },
              { value: 'DAIL', label: 'DAIL - Daily' },
              { value: 'QURT', label: 'QURT - Quarterly' },
              { value: 'YEAR', label: 'YEAR - Yearly' },
              { value: 'ADHO', label: 'ADHO - Adhoc' },
            ],
          },
        ],
      },
      {
        title: '3. Parties & Accounts',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'JOHN DOE', maxLength: 100 },
          { key: 'debtorIban', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: '0177136558', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'ACME BILLING LIMITED', maxLength: 100 },
          { key: 'creditorIban', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: '3157417712', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
      {
        title: '4. Amount & Remittance',
        fields: [
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '100.00', ruleType: 'AMOUNT' },
          { key: 'remittanceInfo', label: 'Remittance Info', type: 'text', placeholder: 'UTILITY BILL FEB-2025', fullWidth: true, maxLength: 140 },
        ],
      },
    ],
    prefill: {
      initiatorId: '999057',
      debtorId: '999057',
      creditorId: '999058',
      mandateId: '0000004/001/0000070986',
      dtOfSgntr: '2025-02-01',
      frstColltnDt: '2025-02-16',
      fnlColltnDt: '2025-12-31',
      freqTp: 'MNTH',
      debtorName: 'JOHN DOE',
      debtorIban: '0177136558',
      creditorName: 'ACME BILLING LIMITED',
      creditorIban: '3157417712',
      amount: 100.0,
      remittanceInfo: 'UTILITY BILL FEB-2025',
    },
  },

  'pain.009': {
    key: 'pain.009',
    label: 'Mandate Initiation Request',
    isoCode: 'pain.009.001.08',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
          { key: 'collectionAmount', label: 'Collection Amount', type: 'number', required: true, placeholder: '2200.00', ruleType: 'AMOUNT' },
        ],
      },
      {
        title: '2. Mandate Terms',
        fields: [
          {
            key: 'sequenceType',
            label: 'Sequence Type',
            type: 'select',
            required: true,
            ruleType: 'SEQUENCE_TYPE',
            options: [
              { value: 'RCUR', label: 'RCUR - Recurring' },
              { value: 'OOFF', label: 'OOFF - One-off' },
              { value: 'FRST', label: 'FRST - First' },
              { value: 'FNAL', label: 'FNAL - Final' },
            ],
          },
          {
            key: 'frequencyType',
            label: 'Frequency Type',
            type: 'select',
            required: true,
            ruleType: 'FREQUENCY_TYPE',
            options: [
              { value: 'MNTH', label: 'MNTH - Monthly' },
              { value: 'WEEK', label: 'WEEK - Weekly' },
              { value: 'DAIL', label: 'DAIL - Daily' },
              { value: 'QURT', label: 'QURT - Quarterly' },
              { value: 'YEAR', label: 'YEAR - Yearly' },
              { value: 'ADHO', label: 'ADHO - Adhoc' },
            ],
          },
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date', required: true },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date', required: true },
        ],
      },
      {
        title: '3. Creditor',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', fullWidth: true, maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account (IBAN)', type: 'text', required: true, placeholder: '0987654321', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
      {
        title: '4. Debtor',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', fullWidth: true, maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account (IBAN)', type: 'text', required: true, placeholder: '0123456789', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998',
      destinationId: '999997',
      currency: 'NGN',
      collectionAmount: 2200.0,
      sequenceType: 'RCUR',
      frequencyType: 'MNTH',
      firstCollectionDate: '2026-02-01',
      finalCollectionDate: '2027-01-31',
      creditorName: 'INNOVATECH SOLUTIONS',
      creditorAccountNumber: '0987654321',
      debtorName: 'ACME TECHNOLOGIES LTD',
      debtorAccountNumber: '0123456789',
    },
  },

  'pain.010': {
    key: 'pain.010',
    label: 'Mandate Amendment Request',
    isoCode: 'pain.010.001.08',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Mandate Reference',
        fields: [
          { key: 'orgnlMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99999820260331160816119597368459797', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'orgnlMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pain.009.001.08', maxLength: 35 },
          { key: 'orgnlMndtId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-00061', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'amdmntRsnCode', label: 'Amendment Reason Code', type: 'text', required: true, placeholder: 'AC04', maxLength: 4, ruleType: 'REASON_CODE' },
        ],
      },
      {
        title: '3. Amended Terms',
        fields: [
          {
            key: 'sequenceType',
            label: 'Sequence Type',
            type: 'select',
            required: true,
            ruleType: 'SEQUENCE_TYPE',
            options: [
              { value: 'RCUR', label: 'RCUR - Recurring' },
              { value: 'OOFF', label: 'OOFF - One-off' },
            ],
          },
          {
            key: 'frequencyType',
            label: 'Frequency Type',
            type: 'select',
            required: true,
            ruleType: 'FREQUENCY_TYPE',
            options: [
              { value: 'MNTH', label: 'MNTH - Monthly' },
              { value: 'WEEK', label: 'WEEK - Weekly' },
              { value: 'DAIL', label: 'DAIL - Daily' },
              { value: 'QURT', label: 'QURT - Quarterly' },
            ],
          },
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date', required: true },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date', required: true },
        ],
      },
      {
        title: '4. Parties',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'Tester', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '0987654320', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Ponmile Joy', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '3157417712', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998',
      destinationId: '999997',
      orgnlMsgId: '99999820260331160816119597368459797',
      orgnlMsgNmId: 'pain.009.001.08',
      orgnlMndtId: 'MNDT-RCUR-00061',
      amdmntRsnCode: 'AC04',
      sequenceType: 'RCUR',
      frequencyType: 'MNTH',
      firstCollectionDate: '2026-04-01',
      finalCollectionDate: '2026-04-30',
      creditorName: 'Tester',
      creditorAccountNumber: '0987654320',
      debtorName: 'Ponmile Joy',
      debtorAccountNumber: '3157417712',
    },
  },

  'pain.011': {
    key: 'pain.011',
    label: 'Mandate Cancellation Request',
    isoCode: 'pain.011.001.08',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Mandate Reference',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99999820260331160816119597368459797', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalMandateId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-00061', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'cancellationReasonCode', label: 'Reason Code', type: 'text', required: true, placeholder: 'AC04', maxLength: 4, ruleType: 'REASON_CODE' },
          { key: 'cancellationReasonDescription', label: 'Reason Description', type: 'text', placeholder: 'Mandate cancelled', maxLength: 100 },
        ],
      },
      {
        title: '3. Parties & Terms',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'Tester', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '0987654320', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Ponmile Joy', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '3157417712', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998',
      destinationId: '999997',
      originalMsgId: '99999820260331160816119597368459797',
      originalMandateId: 'MNDT-RCUR-00061',
      cancellationReasonCode: 'AC04',
      cancellationReasonDescription: 'Mandate cancelled',
      creditorName: 'Tester',
      creditorAccountNumber: '0987654320',
      debtorName: 'Ponmile Joy',
      debtorAccountNumber: '3157417712',
    },
  },

  'pain.012': {
    key: 'pain.012',
    label: 'Mandate Acceptance Report',
    isoCode: 'pain.012.001.08',
    category: 'Mandate Management',
    sections: [
      {
        title: '1. Agents & Result',
        fields: [
          { key: 'creditorAgentMemberId', label: 'Creditor Agent Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
          {
            key: 'accepted',
            label: 'Acceptance Result',
            type: 'select',
            required: true,
            options: [
              { value: 'true', label: 'True - Mandate Accepted' },
              { value: 'false', label: 'False - Mandate Rejected' },
            ],
          },
        ],
      },
      {
        title: '2. Original Mandate Details',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820251211112346125578725905163', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pain.009.001.08', maxLength: 35 },
          { key: 'originalMandateId', label: 'Original Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-00001', maxLength: 35, ruleType: 'NPS_ID' },
        ],
      },
      {
        title: '3. Parties & Accounts',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'CreditorCorp', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '5555544443', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Debtor Customer', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '8888899999', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      creditorAgentMemberId: '999058',
      originalMsgId: '99905820251211112346125578725905163',
      originalMsgNmId: 'pain.009.001.08',
      originalMandateId: 'MNDT-RCUR-00001',
      accepted: 'true',
      creditorName: 'CreditorCorp',
      creditorAccountNumber: '5555544443',
      debtorName: 'Debtor Customer',
      debtorAccountNumber: '8888899999',
    },
  },

  'pacs.008': {
    key: 'pacs.008',
    label: 'Customer Direct Credit',
    isoCode: 'pacs.008.001.12',
    category: 'Credit Transfer & Returns',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Instructing Agent Member ID (6 digits)' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '991040', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Instructed Agent Member ID (6 digits)' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
        ],
      },
      {
        title: '2. Sender',
        fields: [
          { key: 'senderName', label: 'Sender Name', type: 'text', required: true, placeholder: 'Ponmile Joy', fullWidth: true, maxLength: 100 },
          { key: 'senderAccountNumber', label: 'Sender Account (IBAN)', type: 'text', required: true, placeholder: '3157417712', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'senderAccountName', label: 'Sender Account Name', type: 'text', placeholder: 'Ponmile Joy', maxLength: 100 },
        ],
      },
      {
        title: '3. Beneficiary',
        fields: [
          { key: 'beneficiaryName', label: 'Beneficiary Name', type: 'text', required: true, placeholder: 'SAMUEL ADEBOLA', fullWidth: true, maxLength: 100 },
          { key: 'beneficiaryAccountNumber', label: 'Beneficiary Account (IBAN)', type: 'text', required: true, placeholder: '5000002101', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'beneficiaryAccountName', label: 'Beneficiary Account Name', type: 'text', placeholder: 'SAMUEL ADEBOLA', maxLength: 100 },
        ],
      },
      {
        title: '4. Transaction',
        fields: [
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '200.00', ruleType: 'AMOUNT' },
          { key: 'narration', label: 'Narration', type: 'text', placeholder: 'Thanks.', fullWidth: true, maxLength: 100 },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: '1', maxLength: 2, ruleType: 'CHANNEL_CODE', helperText: '1 to 11 (1=Teller, 2=Internet, 3=Mobile, 4=POS)' },
          { key: 'nameEnquiryMsgId', label: 'Name Enquiry Msg ID', type: 'text', placeholder: '99999720260820104556011402881687033', maxLength: 35, ruleType: 'NPS_ID' },
        ],
      },
      {
        title: '5. Supplementary Data',
        fields: [
          { key: 'debtorAccountDesignation', label: 'Debtor Account Designation', type: 'text', placeholder: '1', maxLength: 1, ruleType: 'ACCOUNT_DESIGNATION', helperText: '1=Corp, 2=Indiv, 3=Joint, 4=Other, 5=Juv, 6=SoleProp' },
          { key: 'debtorIdType', label: 'Debtor ID Type', type: 'text', placeholder: 'BVN', maxLength: 7, ruleType: 'ID_TYPE' },
          { key: 'debtorIdValue', label: 'Debtor ID Value', type: 'text', placeholder: '22383706153', maxLength: 11, ruleType: 'BVN' },
          { key: 'debtorAccountTier', label: 'Debtor Account Tier', type: 'text', placeholder: '1', maxLength: 1, ruleType: 'ACCOUNT_TIER', helperText: '1=Tier1, 2=Tier2, 3=Tier3' },
          { key: 'creditorIdType', label: 'Creditor ID Type', type: 'text', placeholder: 'BVN', maxLength: 7, ruleType: 'ID_TYPE' },
          { key: 'creditorIdValue', label: 'Creditor ID Value', type: 'text', placeholder: '22383706153', maxLength: 11, ruleType: 'BVN' },
          { key: 'transactionLocation', label: 'Transaction Location', type: 'text', placeholder: '01080652440N020900337921E', maxLength: 25 },
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
    label: 'Customer Direct Debit Transfer',
    isoCode: 'pacs.003.001.09',
    category: 'Direct Debit Operations',
    sections: [
      {
        title: '1. Party Identifiers',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
          { key: 'amount', label: 'Amount (NGN)', type: 'number', required: true, placeholder: '2200.00', ruleType: 'AMOUNT' },
        ],
      },
      {
        title: '2. Mandate Details',
        fields: [
          { key: 'mandateId', label: 'Mandate ID', type: 'text', required: true, placeholder: 'MNDT-RCUR-123456', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'dateOfSignature', label: 'Date of Signature', type: 'date', required: true },
          { key: 'firstCollectionDate', label: 'First Collection Date', type: 'date', required: true },
          { key: 'finalCollectionDate', label: 'Final Collection Date', type: 'date', required: true },
          {
            key: 'frequencyType',
            label: 'Frequency',
            type: 'select',
            required: true,
            ruleType: 'FREQUENCY_TYPE',
            options: [
              { value: 'MNTH', label: 'MNTH - Monthly' },
              { value: 'WEEK', label: 'WEEK - Weekly' },
              { value: 'DAIL', label: 'DAIL - Daily' },
              { value: 'QURT', label: 'QURT - Quarterly' },
            ],
          },
        ],
      },
      {
        title: '3. Parties & Accounts',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'INNOVATECH SOLUTIONS', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '0987654321', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'ACME TECHNOLOGIES LTD', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '0123456789', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'narration', label: 'Narration', type: 'text', placeholder: 'Monthly subscription fee', fullWidth: true, maxLength: 100 },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: '3', maxLength: 2, ruleType: 'CHANNEL_CODE' },
        ],
      },
    ],
    prefill: {
      sourceId: '999998',
      destinationId: '999997',
      currency: 'NGN',
      amount: 2200.0,
      mandateId: 'MNDT-RCUR-123456',
      dateOfSignature: '2026-01-01',
      firstCollectionDate: '2026-02-01',
      finalCollectionDate: '2027-01-31',
      frequencyType: 'MNTH',
      creditorName: 'INNOVATECH SOLUTIONS',
      creditorAccountNumber: '0987654321',
      debtorName: 'ACME TECHNOLOGIES LTD',
      debtorAccountNumber: '0123456789',
      narration: 'Monthly subscription fee',
      channelCode: '3',
    },
  },

  'pacs.004': {
    key: 'pacs.004',
    label: 'Payment Return',
    isoCode: 'pacs.004.001.11',
    category: 'Credit Transfer & Returns',
    sections: [
      {
        title: '1. Return Header',
        fields: [
          { key: 'sourceId', label: 'Instructing Agent Member ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Instructed Agent Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'returnedAmount', label: 'Returned Amount (NGN)', type: 'number', required: true, placeholder: '1000.00', ruleType: 'AMOUNT' },
          { key: 'returnReasonCode', label: 'Return Reason Code', type: 'text', required: true, placeholder: 'AC04', maxLength: 4, ruleType: 'REASON_CODE' },
        ],
      },
      {
        title: '2. Original Transaction References',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalInstrId', label: 'Original Instruction ID', type: 'text', required: true, placeholder: '99905899905720250802112346977904433', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalEndToEndId', label: 'Original End-to-End ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalTxId', label: 'Original Transaction ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
        ],
      },
      {
        title: '3. Parties & Accounts',
        fields: [
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'John Doe', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '0123456789', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'Jane Smith', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '9876543210', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      sourceId: '999057',
      destinationId: '999058',
      returnedAmount: 1000.0,
      returnReasonCode: 'AC04',
      originalMsgId: '99905820250802112346977904433112345',
      originalInstrId: '99905899905720250802112346977904433',
      originalEndToEndId: '99905820250802112346977904433112345',
      originalTxId: '99905820250802112346977904433112345',
      debtorName: 'John Doe',
      debtorAccountNumber: '0123456789',
      creditorName: 'Jane Smith',
      creditorAccountNumber: '9876543210',
    },
  },

  'acmt.023': {
    key: 'acmt.023',
    label: 'Identification Verification Request',
    isoCode: 'acmt.023.001.04',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Participating Agents',
        fields: [
          { key: 'sourceId', label: 'Instructing Agent Member ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'beneficiaryId', label: 'Instructed Agent Member ID', type: 'text', required: true, placeholder: '991040', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'sendingPartyName', label: 'Sending Party Name', type: 'text', required: true, placeholder: 'Ponmile Joy', maxLength: 100 },
        ],
      },
      {
        title: '2. Verification Target',
        fields: [
          { key: 'partyToBeVerifiedAccountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '5000002100', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'partyToBeVerifiedName', label: 'Expected Party Name', type: 'text', required: true, placeholder: 'James', maxLength: 100 },
        ],
      },
    ],
    prefill: {
      sourceId: '999997',
      beneficiaryId: '991040',
      sendingPartyName: 'Ponmile Joy',
      partyToBeVerifiedAccountNumber: '5000002100',
      partyToBeVerifiedName: 'James',
    },
  },

  'acmt.024': {
    key: 'acmt.024',
    label: 'Identification Verification Report',
    isoCode: 'acmt.024.001.04',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Agents & Result',
        fields: [
          { key: 'sendingInstitutionId', label: 'Sending Institution ID', type: 'text', required: true, placeholder: '999012', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'receivingInstitutionId', label: 'Receiving Institution ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'receiverName', label: 'Receiver Name', type: 'text', placeholder: 'Assigned Org', maxLength: 100 },
          {
            key: 'verificationResponse',
            label: 'Verification Status',
            type: 'select',
            required: true,
            options: [
              { value: 'true', label: 'True - Account Verified Successfully' },
              { value: 'false', label: 'False - Account Verification Failed' },
            ],
          },
        ],
      },
      {
        title: '2. Target Account & Resolved Name',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905720260113101903741123456789012', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID', helperText: '35-character ID of the original acmt.023 request' },
          { key: 'originalCreDtTm', label: 'Original Assignment Creation DateTime', type: 'text', required: true, placeholder: '2026-01-13T10:19:03.741+01:00', fullWidth: true, maxLength: 35, ruleType: 'DATETIME', helperText: 'Creation timestamp in UTC+1 (WAT), e.g. 2026-01-13T10:19:03.741+01:00' },
          { key: 'verifiedAccountNumber', label: 'Verified Account (IBAN)', type: 'text', required: true, placeholder: '1000000001', maxLength: 10, ruleType: 'NUBAN', helperText: '10-digit NUBAN account number' },
          { key: 'verifiedAccountName', label: 'Resolved Account Name', type: 'text', placeholder: 'JOHN DOE ENTERPRISES', maxLength: 100, fullWidth: true, helperText: 'Verified beneficiary name' },
        ],
      },
      {
        title: '3. Supplementary Data (KYC & Risk)',
        fields: [
          { key: 'creditorAccountDesignation', label: 'Account Designation', type: 'text', placeholder: '1', maxLength: 1, ruleType: 'ACCOUNT_DESIGNATION', helperText: '1 (Individual), 2 (Corporate), etc.' },
          {
            key: 'creditorIdType',
            label: 'ID Type',
            type: 'select',
            options: [
              { value: 'BVN', label: 'BVN - Bank Verification Number' },
              { value: 'NIN', label: 'NIN - National Identity Number' },
              { value: 'RC', label: 'RC - Corporate Registration Number' },
            ],
          },
          { key: 'creditorIdValue', label: 'ID Value', type: 'text', placeholder: '22112323460', maxLength: 35, ruleType: 'ID_VALUE' },
          { key: 'creditorAccountTier', label: 'Account Tier', type: 'text', placeholder: '1', maxLength: 1, ruleType: 'ACCOUNT_TIER', helperText: 'Tier 1, 2, or 3' },
          { key: 'transactionRiskRating', label: 'Risk Rating', type: 'text', placeholder: 'R000000000000000000B9', maxLength: 35 },
        ],
      },
    ],
    prefill: {
      sendingInstitutionId: '999012',
      receivingInstitutionId: '999057',
      receiverName: 'Assigned Org',
      originalMsgId: '99905720260113101903741123456789012',
      originalCreDtTm: '2026-01-13T10:19:03.741+01:00',
      verificationResponse: 'true',
      verifiedAccountNumber: '1000000001',
      verifiedAccountName: 'JOHN DOE ENTERPRISES',
      creditorAccountDesignation: '1',
      creditorIdType: 'BVN',
      creditorIdValue: '22112323460',
      creditorAccountTier: '1',
      transactionRiskRating: 'R000000000000000000B9',
    },
  },

  'camt.060': {
    key: 'camt.060',
    label: 'Account Reporting Request',
    isoCode: 'camt.060.001.05',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Requesting Agents',
        fields: [
          { key: 'sourceId', label: 'Source ID', type: 'text', required: true, placeholder: '999998', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Destination ID', type: 'text', required: true, placeholder: '999997', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Account & Report Type',
        fields: [
          { key: 'accountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '0123456789', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
          {
            key: 'requestedMessageType',
            label: 'Report Query Type',
            type: 'select',
            required: true,
            options: [
              { value: 'BALANCE', label: 'BALANCE - Balance Enquiry' },
              { value: 'STATEMENT', label: 'STATEMENT - Account Statement' },
              { value: 'INTRADAY', label: 'INTRADAY - Intraday Report' },
            ],
          },
          { key: 'accountDesignation', label: 'Account Designation', type: 'text', placeholder: '1', maxLength: 1, ruleType: 'ACCOUNT_DESIGNATION' },
          { key: 'channelCode', label: 'Channel Code', type: 'text', placeholder: '1', maxLength: 2, ruleType: 'CHANNEL_CODE' },
        ],
      },
      {
        title: '3. Reporting Period',
        fields: [
          { key: 'fromDate', label: 'From Date', type: 'date', required: true },
          { key: 'toDate', label: 'To Date', type: 'date', required: true },
        ],
      },
    ],
    prefill: {
      sourceId: '999998',
      destinationId: '999997',
      accountNumber: '0123456789',
      currency: 'NGN',
      accountDesignation: '1',
      channelCode: '1',
      requestedMessageType: 'BALANCE',
      fromDate: '2026-01-01',
      toDate: '2026-01-31',
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
          { key: 'sourceId', label: 'Instructing Agent ID', type: 'text', required: true, placeholder: '090004', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'destinationId', label: 'Instructed Agent ID', type: 'text', required: true, placeholder: '100022', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Message Details',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '10002220260402170095982371426577881', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pacs.008.001.12', maxLength: 35 },
          {
            key: 'groupStatus',
            label: 'Group Status',
            type: 'select',
            required: true,
            options: [
              { value: 'ACSC', label: 'ACSC - Accepted Settlement Completed' },
              { value: 'RJCT', label: 'RJCT - Rejected' },
              { value: 'ACCP', label: 'ACCP - Accepted Customer Profile' },
            ],
          },
        ],
      },
      {
        title: '3. Transaction Information',
        fields: [
          { key: 'statusId', label: 'Status ID', type: 'text', placeholder: 'AUTH', maxLength: 35 },
          { key: 'originalInstrId', label: 'Original Instruction ID', type: 'text', placeholder: '10002209000420260402170095982371426', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalTxId', label: 'Original Transaction ID', type: 'text', placeholder: '10002220260331170095982371426577885', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalEndToEndId', label: 'Original End-to-End ID', type: 'text', placeholder: '10002221234519115702293163242525113', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'settlementDate', label: 'Settlement Date', type: 'date' },
        ],
      },
    ],
    prefill: {
      sourceId: '090004',
      destinationId: '100022',
      originalMsgId: '10002220260402170095982371426577881',
      originalMsgNmId: 'pacs.008.001.12',
      groupStatus: 'ACSC',
      statusId: 'AUTH',
      originalInstrId: '10002209000420260402170095982371426',
      originalTxId: '10002220260331170095982371426577885',
      originalEndToEndId: '10002221234519115702293163242525113',
      settlementDate: '2026-04-02',
    },
  },

  'pacs.028': {
    key: 'pacs.028',
    label: 'Payment Status Request',
    isoCode: 'pacs.028.001.06',
    category: 'Credit Transfer & Returns',
    sections: [
      {
        title: '1. Header & Agents',
        fields: [
          { key: 'sourceId', label: 'Instructing Agent Member ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Sending participant institution code' },
          { key: 'destinationId', label: 'Instructed Agent Member ID', type: 'text', required: true, placeholder: '999012', maxLength: 6, ruleType: 'MEMBER_ID', helperText: 'Receiving participant institution code' },
        ],
      },
      {
        title: '2. Original Transaction References',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID', helperText: '35-char MsgId of the original pacs.008' },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pacs.008.001.12', maxLength: 35, helperText: 'e.g. pacs.008.001.12' },
          { key: 'originalCreDtTm', label: 'Original Creation DateTime', type: 'text', placeholder: '2025-02-25T00:02:35.072Z', fullWidth: true, maxLength: 35, ruleType: 'DATETIME', helperText: 'Creation timestamp of original pacs.008' },
          { key: 'originalTxId', label: 'Original Transaction ID', type: 'text', required: true, placeholder: '99905820250802112346977904433112345', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID', helperText: 'TxId of the original pacs.008' },
          { key: 'settlementDate', label: 'Settlement Date', type: 'date', required: true, helperText: 'Settlement date of the original transaction' },
        ],
      },
    ],
    prefill: {
      sourceId: '999057',
      destinationId: '999012',
      originalMsgId: '99905820250802112346977904433112345',
      originalMsgNmId: 'pacs.008.001.12',
      originalCreDtTm: '2025-02-25T00:02:35.072Z',
      originalTxId: '99905820250802112346977904433112345',
      settlementDate: '2025-02-25',
    },
  },

  'pain.014': {
    key: 'pain.014',
    label: 'Payment Activation Status Report',
    isoCode: 'pain.014.001.11',
    category: 'Payment Initiation & Activation',
    sections: [
      {
        title: '1. Status & Agents',
        fields: [
          { key: 'initiatingPartyName', label: 'Initiating Party Name', type: 'text', required: true, placeholder: 'Debtor Bank', maxLength: 100 },
          { key: 'forwardingAgentMemberId', label: 'Forwarding Agent Member ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'debtorAgentMemberId', label: 'Debtor Agent Member ID', type: 'text', placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'creditorAgentMemberId', label: 'Creditor Agent Member ID', type: 'text', placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
          {
            key: 'groupStatus',
            label: 'Group Status',
            type: 'select',
            required: true,
            options: [
              { value: 'ACCP', label: 'ACCP - Accepted' },
              { value: 'RJCT', label: 'RJCT - Rejected' },
            ],
          },
          {
            key: 'transactionStatus',
            label: 'Transaction Status',
            type: 'select',
            required: true,
            options: [
              { value: 'ACCP', label: 'ACCP - Accepted' },
              { value: 'RJCT', label: 'RJCT - Rejected' },
            ],
          },
        ],
      },
      {
        title: '2. Original Activation Reference',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905820260105102349998878725905163', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pain.013.001.11', maxLength: 35 },
          { key: 'originalPmtInfId', label: 'Original Payment Info ID', type: 'text', required: true, placeholder: 'GSFPMTINF035985837', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalEndToEndId', label: 'Original End-to-End ID', type: 'text', required: true, placeholder: 'GSF035985837-E2E', maxLength: 35, ruleType: 'NPS_ID' },
        ],
      },
      {
        title: '3. Parties & Accounts',
        fields: [
          { key: 'creditorName', label: 'Creditor Name', type: 'text', required: true, placeholder: 'CreditorCorp', maxLength: 100 },
          { key: 'creditorAccountNumber', label: 'Creditor Account', type: 'text', required: true, placeholder: '5555544443', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'debtorName', label: 'Debtor Name', type: 'text', required: true, placeholder: 'Debtor Customer', maxLength: 100 },
          { key: 'debtorAccountNumber', label: 'Debtor Account', type: 'text', required: true, placeholder: '8888899999', maxLength: 10, ruleType: 'NUBAN' },
        ],
      },
    ],
    prefill: {
      initiatingPartyName: 'Debtor Bank',
      forwardingAgentMemberId: '999057',
      debtorAgentMemberId: '999058',
      creditorAgentMemberId: '999057',
      groupStatus: 'ACCP',
      transactionStatus: 'ACCP',
      originalMsgId: '99905820260105102349998878725905163',
      originalMsgNmId: 'pain.013.001.11',
      originalPmtInfId: 'GSFPMTINF035985837',
      originalEndToEndId: 'GSF035985837-E2E',
      creditorName: 'CreditorCorp',
      creditorAccountNumber: '5555544443',
      debtorName: 'Debtor Customer',
      debtorAccountNumber: '8888899999',
    },
  },

  'camt.052': {
    key: 'camt.052',
    label: 'Bank To Customer Account Report',
    isoCode: 'camt.052.001.08',
    category: 'Account Services & Statements',
    sections: [
      {
        title: '1. Account Servicer & Scheme',
        fields: [
          { key: 'accountServicerMemberId', label: 'Account Servicer Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'schemeCode', label: 'Scheme Code / Destination ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Query & Account',
        fields: [
          { key: 'originalQueryMsgId', label: 'Original Query Msg ID', type: 'text', required: true, placeholder: '99905820260302123735603795909182287', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'accountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '4488447166', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
        ],
      },
      {
        title: '3. Balance Information',
        fields: [
          { key: 'balanceType', label: 'Balance Type', type: 'text', placeholder: 'CLRG', maxLength: 35 },
          { key: 'balanceAmount', label: 'Balance Amount (NGN)', type: 'number', required: true, placeholder: '500000.00', ruleType: 'AMOUNT' },
          {
            key: 'creditDebitIndicator',
            label: 'Credit / Debit Indicator',
            type: 'select',
            required: true,
            options: [
              { value: 'CRDT', label: 'CRDT - Credit' },
              { value: 'DBIT', label: 'DBIT - Debit' },
            ],
          },
        ],
      },
    ],
    prefill: {
      accountServicerMemberId: '999058',
      schemeCode: '999057',
      originalQueryMsgId: '99905820260302123735603795909182287',
      accountNumber: '4488447166',
      currency: 'NGN',
      balanceType: 'CLRG',
      balanceAmount: 500000.0,
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
        title: '1. Account Servicer & Scheme',
        fields: [
          { key: 'accountServicerMemberId', label: 'Account Servicer Member ID', type: 'text', required: true, placeholder: '999058', maxLength: 6, ruleType: 'MEMBER_ID' },
          { key: 'schemeCode', label: 'Scheme Code / Destination ID', type: 'text', required: true, placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Query & Account',
        fields: [
          { key: 'originalQueryMsgId', label: 'Original Query Msg ID', type: 'text', required: true, placeholder: '99905820260213292033011112202634446', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'accountNumber', label: 'Account Number (IBAN)', type: 'text', required: true, placeholder: '8887788778', maxLength: 10, ruleType: 'NUBAN' },
          { key: 'currency', label: 'Currency', type: 'text', placeholder: 'NGN', maxLength: 3, ruleType: 'CURRENCY' },
        ],
      },
      {
        title: '3. Statement Balances',
        fields: [
          { key: 'openingBalanceAmount', label: 'Opening Balance Amount', type: 'number', required: true, placeholder: '482000.00', ruleType: 'AMOUNT' },
          {
            key: 'openingBalanceCdtDbtInd',
            label: 'Opening Balance Indicator',
            type: 'select',
            required: true,
            options: [
              { value: 'CRDT', label: 'CRDT - Credit' },
              { value: 'DBIT', label: 'DBIT - Debit' },
            ],
          },
          { key: 'closingBalanceAmount', label: 'Closing Balance Amount', type: 'number', required: true, placeholder: '500000.00', ruleType: 'AMOUNT' },
          {
            key: 'closingBalanceCdtDbtInd',
            label: 'Closing Balance Indicator',
            type: 'select',
            required: true,
            options: [
              { value: 'CRDT', label: 'CRDT - Credit' },
              { value: 'DBIT', label: 'DBIT - Debit' },
            ],
          },
        ],
      },
    ],
    prefill: {
      accountServicerMemberId: '999058',
      schemeCode: '999057',
      originalQueryMsgId: '99905820260213292033011112202634446',
      accountNumber: '8887788778',
      currency: 'NGN',
      openingBalanceAmount: 482000.0,
      openingBalanceCdtDbtInd: 'CRDT',
      closingBalanceAmount: 500000.0,
      closingBalanceCdtDbtInd: 'CRDT',
    },
  },

  'pain.002': {
    key: 'pain.002',
    label: 'Customer Payment Status Report',
    isoCode: 'pain.002.001.12',
    category: 'Payment Initiation & Activation',
    sections: [
      {
        title: '1. Initiator & Agents',
        fields: [
          { key: 'initiatingPartyName', label: 'Initiating Party Name', type: 'text', required: true, placeholder: 'Musa', maxLength: 100 },
          { key: 'debtorAgentBIC', label: 'Debtor Agent BIC', type: 'text', placeholder: 'DEUTDEFF', maxLength: 8 },
          { key: 'debtorAgentMemberId', label: 'Debtor Agent Member ID', type: 'text', placeholder: '999057', maxLength: 6, ruleType: 'MEMBER_ID' },
        ],
      },
      {
        title: '2. Original Group Details',
        fields: [
          { key: 'originalMsgId', label: 'Original Message ID', type: 'text', required: true, placeholder: '99905720260225192650869851166984847', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalMsgNmId', label: 'Original Message Name ID', type: 'text', placeholder: 'pain.001.001.12', maxLength: 35 },
          {
            key: 'groupStatus',
            label: 'Group Status',
            type: 'select',
            required: true,
            options: [
              { value: 'ACSC', label: 'ACSC - Accepted Settlement Completed' },
              { value: 'RJCT', label: 'RJCT - Rejected' },
              { value: 'ACCP', label: 'ACCP - Accepted Customer Profile' },
            ],
          },
        ],
      },
      {
        title: '3. Transaction Status & Reasons',
        fields: [
          { key: 'originalPmtInfId', label: 'Original Payment Info ID', type: 'text', required: true, placeholder: 'PMT-20251016-001-SINGLE', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'statusId', label: 'Status ID', type: 'text', placeholder: '99905774143804655117506058383208278', maxLength: 35, ruleType: 'NPS_ID' },
          { key: 'originalEndToEndId', label: 'Original End-to-End ID', type: 'text', required: true, placeholder: '99905774143804655117506058383208278', fullWidth: true, maxLength: 35, ruleType: 'NPS_ID' },
          {
            key: 'transactionStatus',
            label: 'Transaction Status',
            type: 'select',
            required: true,
            options: [
              { value: 'ACSC', label: 'ACSC - Accepted Settlement Completed' },
              { value: 'RJCT', label: 'RJCT - Rejected' },
              { value: 'ACCP', label: 'ACCP - Accepted' },
            ],
          },
          { key: 'statusCode', label: 'Status Reason Code', type: 'text', placeholder: '000', maxLength: 4, ruleType: 'REASON_CODE' },
          { key: 'additionalInformation', label: 'Additional Information', type: 'text', placeholder: 'Accepted', fullWidth: true, maxLength: 140 },
        ],
      },
    ],
    prefill: {
      initiatingPartyName: 'Musa',
      debtorAgentBIC: 'DEUTDEFF',
      debtorAgentMemberId: '999057',
      originalMsgId: '99905720260225192650869851166984847',
      originalMsgNmId: 'pain.001.001.12',
      groupStatus: 'ACSC',
      originalPmtInfId: 'PMT-20251016-001-SINGLE',
      statusId: '99905774143804655117506058383208278',
      originalEndToEndId: '99905774143804655117506058383208278',
      transactionStatus: 'ACSC',
      statusCode: '000',
      additionalInformation: 'Accepted',
    },
  },
};

export interface SidebarGroup {
  label: string;
  items: { key: string; isoCode: string }[];
}

export const SIDEBAR_GROUPS: SidebarGroup[] = [
  {
    label: 'Credit Transfer & Returns',
    items: [
      { key: 'pacs.008', isoCode: 'pacs.008 (Customer Direct Credit)' },
      { key: 'pacs.002', isoCode: 'pacs.002 (Payment Status Report)' },
      { key: 'pacs.028', isoCode: 'pacs.028 (Payment Status Request)' },
      { key: 'pacs.004', isoCode: 'pacs.004 (Payment Return)' },
    ],
  },
  {
    label: 'Direct Debit Operations',
    items: [
      { key: 'pacs.003', isoCode: 'pacs.003 (Direct Debit Transfer)' },
      { key: 'pain.008', isoCode: 'pain.008 (Customer Direct Debit Initiation)' },
    ],
  },
  {
    label: 'Mandate Management',
    items: [
      { key: 'pain.009', isoCode: 'pain.009 (Mandate Initiation Request)' },
      { key: 'pain.010', isoCode: 'pain.010 (Mandate Amendment Request)' },
      { key: 'pain.011', isoCode: 'pain.011 (Mandate Cancellation Request)' },
      { key: 'pain.012', isoCode: 'pain.012 (Mandate Acceptance Report)' },
    ],
  },
  {
    label: 'Payment Initiation & Activation',
    items: [
      { key: 'pain.001', isoCode: 'pain.001 (Customer Credit Transfer Initiation)' },
      { key: 'pain.002', isoCode: 'pain.002 (Customer Payment Status Report)' },
      { key: 'pain.013', isoCode: 'pain.013 (Creditor Payment Activation Request)' },
      { key: 'pain.014', isoCode: 'pain.014 (Payment Activation Status Report)' },
    ],
  },
  {
    label: 'Account Services & Statements',
    items: [
      { key: 'acmt.023', isoCode: 'acmt.023 (Identification Verification Request)' },
      { key: 'acmt.024', isoCode: 'acmt.024 (Identification Verification Report)' },
      { key: 'camt.060', isoCode: 'camt.060 (Account Reporting Request)' },
      { key: 'camt.052', isoCode: 'camt.052 (Bank To Customer Account Report)' },
      { key: 'camt.053', isoCode: 'camt.053 (Bank To Customer Statement)' },
    ],
  },
];

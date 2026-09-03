import { FlowDefinition } from '../../types/orchestrator';

export const ORCHESTRATOR_FLOWS: FlowDefinition[] = [
  {
    id: 'direct-debit',
    name: 'Direct Debit Lifecycle Flow',
    category: 'Direct Debit Operations',
    description:
      'Seamless multi-party direct debit lifecycle: Create mandate (pain.009) ➔ Debtor bank authorization (pain.012) ➔ Trigger debit collection (pain.008 or pacs.003) ➔ Check final settlement confirmation (pacs.002).',
    badge: 'Mandate + Debit + Settlement',
    icon: '💳',
    steps: [
      {
        stepIndex: 0,
        stepId: 'dd-step-1',
        messageType: 'pain.009',
        isoCode: 'pain.009.001.07',
        title: '1. Create Mandate',
        description: 'Creditor initiates a recurring or one-off direct debit mandate request to authorize future collections.',
        role: 'Creditor Bank / Originator',
        producedContextKeys: [
          'mandateId',
          'pain009MsgId',
          'creditorName',
          'creditorAccountNumber',
          'debtorName',
          'debtorAccountNumber',
          'amount',
        ],
        defaultPayload: {
          sourceId: '999057',
          destinationId: '999058',
          mandateId: 'MNDT-RCUR-849201',
          creditorName: 'Swift Telecom Ltd',
          creditorAccountNumber: '3157417712',
          creditorAgentMemberId: '999057',
          debtorName: 'Tunde Fabiyi',
          debtorAccountNumber: '0000002110',
          debtorAgentMemberId: '999058',
          sequenceType: 'RCUR',
          frequencyType: 'MNTH',
          firstCollectionDate: '2026-09-10',
          finalCollectionDate: '2027-09-10',
          currency: 'NGN',
          collectionAmount: 25000,
        },
      },
      {
        stepIndex: 1,
        stepId: 'dd-step-2',
        messageType: 'pain.012',
        isoCode: 'pain.012.001.07',
        title: '2. Authorize Mandate',
        description: 'Debtor bank validates customer account and issues acceptance report authorizing the mandate.',
        role: 'Debtor Bank (Payer Bank)',
        requiredContextKeys: ['mandateId', 'pain009MsgId'],
        producedContextKeys: ['pain012MsgId', 'mandateAccepted'],
        defaultPayload: {
          accepted: 'true',
          sequenceType: 'RCUR',
          frequencyType: 'MNTH',
          firstCollectionDate: '2026-09-10',
          finalCollectionDate: '2027-09-10',
        },
      },
      {
        stepIndex: 2,
        stepId: 'dd-step-3',
        messageType: 'pain.008',
        isoCode: 'pain.008.001.11',
        title: '3. Trigger Direct Debit',
        description: 'Creditor triggers debit pull against the active mandate. Choose between Customer Initiation (pain.008) or Direct Debit Transfer (pacs.003).',
        role: 'Creditor Clearing Agent',
        requiredContextKeys: ['mandateId'],
        producedContextKeys: ['directDebitMsgId', 'endToEndId', 'instructionId'],
        variantOptions: [
          { label: 'pain.008 (Customer Direct Debit Initiation)', messageType: 'pain.008', isoCode: 'pain.008.001.11' },
          { label: 'pacs.003 (Financial Customer Direct Debit)', messageType: 'pacs.003', isoCode: 'pacs.003.001.09' },
        ],
        defaultPayload: {
          initiatorId: '999057',
          serviceLevelCode: 'NURG',
          localInstrumentCode: 'NPSDD',
          sequenceType: 'RCUR',
          currency: 'NGN',
          amount: 25000,
        },
      },
      {
        stepIndex: 3,
        stepId: 'dd-step-4',
        messageType: 'pacs.002',
        isoCode: 'pacs.002.001.10',
        title: '4. Check Settlement',
        description: 'Clearing switch confirms ACSC (Accepted Settlement Completed) status for the debit transaction.',
        role: 'Clearing Switch / Debtor Bank',
        requiredContextKeys: ['directDebitMsgId', 'endToEndId', 'instructionId'],
        producedContextKeys: ['pacs002MsgId', 'settlementStatus'],
        defaultPayload: {
          groupStatus: 'ACSC',
          statusCode: '000',
          additionalInformation: 'Mandate Collection Settled Successfully',
        },
      },
    ],
  },
  {
    id: 'instant-credit-transfer',
    name: 'Instant Credit Transfer Flow',
    category: 'Credit Transfer & Returns',
    description:
      'Standard instant bank transfer with auto-enquiry and reverse payment return: Query beneficiary name (acmt.023) ➔ Execute credit transfer (pacs.008) ➔ Trigger automated return (pacs.004) with swapped routing.',
    badge: 'Enquiry + Transfer + Return',
    icon: '⚡',
    steps: [
      {
        stepIndex: 0,
        stepId: 'ict-step-1',
        messageType: 'acmt.023',
        isoCode: 'acmt.023.001.04',
        title: '1. Name Enquiry',
        description: 'Originating bank verifies recipient account number and captures name enquiry session ID.',
        role: 'Originating Bank',
        producedContextKeys: [
          'nameEnquiryMsgId',
          'sessionId',
          'partyToBeVerifiedName',
          'partyToBeVerifiedAccountNumber',
          'beneficiaryId',
          'sourceId',
        ],
        defaultPayload: {
          sourceId: '999999',
          beneficiaryId: '999015',
          partyToBeVerifiedName: 'Oso Emmanuel',
          partyToBeVerifiedAccountNumber: '1111111111',
          sendingPartyName: 'Fidelity Direct',
        },
      },
      {
        stepIndex: 1,
        stepId: 'ict-step-2',
        messageType: 'pacs.008',
        isoCode: 'pacs.008.001.10',
        title: '2. Credit Transfer',
        description: 'Initiating bank executes the instant payment, automatically binding the verified name and session ID.',
        role: 'Originating Bank',
        requiredContextKeys: ['nameEnquiryMsgId', 'partyToBeVerifiedName', 'partyToBeVerifiedAccountNumber'],
        producedContextKeys: ['pacs008MsgId', 'endToEndId', 'instructionId', 'amount'],
        defaultPayload: {
          amount: 50000,
          currency: 'NGN',
          narration: 'Instant Credit Transfer',
          settlementMethod: 'CLRG',
          channelCode: '1',
        },
      },
      {
        stepIndex: 2,
        stepId: 'ict-step-3',
        messageType: 'pacs.004',
        isoCode: 'pacs.004.001.10',
        title: '3. Payment Return',
        description: 'Receiving bank returns funds with reversed routing (Instructed Bank ➔ Instructing Bank) referencing the pacs.008 ID.',
        role: 'Beneficiary Bank (Returning Agent)',
        requiredContextKeys: ['pacs008MsgId', 'endToEndId', 'instructionId', 'amount'],
        producedContextKeys: ['pacs004MsgId', 'returnReasonCode'],
        defaultPayload: {
          returnReasonCode: 'AC04',
          returnReasonInfo: 'Account Closed or Restricted',
          currency: 'NGN',
          clearingChannel: 'RTNS',
          localInstrument: 'CTAA',
        },
      },
    ],
  },
  {
    id: 'request-to-pay',
    name: 'Request to Pay (RTP) Flow',
    category: 'Payment Initiation & Activation',
    description:
      'End-to-end conversational merchant checkout: Name Enquiry (acmt.023) ➔ Payment Activation Request (pain.013) ➔ Customer authorizes Credit Transfer (pacs.008) ➔ Settlement Confirmation (pacs.002).',
    badge: 'Enquiry + RTP + Transfer + ACSC',
    icon: '📲',
    steps: [
      {
        stepIndex: 0,
        stepId: 'rtp-step-1',
        messageType: 'acmt.023',
        isoCode: 'acmt.023.001.04',
        title: '1. Name Enquiry',
        description: 'Verify payer account and retrieve clearing routing before dispatching payment request.',
        role: 'Merchant / Payee Bank',
        producedContextKeys: ['nameEnquiryMsgId', 'sessionId', 'partyToBeVerifiedName', 'partyToBeVerifiedAccountNumber'],
        defaultPayload: {
          sourceId: '999997',
          beneficiaryId: '991015',
          partyToBeVerifiedName: 'Tunde Fabiyi',
          partyToBeVerifiedAccountNumber: '0000002110',
          sendingPartyName: 'Ponmile Joy',
        },
      },
      {
        stepIndex: 1,
        stepId: 'rtp-step-2',
        messageType: 'pain.013',
        isoCode: 'pain.013.001.11',
        title: '2. Payment Activation (RTP)',
        description: 'Merchant issues digital Request to Pay invoice to payer bank requesting authorization.',
        role: 'Creditor Institution',
        requiredContextKeys: ['nameEnquiryMsgId'],
        producedContextKeys: ['pain013MsgId', 'endToEndId', 'amount'],
        defaultPayload: {
          amount: 1000,
          currency: 'NGN',
          sourceName: 'Ponmile Joy',
          clientId: 'ClientID-123456',
          paymentInformationId: 'GSFPMTINF035985837',
          requestedExecutionDate: '2026-09-04',
          purpose: 'Invoice Funding',
        },
      },
      {
        stepIndex: 2,
        stepId: 'rtp-step-3',
        messageType: 'pacs.008',
        isoCode: 'pacs.008.001.10',
        title: '3. Credit Transfer',
        description: 'Customer accepts the RTP prompt in their banking app, executing payment referencing the invoice.',
        role: 'Debtor Bank',
        requiredContextKeys: ['endToEndId', 'nameEnquiryMsgId'],
        producedContextKeys: ['pacs008MsgId', 'instructionId'],
        defaultPayload: {
          narration: 'Payment for Invoice GSFPMTINF035985837',
          channelCode: '1',
        },
      },
      {
        stepIndex: 3,
        stepId: 'rtp-step-4',
        messageType: 'pacs.002',
        isoCode: 'pacs.002.001.10',
        title: '4. Status Confirmation',
        description: 'Clearing network notifies merchant that funds have been settled into beneficiary account.',
        role: 'Clearing Switch',
        requiredContextKeys: ['pacs008MsgId', 'endToEndId'],
        producedContextKeys: ['pacs002MsgId', 'settlementStatus'],
        defaultPayload: {
          groupStatus: 'ACSC',
        },
      },
    ],
  },
];

/**
 * Client-side fallback context resolution and prefill calculation
 */
export function computeClientNextStepPrefill(
  _flowId: string,
  _targetStepIndex: number,
  targetMessageType: string,
  context: Record<string, string>,
  basePayload: Record<string, any>
): { prefill: Record<string, any>; autoInjectedKeys: Set<string> } {
  const prefill: Record<string, any> = { ...basePayload };
  const autoInjectedKeys = new Set<string>();

  const inject = (key: string, value: any) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      prefill[key] = value;
      autoInjectedKeys.add(key);
    }
  };

  switch (targetMessageType) {
    case 'pain.012':
      inject('originalMandateId', context.mandateId);
      inject('originalMsgId', context.pain009MsgId || context.originalMsgId);
      inject('originalMsgNmId', 'pain.009.001.07');
      inject('accepted', 'true');
      inject('creditorName', context.creditorName);
      inject('creditorAccountNumber', context.creditorAccountNumber);
      inject('creditorAgentMemberId', context.creditorAgentMemberId);
      inject('debtorName', context.debtorName);
      inject('debtorAccountNumber', context.debtorAccountNumber);
      inject('debtorAgentMemberId', context.debtorAgentMemberId);
      inject('sequenceType', context.sequenceType || 'RCUR');
      inject('frequencyType', context.frequencyType || 'MNTH');
      inject('firstCollectionDate', context.firstCollectionDate);
      inject('finalCollectionDate', context.finalCollectionDate);
      break;

    case 'pain.008':
      inject('mandateId', context.mandateId);
      inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      inject('creditorId', context.creditorAgentMemberId || '999057');
      inject('creditorName', context.creditorName);
      inject('creditorIban', context.creditorAccountNumber);
      inject('debtorId', context.debtorAgentMemberId || '999058');
      inject('debtorName', context.debtorName);
      inject('debtorIban', context.debtorAccountNumber);
      inject('amount', context.amount || 25000);
      inject('currency', context.currency || 'NGN');
      inject('sequenceType', context.sequenceType || 'RCUR');
      inject('freqTp', context.frequencyType || 'MNTH');
      inject('frstColltnDt', context.firstCollectionDate || '2026-09-10');
      inject('fnlColltnDt', context.finalCollectionDate || '2027-09-10');
      inject('dtOfSgntr', context.firstCollectionDate || '2026-09-01');
      break;

    case 'pacs.003':
      inject('mandateId', context.mandateId);
      inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      inject('sourceId', context.creditorAgentMemberId || '999057');
      inject('destinationId', context.debtorAgentMemberId || '999058');
      inject('creditorName', context.creditorName);
      inject('creditorAccountNumber', context.creditorAccountNumber);
      inject('debtorName', context.debtorName);
      inject('debtorAccountNumber', context.debtorAccountNumber);
      inject('amount', context.amount || 25000);
      inject('currency', context.currency || 'NGN');
      inject('frequencyType', context.frequencyType || 'MNTH');
      inject('firstCollectionDate', context.firstCollectionDate || '2026-09-10');
      inject('finalCollectionDate', context.finalCollectionDate || '2027-09-10');
      inject('dateOfSignature', context.firstCollectionDate || '2026-09-01');
      break;

    case 'pacs.008':
      inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      inject('sourceId', context.sourceId || '999999');
      inject('destinationId', context.beneficiaryId || context.destinationId || '999015');
      inject('beneficiaryName', context.partyToBeVerifiedName || context.beneficiaryName || context.creditorName);
      inject('beneficiaryAccountNumber', context.partyToBeVerifiedAccountNumber || context.beneficiaryAccountNumber || context.creditorAccountNumber);
      inject('beneficiaryAccountName', context.partyToBeVerifiedName || context.creditorName);
      inject('senderName', context.sendingPartyName || context.debtorName || 'Zenith Bank');
      if (context.amount) inject('amount', Number(context.amount));
      if (context.endToEndId) inject('endToEndId', context.endToEndId);
      break;

    case 'pacs.004':
      inject('sourceId', context.destinationId || '999998');
      inject('destinationId', context.sourceId || '999057');
      inject('originalMsgId', context.pacs008MsgId || context.originalMsgId);
      inject('originalMsgNameId', 'pacs.008.001.10');
      inject('originalInstrId', context.instructionId);
      inject('originalEndToEndId', context.endToEndId);
      inject('originalTxId', context.endToEndId);
      inject('returnedAmount', context.amount ? Number(context.amount) : 50000);
      inject('originalIntrBkSttlmAmt', context.amount ? Number(context.amount) : 50000);
      inject('currency', context.currency || 'NGN');
      inject('debtorName', context.senderName || context.debtorName || 'Original Debtor');
      inject('debtorAccountNumber', context.senderAccountNumber || context.debtorAccountNumber || '0000002110');
      inject('debtorAccountName', context.senderAccountName || context.senderName || 'Original Debtor');
      inject('debtorAgentMmbId', context.sourceId || '999057');
      inject('creditorName', context.beneficiaryName || context.creditorName || 'Original Creditor');
      inject('creditorAccountNumber', context.beneficiaryAccountNumber || context.creditorAccountNumber || '3157417712');
      inject('creditorAgentMmbId', context.destinationId || '999998');
      inject('returnReasonCode', 'AC04');
      inject('returnReasonInfo', 'Account Closed or Restricted');
      break;

    case 'pain.013':
      inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      inject('creditorName', context.partyToBeVerifiedName || context.creditorName);
      inject('creditorAccountNumber', context.partyToBeVerifiedAccountNumber || context.creditorAccountNumber);
      inject('creditorAgentMemberId', context.beneficiaryId || '999997');
      inject('sourceId', context.beneficiaryId || '999997');
      inject('destinationId', context.sourceId || '991015');
      if (context.amount) inject('amount', Number(context.amount));
      break;

    case 'pacs.002':
      const origMsg = context.directDebitMsgId || context.pacs008MsgId || context.pain008MsgId || context.pacs003MsgId || context.originalMsgId;
      inject('originalMsgId', origMsg);
      inject('originalMsgNmId', context.lastTriggerMsgNmId || 'pacs.008.001.10');
      inject('originalInstrId', context.instructionId);
      inject('originalEndToEndId', context.endToEndId);
      inject('originalTxId', context.endToEndId);
      inject('groupStatus', 'ACSC');
      inject('statusId', context.endToEndId || '99905774143804655117506058383208278');
      inject('sourceId', context.destinationId || '999998');
      inject('destinationId', context.sourceId || '999057');
      break;
  }

  return { prefill, autoInjectedKeys };
}

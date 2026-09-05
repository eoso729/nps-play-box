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

import { MESSAGE_CONFIGS } from '../workbench/messageConfigs';

/**
 * Client-side context resolution and prefill calculation
 */
export function computeClientNextStepPrefill(
  _flowId: string,
  _targetStepIndex: number,
  targetMessageType: string,
  context: Record<string, string>,
  basePayload: Record<string, any>
): { prefill: Record<string, any>; autoInjectedKeys: Set<string> } {
  const specPrefill = MESSAGE_CONFIGS[targetMessageType]?.prefill
    ? { ...MESSAGE_CONFIGS[targetMessageType].prefill }
    : {};
  const prefill: Record<string, any> = { ...specPrefill, ...basePayload };
  const autoInjectedKeys = new Set<string>();

  const inject = (key: string, value: any) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      prefill[key] = value;
      autoInjectedKeys.add(key);
    }
  };

  switch (targetMessageType) {
    case 'pain.012':
      if (context.mandateId) inject('originalMandateId', context.mandateId);
      if (context.pain009MsgId || context.originalMsgId) inject('originalMsgId', context.pain009MsgId || context.originalMsgId);
      inject('originalMsgNmId', 'pain.009.001.07');
      inject('accepted', 'true');
      if (context.creditorName) inject('creditorName', context.creditorName);
      if (context.creditorAccountNumber) inject('creditorAccountNumber', context.creditorAccountNumber);
      if (context.creditorAgentMemberId) inject('creditorAgentMemberId', context.creditorAgentMemberId);
      if (context.debtorName) inject('debtorName', context.debtorName);
      if (context.debtorAccountNumber) inject('debtorAccountNumber', context.debtorAccountNumber);
      if (context.debtorAgentMemberId) inject('debtorAgentMemberId', context.debtorAgentMemberId);
      if (context.sequenceType) inject('sequenceType', context.sequenceType);
      if (context.frequencyType) inject('frequencyType', context.frequencyType);
      if (context.firstCollectionDate) inject('firstCollectionDate', context.firstCollectionDate);
      if (context.finalCollectionDate) inject('finalCollectionDate', context.finalCollectionDate);
      break;

    case 'pain.008':
      if (context.mandateId) inject('mandateId', context.mandateId);
      if (context.nameEnquiryMsgId || context.sessionId) inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      if (context.creditorAgentMemberId) {
        inject('creditorId', context.creditorAgentMemberId);
        inject('initiatorId', context.creditorAgentMemberId);
      }
      if (context.creditorName) {
        inject('creditorName', context.creditorName);
        inject('initiatingPartyName', context.creditorName);
      }
      if (context.creditorAccountNumber) inject('creditorIban', context.creditorAccountNumber);
      if (context.debtorAgentMemberId) inject('debtorId', context.debtorAgentMemberId);
      if (context.debtorName) inject('debtorName', context.debtorName);
      if (context.debtorAccountNumber) inject('debtorIban', context.debtorAccountNumber);
      if (context.amount) inject('amount', Number(context.amount));
      if (context.currency) inject('currency', context.currency);
      if (context.sequenceType) inject('sequenceType', context.sequenceType);
      if (context.frequencyType) inject('freqTp', context.frequencyType);
      if (context.firstCollectionDate) {
        inject('frstColltnDt', context.firstCollectionDate);
        inject('dtOfSgntr', context.firstCollectionDate);
      }
      if (context.finalCollectionDate) inject('fnlColltnDt', context.finalCollectionDate);
      break;

    case 'pacs.003':
      if (context.mandateId) inject('mandateId', context.mandateId);
      if (context.nameEnquiryMsgId || context.sessionId) inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      if (context.creditorAgentMemberId) inject('sourceId', context.creditorAgentMemberId);
      if (context.debtorAgentMemberId) inject('destinationId', context.debtorAgentMemberId);
      if (context.creditorName) inject('creditorName', context.creditorName);
      if (context.creditorAccountNumber) inject('creditorAccountNumber', context.creditorAccountNumber);
      if (context.debtorName) inject('debtorName', context.debtorName);
      if (context.debtorAccountNumber) inject('debtorAccountNumber', context.debtorAccountNumber);
      if (context.amount) inject('amount', Number(context.amount));
      if (context.currency) inject('currency', context.currency);
      if (context.frequencyType) inject('frequencyType', context.frequencyType);
      if (context.firstCollectionDate) {
        inject('firstCollectionDate', context.firstCollectionDate);
        inject('dateOfSignature', context.firstCollectionDate);
      }
      if (context.finalCollectionDate) inject('finalCollectionDate', context.finalCollectionDate);
      break;

    case 'pacs.008':
      if (context.nameEnquiryMsgId || context.sessionId) inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      if (context.sourceId) inject('sourceId', context.sourceId);
      if (context.beneficiaryId || context.destinationId) inject('destinationId', context.beneficiaryId || context.destinationId);
      if (context.partyToBeVerifiedName || context.beneficiaryName || context.creditorName) {
        const bName = context.partyToBeVerifiedName || context.beneficiaryName || context.creditorName;
        inject('beneficiaryName', bName);
        inject('beneficiaryAccountName', bName);
      }
      if (context.partyToBeVerifiedAccountNumber || context.beneficiaryAccountNumber || context.creditorAccountNumber) {
        inject('beneficiaryAccountNumber', context.partyToBeVerifiedAccountNumber || context.beneficiaryAccountNumber || context.creditorAccountNumber);
      }
      if (context.sendingPartyName || context.debtorName) inject('senderName', context.sendingPartyName || context.debtorName);
      if (context.debtorAccountNumber) inject('senderAccountNumber', context.debtorAccountNumber);
      if (context.amount) inject('amount', Number(context.amount));
      if (context.endToEndId) inject('endToEndId', context.endToEndId);
      break;

    case 'pacs.004':
      // Reverse routing: beneficiary bank returns to originating bank
      if (context.destinationId) inject('sourceId', context.destinationId);
      if (context.sourceId) inject('destinationId', context.sourceId);
      if (context.pacs008MsgId || context.originalMsgId) inject('originalMsgId', context.pacs008MsgId || context.originalMsgId);
      inject('originalMsgNameId', 'pacs.008.001.10');
      if (context.instructionId) inject('originalInstrId', context.instructionId);
      if (context.endToEndId) {
        inject('originalEndToEndId', context.endToEndId);
        inject('originalTxId', context.endToEndId);
      }
      if (context.amount) {
        inject('returnedAmount', Number(context.amount));
        inject('originalIntrBkSttlmAmt', Number(context.amount));
      }
      if (context.currency) inject('currency', context.currency);
      if (context.senderName || context.debtorName) inject('debtorName', context.senderName || context.debtorName);
      if (context.senderAccountNumber || context.debtorAccountNumber) inject('debtorAccountNumber', context.senderAccountNumber || context.debtorAccountNumber);
      if (context.senderName || context.debtorName) inject('debtorAccountName', context.senderName || context.debtorName);
      if (context.sourceId) inject('debtorAgentMmbId', context.sourceId);
      if (context.beneficiaryName || context.creditorName) inject('creditorName', context.beneficiaryName || context.creditorName);
      if (context.beneficiaryAccountNumber || context.creditorAccountNumber) inject('creditorAccountNumber', context.beneficiaryAccountNumber || context.creditorAccountNumber);
      if (context.destinationId) inject('creditorAgentMmbId', context.destinationId);
      inject('returnReasonCode', 'AC04');
      inject('returnReasonInfo', 'Account Closed or Restricted');
      break;

    case 'pain.013':
      if (context.nameEnquiryMsgId || context.sessionId) inject('nameEnquiryMsgId', context.nameEnquiryMsgId || context.sessionId);
      if (context.partyToBeVerifiedName || context.creditorName) inject('creditorName', context.partyToBeVerifiedName || context.creditorName);
      if (context.partyToBeVerifiedAccountNumber || context.creditorAccountNumber) inject('creditorAccountNumber', context.partyToBeVerifiedAccountNumber || context.creditorAccountNumber);
      if (context.beneficiaryId) {
        inject('creditorAgentMemberId', context.beneficiaryId);
        inject('sourceId', context.beneficiaryId);
      }
      if (context.sourceId) inject('destinationId', context.sourceId);
      if (context.amount) inject('amount', Number(context.amount));
      break;

    case 'pacs.002': {
      const origMsg = context.directDebitMsgId || context.pacs008MsgId || context.pain008MsgId || context.pacs003MsgId || context.originalMsgId;
      if (origMsg) inject('originalMsgId', origMsg);
      inject('originalMsgNmId', context.lastTriggerMsgNmId || 'pacs.008.001.10');
      if (context.instructionId) inject('originalInstrId', context.instructionId);
      if (context.endToEndId) {
        inject('originalEndToEndId', context.endToEndId);
        inject('originalTxId', context.endToEndId);
        inject('statusId', context.endToEndId);
      }
      inject('groupStatus', 'ACSC');
      if (context.destinationId) inject('sourceId', context.destinationId);
      if (context.sourceId) inject('destinationId', context.sourceId);
      break;
    }
  }

  return { prefill, autoInjectedKeys };
}

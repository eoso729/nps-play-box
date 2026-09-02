package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentInitiationRequestDto {

    // 1. Group Header & Initiator
    private String initiatorId;            // Initiating participant code e.g. "999057"
    private String initiatingPartyName;     // e.g. "Musa"
    private String schemeCode;              // Identification scheme code e.g. "999057"
    private String forwardingAgentBIC;      // Forwarding agent BICFI (optional)

    // 2. Payment Information Block
    private String paymentInformationId;    // Unique PmtInf ID e.g. "PMT-20251016-001-SINGLE"
    private Boolean batchBooking;           // Batch booking indicator (default false)
    private String requestedExecutionDate;  // Requested execution date (ISO-8601 YYYY-MM-DD)
    private String chargeBearer;            // Charge bearer e.g. "SLEV", "CRED", "DEBT", "SHAR"

    // 3. Debtor (Payer)
    private String debtorId;                // Debtor Agent Member ID fallback
    private String debtorName;              // Debtor Name e.g. "Musa"
    private String debtorAccountNumber;     // 10-digit NUBAN
    private String debtorAccountName;       // Name on Debtor Account
    private String debtorAgentMemberId;     // Debtor Bank Clearing Code e.g. "999057"
    private String debtorAgentBIC;          // Debtor Bank BICFI e.g. "999057"

    // 4. Creditor (Beneficiary)
    private String creditorId;              // Creditor Agent Member ID fallback
    private String creditorName;            // Creditor Name e.g. "James"
    private String creditorAccountNumber;   // 10-digit NUBAN
    private String creditorAccountName;     // Name on Creditor Account
    private String creditorAgentMemberId;   // Creditor Bank Clearing Code e.g. "999058"
    private String creditorAgentBIC;        // Creditor Bank BICFI e.g. "999058"

    // 5. Payment Transaction Details
    private String endToEndId;              // End-to-end identification (max 35 chars)
    private BigDecimal amount;              // Instructed amount
    private String currency;                // ISO currency code (default "NGN")
    private String remittanceInformation;   // Unstructured remittance text (max 140 chars)

    // 6. Supplementary Data - CreditorInfo
    private String accountDesignation;      // 1=Individual, 2=Corporate, etc.
    private String idType;                  // BVN, NIN, PASSPORT, etc.
    private String idValue;                 // 11-digit BVN or ID string
    private String accountTier;             // 1, 2, 3

    // 7. Supplementary Data - TransactionInfo
    private String transactionLocation;     // GPS coordinates or location code
    private String channelCode;             // 1-Teller, 2-Internet, 3-Mobile, etc.
    private Boolean fixedCollectionAmount;  // Fixed collection amount flag
    private String mandateCode;             // Mandate reference code (optional)

}


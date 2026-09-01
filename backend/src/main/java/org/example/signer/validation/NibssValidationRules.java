package org.example.signer.validation;

import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

public class NibssValidationRules {

    // Regex Patterns
    public static final Pattern BVN_PATTERN = Pattern.compile("^\\d{11}$");
    public static final Pattern NIN_PATTERN = Pattern.compile("^\\d{11}$");
    public static final Pattern NUBAN_PATTERN = Pattern.compile("^\\d{10}$");
    public static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{10,15}$");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern LOCATION_PATTERN = Pattern.compile("^[0-9A-Za-z]{12,30}$");
    public static final Pattern INSTITUTION_CODE_PATTERN = Pattern.compile("^\\d{6}$");
    public static final Pattern SESSION_ID_PATTERN = Pattern.compile("^\\d{6}\\d{12}\\d{12}$");

    // NPS ID Patterns (exactly 35 characters)
    // MsgId / TxId: Source Inst ID (6) + yyyyMMddHHmmss (14) + randomized 15 digits = 35 chars
    public static final Pattern MSG_ID_PATTERN = Pattern.compile("^\\d{6}\\d{14}\\d{15}$");
    // EndToEndId: Source Inst ID (6) + 29 randomized digits = 35 chars
    public static final Pattern END_TO_END_ID_PATTERN = Pattern.compile("^[0-9A-Za-z]{6}[0-9A-Za-z]{29}$");
    // InstrId: Source Inst (6) + Dest Inst (6) + yyyyMMddHHmmss (14) + 9 randomized digits = 35 chars
    public static final Pattern INSTR_ID_PATTERN = Pattern.compile("^\\d{6}\\d{6}\\d{14}\\d{9}$");

    // 1. Account Designation (1-6)
    public static final Map<String, String> ACCOUNT_DESIGNATIONS = new LinkedHashMap<>();
    static {
        ACCOUNT_DESIGNATIONS.put("1", "Corporate");
        ACCOUNT_DESIGNATIONS.put("2", "Individual");
        ACCOUNT_DESIGNATIONS.put("3", "Joint");
        ACCOUNT_DESIGNATIONS.put("4", "Others");
        ACCOUNT_DESIGNATIONS.put("5", "Juvenile");
        ACCOUNT_DESIGNATIONS.put("6", "Sole Proprietorship");
    }

    // 2. Account Tier (1-3)
    public static final Map<String, String> ACCOUNT_TIERS = new LinkedHashMap<>();
    static {
        ACCOUNT_TIERS.put("1", "Tier 1");
        ACCOUNT_TIERS.put("2", "Tier 2");
        ACCOUNT_TIERS.put("3", "Tier 3");
    }

    // 3. ID Types (BVN, NIN, RC, FIRSTIN, JTBTIN)
    public static final Set<String> ID_TYPES = new HashSet<>(Arrays.asList(
            "BVN", "NIN", "RC", "FIRSTIN", "JTBTIN", "1", "2", "3", "4", "5"
    ));

    // 4. Channel Codes (1-11)
    public static final Map<String, String> CHANNEL_CODES = new LinkedHashMap<>();
    static {
        CHANNEL_CODES.put("1", "Bank Teller");
        CHANNEL_CODES.put("2", "Internet Banking");
        CHANNEL_CODES.put("3", "Mobile Phones");
        CHANNEL_CODES.put("4", "POS Terminals");
        CHANNEL_CODES.put("5", "ATM");
        CHANNEL_CODES.put("6", "Vendor/Merchant Web Portal");
        CHANNEL_CODES.put("7", "Third – Party Payment Platform");
        CHANNEL_CODES.put("8", "USSD");
        CHANNEL_CODES.put("9", "Other Channels");
        CHANNEL_CODES.put("10", "Social Media");
        CHANNEL_CODES.put("11", "Agency Banking");
    }

    // 5. Sequence Type (SeqTp)
    public static final Set<String> SEQUENCE_TYPES = new HashSet<>(Arrays.asList(
            "RCUR", "OOFF", "FRST", "FNAL"
    ));

    // 6. Frequency Type (Frqcy Tp)
    public static final Set<String> FREQUENCY_TYPES = new HashSet<>(Arrays.asList(
            "DAIL", "WEEK", "MNTH", "QURT", "YEAR", "ADHO", "MIAN"
    ));

    // 7. Settlement Method (SttlmMtd)
    public static final Set<String> SETTLEMENT_METHODS = new HashSet<>(Arrays.asList(
            "INDA", "INGA", "COVE", "CLRG"
    ));

    // 8. Clearing Channel (ClrChanl)
    public static final Set<String> CLEARING_CHANNELS = new HashSet<>(Arrays.asList(
            "RTGS", "RTNS", "MPNS", "BOOK"
    ));

    // 9. Local Instrument (LclInstrm)
    public static final Set<String> LOCAL_INSTRUMENTS = new HashSet<>(Arrays.asList(
            "CSDC", "CTAA", "CTAW", "CTWA", "NPSDD"
    ));

    // 10. Charge Bearer (ChrgBr)
    public static final Set<String> CHARGE_BEARERS = new HashSet<>(Arrays.asList(
            "DEBT", "CRED", "SHAR", "SLEV"
    ));

    // 11. Cancellation Identification (CxlId)
    public static final Set<String> CANCELLATION_IDS = new HashSet<>(Arrays.asList(
            "CRCL", "CRTN", "MRTN"
    ));

    // 12. Group Status / Transaction Status (GrpSts, TxSts)
    public static final Set<String> STATUS_CODES = new HashSet<>(Arrays.asList(
            "ACSC", "RJCT", "ACCP", "AUTH", "BOOK", "PDNG", "ACTC", "PART"
    ));

    // 13. Credit/Debit Indicator (CdtDbtInd)
    public static final Set<String> CREDIT_DEBIT_INDICATORS = new HashSet<>(Arrays.asList(
            "CRDT", "DBIT"
    ));

    // 14. Requested Message Name Identification (ReqdMsgNmId)
    public static final Set<String> REQD_MSG_NM_IDS = new HashSet<>(Arrays.asList(
            "INTRADAY", "STATEMENT", "BALANCE"
    ));

    // 15. Entry Status (Sts)
    public static final Set<String> ENTRY_STATUSES = new HashSet<>(Arrays.asList(
            "BOOK", "PEND", "INFO"
    ));

    // 16. Bank Transaction Codes
    public static final Set<String> DOMAIN_CODES = new HashSet<>(Arrays.asList(
            "PMNT", "FDFM", "OTHR"
    ));
    public static final Set<String> FAMILY_CODES = new HashSet<>(Arrays.asList(
            "RCDT", "RDDT", "DBDT", "CDDT"
    ));
    public static final Set<String> SUB_FAMILY_CODES = new HashSet<>(Arrays.asList(
            "ESCT", "CHRG", "COMM"
    ));

    // 17. Reporting Period Type (RptgPrd)
    public static final Set<String> REPORTING_PERIOD_TYPES = new HashSet<>(Arrays.asList(
            "ALLL", "CRTD", "MODD", "CURR", "DAILY", "MONTHLY"
    ));

    // 18. Official NIBSS / ISO 20022 Reject Reason Codes Dictionary
    public static final Map<String, ReasonCodeDetail> REJECT_REASON_CODES = new LinkedHashMap<>();
    static {
        addReason("AB05", "TimeoutCreditorAgent", "Transaction stopped due to timeout at the Creditor Agent");
        addReason("AB06", "TimeoutInstructedAgent", "Transaction stopped due to timeout at the Instructed Agent");
        addReason("AB07", "OfflineAgent", "Agent of message is not online. Generic usage if cannot determine who is offline");
        addReason("AB08", "OfflineCreditorAgent", "Creditor Agent is not online");
        addReason("AB09", "ErrorCreditorAgent", "Transaction stopped due to error at the Creditor Agent");
        addReason("AB10", "ErrorInstructedAgent", "Transaction stopped due to error at the Instructed Agent");
        addReason("AB11", "TimeoutDebtorAgent", "Transaction stopped due to timeout at the Debtor Agent");
        addReason("AC01", "IncorrectAccountNumber", "Account number is invalid or missing");
        addReason("AC02", "InvalidDebtorAccountNumber", "Debtor account number invalid or missing");
        addReason("AC03", "InvalidCreditorAccountNumber", "Creditor account number invalid or missing");
        addReason("AC04", "ClosedAccountNumber", "Account number specified has been closed on the bank of account’s books");
        addReason("AC05", "ClosedDebtorAccountNumber", "Debtor account number closed");
        addReason("AC06", "BlockedAccount", "Account specified is blocked, prohibiting posting of transactions against it");
        addReason("AC07", "ClosedCreditorAccountNumber", "Creditor account number closed");
        addReason("AC08", "InvalidBranchCode", "Branch code is invalid or missing");
        addReason("AC09", "InvalidAccountCurrency", "Account currency is invalid or missing");
        addReason("AC10", "InvalidDebtorAccountCurrency", "Debtor account currency is invalid or missing");
        addReason("AC11", "InvalidCreditorAccountCurrency", "Creditor account currency is invalid or missing");
        addReason("AC12", "InvalidAccountType", "Account type missing or invalid");
        addReason("AC13", "InvalidDebtorAccountType", "Debtor account type missing or invalid");
        addReason("AC14", "InvalidCreditorAccountType", "Creditor account type missing or invalid");
        addReason("AC16", "CardNumberInvalid", "Credit or debit card number is invalid");
        addReason("AG01", "TransactionForbidden", "Transaction forbidden on this type of account");
        addReason("AG03", "TransactionNotSupported", "Transaction type not supported/authorized on this account");
        addReason("AG04", "InvalidAgentCountry", "Agent country code is missing or invalid");
        addReason("AG05", "InvalidDebtorAgentCountry", "Debtor agent country code is missing or invalid");
        addReason("AG06", "InvalidCreditorAgentCountry", "Creditor agent country code is missing or invalid");
        addReason("AG08", "InvalidAccessRights", "Transaction failed due to invalid or missing user or access right");
        addReason("AG13", "ForbiddenReturnPayment", "Returned payments derived from previously returned transactions are not allowed");
        addReason("AM01", "ZeroAmount", "Specified message amount is equal to zero");
        addReason("AM02", "NotAllowedAmount", "Specific transaction/message amount is greater than allowed maximum");
        addReason("AM03", "NotAllowedCurrency", "Specified message amount is an non processable currency outside of agreement");
        addReason("AM04", "InsufficientFunds", "Amount of funds available to cover specified message amount is insufficient");
        addReason("AM05", "Duplication", "Duplicate transaction detected");
        addReason("AM06", "TooLowAmount", "Specified transaction amount is less than agreed minimum");
        addReason("AM07", "BlockedAmount", "Amount specified in message has been blocked by regulatory authorities");
        addReason("AM11", "InvalidTransactionCurrency", "Transaction currency is invalid or missing");
        addReason("AM12", "InvalidAmount", "Amount is invalid or missing");
        addReason("AM13", "AmountExceedsClearingSystemLimit", "Transaction amount exceeds limits set by clearing system");
        addReason("AM14", "AmountExceedsAgreedLimit", "Transaction amount exceeds limits agreed between bank and client");
        addReason("AM15", "AmountBelowClearingSystemMinimum", "Transaction amount below minimum set by clearing system");
        addReason("AM16", "InvalidGroupControlSum", "Control Sum at the Group level is invalid");
        addReason("AM17", "InvalidPaymentInfoControlSum", "Control Sum at the Payment Information level is invalid");
        addReason("AM18", "InvalidNumberOfTransactions", "Number of transactions is invalid or missing");
        addReason("AM19", "InvalidGroupNumberOfTransactions", "Number of transactions at the Group level is invalid or missing");
        addReason("AM20", "InvalidPaymentInfoNumberOfTransaction", "Number of transactions at the Payment Information level is invalid");
        addReason("AM21", "LimitExceeded", "Transaction amount exceeds limits agreed between bank and client");
        addReason("AM22", "ZeroAmountNotApplied", "Unable to apply zero amount to designated account");
        addReason("AM23", "AmountExceedsSettlementLimit", "Transaction amount exceeds settlement limit");
        addReason("BE01", "InconsistenWithEndCustomer", "Identification of end customer is not consistent with associated account number");
        addReason("BE04", "MissingCreditorAddress", "Specification of creditor address is missing/not correct");
        addReason("BE05", "UnrecognisedInitiatingParty", "Party who initiated the message is not recognised by the end customer");
        addReason("BE06", "UnknownEndCustomer", "End customer specified is not known at associated Sort/National Bank Code");
        addReason("BE07", "MissingDebtorAddress", "Specification of debtor address is missing/not correct");
        addReason("BE08", "MissingDebtorName", "Debtor name is missing");
        addReason("BE09", "InvalidCountry", "Country code is missing or Invalid");
        addReason("BE10", "InvalidDebtorCountry", "Debtor country code is missing or invalid");
        addReason("BE11", "InvalidCreditorCountry", "Creditor country code is missing or invalid");
        addReason("BE12", "InvalidCountryOfResidence", "Country code of residence is missing or Invalid");
        addReason("BE13", "InvalidDebtorCountryOfResidence", "Country code of debtor’s residence is missing or Invalid");
        addReason("BE14", "InvalidCreditorCountryOfResidence", "Country code of creditor’s residence is missing or Invalid");
        addReason("BE15", "InvalidIdentificationCode", "Identification code missing or invalid");
        addReason("BE16", "InvalidDebtorIdentificationCode", "Debtor identification code missing or invalid");
        addReason("BE17", "InvalidCreditorIdentificationCode", "Creditor identification code missing or invalid");
        addReason("BE18", "InvalidContactDetails", "Contact details missing or invalid");
        addReason("BE19", "InvalidChargeBearerCode", "Charge bearer code for transaction type is invalid");
        addReason("BE20", "InvalidNameLength", "Name length exceeds local rules for payment type");
        addReason("BE21", "MissingName", "Name missing or invalid");
        addReason("CNOR", "CreditorBankIsNotRegistered", "Creditor bank is not registered under this BIC in the CSM");
        addReason("CURR", "IncorrectCurrency", "Currency of the payment is incorrect");
        addReason("DNOR", "DebtorBankIsNotRegistered", "Debtor bank is not registered under this BIC in the CSM");
        addReason("DU02", "DuplicatePaymentInformationID", "Payment Information Block is not unique");
        addReason("DU03", "DuplicateTransaction", "Transaction is not unique");
        addReason("DU04", "DuplicateEndToEndID", "End To End ID is not unique");
        addReason("DU05", "DuplicateInstructionID", "Instruction ID is not unique");
        addReason("DUPL", "DuplicatePaymentOrCharge", "Payment or charge is a duplicate of another payment or charge");
        addReason("FF03", "InvalidPaymentTypeInformation", "Payment Type Information is missing or invalid");
        addReason("FF04", "InvalidServiceLevelCode", "Service Level code is missing or invalid");
        addReason("FF05", "InvalidLocalInstrumentCode", "Local Instrument code is missing or invalid");
        addReason("FF06", "InvalidCategoryPurposeCode", "Category Purpose code is missing or invalid");
        addReason("FF07", "InvalidPurpose", "Purpose is missing or invalid");
        addReason("FF08", "InvalidEndToEndId", "End to End Id missing or invalid");
        addReason("RC01", "BankIdentifierIncorrect", "Bank identifier code specified in the message has an incorrect format");
        addReason("RC02", "InvalidBankIdentifier", "Bank identifier is invalid or missing");
        addReason("RC03", "InvalidDebtorBankIdentifier", "Debtor bank identifier is invalid or missing");
        addReason("RC04", "InvalidCreditorBankIdentifier", "Creditor bank identifier is invalid or missing");
        addReason("RC05", "InvalidBICIdentifier", "BIC identifier is invalid or missing");
        addReason("RC06", "InvalidDebtorBICIdentifier", "Debtor BIC identifier is invalid or missing");
        addReason("RC07", "InvalidCreditorBICIdentifier", "Creditor BIC identifier is invalid or missing");
        addReason("RC10", "InvalidCreditorClearingSystemMemberIdentifier", "Creditor Clearing System Member Identifier is invalid or missing");
        addReason("RF01", "NotUniqueTransactionReference", "Transaction reference is not unique within the message");
        addReason("TM01", "InvalidCutOffTime", "Associated message was received after agreed processing cut‑off time");
        addReason("FR01", "Fraud", "Returned as a result of fraud");
        addReason("NOAS", "NoAnswerFromCustomer", "No response from Beneficiary");
        addReason("FRAD", "FraudulentOrigin", "Cancellation requested following a transaction originated fraudulently");
        addReason("IEDT", "IncorrectExpiryDateTime", "Expiry date time of the request‑to‑pay is incorrect");
        addReason("DS28", "ReturnForTechnicalReason", "Message routed to the wrong environment");
        addReason("INDT", "InvalidDetails", "Details not valid for this field");
        addReason("MINF", "MissingInformation", "Information missing for the field or cannot be empty");
        addReason("EOL1", "EndOfLife", "Expiration of the payment authorisation due to no use for too long");
    }

    private static void addReason(String code, String name, String description) {
        REJECT_REASON_CODES.put(code, ReasonCodeDetail.builder().code(code).name(name).description(description).build());
    }

    @Data
    @Builder
    public static class ReasonCodeDetail {
        private String code;
        private String name;
        private String description;
    }

    // 20. Fields whose values must strictly be UPPERCASE in NIBSS / ISO 20022
    public static final Set<String> UPPERCASE_CODE_FIELDS = new HashSet<>(Arrays.asList(
            "IdType", "Ccy", "SttlmMtd", "ClrChanl", "LclInstrm", "ChrgBr", "SeqTp", "Frqcy",
            "GrpSts", "TxSts", "CdtDbtInd", "Sts", "Domn", "Fmly", "SubFmlyCd", "Cd", "BICFI",
            "IBAN", "UETR", "ClrSys", "SvcLvl", "CtgyPurp", "Purp"
    ));

    // 21. Canonical ISO 20022 and NIBSS Tag Names Dictionary (PascalCase / CamelCase / Uppercase)
    public static final Map<String, String> CANONICAL_TAGS = new LinkedHashMap<>();
    static {
        // Root & Message Wrapper Elements
        addTag("Document");
        addTag("FIToFICstmrCdtTrf");
        addTag("FIToFIPmtStsRpt");
        addTag("FIToFIPmtStsReq");
        addTag("PmtRtr");
        addTag("FIToFICstmrDrctDbt");
        addTag("CdtrPmtActvtnReq");
        addTag("CdtrPmtActvtnStsRpt");
        addTag("CdtrPmtActvtnReqStsRpt");
        addTag("CstmrCdtTrfInitn");
        addTag("CstmrPmtStsRpt");
        addTag("CstmrDrctDbtInitn");
        addTag("MndtInitnReq");
        addTag("MndtAmdmntReq");
        addTag("MndtCxlReq");
        addTag("MndtAccptncRpt");
        addTag("IdVrfctnReq");
        addTag("IdVrfctnRpt");
        addTag("AcctRptgReq");
        addTag("BkToCstmrAcctRpt");
        addTag("BkToCstmrStmt");
        addTag("MsgRcpt");
        addTag("OrgnlBizQry");
        addTag("FrDtTm");
        addTag("ToDtTm");
        addTag("AcctSvcrRef");
        addTag("CdOrPrtry");
        addTag("Ownr");
        addTag("Svcr");
        addTag("SchmeNm");
        addTag("AnyBIC");
        addTag("NtryDtls");
        addTag("TxDtls");
        addTag("RltdAgts");
        addTag("TrckgInd");
        addTag("InitgPty");

        // Group Header & General Elements
        addTag("GrpHdr");
        addTag("MsgId");
        addTag("CreDtTm");
        addTag("BtchBookg");
        addTag("NbOfTxs");
        addTag("CtrlSum");
        addTag("SttlmInf");
        addTag("SttlmMtd");
        addTag("SttlmAcct");
        addTag("ClrSys");
        addTag("InstgAgt");
        addTag("InstdAgt");
        addTag("FinInstnId");
        addTag("ClrSysMmbId");
        addTag("MmbId");
        addTag("BICFI");
        addTag("Nm");
        addTag("PstlAdr");
        addTag("OrgId");
        addTag("PrvtId");
        addTag("Othr");
        addTag("Id");
        addTag("SchemeNm");
        addTag("Prtry");
        addTag("Cd");
        addTag("Issr");

        // Transactions & Payments
        addTag("CdtTrfTxInf");
        addTag("PmtId");
        addTag("InstrId");
        addTag("EndToEndId");
        addTag("TxId");
        addTag("UETR");
        addTag("PmtTpInf");
        addTag("LclInstrm");
        addTag("CtgyPurp");
        addTag("IntrBkSttlmAmt");
        addTag("IntrBkSttlmDt");
        addTag("InstdAmt");
        addTag("XchgRate");
        addTag("ChrgBr");
        addTag("ChrgsInf");
        addTag("Amt");
        addTag("Agt");
        addTag("Dbtr");
        addTag("DbtrAcct");
        addTag("DbtrAgt");
        addTag("CdtrAgt");
        addTag("Cdtr");
        addTag("CdtrAcct");
        addTag("InstrForNxtAgt");
        addTag("InstrInf");
        addTag("BtchBookg");
        addTag("NbOfTxs");
        addTag("SttlmMtd");
        addTag("SttlmInf");
        addTag("DebtorMetadata");
        addTag("CreditorMetadata");
        addTag("BiometricData");
        addTag("Purp");
        addTag("RmtInf");
        addTag("Ustrd");
        addTag("Strd");
        addTag("IBAN");
        addTag("BBAN");
        addTag("Ccy");
        addTag("Tp");
        addTag("PmtInf");
        addTag("PmtInfId");
        addTag("PmtMtd");
        addTag("ReqdExctnDt");
        addTag("ReqdColltnDt");

        // Status, Return, & Cancellation
        addTag("OrgnlGrpInfAndSts");
        addTag("OrgnlMsgId");
        addTag("OrgnlMsgNmId");
        addTag("OrgnlCreDtTm");
        addTag("OrgnlNbOfTxs");
        addTag("OrgnlCtrlSum");
        addTag("GrpSts");
        addTag("StsRsnInf");
        addTag("Rsn");
        addTag("AddtlInf");
        addTag("TxInfAndSts");
        addTag("StsId");
        addTag("OrgnlInstrId");
        addTag("OrgnlEndToEndId");
        addTag("OrgnlTxId");
        addTag("OrgnlUETR");
        addTag("TxSts");
        addTag("OrgnlTxRef");
        addTag("StsReqId");
        addTag("RtrId");
        addTag("OrgnlGrpInf");
        addTag("OrgnlPmtInfId");
        addTag("RtrRsnInf");
        addTag("RtrdIntrBkSttlmAmt");
        addTag("RtrdInstdAmt");
        addTag("CompstnAmt");

        // Mandates & Direct Debits
        addTag("Mndt");
        addTag("MndtId");
        addTag("MndtReqId");
        addTag("SvcLvl");
        addTag("Ocrncs");
        addTag("SeqTp");
        addTag("Frqcy");
        addTag("FrstColltnDt");
        addTag("FnlColltnDt");
        addTag("ColltnAmt");
        addTag("MaxAmt");
        addTag("CdtrSchmeId");
        addTag("UndrlygAccptncDtls");
        addTag("AccptncRslt");
        addTag("Accptd");
        addTag("OrgnlMsgInf");
        addTag("OrgnlMndt");
        addTag("OrgnlMndtId");
        addTag("MndtAmdmntRsn");
        addTag("AmdmntRsn");
        addTag("MndtCxlRsn");
        addTag("CxlRsn");
        addTag("DrctDbtTx");
        addTag("MndtRltdInf");
        addTag("DtOfSgntr");
        addTag("DrctDbtTxInf");

        // Name Verification
        addTag("Assgnmt");
        addTag("Assgnr");
        addTag("Assgne");
        addTag("Pty");
        addTag("OrgnlAssgnmt");
        addTag("Rpt");
        addTag("OrgnlId");
        addTag("Vrfctn");
        addTag("OrgnlPtyAndAcctId");
        addTag("UpdtdPtyAndAcctId");
        addTag("Acct");
        addTag("AcctId");

        // Account Reporting & Statements
        addTag("RptgReq");
        addTag("ReqdMsgNmId");
        addTag("RptgPrd");
        addTag("FrToDt");
        addTag("FrDt");
        addTag("ToDt");
        addTag("FrToTm");
        addTag("FrTm");
        addTag("ToTm");
        addTag("Stmt");
        addTag("AcctRpt");
        addTag("ElctrncSeqNb");
        addTag("LglSeqNb");
        addTag("CpyDplctInd");
        addTag("Bal");
        addTag("CdtDbtInd");
        addTag("Dt");
        addTag("DtTm");
        addTag("Ntry");
        addTag("AmtDtls");
        addTag("BkTxCd");
        addTag("Domn");
        addTag("Fmly");
        addTag("SubFmlyCd");
        addTag("BookgDt");
        addTag("ValDt");
        addTag("AcctSvcr");
        addTag("ClrChanl");
        addTag("Sts");

        // NIBSS Supplementary Data Block
        addTag("SplmtryData");
        addTag("PlcAndNm");
        addTag("Envlp");
        addTag("CustomData");
        addTag("DebtorInfo");
        addTag("CreditorInfo");
        addTag("TransactionInfo");
        addTag("MandateInfo");
        addTag("AccountDesignation");
        addTag("IdType");
        addTag("IdValue");
        addTag("AccountTier");
        addTag("TransactionLocation");
        addTag("NameEnquiryMsgId");
        addTag("ChannelCode");
        addTag("RiskRating");
        addTag("MandateType");
        addTag("Amount");
        addTag("OriginalMsgId");
        addTag("OriginalCreDtTm");
        addTag("ReasonCode");
        addTag("ReasonProprietary");
        addTag("VerifiedAccountNumber");
        addTag("VerifiedAccountName");
        addTag("SessionID");
        addTag("SessionId");
    }

    // Universal Tag Character Length Limits according to ISO 20022 and NIBSS NPS Specifications
    public static final Map<String, Integer> TAG_MAX_LENGTHS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static final Map<String, Integer> TAG_EXACT_LENGTHS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static final Map<String, Integer> TAG_MIN_LENGTHS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        // IDs and Identifiers (35 chars)
        setLengthRule("MsgId", 35, 35, 1);
        setLengthRule("TxId", 35, 35, 1);
        setLengthRule("EndToEndId", 35, null, 1);
        setLengthRule("InstrId", 35, null, 1);
        setLengthRule("OrgnlMsgId", 35, 35, 1);
        setLengthRule("OrgnlTxId", 35, 35, 1);
        setLengthRule("OrgnlEndToEndId", 35, null, 1);
        setLengthRule("OrgnlInstrId", 35, null, 1);
        setLengthRule("OrgnlUETR", 36, null, 1);
        setLengthRule("MndtId", 35, null, 1);
        setLengthRule("OrgnlMndtId", 35, null, 1);
        setLengthRule("MndtReqId", 35, null, 1);
        setLengthRule("StsId", 35, null, 1);
        setLengthRule("StsReqId", 35, null, 1);
        setLengthRule("PmtInfId", 35, null, 1);
        setLengthRule("OrgnlPmtInfId", 35, null, 1);
        setLengthRule("RptId", 35, null, 1);
        setLengthRule("StmtId", 35, null, 1);
        setLengthRule("RtrId", 35, null, 1);
        setLengthRule("Id", 35, null, 1);
        setLengthRule("OrgnlId", 35, null, 1);
        setLengthRule("NameEnquiryMsgId", 35, 35, 1);
        setLengthRule("OriginalMsgId", 35, 35, 1);
        setLengthRule("MsgNmId", 35, null, 4);
        setLengthRule("OrgnlMsgNmId", 35, null, 4);
        setLengthRule("ReqdMsgNmId", 35, null, 4);
        setLengthRule("PlcAndNm", 35, null, 1);
        setLengthRule("SessionID", 30, 30, 30);
        setLengthRule("SessionId", 30, 30, 30);

        // Account & Member Identifiers
        setLengthRule("IBAN", 10, 10, 10); // NUBAN account number (exact 10 digits)
        setLengthRule("MmbId", 6, 6, 6); // Clearing System Member ID (exact 6 numeric digits)
        setLengthRule("ClrSysMmbId", 6, 6, 6);
        setLengthRule("BICFI", 11, null, 6);
        setLengthRule("BIC", 11, null, 6);
        setLengthRule("IdValue", 35, null, 1); // 11 if BVN/NIN, up to 35 for others

        // Names & Addresses
        setLengthRule("Nm", 140, null, 1);
        setLengthRule("AdrLine", 70, null, 1);
        setLengthRule("Ustrd", 140, null, 1);
        setLengthRule("AddtlInf", 140, null, 1);
        setLengthRule("PhneNb", 15, null, 10);
        setLengthRule("EmailAdr", 100, null, 5);
        setLengthRule("TransactionLocation", 30, null, 12);

        // Codes & Enumerations
        setLengthRule("Ccy", 3, 3, 3);
        setLengthRule("ClrChanl", 4, 4, 4);
        setLengthRule("SttlmMtd", 4, 4, 4);
        setLengthRule("SeqTp", 4, 4, 4);
        setLengthRule("Tp", 4, null, 1);
        setLengthRule("ChrgBr", 4, 4, 4);
        setLengthRule("GrpSts", 4, 4, 4);
        setLengthRule("TxSts", 4, 4, 4);
        setLengthRule("Sts", 4, 4, 4);
        setLengthRule("CdtDbtInd", 4, 4, 4);
        setLengthRule("Cd", 35, null, 1);
        setLengthRule("Prtry", 35, null, 1);
        setLengthRule("PmtMtd", 3, null, 2);
        setLengthRule("IdType", 10, null, 1);
        setLengthRule("AccountDesignation", 1, 1, 1);
        setLengthRule("AccountTier", 1, 1, 1);
        setLengthRule("ChannelCode", 2, null, 1);
        setLengthRule("RiskRating", 35, null, 1);
        setLengthRule("MandateCategory", 2, null, 1);

        // Date/Time & Numbers
        setLengthRule("NbOfTxs", 15, null, 1);
        setLengthRule("CtrlSum", 18, null, 1);
        setLengthRule("IntrBkSttlmAmt", 18, null, 1);
        setLengthRule("InstdAmt", 18, null, 1);
        setLengthRule("Amt", 18, null, 1);
        setLengthRule("ColltnAmt", 18, null, 1);
        setLengthRule("MaxAmt", 18, null, 1);
        setLengthRule("BtchBookg", 5, null, 4);
        setLengthRule("TrckgInd", 5, null, 4);
        setLengthRule("Accptd", 5, null, 4);
        setLengthRule("Vrfctn", 5, null, 4);
        setLengthRule("CpyDplctInd", 5, null, 4);
    }

    private static void setLengthRule(String tag, int max, Integer exact, Integer min) {
        TAG_MAX_LENGTHS.put(tag, max);
        if (exact != null) {
            TAG_EXACT_LENGTHS.put(tag, exact);
        }
        if (min != null) {
            TAG_MIN_LENGTHS.put(tag, min);
        }
    }

    public static Integer getMaxTagLength(String tag) {
        if (tag == null) return null;
        return TAG_MAX_LENGTHS.get(tag);
    }

    public static Integer getExactTagLength(String tag) {
        if (tag == null) return null;
        return TAG_EXACT_LENGTHS.get(tag);
    }

    public static Integer getMinTagLength(String tag) {
        if (tag == null) return null;
        return TAG_MIN_LENGTHS.get(tag);
    }

    private static void addTag(String tag) {
        CANONICAL_TAGS.put(tag.toLowerCase(), tag);
    }

    public static String getCanonicalTagName(String tag) {
        if (tag == null) return null;
        return CANONICAL_TAGS.get(tag.toLowerCase());
    }

    public static boolean isUppercaseField(String tag) {
        if (tag == null) return false;
        return UPPERCASE_CODE_FIELDS.contains(tag) ||
                UPPERCASE_CODE_FIELDS.stream().anyMatch(f -> f.equalsIgnoreCase(tag));
    }

    /**
     * Checks if a DateTime string is a valid ISO 8601 with WAT (UTC+1) or UTC timezone.
     */
    public static boolean isValidIsoDateTime(String dtStr) {
        if (dtStr == null || dtStr.trim().isEmpty()) return false;
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(dtStr.trim());
            return true;
        } catch (DateTimeParseException e) {
            try {
                DateTimeFormatter.ISO_DATE.parse(dtStr.trim());
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    /**
     * Checks if DateTime has UTC+1 (WAT) indicator (+01:00) or UTC (Z).
     */
    public static boolean hasWatOrUtcOffset(String dtStr) {
        if (dtStr == null) return false;
        String s = dtStr.trim();
        return s.endsWith("+01:00") || s.endsWith("+0100") || s.endsWith("Z") || s.endsWith("+01");
    }

    /**
     * Check if string is a valid ISO Date (YYYY-MM-DD or YYYY-MM-DDZ).
     */
    public static boolean isValidIsoDate(String dStr) {
        if (dStr == null || dStr.trim().isEmpty()) return false;
        String clean = dStr.trim();
        if (clean.endsWith("Z")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(clean);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


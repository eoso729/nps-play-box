package org.example.signer.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.validation.ValidationReportDto;
import org.example.signer.dto.validation.XmlAutoFixRequestDto;
import org.example.signer.dto.validation.XmlAutoFixResponseDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlAutoFixEngine {

    private final XmlValidationEngine validationEngine;

    public XmlAutoFixResponseDto autoFix(XmlAutoFixRequestDto request) {
        List<String> fixesApplied = new ArrayList<>();
        String xml = request.getXmlContent();

        if (xml == null || xml.trim().isEmpty()) {
            return XmlAutoFixResponseDto.builder()
                    .success(false)
                    .detectedMessageType(null)
                    .fixedXml(xml)
                    .fixesApplied(List.of("Cannot fix empty XML content."))
                    .validationReport(validationEngine.validate(xml, null))
                    .build();
        }

        try {
            // Check if XML is raw text without opening tags
            if (!xml.trim().startsWith("<")) {
                xml = "<Document>" + xml.trim() + "</Document>";
                fixesApplied.add("Wrapped raw payload in root <Document> tag.");
            }

            Document doc = XmlUtils.stringToDocument(xml);
            String messageType = request.getMessageType();
            if (messageType == null || messageType.trim().isEmpty() || "auto".equalsIgnoreCase(messageType)) {
                messageType = IsoMessageRegistry.detectMessageType(doc, xml);
            }
            messageType = IsoMessageRegistry.normalizeKey(messageType);
            IsoMessageDefinition def = IsoMessageRegistry.getDefinition(messageType);

            // Check if the input is already valid before doing heavy modifications
            ValidationReportDto initialReport = validationEngine.validate(xml, messageType);

            if (request.isFormatOnly()) {
                String formattedXml = prettyPrint(doc);
                fixesApplied.add("Cleanly formatted XML hierarchy with standard 4-space indentation.");
                ValidationReportDto report = validationEngine.validate(formattedXml, messageType);
                return XmlAutoFixResponseDto.builder()
                        .success(true)
                        .detectedMessageType(def != null ? def.getKey() : messageType)
                        .fixedXml(formattedXml)
                        .fixesApplied(fixesApplied)
                        .validationReport(report)
                        .build();
            }

            if (initialReport.isValid()) {
                // Already valid! Do not mutate data, just clean format/indent
                String formattedXml = prettyPrint(doc);
                fixesApplied.add("XML payload is already fully valid and compliant. Re-formatted with clean 4-space indentation.");
                ValidationReportDto report = validationEngine.validate(formattedXml, messageType);
                return XmlAutoFixResponseDto.builder()
                        .success(true)
                        .detectedMessageType(def != null ? def.getKey() : messageType)
                        .fixedXml(formattedXml)
                        .fixesApplied(fixesApplied)
                        .validationReport(report)
                        .build();
            }

            // 1. Fix Root Tag and Namespace if needed
            if (def != null) {
                Element root = doc.getDocumentElement();
                String rootName = root.getLocalName() != null ? root.getLocalName() : root.getTagName();
                if (!"Document".equalsIgnoreCase(rootName)) {
                    // Re-wrap in Document
                    Element newRoot = doc.createElement("Document");
                    if (def.getNamespace() != null) {
                        newRoot.setAttribute("xmlns", def.getNamespace());
                    }
                    doc.removeChild(root);
                    newRoot.appendChild(root);
                    doc.appendChild(newRoot);
                    fixesApplied.add("Enclosed main message inside standard ISO 20022 <Document> wrapper.");
                } else {
                    String existingNs = root.getAttribute("xmlns");
                    if (def.getNamespace() != null && (existingNs == null || existingNs.isEmpty())) {
                        root.setAttribute("xmlns", def.getNamespace());
                        fixesApplied.add("Injected official ISO namespace xmlns=\"" + def.getNamespace() + "\" on <Document>.");
                    }
                }
            }

            // 2. Recursively fix elements (casing, enums, dates, etc.)
            fixElementsRecursive(doc.getDocumentElement(), def, request, fixesApplied);

            // 3. Inject Missing Mandatory Dates / Headers if enabled
            if (request.isFixDates()) {
                injectMissingDatesAndHeadersIfNeeded(doc, def, fixesApplied);
            }

            // 4. Inject Missing Mandatory Supplementary Data if enabled and required
            boolean shouldFixSupp = request.isFixSupplementaryData();
            if (shouldFixSupp && def != null && isSupplementaryDataRequired(def.getKey())) {
                injectSupplementaryDataIfNeeded(doc, def, fixesApplied);
            }

            // 5. Cross-field synchronization (e.g. Vrfctn.Id to MsgId in acmt.023)
            if (request.isFixIds()) {
                fixAcmtCrossFieldSync(doc, def, fixesApplied);
            }

            // 6. Format and Pretty Print XML (with DOM whitespace stripping to prevent extra indentation/spaces)
            String fixedXml = prettyPrint(doc);
            fixesApplied.add("Formatted XML hierarchy with clean 4-space indentation.");

            // 5. Re-run validation report
            ValidationReportDto report = validationEngine.validate(fixedXml, messageType);

            return XmlAutoFixResponseDto.builder()
                    .success(true)
                    .detectedMessageType(def != null ? def.getKey() : messageType)
                    .fixedXml(fixedXml)
                    .fixesApplied(fixesApplied)
                    .validationReport(report)
                    .build();

        } catch (Exception e) {
            log.warn("Auto-fix error encountered: {}", e.getMessage());
            // Fallback: try basic text replacements
            String fallbackXml = attemptTextFixes(xml, fixesApplied);
            ValidationReportDto report = validationEngine.validate(fallbackXml, null);

            return XmlAutoFixResponseDto.builder()
                    .success(!fixesApplied.isEmpty())
                    .detectedMessageType(null)
                    .fixedXml(fallbackXml)
                    .fixesApplied(fixesApplied)
                    .validationReport(report)
                    .build();
        }
    }

    private void fixElementsRecursive(Element element, IsoMessageDefinition def, XmlAutoFixRequestDto request, List<String> fixesApplied) {
        String tagName = element.getLocalName() != null ? element.getLocalName() : element.getTagName();

        // 1. Tag Name Casing Correction (e.g. <iban> -> <IBAN>, <grphdr> -> <GrpHdr>, <idtype> -> <IdType>)
        String canonicalTag = NibssValidationRules.getCanonicalTagName(tagName);
        if (canonicalTag != null && !canonicalTag.equals(tagName)) {
            try {
                element.getOwnerDocument().renameNode(element, element.getNamespaceURI(), canonicalTag);
                fixesApplied.add("Corrected XML tag casing: <" + tagName + "> -> <" + canonicalTag + ">");
                tagName = canonicalTag;
            } catch (Exception e) {
                log.debug("Could not rename node: {}", e.getMessage());
            }
        }

        // 2. Attributes Casing and Values (e.g. ccy="ngn" -> Ccy="NGN")
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            List<Node> attrsList = new ArrayList<>();
            for (int i = 0; i < attrs.getLength(); i++) {
                attrsList.add(attrs.item(i));
            }
            for (Node attr : attrsList) {
                String aName = attr.getNodeName();
                String aVal = attr.getNodeValue();
                if ("Ccy".equalsIgnoreCase(aName)) {
                    if (!"Ccy".equals(aName)) {
                        element.removeAttribute(aName);
                        element.setAttribute("Ccy", aVal != null ? aVal.toUpperCase() : "NGN");
                        fixesApplied.add("Corrected attribute name casing: @" + aName + " -> @Ccy");
                    } else if (aVal != null && !aVal.equals(aVal.toUpperCase())) {
                        element.setAttribute("Ccy", aVal.toUpperCase());
                        fixesApplied.add("Normalized currency attribute value to UPPERCASE: Ccy=\"" + aVal.toUpperCase() + "\"");
                    }
                }
            }
        }

        String text = element.getTextContent() != null ? element.getTextContent().trim() : "";

        // Check only leaf elements with text
        boolean isLeaf = true;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                isLeaf = false;
                break;
            }
        }

        if (isLeaf && !text.isEmpty()) {
            // A. Strict UPPERCASE value normalization for codes/enums (e.g., BVN, NIN, RC, CLRG, RTNS, NGN, ACSC, CRDT)
            if (NibssValidationRules.isUppercaseField(tagName) || "IdType".equalsIgnoreCase(tagName)) {
                if (!text.equals(text.toUpperCase())) {
                    element.setTextContent(text.toUpperCase());
                    fixesApplied.add("Normalized <" + tagName + "> value to UPPERCASE: '" + text + "' -> '" + text.toUpperCase() + "'");
                    text = text.toUpperCase();
                }
            }

            // B. Date-Time normalization to UTC+1 WAT (+01:00)
            if (request.isFixDates() && (tagName.contains("DtTm") || "CreDtTm".equalsIgnoreCase(tagName) || "OrgnlCreDtTm".equalsIgnoreCase(tagName))) {
                String fixedDt = normalizeToWatDateTime(text);
                if (!fixedDt.equals(text)) {
                    element.setTextContent(fixedDt);
                    fixesApplied.add("Normalized " + tagName + " to ISO 8601 UTC+1 (WAT): '" + text + "' -> '" + fixedDt + "'");
                    text = fixedDt;
                }
            }

            // C. Date normalization (YYYY-MM-DD)
            if (request.isFixDates() && tagName.contains("Dt") && !tagName.contains("DtTm") && !tagName.contains("Dtls")) {
                String fixedDate = normalizeToIsoDate(text);
                if (!fixedDate.equals(text)) {
                    element.setTextContent(fixedDate);
                    fixesApplied.add("Normalized " + tagName + " date format: '" + text + "' -> '" + fixedDate + "'");
                    text = fixedDate;
                }
            }

            // D. Channel Code Fix
            if ("ChannelCode".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.CHANNEL_CODES.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected ChannelCode from '" + text + "' to standard code '1' (Bank Teller).");
                    text = "1";
                }
            }

            // E. Account Designation Fix
            if ("AccountDesignation".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ACCOUNT_DESIGNATIONS.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected AccountDesignation from '" + text + "' to standard '1' (Corporate).");
                    text = "1";
                }
            }

            // F. Account Tier Fix
            if ("AccountTier".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ACCOUNT_TIERS.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected AccountTier from '" + text + "' to standard '1' (Tier 1).");
                    text = "1";
                }
            }

            // G. ID Type check
            if ("IdType".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ID_TYPES.contains(text.toUpperCase())) {
                    element.setTextContent("BVN");
                    fixesApplied.add("Defaulted invalid IdType '" + text + "' to 'BVN'.");
                    text = "BVN";
                }
            }

            // H. IBAN / NUBAN (10 numeric digits)
            if ("IBAN".equalsIgnoreCase(tagName)) {
                String digitsOnly = text.replaceAll("\\D", "");
                if (digitsOnly.length() > 10) {
                    String fixedIban = digitsOnly.substring(digitsOnly.length() - 10);
                    element.setTextContent(fixedIban);
                    fixesApplied.add("Truncated <" + tagName + "> from " + text.length() + " chars to 10-digit NUBAN: '" + fixedIban + "'");
                    text = fixedIban;
                } else if (digitsOnly.length() < 10 && !digitsOnly.isEmpty() && request.isFixIds()) {
                    String fixedIban = String.format("%010d", Long.parseLong(digitsOnly));
                    element.setTextContent(fixedIban);
                    fixesApplied.add("Formatted <" + tagName + "> with leading zeros to 10-digit NUBAN: '" + fixedIban + "'");
                    text = fixedIban;
                }
            }

            // H2. Institution Code / Member ID (exact 6 numeric digits)
            if ("MmbId".equalsIgnoreCase(tagName) || "ClrSysMmbId".equalsIgnoreCase(tagName)) {
                String digitsOnly = text.replaceAll("\\D", "");
                if (digitsOnly.length() > 6) {
                    String fixedMmb = digitsOnly.substring(0, 6);
                    element.setTextContent(fixedMmb);
                    fixesApplied.add("Truncated <" + tagName + "> from " + text.length() + " chars to 6-digit Institution Code: '" + fixedMmb + "'");
                    text = fixedMmb;
                } else if (digitsOnly.length() < 6 && !digitsOnly.isEmpty() && request.isFixIds()) {
                    String fixedMmb = String.format("%06d", Long.parseLong(digitsOnly));
                    element.setTextContent(fixedMmb);
                    fixesApplied.add("Formatted <" + tagName + "> with leading zeros to 6-digit Institution Code: '" + fixedMmb + "'");
                    text = fixedMmb;
                } else if (digitsOnly.isEmpty() && request.isFixIds()) {
                    String fixedMmb = extractSourceInstCode(element.getOwnerDocument());
                    element.setTextContent(fixedMmb);
                    fixesApplied.add("Injected 6-digit Institution Code '" + fixedMmb + "' into empty <" + tagName + ">.");
                    text = fixedMmb;
                }
            }

            // H2-b. BICFI (6-digit numeric institution code or 8/11 alphanumeric BIC)
            if ("BICFI".equalsIgnoreCase(tagName) || "BIC".equalsIgnoreCase(tagName)) {
                String digitsOnly = text.replaceAll("\\D", "");
                if (!digitsOnly.isEmpty() && digitsOnly.length() == text.length()) {
                    if (digitsOnly.length() > 6) {
                        String fixedBic = digitsOnly.substring(0, 6);
                        element.setTextContent(fixedBic);
                        fixesApplied.add("Truncated <" + tagName + "> to 6-digit Institution Code: '" + fixedBic + "'");
                        text = fixedBic;
                    } else if (digitsOnly.length() < 6 && request.isFixIds()) {
                        String fixedBic = String.format("%06d", Long.parseLong(digitsOnly));
                        element.setTextContent(fixedBic);
                        fixesApplied.add("Formatted <" + tagName + "> with leading zeros to 6-digit Institution Code: '" + fixedBic + "'");
                        text = fixedBic;
                    }
                }
            }

            // H3. Session ID (exact 30 numeric digits)
            if ("SessionID".equalsIgnoreCase(tagName) || "SessionId".equalsIgnoreCase(tagName)) {
                String digitsOnly = text.replaceAll("\\D", "");
                if (digitsOnly.length() > 30) {
                    String fixedSession = digitsOnly.substring(0, 30);
                    element.setTextContent(fixedSession);
                    fixesApplied.add("Truncated <" + tagName + "> to 30 digits: '" + fixedSession + "'");
                    text = fixedSession;
                } else if (digitsOnly.length() < 30 && request.isFixIds()) {
                    String srcInst = extractSourceInstCode(element.getOwnerDocument());
                    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
                    Random random = new Random();
                    StringBuilder rand = new StringBuilder();
                    for (int i = 0; i < 12; i++) {
                        rand.append(random.nextInt(10));
                    }
                    String fixedSession = srcInst + ts + rand;
                    element.setTextContent(fixedSession);
                    fixesApplied.add("Regenerated valid 30-digit NIP Session ID for <" + tagName + ">: '" + fixedSession + "'");
                    text = fixedSession;
                }
            }

            // I. NPS ID Character Length & Format Fix (MsgId, TxId, EndToEndId, InstrId, etc.)
            boolean isIdField = "MsgId".equalsIgnoreCase(tagName) || "TxId".equalsIgnoreCase(tagName)
                    || "EndToEndId".equalsIgnoreCase(tagName) || "InstrId".equalsIgnoreCase(tagName)
                    || "OrgnlMsgId".equalsIgnoreCase(tagName) || "OrgnlTxId".equalsIgnoreCase(tagName)
                    || "OrgnlEndToEndId".equalsIgnoreCase(tagName) || "OrgnlInstrId".equalsIgnoreCase(tagName)
                    || "MndtId".equalsIgnoreCase(tagName) || "OrgnlMndtId".equalsIgnoreCase(tagName)
                    || "PmtInfId".equalsIgnoreCase(tagName) || "StsId".equalsIgnoreCase(tagName)
                    || "NameEnquiryMsgId".equalsIgnoreCase(tagName) || "OriginalMsgId".equalsIgnoreCase(tagName);

            if (isIdField) {
                if (text.length() > 35) {
                    String truncatedId = text.substring(0, 35);
                    element.setTextContent(truncatedId);
                    fixesApplied.add("Truncated <" + tagName + "> length from " + text.length() + " to allowed 35 characters: '" + truncatedId + "'");
                    text = truncatedId;
                } else if (text.length() < 35 && request.isFixIds() && ("MsgId".equalsIgnoreCase(tagName) || "TxId".equalsIgnoreCase(tagName) || "EndToEndId".equalsIgnoreCase(tagName) || "InstrId".equalsIgnoreCase(tagName) || "NameEnquiryMsgId".equalsIgnoreCase(tagName))) {
                    String fixedId = generateCompliantNpsId(element.getOwnerDocument(), text, tagName);
                    element.setTextContent(fixedId);
                    fixesApplied.add("Regenerated valid 35-character NPS ID for <" + tagName + ">: '" + fixedId + "'");
                    text = fixedId;
                }
            }

            // J. General Tag Character Length Truncation
            Integer maxLen = NibssValidationRules.getMaxTagLength(tagName);
            if (maxLen != null && text.length() > maxLen) {
                String truncated = text.substring(0, maxLen);
                element.setTextContent(truncated);
                fixesApplied.add("Truncated <" + tagName + "> character length from " + text.length() + " to maximum allowed " + maxLen + " characters: '" + truncated + "'");
                text = truncated;
            }
        }

        // Recurse children
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child) {
                fixElementsRecursive(child, def, request, fixesApplied);
            }
        }
    }

    private void injectSupplementaryDataIfNeeded(Document doc, IsoMessageDefinition def, List<String> fixesApplied) {
        NodeList list = doc.getElementsByTagName("SplmtryData");
        if (list.getLength() == 0) {
            list = doc.getElementsByTagNameNS("*", "SplmtryData");
        }

        if (list.getLength() == 0) {
            Element splmtry = doc.createElement("SplmtryData");
            Element plcAndNm = doc.createElement("PlcAndNm");
            plcAndNm.setTextContent("AdditionalVerificationDetails");
            splmtry.appendChild(plcAndNm);

            Element envlp = doc.createElement("Envlp");
            Element customData = doc.createElement("CustomData");

            // Debtor Info
            Element debtorInfo = doc.createElement("DebtorInfo");
            Element dDesig = doc.createElement("AccountDesignation");
            dDesig.setTextContent("1");
            Element dIdType = doc.createElement("IdType");
            dIdType.setTextContent("BVN");
            Element dIdVal = doc.createElement("IdValue");
            dIdVal.setTextContent("22222222222");
            Element dTier = doc.createElement("AccountTier");
            dTier.setTextContent("1");
            debtorInfo.appendChild(dDesig);
            debtorInfo.appendChild(dIdType);
            debtorInfo.appendChild(dIdVal);
            debtorInfo.appendChild(dTier);
            customData.appendChild(debtorInfo);

            // Creditor Info
            Element creditorInfo = doc.createElement("CreditorInfo");
            Element cDesig = doc.createElement("AccountDesignation");
            cDesig.setTextContent("1");
            Element cIdType = doc.createElement("IdType");
            cIdType.setTextContent("BVN");
            Element cIdVal = doc.createElement("IdValue");
            cIdVal.setTextContent("22222222222");
            Element cTier = doc.createElement("AccountTier");
            cTier.setTextContent("1");
            creditorInfo.appendChild(cDesig);
            creditorInfo.appendChild(cIdType);
            creditorInfo.appendChild(cIdVal);
            creditorInfo.appendChild(cTier);
            customData.appendChild(creditorInfo);

            // Transaction Info
            Element txInfo = doc.createElement("TransactionInfo");
            Element loc = doc.createElement("TransactionLocation");
            loc.setTextContent("01080652440N020900337921E");
            Element nameEnq = doc.createElement("NameEnquiryMsgId");
            nameEnq.setTextContent(generateCompliantNpsId(doc, extractSourceInstCode(doc), "NameEnquiryMsgId"));
            Element chan = doc.createElement("ChannelCode");
            chan.setTextContent("1");
            txInfo.appendChild(loc);
            txInfo.appendChild(nameEnq);
            txInfo.appendChild(chan);
            customData.appendChild(txInfo);

            envlp.appendChild(customData);
            splmtry.appendChild(envlp);

            // Append to main message tag inside Document
            Element root = doc.getDocumentElement();
            Node targetContainer = root;
            NodeList mainNodes = root.getChildNodes();
            for (int i = 0; i < mainNodes.getLength(); i++) {
                if (mainNodes.item(i) instanceof Element el) {
                    targetContainer = el;
                    break;
                }
            }
            targetContainer.appendChild(splmtry);
            fixesApplied.add("Injected complete mandatory NIBSS Supplementary Data envelope (<SplmtryData>).");
        }
    }

    private void injectMissingDatesAndHeadersIfNeeded(Document doc, IsoMessageDefinition def, List<String> fixesApplied) {
        String nowWat = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

        // 1. Assgnmt / OrgnlAssgnmt in acmt.023 / acmt.024
        ensureDateChild(doc, "Assgnmt", "CreDtTm", nowWat, fixesApplied);
        ensureDateChild(doc, "OrgnlAssgnmt", "CreDtTm", nowWat, fixesApplied);

        // 2. GrpHdr in pacs / pain messages
        ensureDateChild(doc, "GrpHdr", "CreDtTm", nowWat, fixesApplied);

        // 3. OrgnlGrpInfAndSts / OrgnlPmtInfAndSts
        ensureDateChild(doc, "OrgnlGrpInfAndSts", "OrgnlCreDtTm", nowWat, fixesApplied);
    }

    private void ensureDateChild(Document doc, String parentTagName, String childTagName, String defaultVal, List<String> fixesApplied) {
        NodeList parentList = doc.getElementsByTagName(parentTagName);
        if (parentList.getLength() == 0) {
            parentList = doc.getElementsByTagNameNS("*", parentTagName);
        }
        for (int i = 0; i < parentList.getLength(); i++) {
            Element parent = (Element) parentList.item(i);
            NodeList childList = parent.getElementsByTagName(childTagName);
            boolean hasChild = false;
            for (int j = 0; j < childList.getLength(); j++) {
                if (childList.item(j).getParentNode() == parent) {
                    hasChild = true;
                    Element c = (Element) childList.item(j);
                    if (c.getTextContent() == null || c.getTextContent().trim().isEmpty()) {
                        c.setTextContent(defaultVal);
                        fixesApplied.add("Populated empty <" + childTagName + "> inside <" + parentTagName + "> with WAT timestamp (" + defaultVal + ").");
                    }
                    break;
                }
            }
            if (!hasChild) {
                Element newChild = doc.createElement(childTagName);
                newChild.setTextContent(defaultVal);
                parent.appendChild(newChild);
                fixesApplied.add("Injected missing mandatory <" + childTagName + "> (" + defaultVal + ") inside <" + parentTagName + ">.");
            }
        }
    }

    private boolean isSupplementaryDataRequired(String messageKey) {
        if (messageKey == null) return false;
        String k = messageKey.toLowerCase();
        return k.contains("pacs.008") || k.contains("pacs.003") || k.contains("pacs.004")
                || k.contains("pain.009") || k.contains("pain.013") || k.contains("pain.001")
                || k.contains("camt.060") || k.contains("acmt.024");
    }

    private String normalizeToWatDateTime(String dt) {
        if (dt == null || dt.trim().isEmpty()) {
            return dt;
        }
        String clean = dt.trim();
        if (NibssValidationRules.isValidIsoDateTime(clean) && NibssValidationRules.hasWatOrUtcOffset(clean)) {
            return clean;
        }
        if (clean.endsWith("Z")) {
            return clean.substring(0, clean.length() - 1) + "+01:00";
        }
        if (clean.contains("T")) {
            int tIdx = clean.indexOf('T');
            if (!clean.contains("+") && (tIdx == -1 || clean.indexOf('-', tIdx) == -1)) {
                return clean + "+01:00";
            }
        }
        return clean;
    }

    private String normalizeToIsoDate(String d) {
        if (d == null || d.trim().isEmpty()) {
            return d;
        }
        String clean = d.trim();
        if (NibssValidationRules.isValidIsoDate(clean)) {
            return clean;
        }
        if (clean.contains("T")) {
            return clean.substring(0, clean.indexOf('T'));
        }
        return clean;
    }

    private String generateCompliantNpsId(Document doc, String seed, String tag) {
        String srcInst = extractSourceInstCode(doc);
        String dstInst = extractDestInstCode(doc);

        if (seed != null && seed.length() >= 6 && seed.substring(0, 6).matches("\\d{6}")) {
            srcInst = seed.substring(0, 6);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();

        if ("InstrId".equalsIgnoreCase(tag) || "OrgnlInstrId".equalsIgnoreCase(tag)) {
            // InstrId: Source Inst (6) + Dest Inst (6) + Timestamp yyyyMMddHHmmss (14) + 9 random digits = 35 chars
            StringBuilder rand = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                rand.append(random.nextInt(10));
            }
            return srcInst + dstInst + timestamp + rand;
        } else if ("EndToEndId".equalsIgnoreCase(tag) || "OrgnlEndToEndId".equalsIgnoreCase(tag)) {
            // EndToEndId: Source Inst (6) + 29 random digits = 35 chars
            StringBuilder rand = new StringBuilder();
            for (int i = 0; i < 29; i++) {
                rand.append(random.nextInt(10));
            }
            return srcInst + rand;
        } else {
            // MsgId / TxId / NameEnquiryMsgId / OriginalMsgId: Source Inst (6) + Timestamp (14) + 15 random digits = 35 chars
            StringBuilder rand = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                rand.append(random.nextInt(10));
            }
            return srcInst + timestamp + rand;
        }
    }

    public static String extractSourceInstCode(Document doc) {
        if (doc == null) return "999058";
        String[] candidateTags = {
                "InstgAgt", "DbtrAgt", "Assgnr", "MsgSndr"
        };
        for (String cTag : candidateTags) {
            NodeList list = doc.getElementsByTagName(cTag);
            if (list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i) instanceof Element agt) {
                        NodeList mmbList = agt.getElementsByTagName("MmbId");
                        if (mmbList.getLength() > 0) {
                            String val = mmbList.item(0).getTextContent();
                            if (val != null && val.trim().matches("\\d{6}")) {
                                return val.trim();
                            }
                        }
                    }
                }
            }
        }
        // Fallback: search all MmbId tags
        NodeList mmbList = doc.getElementsByTagName("MmbId");
        if (mmbList.getLength() > 0) {
            String val = mmbList.item(0).getTextContent();
            if (val != null && val.trim().matches("\\d{6}")) {
                return val.trim();
            }
        }
        return "999058";
    }

    public static String extractDestInstCode(Document doc) {
        if (doc == null) return "999057";
        String[] candidateTags = {
                "InstdAgt", "CdtrAgt", "Assgne", "Svcr"
        };
        for (String cTag : candidateTags) {
            NodeList list = doc.getElementsByTagName(cTag);
            if (list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i) instanceof Element agt) {
                        NodeList mmbList = agt.getElementsByTagName("MmbId");
                        if (mmbList.getLength() > 0) {
                            String val = mmbList.item(0).getTextContent();
                            if (val != null && val.trim().matches("\\d{6}")) {
                                return val.trim();
                            }
                        }
                    }
                }
            }
        }
        // Fallback: search 2nd MmbId tag if available
        NodeList mmbList = doc.getElementsByTagName("MmbId");
        if (mmbList.getLength() > 1) {
            String val = mmbList.item(1).getTextContent();
            if (val != null && val.trim().matches("\\d{6}")) {
                return val.trim();
            }
        }
        return "999057";
    }

    /**
     * Strips whitespace text nodes and leaf text margins before formatting to ensure
     * strictly clean, non-accumulating 4-space indentation with zero blank lines.
     */
    public static void cleanDomForFormatting(Node node) {
        if (node == null) return;
        NodeList children = node.getChildNodes();
        boolean hasChildElements = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hasChildElements = true;
                break;
            }
        }

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            short type = child.getNodeType();
            if (type == Node.TEXT_NODE) {
                String val = child.getNodeValue();
                if (hasChildElements) {
                    // Container element: text node is whitespace between tags; remove it
                    if (val == null || val.trim().isEmpty()) {
                        node.removeChild(child);
                    }
                } else {
                    // Leaf element: normalize text value by trimming surrounding whitespace
                    if (val != null) {
                        child.setNodeValue(val.trim());
                    }
                }
            } else if (type == Node.ELEMENT_NODE) {
                cleanDomForFormatting(child);
            }
        }
    }

    public static String prettyPrint(Document doc) throws Exception {
        cleanDomForFormatting(doc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String result = sw.toString().trim();
        // Remove any superfluous blank lines
        return result.replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    private String attemptTextFixes(String rawXml, List<String> fixesApplied) {
        String fixed = rawXml;
        if (!fixed.contains("xmlns=")) {
            fixed = fixed.replace("<Document", "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\"");
            fixesApplied.add("Inserted default ISO namespace attribute into <Document>.");
        }
        return fixed;
    }

    private void fixAcmtCrossFieldSync(Document doc, IsoMessageDefinition def, List<String> fixesApplied) {
        if (def == null || doc == null) return;
        String key = def.getKey().toLowerCase();
        if (key.contains("acmt.023")) {
            // 1. Synchronize Vrfctn.Id with Assgnmt.MsgId
            NodeList msgIdNodes = doc.getElementsByTagName("MsgId");
            if (msgIdNodes.getLength() == 0) msgIdNodes = doc.getElementsByTagNameNS("*", "MsgId");
            NodeList vrfctnNodes = doc.getElementsByTagName("Vrfctn");
            if (vrfctnNodes.getLength() == 0) vrfctnNodes = doc.getElementsByTagNameNS("*", "Vrfctn");

            if (msgIdNodes.getLength() > 0 && vrfctnNodes.getLength() > 0) {
                String msgIdVal = msgIdNodes.item(0).getTextContent();
                if (msgIdVal != null && !msgIdVal.trim().isEmpty()) {
                    msgIdVal = msgIdVal.trim();
                    Element vrfctn = (Element) vrfctnNodes.item(0);
                    NodeList idList = vrfctn.getElementsByTagName("Id");
                    if (idList.getLength() > 0) {
                        Element idElem = (Element) idList.item(0);
                        if (!msgIdVal.equals(idElem.getTextContent())) {
                            idElem.setTextContent(msgIdVal);
                            fixesApplied.add("Synchronized Verification ID <Vrfctn><Id> to match Assignment Message ID (" + msgIdVal + ").");
                        }
                    }
                }
            }

            // 2. Synchronize Assgnr BICFI with MmbId if numeric
            syncAgentBicfiWithMmbId(doc, "Assgnr", fixesApplied);
            // 3. Synchronize Assgne BICFI with MmbId if numeric
            syncAgentBicfiWithMmbId(doc, "Assgne", fixesApplied);
        }

        if (key.contains("acmt.024")) {
            // 1. Synchronize Rpt.OrgnlId with OrgnlAssgnmt.MsgId
            NodeList orgnlAssgnmtNodes = doc.getElementsByTagName("OrgnlAssgnmt");
            if (orgnlAssgnmtNodes.getLength() == 0) orgnlAssgnmtNodes = doc.getElementsByTagNameNS("*", "OrgnlAssgnmt");
            NodeList rptNodes = doc.getElementsByTagName("Rpt");
            if (rptNodes.getLength() == 0) rptNodes = doc.getElementsByTagNameNS("*", "Rpt");

            if (orgnlAssgnmtNodes.getLength() > 0 && rptNodes.getLength() > 0) {
                Element orgnlAssgnmt = (Element) orgnlAssgnmtNodes.item(0);
                NodeList msgIdList = orgnlAssgnmt.getElementsByTagName("MsgId");
                if (msgIdList.getLength() > 0) {
                    String orgnlMsgIdVal = msgIdList.item(0).getTextContent();
                    if (orgnlMsgIdVal != null && !orgnlMsgIdVal.trim().isEmpty()) {
                        orgnlMsgIdVal = orgnlMsgIdVal.trim();
                        Element rpt = (Element) rptNodes.item(0);
                        NodeList orgnlIdList = rpt.getElementsByTagName("OrgnlId");
                        if (orgnlIdList.getLength() > 0) {
                            Element orgnlIdElem = (Element) orgnlIdList.item(0);
                            if (!orgnlMsgIdVal.equals(orgnlIdElem.getTextContent())) {
                                orgnlIdElem.setTextContent(orgnlMsgIdVal);
                                fixesApplied.add("Synchronized Report Original ID <Rpt><OrgnlId> to match Original Assignment Message ID (" + orgnlMsgIdVal + ").");
                            }
                        }
                    }
                }
            }

            // 2. Synchronize Assgnr BICFI with MmbId if numeric
            syncAgentBicfiWithMmbId(doc, "Assgnr", fixesApplied);
            // 3. Synchronize Assgne BICFI with MmbId if numeric
            syncAgentBicfiWithMmbId(doc, "Assgne", fixesApplied);
        }

        if (key.contains("pacs.028")) {
            // 1. Synchronize TxInf.StsReqId with GrpHdr.MsgId
            NodeList msgIdNodes = doc.getElementsByTagName("MsgId");
            if (msgIdNodes.getLength() == 0) msgIdNodes = doc.getElementsByTagNameNS("*", "MsgId");
            NodeList stsReqIdNodes = doc.getElementsByTagName("StsReqId");
            if (stsReqIdNodes.getLength() == 0) stsReqIdNodes = doc.getElementsByTagNameNS("*", "StsReqId");

            if (msgIdNodes.getLength() > 0 && stsReqIdNodes.getLength() > 0) {
                String msgIdVal = msgIdNodes.item(0).getTextContent();
                if (msgIdVal != null && !msgIdVal.trim().isEmpty()) {
                    msgIdVal = msgIdVal.trim();
                    Element stsReqIdElem = (Element) stsReqIdNodes.item(0);
                    if (!msgIdVal.equals(stsReqIdElem.getTextContent())) {
                        stsReqIdElem.setTextContent(msgIdVal);
                        fixesApplied.add("Synchronized Status Request ID <TxInf><StsReqId> to match Message ID (" + msgIdVal + ").");
                    }
                }
            }

            // 2. Synchronize TxInf.OrgnlTxId with OrgnlGrpInf.OrgnlMsgId
            NodeList orgnlMsgIdNodes = doc.getElementsByTagName("OrgnlMsgId");
            if (orgnlMsgIdNodes.getLength() == 0) orgnlMsgIdNodes = doc.getElementsByTagNameNS("*", "OrgnlMsgId");
            NodeList orgnlTxIdNodes = doc.getElementsByTagName("OrgnlTxId");
            if (orgnlTxIdNodes.getLength() == 0) orgnlTxIdNodes = doc.getElementsByTagNameNS("*", "OrgnlTxId");

            if (orgnlMsgIdNodes.getLength() > 0 && orgnlTxIdNodes.getLength() > 0) {
                String orgnlMsgIdVal = orgnlMsgIdNodes.item(0).getTextContent();
                if (orgnlMsgIdVal != null && !orgnlMsgIdVal.trim().isEmpty()) {
                    orgnlMsgIdVal = orgnlMsgIdVal.trim();
                    Element orgnlTxIdElem = (Element) orgnlTxIdNodes.item(0);
                    if (!orgnlMsgIdVal.equals(orgnlTxIdElem.getTextContent())) {
                        orgnlTxIdElem.setTextContent(orgnlMsgIdVal);
                        fixesApplied.add("Synchronized Original Transaction ID <TxInf><OrgnlTxId> to match Original Message ID (" + orgnlMsgIdVal + ").");
                    }
                }
            }

            // 3. Synchronize InstgAgt and InstdAgt BICFI with MmbId if numeric
            syncAgentBicfiWithMmbId(doc, "InstgAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "InstdAgt", fixesApplied);
        }

        if (key.contains("pacs.002")) {
            // 1. Synchronize TxInfAndSts.OrgnlTxId with OrgnlGrpInfAndSts.OrgnlMsgId
            NodeList orgnlMsgIdNodes = doc.getElementsByTagName("OrgnlMsgId");
            if (orgnlMsgIdNodes.getLength() == 0) orgnlMsgIdNodes = doc.getElementsByTagNameNS("*", "OrgnlMsgId");
            NodeList orgnlTxIdNodes = doc.getElementsByTagName("OrgnlTxId");
            if (orgnlTxIdNodes.getLength() == 0) orgnlTxIdNodes = doc.getElementsByTagNameNS("*", "OrgnlTxId");

            if (orgnlMsgIdNodes.getLength() > 0 && orgnlTxIdNodes.getLength() > 0) {
                String orgnlMsgIdVal = orgnlMsgIdNodes.item(0).getTextContent();
                if (orgnlMsgIdVal != null && !orgnlMsgIdVal.trim().isEmpty()) {
                    orgnlMsgIdVal = orgnlMsgIdVal.trim();
                    Element orgnlTxIdElem = (Element) orgnlTxIdNodes.item(0);
                    if (!orgnlMsgIdVal.equals(orgnlTxIdElem.getTextContent())) {
                        orgnlTxIdElem.setTextContent(orgnlMsgIdVal);
                        fixesApplied.add("Synchronized Original Transaction ID <TxInfAndSts><OrgnlTxId> to match Original Message ID (" + orgnlMsgIdVal + ").");
                    }
                }
            }

            // 2. Synchronize InstgAgt and InstdAgt BICFI with MmbId
            syncAgentBicfiWithMmbId(doc, "InstgAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "InstdAgt", fixesApplied);
        }

        if (key.contains("pain.010")) {
            NodeList orgnlMndtNodes = doc.getElementsByTagName("OrgnlMndtId");
            if (orgnlMndtNodes.getLength() == 0) orgnlMndtNodes = doc.getElementsByTagNameNS("*", "OrgnlMndtId");
            NodeList mndtNodes = doc.getElementsByTagName("MndtId");
            if (mndtNodes.getLength() == 0) mndtNodes = doc.getElementsByTagNameNS("*", "MndtId");

            if (orgnlMndtNodes.getLength() > 0 && mndtNodes.getLength() > 0) {
                String orgnlMndtVal = orgnlMndtNodes.item(0).getTextContent();
                if (orgnlMndtVal != null && !orgnlMndtVal.trim().isEmpty()) {
                    orgnlMndtVal = orgnlMndtVal.trim();
                    Element mndtElem = (Element) mndtNodes.item(0);
                    if (!orgnlMndtVal.equals(mndtElem.getTextContent())) {
                        mndtElem.setTextContent(orgnlMndtVal);
                        fixesApplied.add("Synchronized Amended Mandate ID <Mndt><MndtId> to match Original Mandate ID (" + orgnlMndtVal + ").");
                    }
                }
            }
            syncAgentBicfiWithMmbId(doc, "CdtrAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "DbtrAgt", fixesApplied);
        }

        if (key.contains("pain.011")) {
            syncAgentBicfiWithMmbId(doc, "CdtrAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "DbtrAgt", fixesApplied);
        }

        if (key.contains("pain.012")) {
            syncAgentBicfiWithMmbId(doc, "CdtrAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "DbtrAgt", fixesApplied);
        }

        if (key.contains("pain.013")) {
            syncAgentBicfiWithMmbId(doc, "CdtrAgt", fixesApplied);
            syncAgentBicfiWithMmbId(doc, "DbtrAgt", fixesApplied);
        }

        // Global Agent BICFI sync for any remaining agents
        syncAgentBicfiWithMmbId(doc, "InstgAgt", fixesApplied);
        syncAgentBicfiWithMmbId(doc, "InstdAgt", fixesApplied);
        syncAgentBicfiWithMmbId(doc, "DbtrAgt", fixesApplied);
        syncAgentBicfiWithMmbId(doc, "CdtrAgt", fixesApplied);
    }

    private void syncAgentBicfiWithMmbId(Document doc, String agentRole, List<String> fixesApplied) {
        NodeList agentNodes = doc.getElementsByTagName(agentRole);
        if (agentNodes.getLength() == 0) agentNodes = doc.getElementsByTagNameNS("*", agentRole);
        for (int i = 0; i < agentNodes.getLength(); i++) {
            Element agent = (Element) agentNodes.item(i);
            NodeList mmbList = agent.getElementsByTagName("MmbId");
            if (mmbList.getLength() == 0) mmbList = agent.getElementsByTagNameNS("*", "MmbId");
            NodeList bicList = agent.getElementsByTagName("BICFI");
            if (bicList.getLength() == 0) bicList = agent.getElementsByTagNameNS("*", "BICFI");

            if (mmbList.getLength() > 0 && bicList.getLength() > 0) {
                String mmbVal = mmbList.item(0).getTextContent();
                Element bicElem = (Element) bicList.item(0);
                String bicVal = bicElem.getTextContent();
                if (mmbVal != null && !mmbVal.trim().isEmpty()) {
                    mmbVal = mmbVal.trim();
                    if (bicVal == null || bicVal.trim().isEmpty() || (bicVal.trim().matches("\\d+") && !bicVal.trim().equals(mmbVal))) {
                        bicElem.setTextContent(mmbVal);
                        fixesApplied.add("Synchronized " + agentRole + " BICFI to match Member ID (" + mmbVal + ").");
                    }
                }
            }
        }
    }
}

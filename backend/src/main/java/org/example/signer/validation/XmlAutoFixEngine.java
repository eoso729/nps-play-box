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

            // 3. Inject Missing Mandatory Supplementary Data if enabled and required
            boolean shouldFixSupp = request.isFixSupplementaryData();
            if (shouldFixSupp && def != null && isSupplementaryDataRequired(def.getKey())) {
                injectSupplementaryDataIfNeeded(doc, def, fixesApplied);
            }

            // 4. Format and Pretty Print XML (with DOM whitespace stripping to prevent extra indentation/spaces)
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
                    String fixedId = generateCompliantNpsId(text, tagName);
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
            nameEnq.setTextContent(generateCompliantNpsId("999058", "MsgId"));
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

    private String generateCompliantNpsId(String seed, String tag) {
        String instId = "999058";
        if (seed != null && seed.length() >= 6 && seed.substring(0, 6).matches("\\d{6}")) {
            instId = seed.substring(0, 6);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        StringBuilder rand = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            rand.append(random.nextInt(10));
        }
        return instId + timestamp + rand;
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
}

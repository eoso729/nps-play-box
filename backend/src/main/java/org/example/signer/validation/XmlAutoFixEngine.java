package org.example.signer.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.validation.ValidationReportDto;
import org.example.signer.dto.validation.XmlAutoFixRequestDto;
import org.example.signer.dto.validation.XmlAutoFixResponseDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
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
            // Check if XML is basic text without tags
            if (!xml.trim().startsWith("<")) {
                xml = "<Document>" + xml.trim() + "</Document>";
                fixesApplied.add("Wrapped raw payload in root <Document> tag.");
            }

            Document doc = XmlUtils.stringToDocument(xml);
            String messageType = request.getMessageType();
            if (messageType == null || messageType.trim().isEmpty()) {
                messageType = IsoMessageRegistry.detectMessageType(doc, xml);
            }
            messageType = IsoMessageRegistry.normalizeKey(messageType);
            IsoMessageDefinition def = IsoMessageRegistry.getDefinition(messageType);

            // 1. Fix Root Tag and Namespace if needed
            if (def != null) {
                Element root = doc.getDocumentElement();
                if (!"Document".equalsIgnoreCase(root.getTagName()) && !"Document".equalsIgnoreCase(root.getLocalName())) {
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
                    if (def.getNamespace() != null && root.getAttribute("xmlns").isEmpty()) {
                        root.setAttribute("xmlns", def.getNamespace());
                        fixesApplied.add("Injected official ISO namespace xmlns=\"" + def.getNamespace() + "\" on <Document>.");
                    }
                }
            }

            // 2. Recursively fix all elements
            fixElementsRecursive(doc.getDocumentElement(), def, fixesApplied);

            // 3. Inject Missing Mandatory Supplementary Data if needed
            if (def != null && isSupplementaryDataRequired(def.getKey())) {
                injectSupplementaryDataIfNeeded(doc, def, fixesApplied);
            }

            // 4. Format and Pretty Print XML
            String fixedXml = prettyPrint(doc);
            fixesApplied.add("Re-formatted and beautified XML hierarchy with standard UTF-8 indentation.");

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

    private void fixElementsRecursive(Element element, IsoMessageDefinition def, List<String> fixesApplied) {
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
            if (tagName.contains("DtTm") || "CreDtTm".equalsIgnoreCase(tagName) || "OrgnlCreDtTm".equalsIgnoreCase(tagName)) {
                String fixedDt = normalizeToWatDateTime(text);
                if (!fixedDt.equals(text)) {
                    element.setTextContent(fixedDt);
                    fixesApplied.add("Normalized " + tagName + " to ISO 8601 UTC+1 (WAT): '" + text + "' -> '" + fixedDt + "'");
                }
            }

            // C. Date normalization (YYYY-MM-DD)
            if (tagName.contains("Dt") && !tagName.contains("DtTm") && !tagName.contains("Dtls")) {
                String fixedDate = normalizeToIsoDate(text);
                if (!fixedDate.equals(text)) {
                    element.setTextContent(fixedDate);
                    fixesApplied.add("Normalized " + tagName + " date format: '" + text + "' -> '" + fixedDate + "'");
                }
            }

            // D. NPS ID Auto-Repair / Format to 35 chars
            if ("MsgId".equalsIgnoreCase(tagName) || "TxId".equalsIgnoreCase(tagName) || "EndToEndId".equalsIgnoreCase(tagName) || "InstrId".equalsIgnoreCase(tagName) || "StsReqId".equalsIgnoreCase(tagName) || "RtrId".equalsIgnoreCase(tagName)) {
                if (text.length() != 35) {
                    String repairedId = generateCompliantNpsId(text, tagName);
                    element.setTextContent(repairedId);
                    fixesApplied.add("Regenerated compliant 35-character NPS ID for <" + tagName + ">: '" + text + "' -> '" + repairedId + "'");
                }
            }

            // E. Channel Code Fix
            if ("ChannelCode".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.CHANNEL_CODES.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected ChannelCode from '" + text + "' to standard code '1' (Bank Teller).");
                }
            }

            // F. Account Designation Fix
            if ("AccountDesignation".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ACCOUNT_DESIGNATIONS.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected AccountDesignation from '" + text + "' to standard '1' (Corporate).");
                }
            }

            // G. Account Tier Fix
            if ("AccountTier".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ACCOUNT_TIERS.containsKey(text)) {
                    element.setTextContent("1");
                    fixesApplied.add("Corrected AccountTier from '" + text + "' to standard '1' (Tier 1).");
                }
            }

            // H. ID Type check
            if ("IdType".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.ID_TYPES.contains(text.toUpperCase())) {
                    element.setTextContent("BVN");
                    fixesApplied.add("Defaulted invalid IdType '" + text + "' to 'BVN'.");
                }
            }

            // I. Currency Attribute fix (e.g. Ccy="USD" -> Ccy="NGN")
            if (element.hasAttribute("Ccy")) {
                String ccy = element.getAttribute("Ccy");
                if (!"NGN".equalsIgnoreCase(ccy)) {
                    element.setAttribute("Ccy", "NGN");
                    fixesApplied.add("Set transaction currency attribute Ccy=\"NGN\" on <" + tagName + ">");
                }
            }
        }

        // Recurse children
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child) {
                fixElementsRecursive(child, def, fixesApplied);
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
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS+01:00"));
        }
        String clean = dt.trim();
        if (clean.endsWith("Z")) {
            // Replace trailing Z with +01:00
            return clean.substring(0, clean.length() - 1) + "+01:00";
        }
        if (!clean.endsWith("+01:00") && !clean.endsWith("+0100") && !clean.endsWith("+01")) {
            if (clean.contains("T")) {
                return clean + "+01:00";
            } else {
                return clean + "T12:00:00.000+01:00";
            }
        }
        return clean;
    }

    private String normalizeToIsoDate(String d) {
        if (d == null || d.trim().isEmpty()) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        String clean = d.trim();
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

    private String prettyPrint(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString().trim();
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

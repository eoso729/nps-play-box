package org.example.signer.validation;

import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.validation.ValidationIssueDto;
import org.example.signer.dto.validation.ValidationReportDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class XmlValidationEngine {

    public static class NodeLocation {
        public final int line;
        public final int column;

        public NodeLocation(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    /**
     * Inspect and validate an XML payload.
     */
    public ValidationReportDto validate(String xmlContent, String requestedMessageType) {
        List<ValidationIssueDto> issues = new ArrayList<>();
        List<String> passedRules = new ArrayList<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-EMPTY")
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("/")
                    .lineNumber(1)
                    .columnNumber(1)
                    .currentValue("")
                    .expected("Non-empty ISO 20022 XML document")
                    .message("The provided XML payload is empty.")
                    .ruleCode("XML_EMPTY")
                    .autoFixable(false)
                    .build());
            return buildReport(false, null, null, null, null, issues, passedRules, categoryCounts);
        }

        // 1. Parse XML with Line Locator
        Document doc;
        try {
            doc = parseWithLocation(xmlContent);
        } catch (SAXParseException spe) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-SYNTAX")
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("/unknown")
                    .lineNumber(spe.getLineNumber())
                    .columnNumber(spe.getColumnNumber())
                    .currentValue("Syntax Error at line " + spe.getLineNumber())
                    .expected("Well-formed XML document")
                    .message("XML Syntax Error: " + spe.getMessage())
                    .ruleCode("XML_SYNTAX_ERROR")
                    .autoFixable(true)
                    .build());
            return buildReport(false, null, null, null, null, issues, passedRules, categoryCounts);
        } catch (Exception e) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-PARSE")
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("/unknown")
                    .lineNumber(1)
                    .columnNumber(1)
                    .currentValue("Parser Failure")
                    .expected("Valid XML Document")
                    .message("Failed to parse XML: " + e.getMessage())
                    .ruleCode("XML_PARSE_ERROR")
                    .autoFixable(true)
                    .build());
            return buildReport(false, null, null, null, null, issues, passedRules, categoryCounts);
        }

        // 2. Identify message type
        String messageType = requestedMessageType;
        if (messageType == null || messageType.trim().isEmpty()) {
            messageType = IsoMessageRegistry.detectMessageType(doc, xmlContent);
        }
        messageType = IsoMessageRegistry.normalizeKey(messageType);
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition(messageType);

        if (def == null) {
            issues.add(ValidationIssueDto.builder()
                    .id("WARN-UNKNOWN-MSG")
                    .severity("WARNING")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("/" + doc.getDocumentElement().getNodeName())
                    .lineNumber(getNodeLine(doc.getDocumentElement()))
                    .columnNumber(getNodeCol(doc.getDocumentElement()))
                    .currentValue(doc.getDocumentElement().getNodeName())
                    .expected("Supported ISO 20022 message type (e.g. pacs.008, pain.013, acmt.023, etc.)")
                    .message("Could not identify specific NIBSS ISO 20022 message type definition.")
                    .ruleCode("UNKNOWN_MESSAGE_TYPE")
                    .autoFixable(false)
                    .build());
        } else {
            passedRules.add("Message Type identified: " + def.getName() + " (" + def.getIsoCode() + ")");
        }

        // 3. Document structure & Namespace validation
        Element root = doc.getDocumentElement();
        if (def != null) {
            if ("Document".equalsIgnoreCase(def.getRootElement())) {
                if (!"Document".equalsIgnoreCase(root.getLocalName() != null ? root.getLocalName() : root.getTagName())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-ROOT-TAG")
                            .severity("ERROR")
                            .category("SCHEMA_STRUCTURE")
                            .xpath("/" + root.getTagName())
                            .lineNumber(getNodeLine(root))
                            .columnNumber(getNodeCol(root))
                            .currentValue(root.getTagName())
                            .expected("<Document>")
                            .message("ISO 20022 message root element must be <Document>.")
                            .ruleCode("ISO_ROOT_ELEMENT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Root element is <Document>");
                }
            }

            // Check namespace
            String ns = root.getAttribute("xmlns");
            if (ns.isEmpty() && root.getNamespaceURI() != null) {
                ns = root.getNamespaceURI();
            }
            if (def.getNamespace() != null && !def.getNamespace().isEmpty()) {
                if (ns.isEmpty()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-NAMESPACE-MISSING")
                            .severity("WARNING")
                            .category("SCHEMA_STRUCTURE")
                            .xpath("/" + root.getTagName())
                            .lineNumber(getNodeLine(root))
                            .columnNumber(getNodeCol(root))
                            .currentValue("")
                            .expected(def.getNamespace())
                            .message("Missing XML namespace xmlns=\"" + def.getNamespace() + "\" on <" + root.getTagName() + ">")
                            .ruleCode("ISO_NAMESPACE_MISSING")
                            .autoFixable(true)
                            .build());
                } else if (!ns.contains(def.getKey())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-NAMESPACE-MISMATCH")
                            .severity("WARNING")
                            .category("SCHEMA_STRUCTURE")
                            .xpath("/" + root.getTagName())
                            .lineNumber(getNodeLine(root))
                            .columnNumber(getNodeCol(root))
                            .currentValue(ns)
                            .expected(def.getNamespace())
                            .message("Namespace might not match " + def.getKey() + " target definition.")
                            .ruleCode("ISO_NAMESPACE_MISMATCH")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid ISO 20022 XML namespace: " + ns);
                }
            }
        }

        // 4. Validate Fields & Business Rules from Message Definition
        if (def != null && def.getFields() != null) {
            for (IsoFieldDef field : def.getFields()) {
                validateField(doc, field, issues, passedRules);
            }
        }

        // 5. Global Deep Scan of XML Elements for NIBSS conventions
        scanAllElementsForRules(doc.getDocumentElement(), "", issues, passedRules);

        // 6. Check for Supplementary Data if mandatory for this message
        if (def != null && isSupplementaryDataRequired(def.getKey())) {
            validateSupplementaryDataPresence(doc, issues, passedRules);
        }

        boolean isValid = issues.stream().noneMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity()));
        return buildReport(isValid,
                def != null ? def.getKey() : messageType,
                def != null ? def.getIsoCode() : null,
                def != null ? def.getName() : null,
                def != null ? def.getCategory() : null,
                issues, passedRules, categoryCounts);
    }

    private void validateField(Document doc, IsoFieldDef field, List<ValidationIssueDto> issues, List<String> passedRules) {
        String xmlPath = field.getXmlPath();
        boolean isAttribute = xmlPath.contains("@");
        String elementPath;
        String attributeName = null;
        if (isAttribute) {
            int atIdx = xmlPath.indexOf('@');
            elementPath = (atIdx > 0 && (xmlPath.charAt(atIdx - 1) == '.' || xmlPath.charAt(atIdx - 1) == '/')) 
                    ? xmlPath.substring(0, atIdx - 1) 
                    : xmlPath.substring(0, atIdx);
            attributeName = xmlPath.substring(atIdx + 1);
        } else {
            elementPath = xmlPath;
        }

        List<Element> foundElements = findElementsByPath(doc, elementPath);

        if (foundElements.isEmpty()) {
            if (field.isMandatory()) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-MISSING-" + sanitizeId(field.getXmlPath()))
                        .severity("ERROR")
                        .category("SCHEMA_STRUCTURE")
                        .xpath(constructXPathFromFieldPath(field.getXmlPath()))
                        .fieldPath(field.getXmlPath())
                        .fieldName(field.getFieldName())
                        .lineNumber(1)
                        .columnNumber(1)
                        .currentValue("[MISSING]")
                        .expected(field.getSampleValue() != null ? field.getSampleValue() : "Mandatory element")
                        .message("Mandatory field '" + field.getFieldName() + "' (<" + extractLastTag(elementPath) + (isAttribute ? " @" + attributeName : "") + ">) is missing.")
                        .ruleCode("MANDATORY_FIELD_MISSING")
                        .autoFixable(true)
                        .build());
            }
            return;
        }

        for (Element elem : foundElements) {
            String text;
            if (isAttribute) {
                text = elem.getAttribute(attributeName);
                if (text == null || text.trim().isEmpty()) {
                    // Try case-insensitive attribute search
                    NamedNodeMap attrs = elem.getAttributes();
                    if (attrs != null) {
                        for (int i = 0; i < attrs.getLength(); i++) {
                            Node attr = attrs.item(i);
                            if (attr.getNodeName().equalsIgnoreCase(attributeName)) {
                                text = attr.getNodeValue();
                                break;
                            }
                        }
                    }
                }
                text = text != null ? text.trim() : "";
            } else {
                text = elem.getTextContent() != null ? elem.getTextContent().trim() : "";
            }

            int line = getNodeLine(elem);
            int col = getNodeCol(elem);
            String xpath = getElementXPath(elem) + (isAttribute ? "/@" + attributeName : "");

            // Empty check for mandatory field
            if (text.isEmpty() && field.isMandatory()) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-EMPTY-" + sanitizeId(field.getXmlPath()))
                        .severity("ERROR")
                        .category("SCHEMA_STRUCTURE")
                        .xpath(xpath)
                        .fieldPath(field.getXmlPath())
                        .fieldName(field.getFieldName())
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue("")
                        .expected("Non-empty value")
                        .message("Mandatory field '" + field.getFieldName() + "' cannot be empty.")
                        .ruleCode("MANDATORY_FIELD_EMPTY")
                        .autoFixable(true)
                        .build());
                continue;
            }

            if (text.isEmpty()) continue;

            // Field length check (allow 10-11 for Date with trailing Z)
            int effectiveMaxLen = field.getMaxLength();
            if ("Date".equalsIgnoreCase(field.getValueType()) && effectiveMaxLen == 10 && text.endsWith("Z")) {
                effectiveMaxLen = 11;
            }

            if (effectiveMaxLen > 0 && text.length() > effectiveMaxLen) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-LEN-" + sanitizeId(field.getXmlPath()))
                        .severity("ERROR")
                        .category("FIELD_LENGTH")
                        .xpath(xpath)
                        .fieldPath(field.getXmlPath())
                        .fieldName(field.getFieldName())
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text + " (length: " + text.length() + ")")
                        .expected("Maximum length: " + field.getMaxLength() + " characters")
                        .message("Field '" + field.getFieldName() + "' length (" + text.length() + ") exceeds NIBSS maximum allowed (" + field.getMaxLength() + ").")
                        .ruleCode("FIELD_LENGTH_EXCEEDED")
                        .autoFixable(true)
                        .build());
            }

            // Data Type checks
            if ("DateTime".equalsIgnoreCase(field.getValueType())) {
                if (!NibssValidationRules.isValidIsoDateTime(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-DT-FMT-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("ISO 8601 DateTime (e.g. 2026-01-06T13:16:44.976+01:00)")
                            .message("Invalid DateTime format in '" + field.getFieldName() + "'.")
                            .ruleCode("INVALID_DATETIME_FORMAT")
                            .autoFixable(true)
                            .build());
                } else if (!NibssValidationRules.hasWatOrUtcOffset(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-WAT-TZ-" + sanitizeId(field.getXmlPath()))
                            .severity("WARNING")
                            .category("TIMEZONE_FORMAT")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("UTC+1 (WAT) timezone offset (+01:00 or Z)")
                            .message("NIBSS requires all date-time values in UTC+1 (WAT), e.g. " + text + "+01:00.")
                            .ruleCode("UTC1_WAT_DATETIME")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid UTC+1 DateTime in " + field.getFieldName());
                }
            } else if ("Date".equalsIgnoreCase(field.getValueType())) {
                if (!NibssValidationRules.isValidIsoDate(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-DATE-FMT-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("ISO 8601 Date (YYYY-MM-DD or YYYY-MM-DDZ)")
                            .message("Invalid Date format in '" + field.getFieldName() + "'.")
                            .ruleCode("INVALID_DATE_FORMAT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid ISO Date in " + field.getFieldName());
                }
            } else if ("Decimal".equalsIgnoreCase(field.getValueType()) || "AMOUNT".equalsIgnoreCase(field.getRuleType())) {
                if (!NibssValidationRules.AMOUNT_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-AMT-FMT-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("Decimal amount with up to 2 decimal places (e.g. 1000.00)")
                            .message("Invalid monetary amount format in '" + field.getFieldName() + "'.")
                            .ruleCode("INVALID_AMOUNT_FORMAT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Amount format in " + field.getFieldName());
                }
            } else if ("Boolean".equalsIgnoreCase(field.getValueType())) {
                if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-BOOL-FMT-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("'true' or 'false'")
                            .message("Boolean field '" + field.getFieldName() + "' must be 'true' or 'false'.")
                            .ruleCode("INVALID_BOOLEAN_FORMAT")
                            .autoFixable(true)
                            .build());
                }
            }

            // Specific Rule validations
            applySpecificRules(elem, field.getRuleType(), field.getFieldName(), text, line, col, xpath, issues, passedRules);
        }
    }

    private void applySpecificRules(Element elem, String ruleType, String fieldName, String text, int line, int col, String xpath, List<ValidationIssueDto> issues, List<String> passedRules) {
        if (ruleType == null) return;

        switch (ruleType) {
            case "BVN":
                if (!NibssValidationRules.BVN_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-BVN-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " digits)")
                            .expected("Exactly 11 numeric digits")
                            .message("NIBSS BVN/NIN must be exactly 11 numeric digits.")
                            .ruleCode("NIBSS_BVN_LEN")
                            .autoFixable(false)
                            .build());
                } else {
                    passedRules.add("11-digit BVN/NIN verification passed (" + text + ")");
                }
                break;

            case "NUBAN":
                if (!NibssValidationRules.NUBAN_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-NUBAN-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " digits)")
                            .expected("Exactly 10 numeric digits (Nigerian NUBAN)")
                            .message("NIBSS Nigerian bank account (IBAN/NUBAN) must be exactly 10 digits.")
                            .ruleCode("NUBAN_10DIGIT")
                            .autoFixable(text.length() != 10)
                            .build());
                } else {
                    passedRules.add("10-digit NUBAN verification passed (" + text + ")");
                }
                break;

            case "CHANNEL_CODE":
                if (!NibssValidationRules.CHANNEL_CODES.containsKey(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-CHANNEL-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("Valid Channel Code (1-11: 1=Bank Teller, 2=Internet Banking, 3=Mobile, 4=POS, 5=ATM, etc.)")
                            .message("Invalid Channel Code '" + text + "'. Must be integer 1 through 11.")
                            .ruleCode("NIBSS_CHANNEL_CODE")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Channel Code: " + text + " (" + NibssValidationRules.CHANNEL_CODES.get(text) + ")");
                }
                break;

            case "ACCOUNT_DESIGNATION":
                if (!NibssValidationRules.ACCOUNT_DESIGNATIONS.containsKey(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-ACCT-DESIG-" + line)
                            .severity("ERROR")
                            .category("NIBSS_METADATA")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("1=Corporate, 2=Individual, 3=Joint, 4=Others, 5=Juvenile, 6=Sole Proprietorship")
                            .message("Invalid Account Designation '" + text + "'. Supported codes are 1 to 6.")
                            .ruleCode("NIBSS_ACCOUNT_DESIGNATION")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Account Designation: " + text + " (" + NibssValidationRules.ACCOUNT_DESIGNATIONS.get(text) + ")");
                }
                break;

            case "ACCOUNT_TIER":
                if (!NibssValidationRules.ACCOUNT_TIERS.containsKey(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-ACCT-TIER-" + line)
                            .severity("ERROR")
                            .category("NIBSS_METADATA")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("1 (Tier 1), 2 (Tier 2), or 3 (Tier 3)")
                            .message("Invalid Account Tier '" + text + "'. Supported values are 1, 2, or 3.")
                            .ruleCode("NIBSS_ACCOUNT_TIER")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Account Tier: " + text + " (" + NibssValidationRules.ACCOUNT_TIERS.get(text) + ")");
                }
                break;

            case "ID_TYPE":
                if (!NibssValidationRules.ID_TYPES.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-ID-TYPE-" + line)
                            .severity("ERROR")
                            .category("NIBSS_METADATA")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("BVN, NIN, RC, FIRSTIN, or JTBTIN")
                            .message("Invalid ID Type '" + text + "'. Acceptable: BVN, NIN, RC, FIRSTIN, JTBTIN.")
                            .ruleCode("NIBSS_ID_TYPE")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid ID Type: " + text);
                }
                break;

            case "NPS_ID":
                if (text.length() != 35) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-NPS-ID-LEN-" + line)
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (length: " + text.length() + ")")
                            .expected("Exactly 35 characters (Source Inst ID + Timestamp/Random)")
                            .message("NPS specification requires 35-character IDs for " + fieldName + ".")
                            .ruleCode("NPS_ID_FORMAT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("35-character NPS ID format valid (" + fieldName + ")");
                }
                break;

            case "SEQUENCE_TYPE":
                if (!NibssValidationRules.SEQUENCE_TYPES.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-SEQ-TYPE-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("RCUR (Recurring) or OOFF (One-off)")
                            .message("Invalid Sequence Type '" + text + "'. Supported: RCUR, OOFF.")
                            .ruleCode("NIBSS_SEQUENCE_TYPE")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Sequence Type: " + text);
                }
                break;

            case "FREQUENCY_TYPE":
                if (!NibssValidationRules.FREQUENCY_TYPES.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-FREQ-TYPE-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("DAIL, WEEK, MNTH, QURT, YEAR, or ADHO")
                            .message("Invalid Frequency Type '" + text + "'.")
                            .ruleCode("NIBSS_FREQUENCY_TYPE")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Frequency Type: " + text);
                }
                break;

            case "SETTLEMENT_METHOD":
                if (!NibssValidationRules.SETTLEMENT_METHODS.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-STTLM-MTD-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("CLRG, INDA, INGA, or COVE")
                            .message("Invalid Settlement Method '" + text + "'. Standard: CLRG.")
                            .ruleCode("NIBSS_SETTLEMENT_METHOD")
                            .autoFixable(true)
                            .build());
                }
                break;

            case "CLEARING_CHANNEL":
                if (!NibssValidationRules.CLEARING_CHANNELS.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-CLR-CHANL-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("RTGS, RTNS, MPNS, or BOOK")
                            .message("Invalid Clearing Channel '" + text + "'. Standard: RTNS.")
                            .ruleCode("NIBSS_CLEARING_CHANNEL")
                            .autoFixable(true)
                            .build());
                }
                break;

            case "CURRENCY":
                if (!"NGN".equalsIgnoreCase(text)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-CCY-" + line)
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("NGN")
                            .message("NIBSS NPS primarily operates in Nigerian Naira (NGN). Found '" + text + "'.")
                            .ruleCode("NIBSS_CURRENCY")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid NGN Currency");
                }
                break;

            case "REASON_CODE":
                if (NibssValidationRules.REJECT_REASON_CODES.containsKey(text.toUpperCase())) {
                    NibssValidationRules.ReasonCodeDetail r = NibssValidationRules.REJECT_REASON_CODES.get(text.toUpperCase());
                    passedRules.add("Recognized NIBSS Reason Code: " + r.getCode() + " (" + r.getName() + " - " + r.getDescription() + ")");
                }
                break;
        }
    }

    private void scanAllElementsForRules(Element element, String currentPath, List<ValidationIssueDto> issues, List<String> passedRules) {
        String tagName = element.getLocalName() != null ? element.getLocalName() : element.getTagName();
        String path = currentPath.isEmpty() ? tagName : currentPath + "." + tagName;
        int line = getNodeLine(element);
        int col = getNodeCol(element);
        String text = element.getTextContent() != null ? element.getTextContent().trim() : "";

        // Check attributes (e.g. Ccy)
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                if ("Ccy".equalsIgnoreCase(attr.getNodeName()) && !"NGN".equalsIgnoreCase(attr.getNodeValue())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-CCY-ATTR-" + line)
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(element) + "/@" + attr.getNodeName())
                            .fieldPath(path + ".@" + attr.getNodeName())
                            .fieldName("Currency Attribute")
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(attr.getNodeValue())
                            .expected("NGN")
                            .message("Currency attribute should typically be 'NGN'.")
                            .ruleCode("NIBSS_CURRENCY")
                            .autoFixable(true)
                            .build());
                }
            }
        }

        // Automatic Tag Name Heuristics for non-schema XML
        if ("MsgId".equalsIgnoreCase(tagName) || "TxId".equalsIgnoreCase(tagName) || "EndToEndId".equalsIgnoreCase(tagName) || "InstrId".equalsIgnoreCase(tagName)) {
            if (!text.isEmpty() && text.length() != 35) {
                issues.add(ValidationIssueDto.builder()
                        .id("WARN-ID-LEN-" + line)
                        .severity("WARNING")
                        .category("BUSINESS_RULE")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName(tagName)
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text + " (" + text.length() + " chars)")
                        .expected("35 characters")
                        .message(tagName + " length is " + text.length() + " chars; NIBSS standard is 35 chars.")
                        .ruleCode("NPS_ID_FORMAT")
                        .autoFixable(true)
                        .build());
            }
        }

        if ("ChannelCode".equalsIgnoreCase(tagName) && !text.isEmpty()) {
            if (!NibssValidationRules.CHANNEL_CODES.containsKey(text)) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-CHAN-SCAN-" + line)
                        .severity("ERROR")
                        .category("BUSINESS_RULE")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName("Channel Code")
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text)
                        .expected("Valid Channel Code (1-11)")
                        .message("Invalid Channel Code '" + text + "'.")
                        .ruleCode("NIBSS_CHANNEL_CODE")
                        .autoFixable(true)
                        .build());
            }
        }

        if ("AccountDesignation".equalsIgnoreCase(tagName) && !text.isEmpty()) {
            if (!NibssValidationRules.ACCOUNT_DESIGNATIONS.containsKey(text)) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-ACCT-DESIG-SCAN-" + line)
                        .severity("ERROR")
                        .category("NIBSS_METADATA")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName("Account Designation")
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text)
                        .expected("1 to 6")
                        .message("Invalid Account Designation '" + text + "'.")
                        .ruleCode("NIBSS_ACCOUNT_DESIGNATION")
                        .autoFixable(true)
                        .build());
            }
        }

        if ("AccountTier".equalsIgnoreCase(tagName) && !text.isEmpty()) {
            if (!NibssValidationRules.ACCOUNT_TIERS.containsKey(text)) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-ACCT-TIER-SCAN-" + line)
                        .severity("ERROR")
                        .category("NIBSS_METADATA")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName("Account Tier")
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text)
                        .expected("1, 2, or 3")
                        .message("Invalid Account Tier '" + text + "'.")
                        .ruleCode("NIBSS_ACCOUNT_TIER")
                        .autoFixable(true)
                        .build());
            }
        }

        // Recurse children
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                scanAllElementsForRules((Element) children.item(i), path, issues, passedRules);
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

    private void validateSupplementaryDataPresence(Document doc, List<ValidationIssueDto> issues, List<String> passedRules) {
        NodeList list = doc.getElementsByTagName("SplmtryData");
        if (list.getLength() == 0) {
            list = doc.getElementsByTagNameNS("*", "SplmtryData");
        }

        if (list.getLength() == 0) {
            issues.add(ValidationIssueDto.builder()
                    .id("WARN-SPLMTRY-MISSING")
                    .severity("WARNING")
                    .category("NIBSS_METADATA")
                    .xpath("//SplmtryData")
                    .fieldPath("SplmtryData")
                    .fieldName("Supplementary Data Block")
                    .lineNumber(1)
                    .columnNumber(1)
                    .currentValue("[MISSING]")
                    .expected("<SplmtryData><PlcAndNm>AdditionalVerificationDetails</PlcAndNm><Envlp><CustomData>...</CustomData></Envlp></SplmtryData>")
                    .message("NIBSS mandatory Supplementary Data (<SplmtryData>) block is missing.")
                    .ruleCode("NIBSS_SUPPLEMENTARY_DATA_MISSING")
                    .autoFixable(true)
                    .build());
        } else {
            passedRules.add("NIBSS Supplementary Data structure present");
        }
    }

    private ValidationReportDto buildReport(boolean isValid, String messageType, String isoCode, String name, String category,
                                            List<ValidationIssueDto> issues, List<String> passedRules, Map<String, Integer> categoryCounts) {
        // De-duplicate issues by ID and line
        Map<String, ValidationIssueDto> unique = new LinkedHashMap<>();
        for (ValidationIssueDto issue : issues) {
            String key = issue.getRuleCode() + ":" + issue.getLineNumber() + ":" + issue.getXpath();
            if (!unique.containsKey(key)) {
                unique.put(key, issue);
                categoryCounts.put(issue.getCategory(), categoryCounts.getOrDefault(issue.getCategory(), 0) + 1);
            }
        }
        List<ValidationIssueDto> dedupedIssues = new ArrayList<>(unique.values());

        int errors = (int) dedupedIssues.stream().filter(i -> "ERROR".equalsIgnoreCase(i.getSeverity())).count();
        int warnings = (int) dedupedIssues.stream().filter(i -> "WARNING".equalsIgnoreCase(i.getSeverity())).count();
        int infos = (int) dedupedIssues.stream().filter(i -> "INFO".equalsIgnoreCase(i.getSeverity())).count();
        int fixable = (int) dedupedIssues.stream().filter(ValidationIssueDto::isAutoFixable).count();

        // Calculate health score: 100 - (errors * 20) - (warnings * 5)
        int score = 100 - (errors * 20) - (warnings * 5);
        if (score < 0) score = 0;
        if (errors == 0 && warnings == 0) score = 100;

        return ValidationReportDto.builder()
                .valid(errors == 0)
                .detectedMessageType(messageType)
                .isoCode(isoCode)
                .messageName(name)
                .category(category)
                .healthScore(score)
                .summary(ValidationReportDto.Summary.builder()
                        .totalErrors(errors)
                        .totalWarnings(warnings)
                        .totalInfo(infos)
                        .totalPassed(passedRules.size())
                        .totalFixable(fixable)
                        .categoryCounts(categoryCounts)
                        .build())
                .issues(dedupedIssues)
                .passedRules(passedRules)
                .build();
    }

    /**
     * Parses XML string while embedding line and column numbers into node UserData.
     */
    public static Document parseWithLocation(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser parser = spf.newSAXParser();

        final Stack<Element> elementStack = new Stack<>();
        final StringBuilder textBuffer = new StringBuilder();
        final Locator[] locatorHolder = new Locator[1];

        DefaultHandler handler = new DefaultHandler() {
            @Override
            public void setDocumentLocator(Locator locator) {
                locatorHolder[0] = locator;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                flushText();
                Element element = doc.createElementNS(uri, qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    element.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
                }

                if (locatorHolder[0] != null) {
                    element.setUserData("location", new NodeLocation(locatorHolder[0].getLineNumber(), locatorHolder[0].getColumnNumber()), null);
                }

                if (elementStack.isEmpty()) {
                    doc.appendChild(element);
                } else {
                    elementStack.peek().appendChild(element);
                }
                elementStack.push(element);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                flushText();
                if (!elementStack.isEmpty()) {
                    elementStack.pop();
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                textBuffer.append(ch, start, length);
            }

            private void flushText() {
                if (textBuffer.length() > 0) {
                    if (!elementStack.isEmpty()) {
                        elementStack.peek().appendChild(doc.createTextNode(textBuffer.toString()));
                    }
                    textBuffer.setLength(0);
                }
            }
        };

        try (InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            parser.parse(is, handler);
        }

        return doc;
    }

    private static int getNodeLine(Node node) {
        if (node != null) {
            Object loc = node.getUserData("location");
            if (loc instanceof NodeLocation nl) {
                return nl.line;
            }
        }
        return 1;
    }

    private static int getNodeCol(Node node) {
        if (node != null) {
            Object loc = node.getUserData("location");
            if (loc instanceof NodeLocation nl) {
                return nl.column;
            }
        }
        return 1;
    }

    private static String getElementXPath(Element elem) {
        StringBuilder sb = new StringBuilder();
        Node curr = elem;
        while (curr instanceof Element e) {
            String name = e.getLocalName() != null ? e.getLocalName() : e.getTagName();
            sb.insert(0, "/" + name);
            curr = curr.getParentNode();
        }
        return sb.length() > 0 ? sb.toString() : "/" + elem.getTagName();
    }

    private static List<Element> findElementsByPath(Document doc, String xmlPath) {
        List<Element> results = new ArrayList<>();
        if (doc == null || xmlPath == null) return results;

        String[] parts = xmlPath.split("\\.");
        if (parts.length == 0) return results;

        // Search recursively for first segment, then descend
        searchRecursive(doc.getDocumentElement(), parts, 0, results);
        return results;
    }

    private static void searchRecursive(Element current, String[] parts, int index, List<Element> results) {
        if (current == null) return;
        String curName = current.getLocalName() != null ? current.getLocalName() : current.getTagName();

        if (curName.equalsIgnoreCase(parts[index])) {
            if (index == parts.length - 1) {
                results.add(current);
                return;
            } else {
                NodeList children = current.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element child) {
                        searchRecursive(child, parts, index + 1, results);
                    }
                }
                return;
            }
        }

        // If not matched at current index 0, scan children to find start of path
        if (index == 0) {
            NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element child) {
                    searchRecursive(child, parts, 0, results);
                }
            }
        }
    }

    private static String constructXPathFromFieldPath(String fieldPath) {
        if (fieldPath == null) return "/";
        return "//" + fieldPath.replace(".", "/");
    }

    private static String extractLastTag(String fieldPath) {
        if (fieldPath == null) return "";
        int idx = fieldPath.lastIndexOf('.');
        return idx >= 0 ? fieldPath.substring(idx + 1) : fieldPath;
    }

    private static String sanitizeId(String path) {
        if (path == null) return "UNKNOWN";
        return path.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
    }
}

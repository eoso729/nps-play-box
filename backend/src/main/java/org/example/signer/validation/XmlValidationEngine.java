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

            // 4. Validate Structural Containers (Main element, GrpHdr/Assgnmt, etc.)
            validateMessageStructure(doc, def, issues, passedRules);
        }

        // 5. Validate Fields & Business Rules from Message Definition
        if (def != null && def.getFields() != null) {
            for (IsoFieldDef field : def.getFields()) {
                validateField(doc, field, issues, passedRules);
            }
        }

        // 6. Global Deep Scan of XML Elements for NIBSS conventions
        scanAllElementsForRules(doc.getDocumentElement(), "", issues, passedRules);

        // 7. Check for Supplementary Data if mandatory for this message
        if (def != null && isSupplementaryDataRequired(def.getKey())) {
            validateSupplementaryDataPresence(doc, issues, passedRules);
        }

        // 8. Cross-Field Source & Destination Institution Code Consistency Validation
        validateCrossFieldInstitutionCodes(doc, def, issues, passedRules);

        boolean isValid = issues.stream().noneMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity()));
        return buildReport(isValid,
                def != null ? def.getKey() : messageType,
                def != null ? def.getIsoCode() : null,
                def != null ? def.getName() : null,
                def != null ? def.getCategory() : null,
                issues, passedRules, categoryCounts);
    }

    private void validateMessageStructure(Document doc, IsoMessageDefinition def, List<ValidationIssueDto> issues, List<String> passedRules) {
        if (doc == null || def == null) return;
        Element root = doc.getDocumentElement();

        // 1. Verify main message element inside Document
        String mainElemName = def.getMainElement();
        List<Element> mainElems = findElementsByPath(doc, mainElemName);
        if (mainElems.isEmpty() && "pain.014".equals(def.getKey())) {
            String alt = "CdtrPmtActvtnReqStsRpt".equals(mainElemName) ? "CdtrPmtActvtnStsRpt" : "CdtrPmtActvtnReqStsRpt";
            mainElems = findElementsByPath(doc, alt);
        }
        if (mainElems.isEmpty()) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-MAIN-CONTAINER-MISSING")
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("/Document/" + mainElemName)
                    .fieldPath(mainElemName)
                    .fieldName("Main Message Element")
                    .lineNumber(getNodeLine(root))
                    .columnNumber(getNodeCol(root))
                    .currentValue("[MISSING]")
                    .expected("<" + mainElemName + ">")
                    .message("Main message container <" + mainElemName + "> is missing from <Document>.")
                    .ruleCode("MAIN_CONTAINER_MISSING")
                    .autoFixable(true)
                    .build());
        } else {
            passedRules.add("Main message element <" + mainElemName + "> is present");
        }

        // 2. Verify Header Container (GrpHdr or Assgnmt)
        String headerName = (def.getKey().startsWith("acmt")) ? "Assgnmt" : "GrpHdr";
        List<Element> headerElems = findElementsByPath(doc, headerName);
        if (headerElems.isEmpty()) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-HEADER-MISSING")
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath("//" + headerName)
                    .fieldPath(headerName)
                    .fieldName(headerName + " Header Block")
                    .lineNumber(getNodeLine(root))
                    .columnNumber(getNodeCol(root))
                    .currentValue("[MISSING]")
                    .expected("<" + headerName + ">")
                    .message("Mandatory header container <" + headerName + "> is missing.")
                    .ruleCode("MANDATORY_HEADER_MISSING")
                    .autoFixable(true)
                    .build());
        } else {
            passedRules.add("Header container <" + headerName + "> is present");
        }
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

            // Empty check for present field
            if (text.isEmpty()) {
                if (field.isMandatory()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-EMPTY-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("SCHEMA_STRUCTURE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue("[EMPTY]")
                            .expected("Non-empty value")
                            .message("Mandatory field '" + field.getFieldName() + "' cannot be empty.")
                            .ruleCode("MANDATORY_FIELD_EMPTY")
                            .autoFixable(true)
                            .build());
                } else {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-EMPTY-OPTIONAL-" + sanitizeId(field.getXmlPath()))
                            .severity("ERROR")
                            .category("SCHEMA_STRUCTURE")
                            .xpath(xpath)
                            .fieldPath(field.getXmlPath())
                            .fieldName(field.getFieldName())
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue("[EMPTY]")
                            .expected("Non-empty value")
                            .message("Field '" + field.getFieldName() + "' is present in XML but contains no value. Either populate it with a valid value or remove the tag.")
                            .ruleCode("EMPTY_TAG_NOT_ALLOWED")
                            .autoFixable(true)
                            .build());
                }
                continue;
            }

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
                if (text.length() > 35) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-NPS-ID-LEN-" + line)
                            .severity("ERROR")
                            .category("FIELD_LENGTH")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (length: " + text.length() + ")")
                            .expected("Maximum 35 characters")
                            .message("NPS specification requires maximum 35-character IDs for " + fieldName + ".")
                            .ruleCode("FIELD_LENGTH_EXCEEDED")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("NPS ID length compliant (" + fieldName + ")");
                }
                break;

            case "ID_VALUE":
                String idType = findSiblingIdType(elem);
                if ("BVN".equalsIgnoreCase(idType) || "NIN".equalsIgnoreCase(idType) || idType == null || idType.isEmpty()) {
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
                                .message("NIBSS BVN/NIN ID Value must be exactly 11 numeric digits.")
                                .ruleCode("NIBSS_BVN_LEN")
                                .autoFixable(false)
                                .build());
                    } else {
                        passedRules.add("11-digit BVN/NIN verification passed (" + text + ")");
                    }
                } else {
                    if (text.length() > 35) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-ID-VAL-LEN-" + line)
                                .severity("ERROR")
                                .category("FIELD_LENGTH")
                                .xpath(xpath)
                                .fieldPath(elem.getNodeName())
                                .fieldName(fieldName)
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue(text + " (" + text.length() + " chars)")
                                .expected("Maximum 35 characters for " + idType)
                                .message(idType + " identification code exceeds maximum allowed 35 characters.")
                                .ruleCode("FIELD_LENGTH_EXCEEDED")
                                .autoFixable(true)
                                .build());
                    } else {
                        passedRules.add("Valid " + idType + " ID Value (" + text + ")");
                    }
                }
                break;

            case "SESSION_ID":
                if (!NibssValidationRules.SESSION_ID_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-SESSION-ID-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " chars)")
                            .expected("Exactly 30 numeric digits (6-digit Source Inst + 12-digit Timestamp YYMMDDHHmmss + 12-digit Seq)")
                            .message("NIP Session ID must be exactly 30 numeric digits.")
                            .ruleCode("INVALID_SESSION_ID")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid 30-digit Session ID: " + text);
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

            case "GROUP_STATUS":
                if (!NibssValidationRules.STATUS_CODES.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-GRP-STS-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("ACSC, RJCT, ACCP, AUTH, BOOK, PDNG, ACTC, or PART")
                            .message("Invalid Status Code '" + text + "'. Supported: ACSC, RJCT, ACCP, AUTH, etc.")
                            .ruleCode("NIBSS_GROUP_STATUS")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Status Code: " + text);
                }
                break;

            case "CREDIT_DEBIT":
                if (!NibssValidationRules.CREDIT_DEBIT_INDICATORS.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-CDT-DBT-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("CRDT or DBIT")
                            .message("Invalid Credit/Debit Indicator '" + text + "'. Must be CRDT or DBIT.")
                            .ruleCode("NIBSS_CREDIT_DEBIT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Credit/Debit Indicator: " + text);
                }
                break;

            case "LOCAL_INSTRUMENT":
                if (!NibssValidationRules.LOCAL_INSTRUMENTS.contains(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-LCL-INSTRM-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("CTAA, CSDC, CTAW, CTWA, or NPSDD")
                            .message("Invalid Local Instrument '" + text + "'.")
                            .ruleCode("NIBSS_LOCAL_INSTRUMENT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Local Instrument: " + text);
                }
                break;

            case "PHONE":
                if (!NibssValidationRules.PHONE_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-PHONE-" + line)
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("10 to 15 digits (e.g. +2348012345678 or 08012345678)")
                            .message("Invalid Phone Number format in '" + fieldName + "'.")
                            .ruleCode("INVALID_PHONE_FORMAT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Phone Number in " + fieldName);
                }
                break;

            case "EMAIL":
                if (!NibssValidationRules.EMAIL_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-EMAIL-" + line)
                            .severity("ERROR")
                            .category("DATA_TYPE")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected("Valid email address (e.g. user@domain.com)")
                            .message("Invalid Email Address format in '" + fieldName + "'.")
                            .ruleCode("INVALID_EMAIL_FORMAT")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid Email Address in " + fieldName);
                }
                break;

            case "MEMBER_ID":
            case "INSTITUTION_CODE":
                if (!NibssValidationRules.INSTITUTION_CODE_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-MMB-ID-" + line)
                            .severity("ERROR")
                            .category("FIELD_LENGTH")
                            .xpath(xpath)
                            .fieldPath(elem.getNodeName())
                            .fieldName(fieldName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " chars)")
                            .expected("Exactly 6 numeric digits (CBN/NIBSS Institution Code, e.g. 000014, 090110, 999058)")
                            .message("Clearing System Member ID / Institution Code must be exactly 6 numeric digits.")
                            .ruleCode("FIELD_LENGTH_MISMATCH")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Valid 6-digit Institution Code: " + text);
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

        // Skip digital signature elements
        if ((element.getNamespaceURI() != null && element.getNamespaceURI().contains("xmldsig")) || "Signature".equalsIgnoreCase(tagName)) {
            return;
        }

        // 1. Tag Name Case Validation (Check exact PascalCase / Uppercase against canonical ISO 20022 & NIBSS dictionary)
        String canonicalTag = NibssValidationRules.getCanonicalTagName(tagName);
        if (canonicalTag != null && !canonicalTag.equals(tagName)) {
            issues.add(ValidationIssueDto.builder()
                    .id("ERR-TAG-CASE-" + line + "-" + sanitizeId(tagName))
                    .severity("ERROR")
                    .category("SCHEMA_STRUCTURE")
                    .xpath(getElementXPath(element))
                    .fieldPath(path)
                    .fieldName(canonicalTag)
                    .lineNumber(line)
                    .columnNumber(col)
                    .currentValue("<" + tagName + ">")
                    .expected("<" + canonicalTag + ">")
                    .message("XML tag <" + tagName + "> has invalid casing. Official ISO 20022 / NIBSS specification requires exact casing: <" + canonicalTag + ">.")
                    .ruleCode("TAG_CASE_MISMATCH")
                    .autoFixable(true)
                    .build());
        }

        // 2. Leaf vs Container Element checks
        boolean isLeaf = true;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                isLeaf = false;
                break;
            }
        }

        if (isLeaf && text.isEmpty()) {
            Integer minLen = NibssValidationRules.getMinTagLength(tagName);
            Integer exactLen = NibssValidationRules.getExactTagLength(tagName);
            boolean isKnownSimple = (minLen != null && minLen > 0) || exactLen != null
                    || NibssValidationRules.isUppercaseField(tagName) || "BICFI".equalsIgnoreCase(tagName)
                    || "IBAN".equalsIgnoreCase(tagName) || "MmbId".equalsIgnoreCase(tagName)
                    || "MsgId".equalsIgnoreCase(tagName) || "Id".equalsIgnoreCase(tagName)
                    || "ClrSysMmbId".equalsIgnoreCase(tagName);

            if (isKnownSimple) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-EMPTY-TAG-" + line + "-" + sanitizeId(tagName))
                        .severity("ERROR")
                        .category("SCHEMA_STRUCTURE")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName(canonicalTag != null ? canonicalTag : tagName)
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue("[EMPTY]")
                        .expected(exactLen != null ? exactLen + " characters" : (minLen != null ? "At least " + minLen + " characters" : "Non-empty value"))
                        .message("XML tag <" + tagName + "> is present but contains no value. Either populate it with a valid value or remove the empty tag.")
                        .ruleCode("EMPTY_TAG_NOT_ALLOWED")
                        .autoFixable(true)
                        .build());
            }
        } else if (isLeaf && !text.isEmpty()) {
            // A. Uppercase normalization checks
            if (NibssValidationRules.isUppercaseField(tagName) || "IdType".equalsIgnoreCase(tagName)) {
                if (!text.equals(text.toUpperCase())) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-UPPERCASE-" + line + "-" + sanitizeId(tagName))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(element))
                            .fieldPath(path)
                            .fieldName(tagName)
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text)
                            .expected(text.toUpperCase())
                            .message("Value '" + text + "' in <" + tagName + "> must be strictly UPPERCASE as required by NIBSS specification: '" + text.toUpperCase() + "'.")
                            .ruleCode("VALUE_UPPERCASE_REQUIRED")
                            .autoFixable(true)
                            .build());
                }
            }

            // B. Universal Tag Maximum Character Length Validation
            Integer maxLen = NibssValidationRules.getMaxTagLength(tagName);
            if (maxLen != null && text.length() > maxLen) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-TAG-MAXLEN-" + line + "-" + sanitizeId(tagName))
                        .severity("ERROR")
                        .category("FIELD_LENGTH")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName(canonicalTag != null ? canonicalTag : tagName)
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text + " (" + text.length() + " chars)")
                        .expected("Maximum " + maxLen + " characters")
                        .message("Tag <" + tagName + "> character length (" + text.length() + ") exceeds maximum allowed limit of " + maxLen + " characters.")
                        .ruleCode("FIELD_LENGTH_EXCEEDED")
                        .autoFixable(true)
                        .build());
            }

            // C. Universal Tag Exact Character Length Validation
            Integer exactLen = NibssValidationRules.getExactTagLength(tagName);
            if (exactLen != null && text.length() != exactLen) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-TAG-EXACTLEN-" + line + "-" + sanitizeId(tagName))
                        .severity("ERROR")
                        .category("FIELD_LENGTH")
                        .xpath(getElementXPath(element))
                        .fieldPath(path)
                        .fieldName(canonicalTag != null ? canonicalTag : tagName)
                        .lineNumber(line)
                        .columnNumber(col)
                        .currentValue(text + " (" + text.length() + " chars)")
                        .expected("Exactly " + exactLen + " characters")
                        .message("Tag <" + tagName + "> character length (" + text.length() + ") does not match required exact length of " + exactLen + " characters.")
                        .ruleCode("FIELD_LENGTH_MISMATCH")
                        .autoFixable(true)
                        .build());
            }

            // D. Channel Code check
            if ("ChannelCode".equalsIgnoreCase(tagName)) {
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

            // E. Account Designation check
            if ("AccountDesignation".equalsIgnoreCase(tagName)) {
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

            // F. Account Tier check
            if ("AccountTier".equalsIgnoreCase(tagName)) {
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

            // G. Institution Code / Member ID Numeric Check
            if ("MmbId".equalsIgnoreCase(tagName) || "ClrSysMmbId".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.INSTITUTION_CODE_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-MMB-NUMERIC-" + line)
                            .severity("ERROR")
                            .category("FIELD_LENGTH")
                            .xpath(getElementXPath(element))
                            .fieldPath(path)
                            .fieldName("Institution Code")
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " chars)")
                            .expected("Exactly 6 numeric digits (e.g. 000014, 090110, 999058)")
                            .message("Institution code <" + tagName + "> must be exactly 6 numeric digits.")
                            .ruleCode("FIELD_LENGTH_MISMATCH")
                            .autoFixable(true)
                            .build());
                }
            }

            // G2. Institution BICFI Format Check (6-digit numeric institution code or 8/11-character BIC)
            if ("BICFI".equalsIgnoreCase(tagName) || "BIC".equalsIgnoreCase(tagName)) {
                boolean isNumeric = text.matches("\\d+");
                if (isNumeric) {
                    if (!NibssValidationRules.INSTITUTION_CODE_PATTERN.matcher(text).matches()) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-BICFI-NUMERIC-" + line)
                                .severity("ERROR")
                                .category("FIELD_LENGTH")
                                .xpath(getElementXPath(element))
                                .fieldPath(path)
                                .fieldName("Institution BICFI")
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue(text + " (" + text.length() + " chars)")
                                .expected("Exactly 6 numeric digits (CBN/NIBSS Institution Code)")
                                .message("Numeric BICFI <" + tagName + "> must be exactly 6 numeric digits.")
                                .ruleCode("FIELD_LENGTH_MISMATCH")
                                .autoFixable(true)
                                .build());
                    }
                } else {
                    if (text.length() < 6 || text.length() > 11) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-BICFI-LEN-" + line)
                                .severity("ERROR")
                                .category("FIELD_LENGTH")
                                .xpath(getElementXPath(element))
                                .fieldPath(path)
                                .fieldName("Institution BICFI")
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue(text + " (" + text.length() + " chars)")
                                .expected("Between 6 and 11 characters for BICFI")
                                .message("Alphanumeric BICFI <" + tagName + "> must be between 6 and 11 characters.")
                                .ruleCode("FIELD_LENGTH_MISMATCH")
                                .autoFixable(true)
                                .build());
                    }
                }
            }

            // H. Session ID Check
            if ("SessionID".equalsIgnoreCase(tagName) || "SessionId".equalsIgnoreCase(tagName)) {
                if (!NibssValidationRules.SESSION_ID_PATTERN.matcher(text).matches()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-SESSION-ID-" + line)
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(element))
                            .fieldPath(path)
                            .fieldName("Session ID")
                            .lineNumber(line)
                            .columnNumber(col)
                            .currentValue(text + " (" + text.length() + " chars)")
                            .expected("Exactly 30 numeric digits (6-digit Source Inst + 12-digit Timestamp YYMMDDHHmmss + 12-digit Seq)")
                            .message("NIP Session ID <" + tagName + "> must be exactly 30 numeric digits.")
                            .ruleCode("INVALID_SESSION_ID")
                            .autoFixable(true)
                            .build());
                }
            }
        }

        // 3. Check attributes (e.g. Ccy)
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String aName = attr.getNodeName();
                String aVal = attr.getNodeValue();
                if ("Ccy".equalsIgnoreCase(aName)) {
                    if (!"Ccy".equals(aName)) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-ATTR-CASE-" + line)
                                .severity("ERROR")
                                .category("SCHEMA_STRUCTURE")
                                .xpath(getElementXPath(element) + "/@" + aName)
                                .fieldPath(path + ".@" + aName)
                                .fieldName("Ccy Attribute")
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue("@" + aName)
                                .expected("@Ccy")
                                .message("Currency attribute name '@" + aName + "' has invalid casing. Expected '@Ccy'.")
                                .ruleCode("TAG_CASE_MISMATCH")
                                .autoFixable(true)
                                .build());
                    }
                    if (aVal != null && !aVal.equals(aVal.toUpperCase())) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-CCY-UPPERCASE-" + line)
                                .severity("ERROR")
                                .category("BUSINESS_RULE")
                                .xpath(getElementXPath(element) + "/@" + aName)
                                .fieldPath(path + ".@" + aName)
                                .fieldName("Currency Value")
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue(aVal)
                                .expected(aVal.toUpperCase())
                                .message("Currency code '" + aVal + "' must be uppercase: '" + aVal.toUpperCase() + "'.")
                                .ruleCode("VALUE_UPPERCASE_REQUIRED")
                                .autoFixable(true)
                                .build());
                    }
                    if (aVal != null && !"NGN".equalsIgnoreCase(aVal)) {
                        issues.add(ValidationIssueDto.builder()
                                .id("WARN-CCY-ATTR-" + line)
                                .severity("WARNING")
                                .category("BUSINESS_RULE")
                                .xpath(getElementXPath(element) + "/@" + aName)
                                .fieldPath(path + ".@" + aName)
                                .fieldName("Currency Attribute")
                                .lineNumber(line)
                                .columnNumber(col)
                                .currentValue(aVal)
                                .expected("NGN")
                                .message("Currency attribute should typically be 'NGN'.")
                                .ruleCode("NIBSS_CURRENCY")
                                .autoFixable(true)
                                .build());
                    }
                }
            }
        }

        // Recurse children
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child) {
                scanAllElementsForRules(child, path, issues, passedRules);
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
        // De-duplicate issues by ID, line and xpath
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

        String[] rawParts = xmlPath.split("\\.");
        if (rawParts.length == 0) return results;

        List<String> cleanedPartsList = new ArrayList<>();
        for (String p : rawParts) {
            String clean = p.replaceAll("\\[.*?\\]", "").trim();
            if (!clean.isEmpty()) {
                cleanedPartsList.add(clean);
            }
        }
        if (cleanedPartsList.isEmpty()) return results;
        String[] parts = cleanedPartsList.toArray(new String[0]);

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

    private static String findSiblingIdType(Element elem) {
        if (elem == null || elem.getParentNode() == null) return null;
        Node parent = elem.getParentNode();
        NodeList siblings = parent.getChildNodes();
        for (int i = 0; i < siblings.getLength(); i++) {
            Node s = siblings.item(i);
            if (s instanceof Element el) {
                String name = el.getLocalName() != null ? el.getLocalName() : el.getTagName();
                if ("IdType".equalsIgnoreCase(name)) {
                    return el.getTextContent() != null ? el.getTextContent().trim() : null;
                }
            }
        }
        return null;
    }

    private void validateCrossFieldInstitutionCodes(Document doc, IsoMessageDefinition def, List<ValidationIssueDto> issues, List<String> passedRules) {
        if (doc == null) return;

        // 1. Find Source Institution Code
        String sourceInstCode = extractSourceInstCodeForDef(doc, def);

        // 2. Find Destination Institution Code
        String destInstCode = extractDestInstCodeForDef(doc, def);

        // 3. Cross-validate MsgId / TxId starts with Source Institution Code
        List<Element> msgIdElems = findElementsByPath(doc, "GrpHdr.MsgId");
        if (msgIdElems.isEmpty()) msgIdElems = findElementsByPath(doc, "Assgnmt.MsgId");
        if (msgIdElems.isEmpty()) msgIdElems = findElementsByPath(doc, "MsgId");
        for (Element el : msgIdElems) {
            String p = getElementXPath(el);
            if (p.contains("Orgnl") || p.contains("Original")) continue;
            String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
            if (sourceInstCode != null && sourceInstCode.matches("\\d{6}") && val.length() >= 6) {
                String prefix = val.substring(0, 6);
                if (!prefix.equals(sourceInstCode)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-MSGID-SRC-MISMATCH-" + getNodeLine(el))
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("MsgId")
                            .fieldName("Message ID Source Code")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(prefix)
                            .expected(sourceInstCode)
                            .message("Message ID prefix (" + prefix + ") does not match document Source Institution Code (" + sourceInstCode + ").")
                            .ruleCode("SOURCE_INSTITUTION_MISMATCH")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("MsgId prefix matches Source Institution Code (" + sourceInstCode + ")");
                }
            }
        }

        // 4. Cross-validate InstrId contains Source Inst Code (chars 1-6) and Destination Inst Code (chars 7-12)
        List<Element> instrIdElems = findElementsByPath(doc, "InstrId");
        for (Element el : instrIdElems) {
            String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
            if (val.length() >= 12) {
                String srcPrefix = val.substring(0, 6);
                String dstPrefix = val.substring(6, 12);
                if (sourceInstCode != null && sourceInstCode.matches("\\d{6}") && !srcPrefix.equals(sourceInstCode)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-INSTRID-SRC-MISMATCH-" + getNodeLine(el))
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("InstrId")
                            .fieldName("Instruction ID Source Institution")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(srcPrefix)
                            .expected(sourceInstCode)
                            .message("Instruction ID source prefix (" + srcPrefix + ") does not match document Source Institution Code (" + sourceInstCode + ").")
                            .ruleCode("INSTR_ID_SOURCE_MISMATCH")
                            .autoFixable(true)
                            .build());
                }
                if (destInstCode != null && destInstCode.matches("\\d{6}") && !dstPrefix.equals(destInstCode)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-INSTRID-DST-MISMATCH-" + getNodeLine(el))
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("InstrId")
                            .fieldName("Instruction ID Destination Institution")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(dstPrefix)
                            .expected(destInstCode)
                            .message("Instruction ID destination prefix (" + dstPrefix + ") does not match document Destination Institution Code (" + destInstCode + ").")
                            .ruleCode("INSTR_ID_DEST_MISMATCH")
                            .autoFixable(true)
                            .build());
                }
            }
        }

        // 5. Cross-validate Vrfctn.Id must match Assgnmt.MsgId for acmt.023
        String key = def != null ? def.getKey().toLowerCase() : "";
        if (key.contains("acmt.023")) {
            List<Element> msgIdList = findElementsByPath(doc, "Assgnmt.MsgId");
            List<Element> vrfctnIdList = findElementsByPath(doc, "Vrfctn.Id");
            if (!msgIdList.isEmpty() && !vrfctnIdList.isEmpty()) {
                String msgIdVal = msgIdList.get(0).getTextContent() != null ? msgIdList.get(0).getTextContent().trim() : "";
                Element vrfctnIdElem = vrfctnIdList.get(0);
                String vrfctnIdVal = vrfctnIdElem.getTextContent() != null ? vrfctnIdElem.getTextContent().trim() : "";

                if (!msgIdVal.isEmpty() && !vrfctnIdVal.isEmpty()) {
                    if (!msgIdVal.equals(vrfctnIdVal)) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-VRFCTN-ID-MISMATCH-" + getNodeLine(vrfctnIdElem))
                                .severity("ERROR")
                                .category("BUSINESS_RULE")
                                .xpath(getElementXPath(vrfctnIdElem))
                                .fieldPath("IdVrfctnReq.Vrfctn.Id")
                                .fieldName("Verification ID")
                                .lineNumber(getNodeLine(vrfctnIdElem))
                                .columnNumber(getNodeCol(vrfctnIdElem))
                                .currentValue(vrfctnIdVal)
                                .expected(msgIdVal)
                                .message("Verification ID <Vrfctn><Id> ('" + vrfctnIdVal + "') must be identical to Assignment Message ID <Assgnmt><MsgId> ('" + msgIdVal + "').")
                                .ruleCode("VERIFICATION_ID_MISMATCH")
                                .autoFixable(true)
                                .build());
                    } else {
                        passedRules.add("Verification ID <Vrfctn><Id> matches Assignment Message ID <Assgnmt><MsgId> (" + msgIdVal + ")");
                    }
                }
            }
        }

        // 5b. Cross-validate Rpt.OrgnlId must match OrgnlAssgnmt.MsgId for acmt.024
        if (key.contains("acmt.024")) {
            List<Element> orgnlMsgIdList = findElementsByPath(doc, "OrgnlAssgnmt.MsgId");
            List<Element> orgnlIdList = findElementsByPath(doc, "Rpt.OrgnlId");
            if (!orgnlMsgIdList.isEmpty() && !orgnlIdList.isEmpty()) {
                String orgnlMsgIdVal = orgnlMsgIdList.get(0).getTextContent() != null ? orgnlMsgIdList.get(0).getTextContent().trim() : "";
                Element orgnlIdElem = orgnlIdList.get(0);
                String orgnlIdVal = orgnlIdElem.getTextContent() != null ? orgnlIdElem.getTextContent().trim() : "";

                if (!orgnlMsgIdVal.isEmpty() && !orgnlIdVal.isEmpty()) {
                    if (!orgnlMsgIdVal.equals(orgnlIdVal)) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-ORGNL-ID-MISMATCH-" + getNodeLine(orgnlIdElem))
                                .severity("ERROR")
                                .category("BUSINESS_RULE")
                                .xpath(getElementXPath(orgnlIdElem))
                                .fieldPath("IdVrfctnRpt.Rpt.OrgnlId")
                                .fieldName("Report Original ID")
                                .lineNumber(getNodeLine(orgnlIdElem))
                                .columnNumber(getNodeCol(orgnlIdElem))
                                .currentValue(orgnlIdVal)
                                .expected(orgnlMsgIdVal)
                                .message("Report Original ID <Rpt><OrgnlId> ('" + orgnlIdVal + "') must be identical to Original Assignment Message ID <OrgnlAssgnmt><MsgId> ('" + orgnlMsgIdVal + "').")
                                .ruleCode("ORIGINAL_ID_MISMATCH")
                                .autoFixable(true)
                                .build());
                    } else {
                        passedRules.add("Report Original ID <Rpt><OrgnlId> matches Original Assignment Message ID <OrgnlAssgnmt><MsgId> (" + orgnlMsgIdVal + ")");
                    }
                }
            }

            // Cross-validate OrgnlAssgnmt.MsgId starts with Destination/Receiving Institution Code (the original requester)
            for (Element el : orgnlMsgIdList) {
                String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
                if (destInstCode != null && destInstCode.matches("\\d{6}") && val.length() >= 6) {
                    String prefix = val.substring(0, 6);
                    if (!prefix.equals(destInstCode)) {
                        issues.add(ValidationIssueDto.builder()
                                .id("WARN-ORGNL-MSGID-DST-MISMATCH-" + getNodeLine(el))
                                .severity("WARNING")
                                .category("BUSINESS_RULE")
                                .xpath(getElementXPath(el))
                                .fieldPath("OrgnlAssgnmt.MsgId")
                                .fieldName("Original Message ID Requesting Institution")
                                .lineNumber(getNodeLine(el))
                                .columnNumber(getNodeCol(el))
                                .currentValue(prefix)
                                .expected(destInstCode)
                                .message("Original Message ID prefix (" + prefix + ") does not match Receiving Institution Code (" + destInstCode + ").")
                                .ruleCode("SOURCE_INSTITUTION_MISMATCH")
                                .autoFixable(true)
                                .build());
                    } else {
                        passedRules.add("OrgnlAssgnmt.MsgId prefix matches Receiving Institution Code (" + destInstCode + ")");
                    }
                }
            }

            // Cross-validate Vrfctn boolean and UpdtdPtyAndAcctId
            List<Element> vrfctnList = findElementsByPath(doc, "Rpt.Vrfctn");
            for (Element vrfctnElem : vrfctnList) {
                String vrfctnText = vrfctnElem.getTextContent() != null ? vrfctnElem.getTextContent().trim() : "";
                boolean isValidBool = "true".equalsIgnoreCase(vrfctnText) || "false".equalsIgnoreCase(vrfctnText);
                if (!isValidBool) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-VRFCTN-BOOL-" + getNodeLine(vrfctnElem))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(vrfctnElem))
                            .fieldPath("IdVrfctnRpt.Rpt.Vrfctn")
                            .fieldName("Verification Result")
                            .lineNumber(getNodeLine(vrfctnElem))
                            .columnNumber(getNodeCol(vrfctnElem))
                            .currentValue(vrfctnText)
                            .expected("'true' or 'false'")
                            .message("Verification result <Vrfctn> must be a valid boolean ('true' or 'false').")
                            .ruleCode("INVALID_BOOLEAN")
                            .autoFixable(true)
                            .build());
                } else if ("true".equalsIgnoreCase(vrfctnText)) {
                    // When verification is successful, updated party name must be present
                    List<Element> updtdNmList = findElementsByPath(doc, "UpdtdPtyAndAcctId.Pty.Nm");
                    if (updtdNmList.isEmpty() || updtdNmList.get(0).getTextContent() == null || updtdNmList.get(0).getTextContent().trim().isEmpty()) {
                        issues.add(ValidationIssueDto.builder()
                                .id("ERR-UPDTD-NM-MISSING")
                                .severity("ERROR")
                                .category("BUSINESS_RULE")
                                .xpath("/Document/IdVrfctnRpt/Rpt/UpdtdPtyAndAcctId/Pty/Nm")
                                .fieldPath("IdVrfctnRpt.Rpt.UpdtdPtyAndAcctId.Pty.Nm")
                                .fieldName("Updated Party Name")
                                .lineNumber(getNodeLine(vrfctnElem))
                                .columnNumber(getNodeCol(vrfctnElem))
                                .currentValue("[MISSING]")
                                .expected("Verified Account Holder Name")
                                .message("When verification result <Vrfctn> is 'true', verified account name <UpdtdPtyAndAcctId><Pty><Nm> is mandatory.")
                                .ruleCode("MANDATORY_FIELD_MISSING")
                                .autoFixable(true)
                                .build());
                    } else {
                        passedRules.add("Verified account name is present for successful verification");
                    }
                }
            }
        }

        // 5c. Cross-validate pacs.008 specific rules (NbOfTxs, BtchBookg, SttlmMtd, NameEnquiryMsgId)
        if (key.contains("pacs.008")) {
            // Check NbOfTxs == 1
            List<Element> nbOfTxsList = findElementsByPath(doc, "GrpHdr.NbOfTxs");
            for (Element el : nbOfTxsList) {
                String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
                if (!"1".equals(val)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-NBOFTXS-" + getNodeLine(el))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("GrpHdr.NbOfTxs")
                            .fieldName("Number of Transactions")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(val)
                            .expected("1")
                            .message("Single customer credit transfer (pacs.008) requires NbOfTxs to be exactly 1. Current value is '" + val + "'.")
                            .ruleCode("FIELD_VALUE_INVALID")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Number of transactions is 1 for single customer credit");
                }
            }

            // Check BtchBookg == false
            List<Element> btchBookgList = findElementsByPath(doc, "GrpHdr.BtchBookg");
            for (Element el : btchBookgList) {
                String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
                if (!"false".equalsIgnoreCase(val)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-BTCHBOOKG-" + getNodeLine(el))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("GrpHdr.BtchBookg")
                            .fieldName("Batch Booking")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(val)
                            .expected("false")
                            .message("Single customer credit transfer (pacs.008) requires BtchBookg to be false.")
                            .ruleCode("FIELD_VALUE_INVALID")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Batch booking flag is false");
                }
            }

            // Check SttlmMtd == CLRG
            List<Element> sttlmMtdList = findElementsByPath(doc, "GrpHdr.SttlmInf.SttlmMtd");
            for (Element el : sttlmMtdList) {
                String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
                if (!"CLRG".equalsIgnoreCase(val)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-STTLMMTD-" + getNodeLine(el))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("GrpHdr.SttlmInf.SttlmMtd")
                            .fieldName("Settlement Method")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue(val)
                            .expected("CLRG")
                            .message("Settlement method must be 'CLRG' (Clearing).")
                            .ruleCode("FIELD_VALUE_INVALID")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Settlement method is CLRG");
                }
            }

            // Check NameEnquiryMsgId is present and non-empty in TransactionInfo
            List<Element> nameEnquiryMsgIdList = findElementsByPath(doc, "TransactionInfo.NameEnquiryMsgId");
            for (Element el : nameEnquiryMsgIdList) {
                String val = el.getTextContent() != null ? el.getTextContent().trim() : "";
                if (val.isEmpty()) {
                    issues.add(ValidationIssueDto.builder()
                            .id("ERR-NAMEENQUIRYMSGID-EMPTY-" + getNodeLine(el))
                            .severity("ERROR")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(el))
                            .fieldPath("SplmtryData.Envlp.CustomData.TransactionInfo.NameEnquiryMsgId")
                            .fieldName("Name Enquiry Message ID")
                            .lineNumber(getNodeLine(el))
                            .columnNumber(getNodeCol(el))
                            .currentValue("[EMPTY]")
                            .expected("35-character NPS Message ID from previous acmt.023")
                            .message("Name Enquiry Message ID <NameEnquiryMsgId> is mandatory and cannot be empty.")
                            .ruleCode("EMPTY_TAG_NOT_ALLOWED")
                            .autoFixable(true)
                            .build());
                } else {
                    passedRules.add("Name Enquiry Message ID is present (" + val + ")");
                }
            }
        }

        // 6. Cross-validate Agent FinInstnId containers in acmt messages
        if (key.contains("acmt.023") || key.contains("acmt.024")) {
            validateAcmtAgentFinInstnId(doc, "Assgnr", issues, passedRules);
            validateAcmtAgentFinInstnId(doc, "Assgne", issues, passedRules);
        }
    }

    private void validateAcmtAgentFinInstnId(Document doc, String agentRole, List<ValidationIssueDto> issues, List<String> passedRules) {
        String roleName = "Assgnr".equalsIgnoreCase(agentRole) ? "Assigner" : "Assgne".equalsIgnoreCase(agentRole) ? "Assignee" : agentRole;
        List<Element> finInstnIdElems = findElementsByPath(doc, agentRole + ".Agt.FinInstnId");
        if (finInstnIdElems.isEmpty()) {
            finInstnIdElems = findElementsByPath(doc, "Assgnmt." + agentRole + ".Agt.FinInstnId");
        }
        for (Element finInstnId : finInstnIdElems) {
            Element bicfiElem = findChildElement(finInstnId, "BICFI");
            String bicfi = bicfiElem != null ? (bicfiElem.getTextContent() != null ? bicfiElem.getTextContent().trim() : "") : null;
            String mmbId = findChildText(finInstnId, "MmbId");
            if (mmbId == null || mmbId.isEmpty()) {
                Element clrSys = findChildElement(finInstnId, "ClrSysMmbId");
                if (clrSys != null) {
                    mmbId = findChildText(clrSys, "MmbId");
                }
            }

            if (bicfiElem == null && (mmbId == null || mmbId.isEmpty())) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-" + agentRole.toUpperCase() + "-INST-MISSING-" + getNodeLine(finInstnId))
                        .severity("ERROR")
                        .category("SCHEMA_STRUCTURE")
                        .xpath(getElementXPath(finInstnId))
                        .fieldPath(agentRole + ".Agt.FinInstnId")
                        .fieldName(roleName + " Agent Financial Institution Identifier")
                        .lineNumber(getNodeLine(finInstnId))
                        .columnNumber(getNodeCol(finInstnId))
                        .currentValue("[MISSING]")
                        .expected("<BICFI> and/or <ClrSysMmbId><MmbId>")
                        .message(roleName + " Agent Financial Institution Identifier (<BICFI> or <ClrSysMmbId><MmbId>) is missing.")
                        .ruleCode("MANDATORY_FIELD_MISSING")
                        .autoFixable(true)
                        .build());
            } else {
                passedRules.add(roleName + " Agent Financial Institution Identifier is present");
            }

            // Check if BICFI is present but empty
            if (bicfiElem != null && bicfi.isEmpty()) {
                issues.add(ValidationIssueDto.builder()
                        .id("ERR-" + agentRole.toUpperCase() + "-BICFI-EMPTY-" + getNodeLine(bicfiElem))
                        .severity("ERROR")
                        .category("BUSINESS_RULE")
                        .xpath(getElementXPath(bicfiElem))
                        .fieldPath(agentRole + ".Agt.FinInstnId.BICFI")
                        .fieldName(roleName + " Agent BICFI")
                        .lineNumber(getNodeLine(bicfiElem))
                        .columnNumber(getNodeCol(bicfiElem))
                        .currentValue("[EMPTY]")
                        .expected(mmbId != null && !mmbId.isEmpty() ? mmbId : "6-digit Institution Code (e.g. 991040)")
                        .message(roleName + " Agent <BICFI> is present but contains no value. Either populate with the 6-digit institution code (" + (mmbId != null && !mmbId.isEmpty() ? mmbId : "e.g. 991040") + ") or remove the empty <BICFI> tag.")
                        .ruleCode("EMPTY_TAG_NOT_ALLOWED")
                        .autoFixable(true)
                        .build());
            } else if (bicfi != null && !bicfi.isEmpty() && mmbId != null && !mmbId.isEmpty()) {
                if (bicfi.matches("\\d{6}") && mmbId.matches("\\d{6}") && !bicfi.equals(mmbId)) {
                    issues.add(ValidationIssueDto.builder()
                            .id("WARN-" + agentRole.toUpperCase() + "-BICFI-MISMATCH-" + getNodeLine(finInstnId))
                            .severity("WARNING")
                            .category("BUSINESS_RULE")
                            .xpath(getElementXPath(finInstnId) + "/BICFI")
                            .fieldPath(agentRole + ".Agt.FinInstnId.BICFI")
                            .fieldName(roleName + " Agent BICFI")
                            .lineNumber(getNodeLine(finInstnId))
                            .columnNumber(getNodeCol(finInstnId))
                            .currentValue(bicfi)
                            .expected(mmbId)
                            .message(roleName + " Agent BICFI ('" + bicfi + "') does not match Member ID ('" + mmbId + "').")
                            .ruleCode("INSTITUTION_CODE_MISMATCH")
                            .autoFixable(true)
                            .build());
                } else if (bicfi.equals(mmbId)) {
                    passedRules.add(roleName + " Agent BICFI matches Member ID (" + bicfi + ")");
                }
            }
        }
    }

    private static String findChildText(Element parent, String childTag) {
        if (parent == null || childTag == null) return null;
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Element el) {
                String name = el.getLocalName() != null ? el.getLocalName() : el.getTagName();
                if (childTag.equalsIgnoreCase(name)) {
                    String t = el.getTextContent();
                    return t != null ? t.trim() : "";
                }
            }
        }
        return null;
    }

    private static Element findChildElement(Element parent, String childTag) {
        if (parent == null || childTag == null) return null;
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Element el) {
                String name = el.getLocalName() != null ? el.getLocalName() : el.getTagName();
                if (childTag.equalsIgnoreCase(name)) {
                    return el;
                }
            }
        }
        return null;
    }

    private static String findFirstElementText(Document doc, String... paths) {
        if (doc == null || paths == null) return null;
        for (String path : paths) {
            List<Element> list = findElementsByPath(doc, path);
            if (!list.isEmpty()) {
                String text = list.get(0).getTextContent();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private static String extractSourceInstCodeForDef(Document doc, IsoMessageDefinition def) {
        if (doc == null) return null;
        String key = def != null ? def.getKey().toLowerCase() : "";
        if (key.contains("pain.008") || key.contains("pain.009") || key.contains("pain.010") || key.contains("pain.013")) {
            return findFirstElementText(doc,
                    "CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "PmtInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        if (key.contains("pain.014") || key.contains("pain.001") || key.contains("pain.002")) {
            return findFirstElementText(doc,
                    "DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        if (key.contains("acmt.023") || key.contains("acmt.024")) {
            return findFirstElementText(doc,
                    "Assgnmt.Assgnr.Agt.FinInstnId.ClrSysMmbId.MmbId",
                    "Assgnmt.Assgnr.Agt.FinInstnId.BICFI",
                    "OrgnlAssgnmt.Assgnr.Agt.FinInstnId.ClrSysMmbId.MmbId",
                    "OrgnlAssgnmt.Assgnr.Agt.FinInstnId.BICFI"
            );
        }
        if (key.contains("camt.060")) {
            return findFirstElementText(doc,
                    "AcctRptgReq.GrpHdr.MsgSndr.Agt.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        if (key.contains("camt.052") || key.contains("camt.053")) {
            return findFirstElementText(doc,
                    "BkToCstmrAcctRpt.Rpt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId",
                    "BkToCstmrStmt.Stmt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        return findFirstElementText(doc,
                "GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId",
                "PmtInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "CdtTrfTxInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "CdtTrfTxInf.InstgAgt.FinInstnId.ClrSysMmbId.MmbId",
                "Assgnmt.Assgnr.Agt.FinInstnId.ClrSysMmbId.MmbId",
                "AcctRptgReq.GrpHdr.MsgSndr.Agt.FinInstnId.ClrSysMmbId.MmbId",
                "CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "PmtRtr.GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId"
        );
    }

    private static String extractDestInstCodeForDef(Document doc, IsoMessageDefinition def) {
        if (doc == null) return null;
        String key = def != null ? def.getKey().toLowerCase() : "";
        if (key.contains("pain.008") || key.contains("pain.009") || key.contains("pain.010") || key.contains("pain.013")) {
            return findFirstElementText(doc,
                    "DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "PmtInf.DrctDbtTxInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        if (key.contains("pain.014") || key.contains("pain.001") || key.contains("pain.002")) {
            return findFirstElementText(doc,
                    "CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                    "GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        if (key.contains("acmt.023") || key.contains("acmt.024")) {
            return findFirstElementText(doc,
                    "Assgnmt.Assgne.Agt.FinInstnId.ClrSysMmbId.MmbId",
                    "Assgnmt.Assgne.Agt.FinInstnId.BICFI",
                    "OrgnlAssgnmt.Assgne.Agt.FinInstnId.ClrSysMmbId.MmbId",
                    "OrgnlAssgnmt.Assgne.Agt.FinInstnId.BICFI"
            );
        }
        if (key.contains("camt.060")) {
            return findFirstElementText(doc,
                    "AcctRptgReq.RptgReq.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId",
                    "Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId"
            );
        }
        return findFirstElementText(doc,
                "GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId",
                "PmtInf.CdtTrfTx.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "CdtTrfTxInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "CdtTrfTxInf.InstdAgt.FinInstnId.ClrSysMmbId.MmbId",
                "Assgnmt.Assgne.Agt.FinInstnId.ClrSysMmbId.MmbId",
                "BkToCstmrAcctRpt.Rpt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId",
                "BkToCstmrStmt.Stmt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId",
                "CdtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "DbtrAgt.FinInstnId.ClrSysMmbId.MmbId",
                "PmtRtr.GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId"
        );
    }

    private static String sanitizeId(String path) {
        if (path == null) return "UNKNOWN";
        return path.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
    }
}

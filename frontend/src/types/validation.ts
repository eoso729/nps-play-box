export interface ValidationIssue {
  id: string;
  severity: 'ERROR' | 'WARNING' | 'INFO';
  category: 'SCHEMA_STRUCTURE' | 'FIELD_LENGTH' | 'DATA_TYPE' | 'BUSINESS_RULE' | 'NIBSS_METADATA' | 'TIMEZONE_FORMAT';
  xpath: string;
  fieldPath?: string;
  fieldName?: string;
  lineNumber: number;
  columnNumber: number;
  currentValue: string;
  expected: string;
  message: string;
  ruleCode: string;
  autoFixable: boolean;
}

export interface ValidationSummary {
  totalErrors: number;
  totalWarnings: number;
  totalInfo: number;
  totalPassed: number;
  totalFixable: number;
  categoryCounts: Record<string, number>;
}

export interface ValidationReport {
  valid: boolean;
  detectedMessageType: string;
  isoCode: string;
  messageName: string;
  category: string;
  healthScore: number;
  summary: ValidationSummary;
  issues: ValidationIssue[];
  passedRules: string[];
}

export interface XmlInspectRequest {
  xmlContent: string;
  messageType?: string;
}

export interface XmlAutoFixRequest {
  xmlContent: string;
  messageType?: string;
  formatOnly?: boolean;
  fixIds?: boolean;
  fixDates?: boolean;
  fixSupplementaryData?: boolean;
  truncateOversized?: boolean;
}

export interface XmlAutoFixResponse {
  success: boolean;
  fixedXml: string;
  detectedMessageType: string;
  fixesApplied: string[];
  validationReport: ValidationReport;
}

export interface IsoFieldDef {
  fieldName: string;
  xmlPath: string;
  xpath: string;
  sampleValue: string;
  valueType: string;
  maxLength: number;
  mandatory: boolean;
  conditional: boolean;
  ruleType?: string;
  description?: string;
}

export interface MessageSample {
  key: string;
  name: string;
  isoCode: string;
  category: string;
  rootElement: string;
  sampleXml: string;
  fields: IsoFieldDef[];
}

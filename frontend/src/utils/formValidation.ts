import { z } from 'zod';
import { FieldDef, FieldsetDef, ValidationRuleType } from '../types/workbench';

export interface FieldValidationResult {
  valid: boolean;
  error?: string;
  warning?: string;
}

// Regex patterns for NIBSS / ISO 20022 validations
const NUBAN_REGEX = /^\d{10}$/;
const BVN_REGEX = /^\d{11}$/;
const CHANNEL_CODE_REGEX = /^(1[0-1]|[1-9])$/;
const ACCOUNT_DESIGNATION_REGEX = /^[1-6]$/;
const ACCOUNT_TIER_REGEX = /^[1-3]$/;
const CURRENCY_REGEX = /^[A-Z]{3}$/;
const AMOUNT_REGEX = /^\d+\.\d{2}$/;
const DATE_REGEX = /^\d{4}-\d{2}-\d{2}Z?$/;
const DATETIME_REGEX = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})?$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_REGEX = /^\+?\d{10,15}$/;
const NPS_ID_REGEX = /^[A-Za-z0-9\-_./]{1,35}$/;

const VALID_ID_TYPES = ['BVN', 'NIN', 'RC', 'FIRSTIN', 'JTBTIN'];
const VALID_SEQUENCE_TYPES = ['RCUR', 'OOFF', 'FRST', 'FNAL'];
const VALID_FREQUENCY_TYPES = ['DAIL', 'WEEK', 'MNTH', 'QURT', 'YEAR', 'ADHO'];
const VALID_SETTLEMENT_METHODS = ['CLRG', 'INDA', 'INGA', 'COVE'];
const VALID_CLEARING_CHANNELS = ['RTNS', 'RTGS', 'MPNS', 'BOOK'];
const VALID_LOCAL_INSTRUMENTS = ['CTAA', 'CSDC', 'CTAW', 'CTWA', 'NPSDD'];
const VALID_CHARGE_BEARERS = ['SLEV', 'DEBT', 'CRED', 'SHAR'];

/**
 * Validates a single form field based on its FieldDef rules and current value.
 */
export function validateFormField(field: FieldDef, value: any): FieldValidationResult {
  const isValueEmpty = value === undefined || value === null || value === '' || (typeof value === 'string' && value.trim() === '');

  // 1. Check Mandatory / Required
  if (field.required && isValueEmpty) {
    return {
      valid: false,
      error: `${field.label} is required`,
    };
  }

  // If empty and not required, it is valid
  if (isValueEmpty) {
    return { valid: true };
  }

  const strValue = String(value).trim();

  // 2. Check Length Constraints
  if (field.maxLength && strValue.length > field.maxLength) {
    return {
      valid: false,
      error: `Maximum length is ${field.maxLength} characters (currently ${strValue.length})`,
    };
  }

  if (field.minLength && strValue.length < field.minLength) {
    return {
      valid: false,
      error: `Minimum length is ${field.minLength} characters (currently ${strValue.length})`,
    };
  }

  // 3. Check Custom Regex Pattern
  if (field.pattern) {
    try {
      const reg = new RegExp(field.pattern);
      if (!reg.test(strValue)) {
        return {
          valid: false,
          error: `Invalid format for ${field.label}`,
        };
      }
    } catch {
      // Ignore invalid regex patterns
    }
  }

  // 4. Check Rule Type
  if (field.ruleType) {
    const result = validateRuleType(field.ruleType, strValue, field.label);
    if (!result.valid) {
      return result;
    }
  }

  return { valid: true };
}

/**
 * Validates against a specific ISO 20022 / NIBSS rule type.
 */
function validateRuleType(ruleType: ValidationRuleType, value: string, fieldLabel: string): FieldValidationResult {
  switch (ruleType) {
    case 'NUBAN':
      if (!NUBAN_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be exactly 10 numeric digits (NUBAN)`,
        };
      }
      break;

    case 'BVN':
      if (!BVN_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be exactly 11 numeric digits (BVN/NIN)`,
        };
      }
      break;

    case 'NPS_ID':
      if (!NPS_ID_REGEX.test(value) || value.length > 35) {
        return {
          valid: false,
          error: `${fieldLabel} must not exceed 35 characters`,
        };
      }
      break;

    case 'MEMBER_ID':
      if (!/^\d{6}$/.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be exactly 6 numeric digits (Clearing Member ID)`,
        };
      }
      break;

    case 'CHANNEL_CODE':
      if (!CHANNEL_CODE_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be between 1 and 11 (e.g. 1=Bank Teller, 2=Internet, 3=Mobile, 4=POS)`,
        };
      }
      break;

    case 'ACCOUNT_DESIGNATION':
      if (!ACCOUNT_DESIGNATION_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be 1 (Corporate), 2 (Individual), 3 (Joint), 4 (Others), 5 (Juvenile), or 6 (Sole Prop)`,
        };
      }
      break;

    case 'ACCOUNT_TIER':
      if (!ACCOUNT_TIER_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be 1 (Tier 1), 2 (Tier 2), or 3 (Tier 3)`,
        };
      }
      break;

    case 'ID_TYPE':
      if (!VALID_ID_TYPES.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_ID_TYPES.join(', ')}`,
        };
      }
      break;

    case 'SEQUENCE_TYPE':
      if (!VALID_SEQUENCE_TYPES.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_SEQUENCE_TYPES.join(', ')}`,
        };
      }
      break;

    case 'FREQUENCY_TYPE':
      if (!VALID_FREQUENCY_TYPES.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_FREQUENCY_TYPES.join(', ')}`,
        };
      }
      break;

    case 'SETTLEMENT_METHOD':
      if (!VALID_SETTLEMENT_METHODS.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_SETTLEMENT_METHODS.join(', ')}`,
        };
      }
      break;

    case 'CLEARING_CHANNEL':
      if (!VALID_CLEARING_CHANNELS.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_CLEARING_CHANNELS.join(', ')}`,
        };
      }
      break;

    case 'LOCAL_INSTRUMENT':
      if (!VALID_LOCAL_INSTRUMENTS.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_LOCAL_INSTRUMENTS.join(', ')}`,
        };
      }
      break;

    case 'CHARGE_BEARER':
      if (!VALID_CHARGE_BEARERS.includes(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be one of: ${VALID_CHARGE_BEARERS.join(', ')}`,
        };
      }
      break;

    case 'REASON_CODE':
      if (!/^[A-Z0-9]{3,4}$/.test(value.toUpperCase())) {
        return {
          valid: false,
          error: `${fieldLabel} must be 3–4 alphanumeric characters (e.g. AC04, AG01)`,
        };
      }
      break;

    case 'ID_VALUE':
      if (value.length > 35) {
        return {
          valid: false,
          error: `${fieldLabel} must not exceed 35 characters`,
        };
      }
      break;

    case 'CURRENCY':
      if (!CURRENCY_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be a 3-letter currency code (e.g. NGN)`,
        };
      }
      break;

    case 'AMOUNT':
      if (!AMOUNT_REGEX.test(value) || parseFloat(value) <= 0) {
        return {
          valid: false,
          error: `${fieldLabel} must be a positive decimal amount with exactly 2 decimal places (e.g. 50000.00)`,
        };
      }
      break;

    case 'DATE':
      if (!DATE_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be in YYYY-MM-DD or YYYY-MM-DDZ format`,
        };
      }
      break;

    case 'DATETIME':
      if (!DATETIME_REGEX.test(value)) {
        return {
          valid: false,
          error: `${fieldLabel} must be an ISO 8601 DateTime (e.g. YYYY-MM-DDTHH:mm:ss+01:00)`,
        };
      }
      break;

    case 'EMAIL':
      if (!EMAIL_REGEX.test(value)) {
        return {
          valid: false,
          error: `Please enter a valid email address`,
        };
      }
      break;

    case 'PHONE':
      if (!PHONE_REGEX.test(value)) {
        return {
          valid: false,
          error: `Phone number must be between 10 and 15 digits`,
        };
      }
      break;

    case 'UPPERCASE':
      if (value !== value.toUpperCase()) {
        return {
          valid: false,
          error: `${fieldLabel} must be in uppercase`,
        };
      }
      break;

    default:
      break;
  }

  return { valid: true };
}

/**
 * Validates the entire form data against all sections in a message configuration.
 * Returns a record mapping field keys to error messages.
 */
export function validateMessageForm(
  sections: FieldsetDef[],
  formData: Record<string, any>
): Record<string, string> {
  const errors: Record<string, string> = {};

  for (const section of sections) {
    for (const field of section.fields) {
      const val = formData[field.key];
      const result = validateFormField(field, val);
      if (!result.valid && result.error) {
        errors[field.key] = result.error;
      }
    }
  }

  return errors;
}

/**
 * Creates a dynamic Zod schema for a single FieldDef.
 */
export function createFieldZodSchema(field: FieldDef): z.ZodTypeAny {
  let schema: z.ZodTypeAny = z.string();

  if (field.required) {
    schema = (schema as z.ZodString).min(1, `${field.label} is required`);
  }

  if (field.maxLength) {
    schema = (schema as z.ZodString).max(
      field.maxLength,
      `Maximum length is ${field.maxLength} characters`
    );
  }

  if (field.minLength) {
    schema = (schema as z.ZodString).min(
      field.minLength,
      `Minimum length is ${field.minLength} characters`
    );
  }

  if (field.pattern) {
    try {
      const reg = new RegExp(field.pattern);
      schema = (schema as z.ZodString).regex(reg, `Invalid format for ${field.label}`);
    } catch {
      // Ignore invalid regex patterns
    }
  }

  if (field.ruleType) {
    switch (field.ruleType) {
      case 'NUBAN':
        schema = (schema as z.ZodString).regex(
          NUBAN_REGEX,
          `${field.label} must be exactly 10 numeric digits (NUBAN)`
        );
        break;
      case 'BVN':
        schema = (schema as z.ZodString).regex(
          BVN_REGEX,
          `${field.label} must be exactly 11 numeric digits (BVN/NIN)`
        );
        break;
      case 'NPS_ID':
        schema = (schema as z.ZodString)
          .regex(NPS_ID_REGEX, `${field.label} contains invalid characters`)
          .max(35, `${field.label} must not exceed 35 characters`);
        break;
      case 'MEMBER_ID':
        schema = (schema as z.ZodString).regex(
          /^\d{6}$/,
          `${field.label} must be exactly 6 numeric digits (Clearing Member ID)`
        );
        break;
      case 'CHANNEL_CODE':
        schema = (schema as z.ZodString).regex(
          CHANNEL_CODE_REGEX,
          `${field.label} must be between 1 and 11 (e.g. 1=Bank Teller, 2=Internet, 3=Mobile, 4=POS)`
        );
        break;
      case 'ACCOUNT_DESIGNATION':
        schema = (schema as z.ZodString).regex(
          ACCOUNT_DESIGNATION_REGEX,
          `${field.label} must be 1 (Corporate), 2 (Individual), 3 (Joint), 4 (Others), 5 (Juvenile), or 6 (Sole Prop)`
        );
        break;
      case 'ACCOUNT_TIER':
        schema = (schema as z.ZodString).regex(
          ACCOUNT_TIER_REGEX,
          `${field.label} must be 1 (Tier 1), 2 (Tier 2), or 3 (Tier 3)`
        );
        break;
      case 'ID_TYPE':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_ID_TYPES.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_ID_TYPES.join(', ')}` }
        );
        break;
      case 'SEQUENCE_TYPE':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_SEQUENCE_TYPES.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_SEQUENCE_TYPES.join(', ')}` }
        );
        break;
      case 'FREQUENCY_TYPE':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_FREQUENCY_TYPES.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_FREQUENCY_TYPES.join(', ')}` }
        );
        break;
      case 'SETTLEMENT_METHOD':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_SETTLEMENT_METHODS.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_SETTLEMENT_METHODS.join(', ')}` }
        );
        break;
      case 'CLEARING_CHANNEL':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_CLEARING_CHANNELS.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_CLEARING_CHANNELS.join(', ')}` }
        );
        break;
      case 'LOCAL_INSTRUMENT':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_LOCAL_INSTRUMENTS.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_LOCAL_INSTRUMENTS.join(', ')}` }
        );
        break;
      case 'CHARGE_BEARER':
        schema = (schema as z.ZodString).refine(
          val => !val || VALID_CHARGE_BEARERS.includes(val.toUpperCase()),
          { message: `${field.label} must be one of: ${VALID_CHARGE_BEARERS.join(', ')}` }
        );
        break;
      case 'REASON_CODE':
        schema = (schema as z.ZodString).regex(
          /^[A-Z0-9]{3,4}$/i,
          `${field.label} must be 3–4 alphanumeric characters (e.g. AC04, AG01)`
        );
        break;
      case 'ID_VALUE':
        schema = (schema as z.ZodString).max(35, `${field.label} must not exceed 35 characters`);
        break;
      case 'CURRENCY':
        schema = (schema as z.ZodString).regex(
          CURRENCY_REGEX,
          `${field.label} must be a 3-letter currency code (e.g. NGN)`
        );
        break;
      case 'AMOUNT':
        schema = (schema as z.ZodString).refine(
          val => !val || (AMOUNT_REGEX.test(val) && parseFloat(val) > 0),
          { message: `${field.label} must be a positive decimal amount with exactly 2 decimal places (e.g. 50000.00)` }
        );
        break;
      case 'DATE':
        schema = (schema as z.ZodString).regex(
          DATE_REGEX,
          `${field.label} must be in YYYY-MM-DD or YYYY-MM-DDZ format`
        );
        break;
      case 'DATETIME':
        schema = (schema as z.ZodString).regex(
          DATETIME_REGEX,
          `${field.label} must be an ISO 8601 DateTime (e.g. YYYY-MM-DDTHH:mm:ss+01:00)`
        );
        break;
      case 'EMAIL':
        schema = (schema as z.ZodString).regex(EMAIL_REGEX, 'Please enter a valid email address');
        break;
      case 'PHONE':
        schema = (schema as z.ZodString).regex(
          PHONE_REGEX,
          'Phone number must be between 10 and 15 digits'
        );
        break;
      case 'UPPERCASE':
        schema = (schema as z.ZodString).refine(val => !val || val === val.toUpperCase(), {
          message: `${field.label} must be in uppercase`,
        });
        break;
    }
  }

  // If optional, allow undefined or empty string
  if (!field.required) {
    schema = schema.optional().or(z.literal(''));
  }

  return schema;
}

/**
 * Creates a dynamic Zod schema across all sections of a message configuration.
 */
export function createMessageZodSchema(sections: FieldsetDef[]): z.ZodObject<Record<string, z.ZodTypeAny>> {
  const shape: Record<string, z.ZodTypeAny> = {};
  for (const section of sections) {
    for (const field of section.fields) {
      shape[field.key] = createFieldZodSchema(field);
    }
  }
  return z.object(shape);
}

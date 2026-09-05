import { describe, it, expect } from 'vitest';
import {
  validateFormField,
  createFieldZodSchema,
  createMessageZodSchema,
} from '../formValidation';
import { FieldDef, FieldsetDef } from '../../types/workbench';

describe('formValidation & dynamic Zod schemas', () => {
  describe('NUBAN rule validation', () => {
    const nubanField: FieldDef = {
      key: 'accountNumber',
      label: 'Account Number',
      type: 'text',
      required: true,
      ruleType: 'NUBAN',
    };

    it('accepts valid 10-digit NUBAN numbers', () => {
      const res = validateFormField(nubanField, '0123456789');
      expect(res.valid).toBe(true);

      const zodSchema = createFieldZodSchema(nubanField);
      expect(zodSchema.safeParse('0123456789').success).toBe(true);
    });

    it('rejects non-10-digit numbers', () => {
      const res = validateFormField(nubanField, '12345');
      expect(res.valid).toBe(false);

      const zodSchema = createFieldZodSchema(nubanField);
      expect(zodSchema.safeParse('12345').success).toBe(false);
    });
  });

  describe('BVN rule validation', () => {
    const bvnField: FieldDef = {
      key: 'bvn',
      label: 'BVN',
      type: 'text',
      required: true,
      ruleType: 'BVN',
    };

    it('accepts valid 11-digit BVN numbers', () => {
      const res = validateFormField(bvnField, '12345678901');
      expect(res.valid).toBe(true);

      const zodSchema = createFieldZodSchema(bvnField);
      expect(zodSchema.safeParse('12345678901').success).toBe(true);
    });

    it('rejects invalid BVN numbers', () => {
      const res = validateFormField(bvnField, '1234567890');
      expect(res.valid).toBe(false);

      const zodSchema = createFieldZodSchema(bvnField);
      expect(zodSchema.safeParse('1234567890').success).toBe(false);
    });
  });

  describe('AMOUNT rule validation', () => {
    const amountField: FieldDef = {
      key: 'amount',
      label: 'Transaction Amount',
      type: 'text',
      required: true,
      ruleType: 'AMOUNT',
    };

    it('accepts positive decimal strings formatted to 2 places', () => {
      const res = validateFormField(amountField, '5000.00');
      expect(res.valid).toBe(true);

      const zodSchema = createFieldZodSchema(amountField);
      expect(zodSchema.safeParse('5000.00').success).toBe(true);
    });

    it('rejects non-2-decimal amounts or negative/zero amounts', () => {
      expect(validateFormField(amountField, '5000').valid).toBe(false);
      expect(validateFormField(amountField, '0.00').valid).toBe(false);

      const zodSchema = createFieldZodSchema(amountField);
      expect(zodSchema.safeParse('5000').success).toBe(false);
      expect(zodSchema.safeParse('0.00').success).toBe(false);
    });
  });

  describe('createMessageZodSchema', () => {
    it('generates a combined schema for all sections in a message', () => {
      const testSections: FieldsetDef[] = [
        {
          title: 'Header',
          fields: [
            { key: 'msgId', label: 'Message ID', type: 'text', required: true, ruleType: 'NPS_ID' },
            { key: 'curr', label: 'Currency', type: 'text', required: false, ruleType: 'CURRENCY' },
          ],
        },
      ];

      const schema = createMessageZodSchema(testSections);

      // Valid case
      const validResult = schema.safeParse({ msgId: 'MSG-001', curr: 'NGN' });
      expect(validResult.success).toBe(true);

      // Optional field empty is valid
      const optionalEmptyResult = schema.safeParse({ msgId: 'MSG-001', curr: '' });
      expect(optionalEmptyResult.success).toBe(true);

      // Missing required field fails
      const missingRequired = schema.safeParse({ msgId: '', curr: 'NGN' });
      expect(missingRequired.success).toBe(false);
    });
  });
});

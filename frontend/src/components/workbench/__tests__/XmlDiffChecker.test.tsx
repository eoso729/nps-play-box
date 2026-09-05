import { describe, it, expect } from 'vitest';
import { computeMyersDiff } from '../XmlDiffChecker';

describe('XmlDiffChecker - Myers Diff Algorithm', () => {
  it('identifies identical payloads without flagging diffs', () => {
    const xml = `<Document>
  <MsgId>12345</MsgId>
</Document>`;

    const { left, right } = computeMyersDiff(xml, xml, false);
    expect(left.length).toBe(right.length);
    expect(left.every(l => l.type === 'unchanged')).toBe(true);
    expect(right.every(r => r.type === 'unchanged')).toBe(true);
  });

  it('correctly flags additions and deletions', () => {
    const original = `<Document>
  <MsgId>OLD_ID</MsgId>
</Document>`;
    const modified = `<Document>
  <MsgId>NEW_ID</MsgId>
  <Extra>Value</Extra>
</Document>`;

    const { left, right } = computeMyersDiff(original, modified, false);

    expect(left.some(l => l.type === 'removed' && l.value.includes('OLD_ID'))).toBe(true);
    expect(right.some(r => r.type === 'added' && r.value.includes('NEW_ID'))).toBe(true);
    expect(right.some(r => r.type === 'added' && r.value.includes('Extra'))).toBe(true);
  });

  it('ignores attribute ordering differences when canonicalize is enabled', () => {
    const original = '<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><Id>1</Id></Document>';
    const modified = '<Document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013">\n  <Id>1</Id>\n</Document>';

    const rawDiff = computeMyersDiff(original, modified, false);
    // Without canonicalization, the single-line vs multi-line attribute swap is marked as diff
    expect(rawDiff.left.some(l => l.type === 'removed')).toBe(true);

    // With canonicalization, semantic equality is recognized
    const canonicalDiff = computeMyersDiff(original, modified, true);
    expect(canonicalDiff.left.every(l => l.type === 'unchanged')).toBe(true);
    expect(canonicalDiff.right.every(r => r.type === 'unchanged')).toBe(true);
  });

  it('handles large 1000+ line documents efficiently without freezing or out-of-memory errors', () => {
    const lineCount = 1200;
    const lines1 = ['<Document>'];
    const lines2 = ['<Document>'];

    for (let i = 0; i < lineCount; i++) {
      lines1.push(`  <Tx id="${i}">Item-${i}</Tx>`);
      // Change every 50th line
      if (i % 50 === 0) {
        lines2.push(`  <Tx id="${i}">Modified-${i}</Tx>`);
      } else {
        lines2.push(`  <Tx id="${i}">Item-${i}</Tx>`);
      }
    }
    lines1.push('</Document>');
    lines2.push('</Document>');

    const start = performance.now();
    const { left, right } = computeMyersDiff(lines1.join('\n'), lines2.join('\n'), false);
    const duration = performance.now() - start;

    expect(duration).toBeLessThan(1000); // Myers diff completes in well under a second for 1.2k lines
    expect(left.length).toBeGreaterThan(0);
    expect(right.length).toBeGreaterThan(0);
  });
});

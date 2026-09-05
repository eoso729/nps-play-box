import { describe, it, expect } from 'vitest';
import { canonicalizeXml } from '../xmlCanonicalizer';

describe('xmlCanonicalizer', () => {
  it('normalizes element whitespace and indentation', () => {
    const raw = `<Document>
        <GrpHdr>
              <MsgId>MSG123</MsgId>
    </GrpHdr>
</Document>`;

    const canonical = canonicalizeXml(raw);
    expect(canonical).toBe(
`<Document>
  <GrpHdr>
    <MsgId>MSG123</MsgId>
  </GrpHdr>
</Document>`
    );
  });

  it('sorts XML attributes alphabetically', () => {
    const xml1 = '<tag z="last" b="second" a="first">value</tag>';
    const xml2 = '<tag a="first" z="last" b="second">value</tag>';

    const canonical1 = canonicalizeXml(xml1);
    const canonical2 = canonicalizeXml(xml2);

    expect(canonical1).toBe('<tag a="first" b="second" z="last">value</tag>');
    expect(canonical1).toBe(canonical2);
  });

  it('formats self-closing tags consistently', () => {
    const raw = '<tag  attr="1"   ></tag>';
    const canonical = canonicalizeXml(raw);
    expect(canonical).toBe('<tag attr="1" />');
  });

  it('preserves XML declaration when present', () => {
    const raw = '<?xml version="1.0" encoding="UTF-8"?><root><child>1</child></root>';
    const canonical = canonicalizeXml(raw);
    expect(canonical.startsWith('<?xml version="1.0" encoding="UTF-8"?>')).toBe(true);
  });

  it('gracefully handles empty or invalid inputs', () => {
    expect(canonicalizeXml('')).toBe('');
    expect(canonicalizeXml('   ')).toBe('');
    const malformed = '<root><unclosed></root>';
    expect(canonicalizeXml(malformed)).toBeDefined();
  });
});

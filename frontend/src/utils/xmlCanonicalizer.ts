/**
 * Utility to canonicalize and normalize XML documents for accurate diffing.
 * Normalizes insignificant whitespace, indents nested elements, and sorts attributes.
 */

export function canonicalizeXml(xml: string): string {
  if (!xml || typeof xml !== 'string') return '';
  const trimmed = xml.trim();
  if (!trimmed) return '';

  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(trimmed, 'application/xml');

    // Check for parser errors
    const parserError = doc.querySelector('parsererror');
    if (parserError) {
      // Fall back to clean whitespace normalization if XML cannot be parsed as a full document
      return fallbackNormalizeXml(trimmed);
    }

    const lines: string[] = [];
    // Check if original had XML declaration
    if (trimmed.startsWith('<?xml')) {
      const declMatch = trimmed.match(/^<\?xml[^>]*\?>/);
      if (declMatch) {
        lines.push(declMatch[0]);
      }
    }

    function serializeNode(node: Node, depth: number) {
      const indent = '  '.repeat(depth);

      if (node.nodeType === Node.ELEMENT_NODE) {
        const elem = node as Element;
        const tagName = elem.tagName;

        // Sort attributes alphabetically by name
        const attrs = Array.from(elem.attributes)
          .sort((a, b) => a.name.localeCompare(b.name))
          .map(a => `${a.name}="${escapeXmlAttr(a.value)}"`);

        const attrStr = attrs.length > 0 ? ' ' + attrs.join(' ') : '';

        // Inspect non-empty child nodes
        const childNodes = Array.from(elem.childNodes).filter(child => {
          if (child.nodeType === Node.TEXT_NODE) {
            return (child.textContent || '').trim().length > 0;
          }
          return (
            child.nodeType === Node.ELEMENT_NODE ||
            child.nodeType === Node.COMMENT_NODE ||
            child.nodeType === Node.CDATA_SECTION_NODE
          );
        });

        if (childNodes.length === 0) {
          lines.push(`${indent}<${tagName}${attrStr} />`);
        } else if (childNodes.length === 1 && childNodes[0].nodeType === Node.TEXT_NODE) {
          const textContent = (childNodes[0].textContent || '').trim();
          lines.push(`${indent}<${tagName}${attrStr}>${escapeXmlText(textContent)}</${tagName}>`);
        } else {
          lines.push(`${indent}<${tagName}${attrStr}>`);
          for (const child of childNodes) {
            serializeNode(child, depth + 1);
          }
          lines.push(`${indent}</${tagName}>`);
        }
      } else if (node.nodeType === Node.COMMENT_NODE) {
        lines.push(`${indent}<!--${node.textContent?.trim() || ''}-->`);
      } else if (node.nodeType === Node.CDATA_SECTION_NODE) {
        lines.push(`${indent}<![CDATA[${node.textContent || ''}]]>`);
      }
    }

    if (doc.documentElement) {
      serializeNode(doc.documentElement, 0);
    }

    return lines.join('\n');
  } catch {
    return fallbackNormalizeXml(trimmed);
  }
}

function escapeXmlAttr(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function escapeXmlText(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function fallbackNormalizeXml(raw: string): string {
  return raw
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
    .join('\n');
}

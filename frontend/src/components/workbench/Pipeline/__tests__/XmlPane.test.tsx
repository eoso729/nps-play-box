import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { XmlPane } from '../XmlPane';

// Mock @monaco-editor/react to avoid canvas/web-worker dependencies in jsdom
vi.mock('@monaco-editor/react', () => ({
  default: ({ value, language, theme, options }: any) => (
    <div data-testid="monaco-editor" data-language={language} data-theme={theme} data-readonly={String(options?.readOnly)}>
      <pre>{value}</pre>
    </div>
  ),
}));

describe('XmlPane Component', () => {
  it('renders empty state when no XML is provided', () => {
    render(
      <MemoryRouter>
        <XmlPane
          title="Generated Payload"
          stageNum={1}
          statusText="Awaiting input"
          statusVariant="idle"
          xml={null}
          isLoading={false}
        />
      </MemoryRouter>
    );

    expect(screen.getByText(/Fill the form and run a pipeline/i)).toBeInTheDocument();
  });

  it('renders loading skeletons when isLoading is true', () => {
    const { container } = render(
      <MemoryRouter>
        <XmlPane
          title="Generated Payload"
          stageNum={1}
          statusText="Generating..."
          statusVariant="gen"
          xml={null}
          isLoading={true}
        />
      </MemoryRouter>
    );

    const skeletons = container.querySelectorAll('.animate-pulse');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('renders Monaco editor in read-only mode with XML content', () => {
    const sampleXml = '<Document><MsgId>123</MsgId></Document>';
    render(
      <MemoryRouter>
        <XmlPane
          title="Plain XML"
          stageNum={1}
          statusText="Generated"
          statusVariant="gen"
          xml={sampleXml}
          isLoading={false}
        />
      </MemoryRouter>
    );

    const editor = screen.getByTestId('monaco-editor');
    expect(editor).toBeInTheDocument();
    expect(editor).toHaveAttribute('data-language', 'xml');
    expect(editor).toHaveAttribute('data-theme', 'nps-emerald-dark');
    expect(editor).toHaveAttribute('data-readonly', 'true');
    expect(screen.getByText(sampleXml)).toBeInTheDocument();
  });
});

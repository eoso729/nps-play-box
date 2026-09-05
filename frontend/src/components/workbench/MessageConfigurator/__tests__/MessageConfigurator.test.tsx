import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { WorkbenchProvider } from '../../../../context/WorkbenchContext';
import { MessageConfigurator } from '../MessageConfigurator';

describe('MessageConfigurator Component (react-hook-form + zod)', () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('renders message fields and ISO badge', () => {
    render(
      <WorkbenchProvider>
        <MessageConfigurator
          messageKey="pain.013"
          onGenerate={vi.fn()}
          onSend={vi.fn()}
          isLoading={false}
        />
      </WorkbenchProvider>
    );

    expect(screen.getByText(/Message Configurator/i)).toBeInTheDocument();
    expect(screen.getByText(/ISO:PAIN.013/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Load Pre-filled Spec Data/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Generate ISO 20022 XML/i })).toBeInTheDocument();
  });

  it('loads pre-filled spec data when clicking Load button', async () => {
    const user = userEvent.setup();
    render(
      <WorkbenchProvider>
        <MessageConfigurator
          messageKey="pain.013"
          onGenerate={vi.fn()}
          onSend={vi.fn()}
          isLoading={false}
        />
      </WorkbenchProvider>
    );

    const loadButton = screen.getByRole('button', { name: /Load Pre-filled Spec Data/i });
    await user.click(loadButton);

    const inputs = screen.getAllByDisplayValue(/Ponmile Joy/i);
    expect(inputs.length).toBeGreaterThan(0);
  });

  it('triggers onGenerate with parsed payload on valid submission', async () => {
    const onGenerate = vi.fn();
    const user = userEvent.setup();

    render(
      <WorkbenchProvider>
        <MessageConfigurator
          messageKey="pain.013"
          onGenerate={onGenerate}
          onSend={vi.fn()}
          isLoading={false}
        />
      </WorkbenchProvider>
    );

    // Load valid prefill data
    await user.click(screen.getByRole('button', { name: /Load Pre-filled Spec Data/i }));

    // Click Generate XML
    const generateBtn = screen.getByRole('button', { name: /Generate ISO 20022 XML/i });
    await user.click(generateBtn);

    await waitFor(() => {
      expect(onGenerate).toHaveBeenCalled();
      const payload = onGenerate.mock.calls[0][0];
      expect(payload).toBeDefined();
      expect(typeof payload).toBe('object');
      expect(payload.sourceId).toBe('999997');
    });
  });

  it('shows validation warning summary when required fields are missing on submit', async () => {
    const onGenerate = vi.fn();
    const user = userEvent.setup();

    render(
      <WorkbenchProvider>
        <MessageConfigurator
          messageKey="pain.013"
          onGenerate={onGenerate}
          onSend={vi.fn()}
          isLoading={false}
        />
      </WorkbenchProvider>
    );

    // Clear form to make required fields empty
    const clearBtn = screen.getByRole('button', { name: /Clear Form/i });
    await user.click(clearBtn);

    // Attempt to submit empty form
    const generateBtn = screen.getByRole('button', { name: /Generate ISO 20022 XML/i });
    fireEvent.click(generateBtn);

    await waitFor(() => {
      expect(onGenerate).not.toHaveBeenCalled();
      expect(screen.getByText(/validation error/i)).toBeInTheDocument();
    });
  });
});

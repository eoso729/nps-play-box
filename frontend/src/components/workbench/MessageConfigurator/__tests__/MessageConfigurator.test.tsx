import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { WorkbenchProvider } from '../../../../context/WorkbenchContext';
import { AuthContext } from '../../../../context/AuthContext';
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
    expect(screen.getByRole('button', { name: /Use Sample Data/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Generate XML/i })).toBeInTheDocument();
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

    const loadButton = screen.getByRole('button', { name: /Use Sample Data/i });
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
    await user.click(screen.getByRole('button', { name: /Use Sample Data/i }));

    // Click Generate XML
    const generateBtn = screen.getByRole('button', { name: /Generate XML/i });
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
    const generateBtn = screen.getByRole('button', { name: /Generate XML/i });
    fireEvent.click(generateBtn);

    await waitFor(() => {
      expect(onGenerate).not.toHaveBeenCalled();
      expect(screen.getByText(/validation error/i)).toBeInTheDocument();
    });
  });

  it('disables generate button and displays banner for VIEWER role', () => {
    const mockAuthValue: any = {
      user: {
        id: 99,
        email: 'viewer@example.com',
        fullName: 'Test Viewer',
        role: 'VIEWER',
        tenantId: 1,
        tenantCode: 'SEC',
        tenantName: 'Security Bank',
        active: true
      },
      token: 'mock-token',
      isAuthenticated: true,
      isLoading: false,
      error: null,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      clearError: vi.fn(),
    };

    render(
      <AuthContext.Provider value={mockAuthValue}>
        <WorkbenchProvider>
          <MessageConfigurator
            messageKey="pain.013"
            onGenerate={vi.fn()}
            onSend={vi.fn()}
            isLoading={false}
          />
        </WorkbenchProvider>
      </AuthContext.Provider>
    );

    expect(screen.getByText(/Viewer \(Read-Only\)/i)).toBeInTheDocument();
    const generateBtn = screen.getByRole('button', { name: /Generate XML \(Restricted\)/i });
    expect(generateBtn).toBeInTheDocument();
    expect(generateBtn).toBeDisabled();
  });
});

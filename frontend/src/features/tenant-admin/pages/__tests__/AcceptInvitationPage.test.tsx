import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AcceptInvitationPage } from '../AcceptInvitationPage';
import { invitationsApi } from '../../api/invitations.api';

describe('AcceptInvitationPage Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows error if no token is provided in the URL', async () => {
    render(
      <MemoryRouter initialEntries={['/accept-invitation']}>
        <AcceptInvitationPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Invitation Link Invalid/i)).toBeInTheDocument();
      expect(screen.getByText(/No invitation token provided/i)).toBeInTheDocument();
    });
  });

  it('verifies valid token and displays tenant onboarding form', async () => {
    vi.spyOn(invitationsApi, 'verifyInvitation').mockResolvedValueOnce({
      token: 'valid-token-123',
      email: 'admin@zenith.com',
      role: 'TENANT_ADMIN',
      tenantId: 1,
      tenantName: 'Zenith Bank',
      tenantSlug: 'zenith',
      valid: true,
      message: 'Invitation is valid',
      expiresAt: '2026-09-10T00:00:00',
    });

    render(
      <MemoryRouter initialEntries={['/accept-invitation?token=valid-token-123']}>
        <AcceptInvitationPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Zenith Bank')).toBeInTheDocument();
      expect(screen.getByText(/Primary Admin/i)).toBeInTheDocument();
      expect(screen.getByText(/admin@zenith.com/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/First Name \*/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Last Name \*/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Create Password \*/i)).toBeInTheDocument();
    });
  });

  it('submits activation and displays success confirmation', async () => {
    vi.spyOn(invitationsApi, 'verifyInvitation').mockResolvedValueOnce({
      token: 'valid-token-123',
      email: 'admin@zenith.com',
      role: 'TENANT_ADMIN',
      tenantId: 1,
      tenantName: 'Zenith Bank',
      tenantSlug: 'zenith',
      valid: true,
      message: 'Invitation is valid',
      expiresAt: '2026-09-10T00:00:00',
    });

    const acceptSpy = vi.spyOn(invitationsApi, 'acceptInvitation').mockResolvedValueOnce({
      id: 10,
      email: 'admin@zenith.com',
    });

    render(
      <MemoryRouter initialEntries={['/accept-invitation?token=valid-token-123']}>
        <AcceptInvitationPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText(/First Name \*/i)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/First Name \*/i), { target: { value: 'Emmanuel' } });
    fireEvent.change(screen.getByLabelText(/Last Name \*/i), { target: { value: 'Oso' } });
    fireEvent.change(screen.getByLabelText(/Create Password \*/i), { target: { value: 'SecurePass123!' } });
    fireEvent.change(screen.getByLabelText(/Confirm Password \*/i), { target: { value: 'SecurePass123!' } });

    fireEvent.click(screen.getByRole('button', { name: /Complete Setup & Activate Workspace/i }));

    await waitFor(() => {
      expect(acceptSpy).toHaveBeenCalledWith({
        token: 'valid-token-123',
        firstName: 'Emmanuel',
        lastName: 'Oso',
        password: 'SecurePass123!',
      });
      expect(screen.getByText(/Account Successfully Activated!/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Proceed to Sign In/i })).toBeInTheDocument();
    });
  });
});

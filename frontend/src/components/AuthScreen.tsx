import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../context/AuthContext';

// Validation Schemas using Zod
const loginSchema = z.object({
  email: z.string().min(1, 'Email or username is required'),
  password: z.string().min(1, 'Password is required'),
  keepSignedIn: z.boolean().optional(),
});

const registerSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  workEmail: z.string().email('Please enter a valid work email address'),
  organization: z.string().optional(),
  password: z.string().min(8, 'Password must be at least 8 characters long'),
  agreeTerms: z.literal(true, {
    errorMap: () => ({ message: 'You must accept the Sandbox Terms of Use' }),
  }),
});

type LoginFormValues = z.infer<typeof loginSchema>;
type RegisterFormValues = z.infer<typeof registerSchema>;

export const AuthScreen: React.FC = () => {
  const [mode, setMode] = useState<'signin' | 'register'>('signin');
  const [copiedToken, setCopiedToken] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  const { login, register, token, user, isAuthenticated, logout } = useAuth();

  // Login Form Hook
  const {
    register: registerLogin,
    handleSubmit: handleLoginSubmit,
    formState: { errors: loginErrors, isSubmitting: isLoginSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
      keepSignedIn: true,
    },
  });

  // Register Form Hook
  const {
    register: registerReg,
    handleSubmit: handleRegisterSubmit,
    formState: { errors: regErrors, isSubmitting: isRegSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      workEmail: '',
      organization: '',
      password: '',
      agreeTerms: true as any,
    },
  });

  const onLogin = async (data: LoginFormValues) => {
    setAuthError(null);
    try {
      await login({ email: data.email, password: data.password });
    } catch (err: any) {
      setAuthError(err.message || 'Authentication failed. Please check your credentials.');
    }
  };

  const onRegister = async (data: RegisterFormValues) => {
    setAuthError(null);
    try {
      const username = data.workEmail.split('@')[0];
      await register({
        firstName: data.firstName,
        lastName: data.lastName,
        username,
        email: data.workEmail,
        organization: data.organization || '',
        password: data.password,
      });
    } catch (err: any) {
      setAuthError(err.message || 'Registration failed. Please try again.');
    }
  };

  const handleMicrosoftLogin = () => {
    window.location.href = '/oauth2/authorization/microsoft';
  };

  const handleCopyToken = () => {
    if (token) {
      navigator.clipboard.writeText(token);
      setCopiedToken(true);
      setTimeout(() => setCopiedToken(false), 1200);
    }
  };

  return (
    <>
      {/* LEFT: brand panel */}
      <div className="brand-panel">
        <div className="brand-top">
          <div className="brand-logo">
            <div className="mark">NPS</div>
            <div className="name">
              NPS Play Box Engine
              <span>ISO 20022 Message Engineering Portal</span>
            </div>
          </div>
        </div>

        <div className="brand-mid">
          <h1>Build, sign, and dispatch ISO 20022 payment messages against the NIBSS sandbox.</h1>
          <p>
            Configure message fields, generate compliant XML, apply PKCS#7 signing, and inspect the live gateway response — all in one pipeline.
          </p>
          <div className="msg-chip">
            <span className="dot"></span> pain.013.001.11 · pacs.008.001.10 · camt.060.001.05
          </div>
        </div>

        <div className="brand-bottom">
          <div className="divider"></div>
          <div className="env-row">
            <div>
              <strong>NIBSS Sandbox v2.4</strong>Active target
            </div>
            <div>
              <strong>SHA-256 / RSA</strong>Signing algorithm
            </div>
            <div>
              <strong>984 / 1000</strong>Rate limit today
            </div>
          </div>
        </div>
      </div>

      {/* RIGHT: form panel */}
      <div className="form-panel">
        <div className="form-card">
          <span className="badge-sandbox">
            <span className="dot"></span>Sandbox environment
          </span>

          <div className="toggle-group">
            <div
              className={`toggle-btn ${mode === 'signin' ? 'active' : ''}`}
              id="tab-signin"
              onClick={() => { setMode('signin'); setAuthError(null); }}
            >
              Sign In
            </div>
            <div
              className={`toggle-btn ${mode === 'register' ? 'active' : ''}`}
              id="tab-register"
              onClick={() => { setMode('register'); setAuthError(null); }}
            >
              Register Account
            </div>
          </div>

          {/* Auth Error Banner */}
          {authError && (
            <div style={{ color: 'var(--danger)', fontSize: '12.5px', marginBottom: '16px', fontWeight: 500 }}>
              {authError}
            </div>
          )}

          {/* User Session Bar if Authenticated */}
          {isAuthenticated && user && (
            <div style={{ marginBottom: '20px', padding: '12px', background: 'var(--green-50)', border: '1px solid var(--border)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <strong style={{ fontSize: '13px', color: 'var(--green-900)' }}>{user.email}</strong>
                <span style={{ display: 'block', fontSize: '11px', color: 'var(--muted)' }}>Role: {user.role || 'USER'}</span>
              </div>
              <button onClick={logout} style={{ background: 'none', border: 'none', color: 'var(--danger)', fontSize: '12px', fontWeight: 600, cursor: 'pointer' }}>
                Sign Out
              </button>
            </div>
          )}

          {/* SIGN IN PANEL */}
          {mode === 'signin' && (
            <div id="panel-signin">
              <h2 className="form-title">Welcome back</h2>
              <p className="form-sub">Sign in to continue to your workspace.</p>

              <form onSubmit={handleLoginSubmit(onLogin)}>
                <div className="field">
                  <label>
                    Email or username <span className="req">*</span>
                  </label>
                  <input
                    type="text"
                    {...registerLogin('email')}
                    placeholder="you@nibss-plc.com.ng"
                  />
                  {loginErrors.email && (
                    <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                      {loginErrors.email.message}
                    </div>
                  )}
                </div>

                <div className="field">
                  <label>
                    Password <span className="req">*</span>
                  </label>
                  <input
                    type="password"
                    {...registerLogin('password')}
                    placeholder="••••••••••••"
                  />
                  {loginErrors.password && (
                    <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                      {loginErrors.password.message}
                    </div>
                  )}
                </div>

                <div className="row-between">
                  <label className="checkbox-row">
                    <input type="checkbox" {...registerLogin('keepSignedIn')} /> Keep me signed in
                  </label>
                  <a href="#" className="link" onClick={(e) => { e.preventDefault(); alert('Password reset requested.'); }}>
                    Forgot password?
                  </a>
                </div>

                <button type="submit" className="btn-primary" disabled={isLoginSubmitting}>
                  {isLoginSubmitting ? 'Signing In...' : 'Sign In'}
                </button>

                <div className="divider-row">OR ENTERPRISE SSO</div>

                {/* Microsoft OAuth Button */}
                <button
                  type="button"
                  onClick={handleMicrosoftLogin}
                  style={{
                    width: '100%',
                    background: '#fff',
                    border: '1px solid var(--border)',
                    padding: '11px 0',
                    borderRadius: '8px',
                    fontSize: '13.5px',
                    fontWeight: 600,
                    color: 'var(--text)',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '10px'
                  }}
                >
                  <svg style={{ width: '16px', height: '16px' }} viewBox="0 0 23 23">
                    <path fill="#f35325" d="M1 1h10v10H1z" />
                    <path fill="#81bc06" d="M12 1h10v10H12z" />
                    <path fill="#05a6f0" d="M1 12h10v10H1z" />
                    <path fill="#ffba08" d="M12 12h10v10H12z" />
                  </svg>
                  <span>Sign in with Microsoft</span>
                </button>

                {/* Token Panel (Shows when authenticated or after login) */}
                <div className={`token-panel ${(token || isAuthenticated) ? 'show' : ''}`} id="tokenPanel">
                  <div className="label">Session · API Token</div>
                  <div className="token-row">
                    <span id="tokenText">{token || 'eyJhbGciOiJSUzI1NiIs...NPSDEV001.9fA2'}</span>
                    <button type="button" className="copy-mini" onClick={handleCopyToken}>
                      {copiedToken ? 'Copied' : 'Copy'}
                    </button>
                  </div>
                </div>
              </form>
            </div>
          )}

          {/* REGISTER PANEL */}
          {mode === 'register' && (
            <div id="panel-register">
              <h2 className="form-title">Create an account</h2>
              <p className="form-sub">Get sandbox access and an API token for testing.</p>

              <form onSubmit={handleRegisterSubmit(onRegister)}>
                <div className="row-2">
                  <div className="field">
                    <label>
                      First name <span className="req">*</span>
                    </label>
                    <input type="text" {...registerReg('firstName')} placeholder="John" />
                    {regErrors.firstName && (
                      <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                        {regErrors.firstName.message}
                      </div>
                    )}
                  </div>
                  <div className="field">
                    <label>
                      Last name <span className="req">*</span>
                    </label>
                    <input type="text" {...registerReg('lastName')} placeholder="Developer" />
                    {regErrors.lastName && (
                      <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                        {regErrors.lastName.message}
                      </div>
                    )}
                  </div>
                </div>

                <div className="field">
                  <label>
                    Work email <span className="req">*</span>
                  </label>
                  <input type="text" {...registerReg('workEmail')} placeholder="you@company.com" />
                  {regErrors.workEmail && (
                    <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                      {regErrors.workEmail.message}
                    </div>
                  )}
                </div>

                <div className="field">
                  <label>Organization</label>
                  <input type="text" {...registerReg('organization')} placeholder="ACME Financial Services" />
                </div>

                <div className="field">
                  <label>
                    Password <span className="req">*</span>
                  </label>
                  <input type="password" {...registerReg('password')} placeholder="Minimum 8 characters" />
                  <div className="hint">Use at least one uppercase letter, one number, and one symbol.</div>
                  {regErrors.password && (
                    <div style={{ fontSize: '11px', color: 'var(--danger)', marginTop: '4px' }}>
                      {regErrors.password.message}
                    </div>
                  )}
                </div>

                <label className="checkbox-row" style={{ marginBottom: '20px' }}>
                  <input type="checkbox" {...registerReg('agreeTerms')} /> I agree to the Sandbox Terms of Use
                </label>
                {regErrors.agreeTerms && (
                  <div style={{ fontSize: '11px', color: 'var(--danger)', marginBottom: '12px' }}>
                    {regErrors.agreeTerms.message}
                  </div>
                )}

                <button type="submit" className="btn-primary" disabled={isRegSubmitting}>
                  {isRegSubmitting ? 'Creating Account...' : 'Create Account'}
                </button>
              </form>
            </div>
          )}

          <div className="foot-note">
            Need gateway credentials instead? <a href="#">Contact NIBSS integration support</a>
          </div>
        </div>
      </div>
    </>
  );
};

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
    <div className="w-full min-h-screen flex flex-col md:flex-row bg-bg text-text">
      {/* LEFT: brand panel (44% width desktop) */}
      <div className="hidden md:flex md:w-[44%] bg-brand-radial text-[#eafcf1] flex-col justify-between p-[48px_56px] relative overflow-hidden bg-grid-pattern min-h-screen shrink-0">
        {/* Top Section */}
        <div className="relative z-10">
          <div className="flex items-center gap-[12px]">
            <div className="w-[42px] h-[42px] rounded-[10px] bg-mark-gradient flex items-center justify-center font-bold tracking-[0.5px] text-white text-[15px] shadow-[0_6px_20px_rgba(21,128,61,0.45)]">
              NPS
            </div>
            <div className="text-[18px] font-bold tracking-[0.2px]">
              NPS Play Box Engine
              <span className="block text-[12px] font-medium text-[#9fd8b3] mt-[2px]">
                ISO 20022 Message Engineering Portal
              </span>
            </div>
          </div>
        </div>

        {/* Mid Section */}
        <div className="relative z-10 mt-[64px]">
          <h1 className="text-[32px] leading-[1.25] font-bold mb-[16px] max-w-[420px] text-white">
            Build, sign, and dispatch ISO 20022 payment messages against the NIBSS sandbox.
          </h1>
          <p className="text-[#b7e3c4] text-[14.5px] leading-[1.6] max-w-[400px] font-normal">
            Configure message fields, generate compliant XML, apply PKCS#7 signing, and inspect the live gateway response — all in one pipeline.
          </p>
          <div className="inline-flex items-center gap-[8px] mt-[28px] font-mono text-[12px] bg-white/[0.06] border border-white/[0.12] px-[12px] py-[8px] rounded-[8px] text-[#a7e8bd]">
            <span className="w-[6px] h-[6px] rounded-full bg-nps-500 shadow-[0_0_0_3px_rgba(34,160,90,0.25)]"></span>
            pain.013.001.11 · pacs.008.001.10 · camt.060.001.05
          </div>
        </div>

        {/* Bottom Section */}
        <div className="relative z-10 text-[12px] text-[#77a98b]">
          <div className="h-[1px] bg-white/10 mb-[16px]"></div>
          <div className="flex gap-[22px] flex-wrap">
            <div>
              <strong className="block text-[#dff3e6] text-[13px] font-semibold">NIBSS Sandbox v2.4</strong>
              Active target
            </div>
            <div>
              <strong className="block text-[#dff3e6] text-[13px] font-semibold">SHA-256 / RSA</strong>
              Signing algorithm
            </div>
            <div>
              <strong className="block text-[#dff3e6] text-[13px] font-semibold">984 / 1000</strong>
              Rate limit today
            </div>
          </div>
        </div>
      </div>

      {/* RIGHT: form panel */}
      <div className="flex-1 flex items-center justify-center p-6 md:p-8 bg-bg min-h-screen">
        <div className="w-full max-w-[400px]">
          <span className="inline-flex items-center gap-[6px] text-[11px] font-semibold text-nps-700 bg-nps-100 border border-[#c9ecd6] px-[10px] py-[5px] rounded-full mb-[18px]">
            <span className="w-[6px] h-[6px] rounded-full bg-nps-600"></span>
            Sandbox environment
          </span>

          {/* Mode Switcher */}
          <div className="flex bg-nps-50 border border-border rounded-[10px] p-[4px] mb-[32px]">
            <div
              className={`flex-1 text-center py-[10px] text-[13.5px] font-semibold rounded-[8px] cursor-pointer transition-all select-none ${
                mode === 'signin'
                  ? 'bg-panel text-nps-700 shadow-[0_1px_3px_rgba(0,0,0,0.08)]'
                  : 'text-muted hover:text-text'
              }`}
              onClick={() => { setMode('signin'); setAuthError(null); }}
            >
              Sign In
            </div>
            <div
              className={`flex-1 text-center py-[10px] text-[13.5px] font-semibold rounded-[8px] cursor-pointer transition-all select-none ${
                mode === 'register'
                  ? 'bg-panel text-nps-700 shadow-[0_1px_3px_rgba(0,0,0,0.08)]'
                  : 'text-muted hover:text-text'
              }`}
              onClick={() => { setMode('register'); setAuthError(null); }}
            >
              Register Account
            </div>
          </div>

          {/* Error Message Banner */}
          {authError && (
            <div className="mb-4 text-[12.5px] font-medium text-red-600 bg-red-50 border border-red-200 p-3 rounded-lg">
              {authError}
            </div>
          )}

          {/* User Authenticated Session Bar */}
          {isAuthenticated && user && (
            <div className="mb-5 p-3.5 bg-nps-50 border border-border rounded-lg flex items-center justify-between">
              <div>
                <strong className="text-[13px] font-semibold text-nps-900 block">{user.email}</strong>
                <span className="text-[11px] text-muted">Role: {user.role || 'USER'}</span>
              </div>
              <button
                type="button"
                onClick={logout}
                className="text-[12px] font-semibold text-red-600 hover:underline bg-transparent border-0 cursor-pointer"
              >
                Sign Out
              </button>
            </div>
          )}

          {/* SIGN IN FORM */}
          {mode === 'signin' && (
            <div>
              <h2 className="text-[22px] font-bold mb-[6px] text-text">Welcome back</h2>
              <p className="text-[13.5px] text-muted mb-[28px]">Sign in to continue to your workspace.</p>

              <form onSubmit={handleLoginSubmit(onLogin)}>
                <div className="mb-[18px]">
                  <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                    Email or username <span className="text-nps-600">*</span>
                  </label>
                  <input
                    type="text"
                    {...registerLogin('email')}
                    placeholder="you@nibss-plc.com.ng"
                    className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                  />
                  {loginErrors.email && (
                    <div className="text-[11.5px] text-red-600 mt-[5px]">{loginErrors.email.message}</div>
                  )}
                </div>

                <div className="mb-[18px]">
                  <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                    Password <span className="text-nps-600">*</span>
                  </label>
                  <input
                    type="password"
                    {...registerLogin('password')}
                    placeholder="••••••••••••"
                    className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                  />
                  {loginErrors.password && (
                    <div className="text-[11.5px] text-red-600 mt-[5px]">{loginErrors.password.message}</div>
                  )}
                </div>

                <div className="flex items-center justify-between mb-[22px]">
                  <label className="flex items-center gap-[8px] text-[13px] text-muted cursor-pointer">
                    <input
                      type="checkbox"
                      {...registerLogin('keepSignedIn')}
                      className="w-[14px] h-[14px] accent-nps-600"
                    />
                    Keep me signed in
                  </label>
                  <a
                    href="#"
                    onClick={(e) => { e.preventDefault(); alert('Password reset link requested.'); }}
                    className="text-[13px] font-semibold text-nps-700 hover:underline"
                  >
                    Forgot password?
                  </a>
                </div>

                <button
                  type="submit"
                  disabled={isLoginSubmitting}
                  className="btn-primary"
                >
                  {isLoginSubmitting ? 'Signing In...' : 'Sign In'}
                </button>

                <div className="divider-row">OR ENTERPRISE SSO</div>

                {/* Microsoft OAuth Button */}
                <button
                  type="button"
                  onClick={handleMicrosoftLogin}
                  className="w-full bg-panel border border-border py-[11px] rounded-[8px] text-[13.5px] font-semibold text-text cursor-pointer flex items-center justify-center gap-[10px] hover:bg-nps-50 transition-all"
                >
                  <svg className="w-[16px] h-[16px]" viewBox="0 0 23 23">
                    <path fill="#f35325" d="M1 1h10v10H1z" />
                    <path fill="#81bc06" d="M12 1h10v10H12z" />
                    <path fill="#05a6f0" d="M1 12h10v10H1z" />
                    <path fill="#ffba08" d="M12 12h10v10H12z" />
                  </svg>
                  <span>Sign in with Microsoft</span>
                </button>

                {/* Token Panel */}
                <div className={`token-panel ${(token || isAuthenticated) ? 'show' : ''}`}>
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

          {/* REGISTER FORM */}
          {mode === 'register' && (
            <div>
              <h2 className="text-[22px] font-bold mb-[6px] text-text">Create an account</h2>
              <p className="text-[13.5px] text-muted mb-[28px]">Get sandbox access and an API token for testing.</p>

              <form onSubmit={handleRegisterSubmit(onRegister)}>
                <div className="grid grid-cols-2 gap-[12px] mb-[18px]">
                  <div>
                    <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                      First name <span className="text-nps-600">*</span>
                    </label>
                    <input
                      type="text"
                      {...registerReg('firstName')}
                      placeholder="John"
                      className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                    />
                    {regErrors.firstName && (
                      <div className="text-[11.5px] text-red-600 mt-[5px]">{regErrors.firstName.message}</div>
                    )}
                  </div>

                  <div>
                    <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                      Last name <span className="text-nps-600">*</span>
                    </label>
                    <input
                      type="text"
                      {...registerReg('lastName')}
                      placeholder="Developer"
                      className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                    />
                    {regErrors.lastName && (
                      <div className="text-[11.5px] text-red-600 mt-[5px]">{regErrors.lastName.message}</div>
                    )}
                  </div>
                </div>

                <div className="mb-[18px]">
                  <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                    Work email <span className="text-nps-600">*</span>
                  </label>
                  <input
                    type="text"
                    {...registerReg('workEmail')}
                    placeholder="you@company.com"
                    className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                  />
                  {regErrors.workEmail && (
                    <div className="text-[11.5px] text-red-600 mt-[5px]">{regErrors.workEmail.message}</div>
                  )}
                </div>

                <div className="mb-[18px]">
                  <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                    Organization
                  </label>
                  <input
                    type="text"
                    {...registerReg('organization')}
                    placeholder="ACME Financial Services"
                    className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                  />
                </div>

                <div className="mb-[18px]">
                  <label className="block text-[12.5px] font-semibold text-text mb-[6px] tracking-[0.2px]">
                    Password <span className="text-nps-600">*</span>
                  </label>
                  <input
                    type="password"
                    {...registerReg('password')}
                    placeholder="Minimum 8 characters"
                    className="w-full px-[12px] py-[11px] text-[13.5px] border border-border rounded-[8px] bg-panel text-text outline-none focus:border-nps-600 focus:ring-4 focus:ring-nps-600/10 transition-all"
                  />
                  <div className="text-[11.5px] text-muted mt-[5px]">
                    Use at least one uppercase letter, one number, and one symbol.
                  </div>
                  {regErrors.password && (
                    <div className="text-[11.5px] text-red-600 mt-[5px]">{regErrors.password.message}</div>
                  )}
                </div>

                <label className="flex items-center gap-[8px] text-[13px] text-muted mb-[20px] cursor-pointer">
                  <input
                    type="checkbox"
                    {...registerReg('agreeTerms')}
                    className="w-[14px] h-[14px] accent-nps-600"
                  />
                  I agree to the Sandbox Terms of Use
                </label>
                {regErrors.agreeTerms && (
                  <div className="text-[11.5px] text-red-600 mb-[12px]">{regErrors.agreeTerms.message}</div>
                )}

                <button
                  type="submit"
                  disabled={isRegSubmitting}
                  className="btn-primary"
                >
                  {isRegSubmitting ? 'Creating Account...' : 'Create Account'}
                </button>
              </form>
            </div>
          )}

          <div className="foot-note">
            Need gateway credentials instead? <a href="#" className="text-nps-700 font-semibold hover:underline">Contact NIBSS integration support</a>
          </div>
        </div>
      </div>
    </div>
  );
};

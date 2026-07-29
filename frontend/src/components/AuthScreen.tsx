import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useLocation, useNavigate } from 'react-router-dom';
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
  const location = useLocation();
  const navigate = useNavigate();

  const [mode, setMode] = useState<'signin' | 'register'>(
    location.pathname === '/register' ? 'register' : 'signin'
  );
  const [copiedToken, setCopiedToken] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isRedirecting, setIsRedirecting] = useState(false);

  const { login, register, token, user, isAuthenticated, logout } = useAuth();

  useEffect(() => {
    if (location.pathname === '/register') {
      setMode('register');
    } else if (location.pathname === '/login') {
      setMode('signin');
    }
  }, [location.pathname]);

  // Redirect users who are already authenticated
  useEffect(() => {
    if (isAuthenticated && !isRedirecting) {
      navigate('/workbench', { replace: true });
    }
  }, [isAuthenticated, isRedirecting, navigate]);

  const handleTabSwitch = (newMode: 'signin' | 'register') => {
    setMode(newMode);
    setAuthError(null);
    navigate(newMode === 'register' ? '/register' : '/login', { replace: true });
  };

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
    setSuccessMessage(null);
    try {
      await login({ email: data.email, password: data.password });
      setIsRedirecting(true);
      setSuccessMessage('Sign in successful! Redirecting to your workbench...');
      setTimeout(() => {
        navigate('/workbench');
      }, 1500);
    } catch (err: any) {
      setAuthError(err.message || 'Authentication failed. Please check your credentials.');
    }
  };

  const onRegister = async (data: RegisterFormValues) => {
    setAuthError(null);
    setSuccessMessage(null);
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
      setIsRedirecting(true);
      setSuccessMessage('Account created successfully! Preparing your sandbox workspace...');
      setTimeout(() => {
        navigate('/workbench');
      }, 2000);
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
    <div className="w-full min-h-screen flex flex-col md:flex-row bg-[#f7faf8] text-gray-900 font-sans">
      {/* LEFT: Branded Information Panel */}
      <div
        className="hidden md:flex md:w-[46%] lg:w-[44%] text-white flex-col justify-between p-10 md:p-12 lg:p-14 min-h-screen shrink-0 relative overflow-hidden bg-brand-radial"
        style={{
          backgroundColor: '#0b3b1e',
          backgroundImage:
            'linear-gradient(to right, rgba(255, 255, 255, 0.04) 1px, transparent 1px), linear-gradient(to bottom, rgba(255, 255, 255, 0.04) 1px, transparent 1px), radial-gradient(130% 130% at 10% 10%, #0f4c28 0%, #0b3b1e 55%, #082e18 100%)',
          backgroundSize: '32px 32px, 32px 32px, 100% 100%',
        }}
      >
        {/* Top Header */}
        <div className="relative z-10">
          <div className="flex items-center gap-3">
            <div className="w-[42px] h-[42px] rounded-xl bg-[#16a34a] flex items-center justify-center font-bold tracking-wide text-white text-[15px] shadow-[0_4px_16px_rgba(22,163,74,0.4)] shrink-0">
              NPS
            </div>
            <div>
              <h2 className="text-[18px] font-bold tracking-tight text-white leading-tight">
                NPS Play Box Engine
              </h2>
              <span className="block text-[12px] font-medium text-[#82ca9c]">
                ISO 20022 Message Engineering Portal
              </span>
            </div>
          </div>
        </div>

        {/* Middle Feature Copy */}
        <div className="relative z-10 my-auto py-8">
          <h1 className="text-[32px] lg:text-[34px] leading-[1.22] font-bold max-w-[430px] text-white tracking-tight">
            Build, sign, and dispatch ISO 20022 payment messages against the NIBSS sandbox.
          </h1>
          <p className="text-[#abdcba] text-[14.5px] leading-[1.6] max-w-[410px] font-normal mt-4">
            Configure message fields, generate compliant XML, apply PKCS#7 signing, and inspect the live gateway response — all in one pipeline.
          </p>

          <div className="inline-flex items-center gap-2.5 mt-8 font-mono text-[12px] bg-[#0c2b1a]/90 border border-[#1b4d2e] px-3.5 py-2 rounded-lg text-[#4ade80] shadow-inner">
            <span className="w-2 h-2 rounded-full bg-[#22c55e] shadow-[0_0_8px_#22c55e]"></span>
            <span>pain.013.001.11</span>
            <span className="text-[#1b4d2e]">·</span>
            <span>pacs.008.001.10</span>
            <span className="text-[#1b4d2e]">·</span>
            <span>camt.060.001.05</span>
          </div>
        </div>

        {/* Bottom Metrics Footer */}
        <div className="relative z-10 pt-5 border-t border-white/10 text-[12px]">
          <div className="flex gap-6 lg:gap-8 flex-wrap">
            <div>
              <strong className="block text-white text-[13px] font-semibold">NIBSS Sandbox v2.4</strong>
              <span className="text-[#6aa380] text-[12px]">Active target</span>
            </div>
            <div>
              <strong className="block text-white text-[13px] font-semibold">SHA-256 / RSA</strong>
              <span className="text-[#6aa380] text-[12px]">Signing algorithm</span>
            </div>
            <div>
              <strong className="block text-white text-[13px] font-semibold">984 / 1000</strong>
              <span className="text-[#6aa380] text-[12px]">Rate limit today</span>
            </div>
          </div>
        </div>
      </div>

      {/* RIGHT: Form Panel */}
      <div className="flex-1 flex flex-col justify-center items-center p-6 sm:p-10 md:p-12 bg-[#f7faf8] min-h-screen">
        {successMessage ? (
          <div className="w-full max-w-[400px] text-center flex flex-col items-center">
            {/* Green glowing circle checkmark icon */}
            <div className="w-20 h-20 rounded-full bg-[#e8f6ed] border border-[#d2efe0] flex items-center justify-center text-[#16a34a] shadow-[0_4px_20px_rgba(22,163,74,0.15)] mb-6 animate-bounce">
              <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            
            <h2 className="text-[24px] font-bold text-gray-900 tracking-tight mb-2">
              {mode === 'register' ? 'Welcome to NPS Play Box!' : 'Welcome back'}
            </h2>
            
            <p className="text-[14.5px] text-gray-600 mb-6 leading-relaxed max-w-[340px]">
              {successMessage}
            </p>
            
            {/* Smooth spinner */}
            <div className="flex items-center justify-center gap-2 text-[13px] text-[#16a34a] font-semibold">
              <svg className="animate-spin h-5 w-5 text-[#16a34a]" xmlns="http://www.w3.org/2000/09/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span>Configuring your workspace...</span>
            </div>
          </div>
        ) : (
          <div className="w-full max-w-[410px]">
            {/* Environment Status Badge */}
            <div className="flex justify-center mb-6">
              <span className="inline-flex items-center gap-1.5 text-[12px] font-semibold text-[#16a34a] bg-[#e8f6ed] border border-[#d2efe0] px-3 py-1 rounded-full shadow-2xs">
                <span className="w-2 h-2 rounded-full bg-[#16a34a]"></span>
                Sandbox environment
              </span>
            </div>

            {/* Mode Switcher Tabs */}
            <div className="flex bg-[#edf2ee] border border-[#e1e9e3] rounded-xl p-1 mb-8 shadow-inner">
              <button
                type="button"
                className={`flex-1 py-2.5 text-center text-[13.5px] font-semibold rounded-lg transition-all select-none cursor-pointer ${
                  mode === 'signin'
                    ? 'bg-white text-[#16a34a] border border-[#cde7d6] shadow-[0_1px_3px_rgba(0,0,0,0.06)]'
                    : 'text-gray-500 hover:text-gray-900 border border-transparent'
                }`}
                onClick={() => handleTabSwitch('signin')}
              >
                Sign In
              </button>
              <button
                type="button"
                className={`flex-1 py-2.5 text-center text-[13.5px] font-semibold rounded-lg transition-all select-none cursor-pointer ${
                  mode === 'register'
                    ? 'bg-white text-[#16a34a] border border-[#cde7d6] shadow-[0_1px_3px_rgba(0,0,0,0.06)]'
                    : 'text-gray-500 hover:text-gray-900 border border-transparent'
                }`}
                onClick={() => handleTabSwitch('register')}
              >
                Register Account
              </button>
            </div>

            {/* Error Banner */}
            {authError && (
              <div className="mb-5 text-[13px] font-medium text-red-700 bg-red-50 border border-red-200 p-3 rounded-lg flex items-center gap-2">
                <svg className="w-4 h-4 text-red-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{authError}</span>
              </div>
            )}

            {/* User Authenticated Session Bar */}
            {isAuthenticated && user && (
              <div className="mb-6 p-4 bg-[#e8f6ed]/60 border border-[#d2efe0] rounded-xl flex items-center justify-between">
                <div>
                  <strong className="text-[13.5px] font-semibold text-gray-900 block">{user.email}</strong>
                  <span className="text-[12px] text-gray-500">Role: {user.role || 'USER'}</span>
                </div>
                <button
                  type="button"
                  onClick={logout}
                  className="text-[12px] font-semibold text-red-600 hover:text-red-700 hover:underline bg-transparent border-0 cursor-pointer"
                >
                  Sign Out
                </button>
              </div>
            )}

            {/* SIGN IN FORM */}
            {mode === 'signin' && (
              <div>
                <h2 className="text-[24px] font-bold mb-1.5 text-gray-900 tracking-tight">Welcome back</h2>
                <p className="text-[13.5px] text-gray-500 mb-7">Sign in to continue to your workspace.</p>

                <form onSubmit={handleLoginSubmit(onLogin)} className="space-y-4">
                  <div>
                    <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                      Email or username <span className="text-[#16a34a]">*</span>
                    </label>
                    <input
                      type="text"
                      {...registerLogin('email')}
                      placeholder="you@nibss-plc.com.ng"
                      className="form-input"
                    />
                    {loginErrors.email && (
                      <div className="text-[12px] text-red-600 mt-1">{loginErrors.email.message}</div>
                    )}
                  </div>

                  <div>
                    <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                      Password <span className="text-[#16a34a]">*</span>
                    </label>
                    <input
                      type="password"
                      {...registerLogin('password')}
                      placeholder="••••••••••••"
                      className="form-input"
                    />
                    {loginErrors.password && (
                      <div className="text-[12px] text-red-600 mt-1">{loginErrors.password.message}</div>
                    )}
                  </div>

                  <div className="flex items-center justify-between pt-1 pb-2">
                    <label className="flex items-center gap-2 text-[13px] text-gray-600 cursor-pointer select-none">
                      <input
                        type="checkbox"
                        {...registerLogin('keepSignedIn')}
                        className="w-4 h-4 accent-[#16a34a] rounded border-gray-300 focus:ring-[#16a34a]"
                      />
                      Keep me signed in
                    </label>
                    <a
                      href="#"
                      onClick={(e) => { e.preventDefault(); alert('Password reset request initiated.'); }}
                      className="text-[13px] font-semibold text-[#16a34a] hover:underline"
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

                  <div className="relative flex py-3 items-center">
                    <div className="flex-grow border-t border-gray-200"></div>
                    <span className="flex-shrink mx-3 text-[11px] font-semibold tracking-wider text-gray-400 uppercase">OR ENTERPRISE SSO</span>
                    <div className="flex-grow border-t border-gray-200"></div>
                  </div>

                  {/* Microsoft OAuth Button */}
                  <button
                    type="button"
                    onClick={handleMicrosoftLogin}
                    className="w-full bg-white border border-gray-200 py-2.5 px-4 rounded-xl text-[13.5px] font-semibold text-gray-700 hover:bg-gray-50 hover:border-gray-300 transition-all flex items-center justify-center gap-2.5 shadow-xs cursor-pointer"
                  >
                    <svg className="w-4 h-4 shrink-0" viewBox="0 0 23 23">
                      <path fill="#f35325" d="M1 1h10v10H1z" />
                      <path fill="#81bc06" d="M12 1h10v10H12z" />
                      <path fill="#05a6f0" d="M1 12h10v10H1z" />
                      <path fill="#ffba08" d="M12 12h10v10H12z" />
                    </svg>
                    <span>Sign in with Microsoft</span>
                  </button>

                  {/* Token Box if available */}
                  {(token || isAuthenticated) && (
                    <div className="mt-4 p-3 bg-gray-50 border border-gray-200 rounded-lg">
                      <div className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">Active Session Token</div>
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-[12px] font-mono text-gray-700 truncate">{token || 'Session Active'}</span>
                        <button
                          type="button"
                          className="text-[11px] font-semibold text-[#16a34a] hover:underline shrink-0"
                          onClick={handleCopyToken}
                        >
                          {copiedToken ? 'Copied!' : 'Copy'}
                        </button>
                      </div>
                    </div>
                  )}
                </form>
              </div>
            )}

            {/* REGISTER FORM */}
            {mode === 'register' && (
              <div>
                <h2 className="text-[24px] font-bold mb-1.5 text-gray-900 tracking-tight">Create an account</h2>
                <p className="text-[13.5px] text-gray-500 mb-7">Get sandbox access and an API token for testing.</p>

                <form onSubmit={handleRegisterSubmit(onRegister)} className="space-y-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                        First name <span className="text-[#16a34a]">*</span>
                      </label>
                      <input
                        type="text"
                        {...registerReg('firstName')}
                        placeholder="John"
                        className="form-input"
                      />
                      {regErrors.firstName && (
                        <div className="text-[12px] text-red-600 mt-1">{regErrors.firstName.message}</div>
                      )}
                    </div>

                    <div>
                      <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                        Last name <span className="text-[#16a34a]">*</span>
                      </label>
                      <input
                        type="text"
                        {...registerReg('lastName')}
                        placeholder="Developer"
                        className="form-input"
                      />
                      {regErrors.lastName && (
                        <div className="text-[12px] text-red-600 mt-1">{regErrors.lastName.message}</div>
                      )}
                    </div>
                  </div>

                  <div>
                    <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                      Work email <span className="text-[#16a34a]">*</span>
                    </label>
                    <input
                      type="email"
                      {...registerReg('workEmail')}
                      placeholder="you@company.com"
                      className="form-input"
                    />
                    {regErrors.workEmail && (
                      <div className="text-[12px] text-red-600 mt-1">{regErrors.workEmail.message}</div>
                    )}
                  </div>

                  <div>
                    <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                      Organization
                    </label>
                    <input
                      type="text"
                      {...registerReg('organization')}
                      placeholder="ACME Financial Services"
                      className="form-input"
                    />
                  </div>

                  <div>
                    <label className="block text-[13px] font-semibold text-gray-700 mb-1.5">
                      Password <span className="text-[#16a34a]">*</span>
                    </label>
                    <input
                      type="password"
                      {...registerReg('password')}
                      placeholder="Minimum 8 characters"
                      className="form-input"
                    />
                    <div className="text-[12px] text-gray-500 mt-1.5 leading-tight">
                      Use at least one uppercase letter, one number, and one symbol.
                    </div>
                    {regErrors.password && (
                      <div className="text-[12px] text-red-600 mt-1">{regErrors.password.message}</div>
                    )}
                  </div>

                  <div className="pt-1">
                    <label className="flex items-center gap-2.5 text-[13px] text-gray-600 cursor-pointer select-none">
                      <input
                        type="checkbox"
                        {...registerReg('agreeTerms')}
                        className="w-4 h-4 accent-[#16a34a] rounded border-gray-300 focus:ring-[#16a34a]"
                      />
                      <span>I agree to the Sandbox Terms of Use</span>
                    </label>
                    {regErrors.agreeTerms && (
                      <div className="text-[12px] text-red-600 mt-1">{regErrors.agreeTerms.message}</div>
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={isRegSubmitting}
                    className="btn-primary mt-2"
                  >
                    {isRegSubmitting ? 'Creating Account...' : 'Create Account'}
                  </button>
                </form>
              </div>
            )}

            <div className="mt-8 text-center text-[13px] text-gray-500">
              Need gateway credentials instead?{' '}
              <a href="#" className="text-[#16a34a] font-semibold hover:underline">
                Contact NIBSS integration support
              </a>
            </div>
          </div>
        )}
      </div>

    </div>
  );

};


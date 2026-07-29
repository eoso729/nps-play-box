import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Shield,
  Key,
  Copy,
  Check,
  Lock,
  Mail,
  User as UserIcon,
  Building,
  Activity,
  ArrowRight,
  Eye,
  EyeOff,
  Server,
  AlertCircle,
  LogOut
} from 'lucide-react';
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
  organization: z.string().min(1, 'Organization is required'),
  password: z.string().min(8, 'Password must be at least 8 characters long'),
  agreeTerms: z.literal(true, {
    errorMap: () => ({ message: 'You must accept the Sandbox Terms of Service' }),
  }),
});

type LoginFormValues = z.infer<typeof loginSchema>;
type RegisterFormValues = z.infer<typeof registerSchema>;

export const AuthScreen: React.FC = () => {
  const [mode, setMode] = useState<'signin' | 'register'>('signin');
  const [showPassword, setShowPassword] = useState(false);
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
        organization: data.organization,
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
      setTimeout(() => setCopiedToken(false), 2000);
    }
  };

  return (
    <div className="min-h-screen w-full flex flex-col lg:flex-row bg-[#f7faf8] text-[#111827]">
      {/* Left Brand Panel (44% width on desktop) */}
      <div className="lg:w-[44%] brand-panel-gradient grid-pattern p-8 lg:p-12 text-white flex flex-col justify-between relative overflow-hidden min-h-[480px]">
        {/* Subtle decorative Glow */}
        <div className="absolute -top-24 -left-24 w-96 h-96 bg-[#22a05a]/10 rounded-full blur-3xl pointer-events-none" />
        
        {/* Top Header & Logo */}
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-8">
            <div className="h-10 w-10 rounded-lg bg-[#22a05a] flex items-center justify-center font-bold text-white shadow-lg shadow-[#15803d]/30 tracking-wide text-sm border border-[#16a34a]">
              NPS
            </div>
            <div>
              <h2 className="font-semibold text-base leading-snug tracking-tight text-white">
                NPS Play Box Engine
              </h2>
              <p className="text-xs text-[#e6f6ec]/70 font-medium">
                ISO 20022 Message Engineering Portal
              </p>
            </div>
          </div>

          {/* Main Headline & Description */}
          <div className="mt-8 lg:mt-12 space-y-4">
            <h1 className="text-2xl lg:text-3xl font-bold leading-tight tracking-tight text-white">
              Build, sign, and dispatch ISO 20022 payment messages against the NIBSS sandbox.
            </h1>
            <p className="text-sm lg:text-base text-[#e6f6ec]/80 leading-relaxed font-normal">
              Configure message fields, generate compliant XML, apply PKCS#7 signing, and inspect the live gateway response — all in one pipeline.
            </p>
          </div>

          {/* Protocol Chip */}
          <div className="mt-6 inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-[#0f3a22]/80 border border-[#15803d]/40 text-xs font-mono text-[#e6f6ec]">
            <Server className="w-3.5 h-3.5 text-[#22a05a]" />
            <span>pain.013.001.11 · pacs.008.001.10 · camt.060.001.05</span>
          </div>
        </div>

        {/* Bottom Environment Metadata */}
        <div className="relative z-10 pt-8 mt-8 border-t border-[#15803d]/30 text-xs text-[#e6f6ec]/70 space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="font-mono text-[#e6f6ec]/90 flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#22a05a] animate-pulse" />
              NIBSS Sandbox v2.4
            </span>
            <span className="font-mono bg-[#0b2818] px-2 py-0.5 rounded border border-[#15803d]/40">
              SHA-256 / RSA signing
            </span>
          </div>
          <div className="flex items-center justify-between text-[11px] text-[#e6f6ec]/60">
            <span className="flex items-center gap-1">
              <Activity className="w-3 h-3 text-[#22a05a]" /> Rate limit indicator
            </span>
            <span>100 req / min</span>
          </div>
        </div>
      </div>

      {/* Right Form Panel */}
      <div className="lg:w-[56%] flex flex-col justify-between p-6 lg:p-12 bg-white">
        <div className="max-w-md w-full mx-auto my-auto space-y-6">
          {/* Top Badge & Mode Switcher */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-[#e6f6ec] text-[#0f3a22] border border-[#22a05a]/30">
                <span className="w-1.5 h-1.5 rounded-full bg-[#16a34a]" />
                Sandbox environment
              </span>
            </div>

            {/* Mode Switcher Buttons */}
            <div className="grid grid-cols-2 p-1 bg-[#f3faf5] rounded-lg border border-[#e2e8e6]">
              <button
                type="button"
                onClick={() => { setMode('signin'); setAuthError(null); }}
                className={`py-2 text-xs font-semibold rounded-md transition-all ${
                  mode === 'signin'
                    ? 'bg-white text-[#0b2818] shadow-sm border border-[#e2e8e6]'
                    : 'text-[#6b7280] hover:text-[#111827]'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => { setMode('register'); setAuthError(null); }}
                className={`py-2 text-xs font-semibold rounded-md transition-all ${
                  mode === 'register'
                    ? 'bg-white text-[#0b2818] shadow-sm border border-[#e2e8e6]'
                    : 'text-[#6b7280] hover:text-[#111827]'
                }`}
              >
                Register Account
              </button>
            </div>
          </div>

          {/* Alert Message for Auth Errors */}
          {authError && (
            <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <span>{authError}</span>
            </div>
          )}

          {/* Authenticated State Banner & Token Panel */}
          {isAuthenticated && user && (
            <div className="p-4 rounded-xl bg-[#f3faf5] border border-[#22a05a]/40 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-full bg-[#15803d] text-white font-semibold flex items-center justify-center text-xs">
                    {user.username ? user.username[0].toUpperCase() : 'U'}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-[#0b2818]">{user.email}</p>
                    <p className="text-[11px] text-[#6b7280]">Role: {user.role || 'USER'} · Provider: {user.authProvider || 'LOCAL'}</p>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={logout}
                  className="inline-flex items-center gap-1 text-xs text-red-600 hover:text-red-700 font-medium"
                >
                  <LogOut className="w-3.5 h-3.5" /> Logout
                </button>
              </div>

              {/* JWT tokenPanel Element */}
              {token && (
                <div id="tokenPanel" className="pt-2 border-t border-[#e2e8e6] space-y-1.5">
                  <div className="flex items-center justify-between text-[11px] font-semibold text-[#0f3a22]">
                    <span className="flex items-center gap-1">
                      <Key className="w-3 h-3 text-[#16a34a]" /> Active JWT API Token
                    </span>
                    <button
                      type="button"
                      onClick={handleCopyToken}
                      className="inline-flex items-center gap-1 text-[11px] text-[#15803d] hover:text-[#0b2818] font-medium"
                    >
                      {copiedToken ? <Check className="w-3 h-3 text-green-600" /> : <Copy className="w-3 h-3" />}
                      {copiedToken ? 'Copied!' : 'Copy Token'}
                    </button>
                  </div>
                  <div className="p-2 bg-white rounded border border-[#e2e8e6] font-mono text-[10px] text-[#6b7280] break-all max-h-16 overflow-y-auto">
                    {token}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Mode 1: Sign In Form */}
          {mode === 'signin' && (
            <form onSubmit={handleLoginSubmit(onLogin)} className="space-y-4">
              {/* Email / Username Input */}
              <div className="space-y-1">
                <label className="block text-xs font-semibold text-[#111827]">
                  Work Email or Username
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    {...registerLogin('email')}
                    placeholder="developer@bank.com"
                    className="w-full pl-9 pr-3 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                </div>
                {loginErrors.email && (
                  <p className="text-[11px] text-red-600 mt-1">{loginErrors.email.message}</p>
                )}
              </div>

              {/* Password Input */}
              <div className="space-y-1">
                <label className="block text-xs font-semibold text-[#111827]">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    {...registerLogin('password')}
                    placeholder="••••••••"
                    className="w-full pl-9 pr-9 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-[#6b7280] hover:text-[#111827]"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
                {loginErrors.password && (
                  <p className="text-[11px] text-red-600 mt-1">{loginErrors.password.message}</p>
                )}
              </div>

              {/* Checkbox & Forgot Password */}
              <div className="flex items-center justify-between text-xs pt-1">
                <label className="flex items-center gap-2 text-[#6b7280] cursor-pointer">
                  <input
                    type="checkbox"
                    {...registerLogin('keepSignedIn')}
                    className="rounded border-[#e2e8e6] text-[#15803d] focus:ring-[#15803d]"
                  />
                  <span>Keep me signed in</span>
                </label>
                <a href="#forgot" onClick={(e) => { e.preventDefault(); alert('Password reset link has been dispatched to your email.'); }} className="text-[#15803d] hover:underline font-medium">
                  Forgot password?
                </a>
              </div>

              {/* Primary Submit Button */}
              <button
                type="submit"
                disabled={isLoginSubmitting}
                className="w-full py-2.5 px-4 rounded-md bg-[#15803d] hover:bg-[#0f3a22] text-white font-medium text-xs shadow-sm flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                {isLoginSubmitting ? 'Signing In...' : 'Sign In'}
                {!isLoginSubmitting && <ArrowRight className="w-4 h-4" />}
              </button>

              {/* Divider */}
              <div className="relative py-2 flex items-center justify-center">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-[#e2e8e6]" />
                </div>
                <span className="relative px-3 bg-white text-[11px] text-[#6b7280]">
                  OR ENTERPRISE SSO
                </span>
              </div>

              {/* Microsoft OAuth Button */}
              <button
                type="button"
                onClick={handleMicrosoftLogin}
                className="w-full py-2.5 px-4 rounded-md border border-[#e2e8e6] bg-white hover:bg-gray-50 text-[#111827] font-medium text-xs flex items-center justify-center gap-2.5 shadow-sm transition-all"
              >
                {/* Official Microsoft 4-Color Logo SVG */}
                <svg className="w-4 h-4" viewBox="0 0 23 23">
                  <path fill="#f35325" d="M1 1h10v10H1z" />
                  <path fill="#81bc06" d="M12 1h10v10H12z" />
                  <path fill="#05a6f0" d="M1 12h10v10H1z" />
                  <path fill="#ffba08" d="M12 12h10v10H12z" />
                </svg>
                <span>Sign in with Microsoft</span>
              </button>
            </form>
          )}

          {/* Mode 2: Register Form */}
          {mode === 'register' && (
            <form onSubmit={handleRegisterSubmit(onRegister)} className="space-y-3">
              {/* First Name & Last Name */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="block text-xs font-semibold text-[#111827]">
                    First Name
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                      <UserIcon className="w-3.5 h-3.5" />
                    </div>
                    <input
                      type="text"
                      {...registerReg('firstName')}
                      placeholder="Jane"
                      className="w-full pl-8 pr-3 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                    />
                  </div>
                  {regErrors.firstName && (
                    <p className="text-[10px] text-red-600 mt-0.5">{regErrors.firstName.message}</p>
                  )}
                </div>

                <div className="space-y-1">
                  <label className="block text-xs font-semibold text-[#111827]">
                    Last Name
                  </label>
                  <input
                    type="text"
                    {...registerReg('lastName')}
                    placeholder="Doe"
                    className="w-full px-3 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                  {regErrors.lastName && (
                    <p className="text-[10px] text-red-600 mt-0.5">{regErrors.lastName.message}</p>
                  )}
                </div>
              </div>

              {/* Work Email */}
              <div className="space-y-1">
                <label className="block text-xs font-semibold text-[#111827]">
                  Work Email
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                    <Mail className="w-3.5 h-3.5" />
                  </div>
                  <input
                    type="email"
                    {...registerReg('workEmail')}
                    placeholder="jane.doe@firstbank.ng"
                    className="w-full pl-8 pr-3 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                </div>
                {regErrors.workEmail && (
                  <p className="text-[10px] text-red-600 mt-0.5">{regErrors.workEmail.message}</p>
                )}
              </div>

              {/* Organization */}
              <div className="space-y-1">
                <label className="block text-xs font-semibold text-[#111827]">
                  Organization / Bank Name
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                    <Building className="w-3.5 h-3.5" />
                  </div>
                  <input
                    type="text"
                    {...registerReg('organization')}
                    placeholder="First Bank of Nigeria"
                    className="w-full pl-8 pr-3 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                </div>
                {regErrors.organization && (
                  <p className="text-[10px] text-red-600 mt-0.5">{regErrors.organization.message}</p>
                )}
              </div>

              {/* Password with 8 char hint */}
              <div className="space-y-1">
                <label className="block text-xs font-semibold text-[#111827]">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#6b7280]">
                    <Lock className="w-3.5 h-3.5" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    {...registerReg('password')}
                    placeholder="Minimum 8 characters"
                    className="w-full pl-8 pr-8 py-2 text-xs rounded-md border border-[#e2e8e6] focus:outline-none focus:ring-2 focus:ring-[#15803d] focus:border-transparent bg-white"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-2.5 flex items-center text-[#6b7280] hover:text-[#111827]"
                  >
                    {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
                <p className="text-[10px] text-[#6b7280]">Must be at least 8 characters</p>
                {regErrors.password && (
                  <p className="text-[10px] text-red-600 mt-0.5">{regErrors.password.message}</p>
                )}
              </div>

              {/* Terms Checkbox */}
              <div className="pt-1">
                <label className="flex items-start gap-2 text-xs text-[#6b7280] cursor-pointer">
                  <input
                    type="checkbox"
                    {...registerReg('agreeTerms')}
                    className="mt-0.5 rounded border-[#e2e8e6] text-[#15803d] focus:ring-[#15803d]"
                  />
                  <span>
                    I agree to the Sandbox Terms of Service and NIBSS API integration guidelines.
                  </span>
                </label>
                {regErrors.agreeTerms && (
                  <p className="text-[10px] text-red-600 mt-0.5">{regErrors.agreeTerms.message}</p>
                )}
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isRegSubmitting}
                className="w-full py-2.5 px-4 rounded-md bg-[#15803d] hover:bg-[#0f3a22] text-white font-medium text-xs shadow-sm flex items-center justify-center gap-2 transition-all disabled:opacity-50 mt-2"
              >
                {isRegSubmitting ? 'Creating Account...' : 'Create Account'}
                {!isRegSubmitting && <ArrowRight className="w-4 h-4" />}
              </button>
            </form>
          )}

          {/* Footer note */}
          <div className="pt-4 border-t border-[#e2e8e6] text-center text-xs text-[#6b7280]">
            Need gateway credentials instead?{' '}
            <a href="mailto:support@nibss-plc.com.ng" className="text-[#15803d] hover:underline font-medium">
              Contact NIBSS integration support
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

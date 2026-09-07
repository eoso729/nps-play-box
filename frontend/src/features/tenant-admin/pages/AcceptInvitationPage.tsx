import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { Building2, ShieldCheck, CheckCircle2, AlertCircle, Lock, User, ArrowRight, Eye, EyeOff, Sparkles } from 'lucide-react';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { invitationsApi, VerifyInvitationResult } from '../api/invitations.api';

export const AcceptInvitationPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  const [isLoading, setIsLoading] = useState(true);
  const [invitation, setInvitation] = useState<VerifyInvitationResult | null>(null);
  const [verificationError, setVerificationError] = useState<string | null>(null);

  // Form states
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  useEffect(() => {
    if (!token) {
      setIsLoading(false);
      setVerificationError('No invitation token provided. Please check your invitation email link.');
      return;
    }

    const verifyToken = async () => {
      try {
        setIsLoading(true);
        const res = await invitationsApi.verifyInvitation(token);
        if (res.valid) {
          setInvitation(res);
        } else {
          setVerificationError(res.message || 'This invitation is invalid or has expired.');
        }
      } catch (err: any) {
        setVerificationError(
          err.response?.data?.message || 'Unable to verify invitation token. It may have expired or already been accepted.'
        );
      } finally {
        setIsLoading(false);
      }
    };

    verifyToken();
  }, [token]);

  const validate = () => {
    const errs: Record<string, string> = {};
    if (!firstName.trim()) {
      errs.firstName = 'First name is required';
    }
    if (!lastName.trim()) {
      errs.lastName = 'Last name is required';
    }
    if (!password) {
      errs.password = 'Password is required';
    } else if (password.length < 8) {
      errs.password = 'Password must be at least 8 characters long';
    }
    if (password !== confirmPassword) {
      errs.confirmPassword = 'Passwords do not match';
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setIsSubmitting(true);
      await invitationsApi.acceptInvitation({
        token,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        password,
      });
      setSubmitSuccess(true);
    } catch (err: any) {
      setFormErrors({
        submit: err.response?.data?.message || 'Failed to complete account activation. Please try again.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f6f9f7] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      {/* Brand Header */}
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center mb-6">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-[#16a34a] text-white shadow-sm mb-3">
          <Building2 className="w-6 h-6" />
        </div>
        <h2 className="text-2xl font-extrabold text-gray-900 tracking-tight">
          NPS Play Box
        </h2>
        <p className="text-xs text-gray-500 mt-1 uppercase tracking-wider font-semibold">
          Enterprise Multi-Tenant Clearing Sandbox
        </p>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-lg px-4 sm:px-0">
        <div className="bg-white py-8 px-6 sm:px-10 shadow-sm border border-gray-200/80 rounded-2xl">
          {isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-3">
              <div className="w-8 h-8 border-2 border-[#16a34a]/20 border-t-[#16a34a] rounded-full animate-spin" />
              <p className="text-xs font-semibold text-gray-500">Validating your invitation...</p>
            </div>
          ) : verificationError ? (
            <div className="text-center py-6 space-y-4">
              <div className="inline-flex p-3 rounded-full bg-red-50 text-red-600 mb-2">
                <AlertCircle className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-gray-900">Invitation Link Invalid</h3>
              <p className="text-sm text-gray-600 max-w-sm mx-auto">
                {verificationError}
              </p>
              <div className="pt-4 border-t border-gray-100 flex justify-center">
                <Button
                  variant="primary"
                  onClick={() => navigate('/login')}
                  className="flex items-center gap-2"
                >
                  Return to Sign In <ArrowRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          ) : submitSuccess ? (
            <div className="text-center py-6 space-y-4">
              <div className="inline-flex p-3 rounded-full bg-emerald-50 text-emerald-600 mb-2 shadow-inner">
                <CheckCircle2 className="w-10 h-10" />
              </div>
              <h3 className="text-xl font-extrabold text-gray-900">
                Account Successfully Activated!
              </h3>
              <p className="text-sm text-gray-600 max-w-sm mx-auto">
                Welcome to <strong>{invitation?.tenantName}</strong>. Your administrator credentials have been configured. You can now access your workspace.
              </p>

              <div className="pt-4 flex justify-center">
                <Button
                  variant="primary"
                  onClick={() => navigate('/login')}
                  className="w-full flex items-center justify-center gap-2"
                >
                  Proceed to Sign In <ArrowRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          ) : (
            <div>
              {/* Organization and Role Banner */}
              <div className="mb-6 p-4 rounded-xl bg-emerald-50/70 border border-emerald-100 flex items-start gap-3">
                <div className="p-2 rounded-lg bg-emerald-600 text-white shrink-0 mt-0.5">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-sm font-bold text-emerald-950">
                      {invitation?.tenantName}
                    </h3>
                    <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-emerald-200/80 text-emerald-900">
                      {invitation?.role === 'TENANT_ADMIN' ? 'Primary Admin' : invitation?.role}
                    </span>
                  </div>
                  <p className="text-xs text-emerald-800 mt-0.5">
                    Setting up credentials for <strong className="text-emerald-950">{invitation?.email}</strong>
                  </p>
                </div>
              </div>

              <div className="mb-6">
                <h3 className="text-lg font-bold text-gray-900">Activate Your Account</h3>
                <p className="text-xs text-gray-500 mt-0.5">
                  Complete your profile details and set a secure password to initialize your workspace.
                </p>
              </div>

              {formErrors.submit && (
                <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700 flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{formErrors.submit}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="First Name *"
                    placeholder="Jane"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    error={formErrors.firstName}
                    leftIcon={<User className="w-4 h-4 text-gray-400" />}
                  />
                  <Input
                    label="Last Name *"
                    placeholder="Doe"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    error={formErrors.lastName}
                    leftIcon={<User className="w-4 h-4 text-gray-400" />}
                  />
                </div>

                <Input
                  label="Create Password *"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Min. 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  error={formErrors.password}
                  helperText="Must be at least 8 characters long"
                  leftIcon={<Lock className="w-4 h-4 text-gray-400" />}
                  rightIcon={
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowPassword(!showPassword)}
                      className="pointer-events-auto text-gray-400 hover:text-gray-600 focus:outline-none"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  }
                />

                <Input
                  label="Confirm Password *"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Re-enter password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  error={formErrors.confirmPassword}
                  leftIcon={<Lock className="w-4 h-4 text-gray-400" />}
                />

                <div className="pt-3">
                  <Button
                    type="submit"
                    variant="primary"
                    className="w-full justify-center flex items-center gap-2"
                    isLoading={isSubmitting}
                  >
                    <Sparkles className="w-4 h-4" />
                    Complete Setup & Activate Workspace
                  </Button>
                </div>
              </form>

              <div className="mt-6 text-center text-xs text-gray-500">
                Already activated your account?{' '}
                <Link to="/login" className="font-semibold text-[#16a34a] hover:underline">
                  Sign In
                </Link>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

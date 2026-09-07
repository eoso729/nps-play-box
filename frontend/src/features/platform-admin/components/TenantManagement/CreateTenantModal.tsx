import React, { useState } from 'react';
import { Building2, Mail, User, Layers, Hash, Lock, Eye, EyeOff, CheckCircle2, Copy, Check, ExternalLink } from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { useCreateTenant } from '../../hooks/usePlatformTenants';
import { SubscriptionTier, PlatformTenant } from '../../types/platform-admin.types';

interface CreateTenantModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const CreateTenantModal: React.FC<CreateTenantModalProps> = ({
  isOpen,
  onClose,
}) => {
  const createTenant = useCreateTenant();

  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminFirstName, setAdminFirstName] = useState('');
  const [adminLastName, setAdminLastName] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [maxSeats, setMaxSeats] = useState<number>(10);
  const [subscriptionTier, setSubscriptionTier] = useState<string>(SubscriptionTier.PROFESSIONAL);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [provisionedTenant, setProvisionedTenant] = useState<PlatformTenant | null>(null);
  const [copiedLink, setCopiedLink] = useState(false);

  // Auto-generate slug when name changes (if user hasn't explicitly customized slug)
  const [slugCustomized, setSlugCustomized] = useState(false);
  const handleNameChange = (val: string) => {
    setName(val);
    if (!slugCustomized) {
      setSlug(
        val
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/^-+|-+$/g, '')
      );
    }
  };

  const resetForm = () => {
    setName('');
    setSlug('');
    setAdminEmail('');
    setAdminFirstName('');
    setAdminLastName('');
    setAdminPassword('');
    setShowPassword(false);
    setMaxSeats(10);
    setSubscriptionTier(SubscriptionTier.PROFESSIONAL);
    setSlugCustomized(false);
    setErrors({});
    setProvisionedTenant(null);
    setCopiedLink(false);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const validate = () => {
    const errs: Record<string, string> = {};
    if (!name.trim() || name.trim().length < 2) {
      errs.name = 'Tenant organization name must be at least 2 characters';
    }
    if (!slug.trim() || !/^[a-z0-9-]+$/.test(slug)) {
      errs.slug = 'Slug must only contain lowercase alphanumeric characters and hyphens';
    }
    if (!adminEmail.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(adminEmail)) {
      errs.adminEmail = 'Please provide a valid administrator email address';
    }
    if (adminPassword.trim() && adminPassword.trim().length < 8) {
      errs.adminPassword = 'Password must be at least 8 characters if provided';
    }
    if (!maxSeats || maxSeats < 1) {
      errs.maxSeats = 'Seat quota must be at least 1';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      const res = await createTenant.mutateAsync({
        name: name.trim(),
        slug: slug.trim(),
        adminEmail: adminEmail.trim(),
        adminFirstName: adminFirstName.trim() || undefined,
        adminLastName: adminLastName.trim() || undefined,
        adminPassword: adminPassword.trim() || undefined,
        maxSeats: Number(maxSeats),
        subscriptionTier,
      });

      setProvisionedTenant(res);
    } catch {
      // Handled by mutation toast
    }
  };

  const copySetupLink = (link: string) => {
    navigator.clipboard.writeText(link);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2500);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={provisionedTenant ? "Tenant Provisioning Complete" : "Provision New Enterprise Tenant"}
      description={
        provisionedTenant
          ? "The isolated tenant workspace has been initialized successfully."
          : "Initialize a dedicated isolated workspace, primary administrator, and licensed seats"
      }
      maxWidth="lg"
    >
      {provisionedTenant ? (
        <div className="p-6 space-y-5">
          <div className="flex items-center gap-3.5 p-4 rounded-xl bg-emerald-50 border border-emerald-200">
            <div className="p-2.5 rounded-lg bg-emerald-600 text-white shrink-0 shadow-sm">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-emerald-950">
                {provisionedTenant.name} successfully provisioned
              </h3>
              <p className="text-xs text-emerald-800 mt-0.5">
                Slug: <code className="bg-emerald-100/80 px-1 py-0.5 rounded text-emerald-900 font-mono font-bold">{provisionedTenant.slug}</code> • Tier: <span className="font-semibold">{provisionedTenant.subscriptionTier}</span> • Quota: <span className="font-semibold">{provisionedTenant.maxSeats} seats</span>
              </p>
            </div>
          </div>

          {provisionedTenant.invitationUrl ? (
            <div className="p-4 rounded-xl bg-gray-50 border border-gray-200 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-700">
                    Administrator Setup Link
                  </h4>
                  <p className="text-xs text-gray-500 mt-0.5">
                    An onboarding invitation email was dispatched to <strong className="text-gray-700">{adminEmail}</strong>. You can also share the setup link directly:
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="text"
                  readOnly
                  value={provisionedTenant.invitationUrl}
                  className="w-full text-xs font-mono bg-white border border-gray-300 rounded-lg px-3 py-2 text-gray-700 select-all focus:outline-none"
                />
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => copySetupLink(provisionedTenant.invitationUrl!)}
                  className="shrink-0 flex items-center gap-1.5 min-w-[110px]"
                >
                  {copiedLink ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-600" />
                      <span className="text-emerald-700 font-semibold">Copied!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Copy Link</span>
                    </>
                  )}
                </Button>
              </div>

              <div className="flex items-center justify-end">
                <a
                  href={provisionedTenant.invitationUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-xs font-medium text-emerald-700 hover:text-emerald-800 inline-flex items-center gap-1 hover:underline"
                >
                  Open Setup Page <ExternalLink className="w-3 h-3" />
                </a>
              </div>
            </div>
          ) : (
            <div className="p-4 rounded-xl bg-blue-50 border border-blue-200 text-xs text-blue-900">
              <p className="font-semibold mb-1">Direct Administrator Credentials Configured</p>
              <p>
                The primary administrator account for <strong>{adminEmail}</strong> has been created with the specified initial password and is immediately active.
              </p>
            </div>
          )}

          <div className="flex items-center justify-end pt-3 border-t border-gray-100">
            <Button
              type="button"
              variant="primary"
              onClick={handleClose}
            >
              Done
            </Button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {/* Organization Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Organization Name *"
              placeholder="e.g. Apex Global Payments"
              value={name}
              onChange={(e) => handleNameChange(e.target.value)}
              error={errors.name}
              leftIcon={<Building2 className="w-4 h-4 text-gray-400" />}
            />

            <Input
              label="Subdomain / Slug *"
              placeholder="e.g. apex-payments"
              value={slug}
              onChange={(e) => {
                setSlugCustomized(true);
                setSlug(e.target.value.toLowerCase());
              }}
              error={errors.slug}
              helperText={slug ? `${slug}.npsplaybox.com` : undefined}
              leftIcon={<Hash className="w-4 h-4 text-gray-400" />}
            />
          </div>

          {/* Subscription & Seat Allocation */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Subscription Tier"
              value={subscriptionTier}
              onChange={(e) => setSubscriptionTier(e.target.value)}
              options={[
                { value: SubscriptionTier.STANDARD, label: 'Standard Tier' },
                { value: SubscriptionTier.PROFESSIONAL, label: 'Professional Tier' },
                { value: SubscriptionTier.ENTERPRISE, label: 'Enterprise Tier' },
              ]}
            />

            <Input
              label="Seat Quota *"
              type="number"
              min={1}
              max={10000}
              value={maxSeats.toString()}
              onChange={(e) => setMaxSeats(parseInt(e.target.value, 10) || 1)}
              error={errors.maxSeats}
              helperText="Maximum concurrent user licenses allowed"
              leftIcon={<Layers className="w-4 h-4 text-gray-400" />}
            />
          </div>

          {/* Primary Admin Account Details */}
          <div className="pt-2 border-t border-gray-100">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500 mb-3">
              Primary Tenant Administrator
            </h4>

            <div className="space-y-3">
              <Input
                label="Admin Work Email *"
                type="email"
                placeholder="admin@organization.com"
                value={adminEmail}
                onChange={(e) => setAdminEmail(e.target.value)}
                error={errors.adminEmail}
                helperText="Leave password blank below to generate and dispatch an onboarding invitation link"
                leftIcon={<Mail className="w-4 h-4 text-gray-400" />}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <Input
                  label="Admin First Name"
                  placeholder="Jane"
                  value={adminFirstName}
                  onChange={(e) => setAdminFirstName(e.target.value)}
                  leftIcon={<User className="w-4 h-4 text-gray-400" />}
                />

                <Input
                  label="Admin Last Name"
                  placeholder="Doe"
                  value={adminLastName}
                  onChange={(e) => setAdminLastName(e.target.value)}
                  leftIcon={<User className="w-4 h-4 text-gray-400" />}
                />
              </div>

              {/* Optional Password Input */}
              <Input
                label="Initial Password (Optional)"
                type={showPassword ? 'text' : 'password'}
                placeholder="Leave blank to generate an invitation link"
                value={adminPassword}
                onChange={(e) => setAdminPassword(e.target.value)}
                error={errors.adminPassword}
                helperText={
                  adminPassword.trim()
                    ? "Admin account will be immediately activated with this password (min. 8 characters)."
                    : "No password set: An onboarding invitation link will be dispatched to the admin's email."
                }
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
            </div>
          </div>

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
            <Button
              type="button"
              variant="ghost"
              onClick={handleClose}
              disabled={createTenant.isPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              isLoading={createTenant.isPending}
            >
              {adminPassword.trim() ? "Provision Tenant & User" : "Provision & Generate Invite"}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

import React, { useState } from 'react';
import { Building2, Mail, User, Layers, Hash } from 'lucide-react';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Input } from '../../../shared/components/Input';
import { Select } from '../../../shared/components/Select';
import { useCreateTenant } from '../../hooks/usePlatformTenants';
import { SubscriptionTier } from '../../types/platform-admin.types';

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
  const [maxSeats, setMaxSeats] = useState<number>(10);
  const [subscriptionTier, setSubscriptionTier] = useState<string>(SubscriptionTier.PROFESSIONAL);

  const [errors, setErrors] = useState<Record<string, string>>({});

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
      await createTenant.mutateAsync({
        name: name.trim(),
        slug: slug.trim(),
        adminEmail: adminEmail.trim(),
        adminFirstName: adminFirstName.trim() || undefined,
        adminLastName: adminLastName.trim() || undefined,
        maxSeats: Number(maxSeats),
        subscriptionTier,
      });
      // Reset form and close
      setName('');
      setSlug('');
      setAdminEmail('');
      setAdminFirstName('');
      setAdminLastName('');
      setMaxSeats(10);
      setSubscriptionTier(SubscriptionTier.PROFESSIONAL);
      setSlugCustomized(false);
      setErrors({});
      onClose();
    } catch {
      // Handled by mutation toast
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Provision New Enterprise Tenant"
      description="Initialize a dedicated isolated workspace, primary administrator, and licensed seats"
      maxWidth="lg"
    >
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
              helperText="An initial invitation and credentials setup link will be dispatched"
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
          </div>
        </div>

        {/* Modal Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <Button
            type="button"
            variant="ghost"
            onClick={onClose}
            disabled={createTenant.isPending}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={createTenant.isPending}
          >
            Provision Tenant
          </Button>
        </div>
      </form>
    </Modal>
  );
};

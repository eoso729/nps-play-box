import React, { useState } from 'react';
import { Input } from '../../../shared/components/Input';
import { Button } from '../../../shared/components/Button';
import { useToast } from '../../../shared/hooks/useToast';
import { Palette, Shield, Save, Globe } from 'lucide-react';

interface BrandingSettingsProps {
  initialLogoUrl?: string;
  initialPrimaryColor?: string;
  initialCompanyName?: string;
}

export const BrandingSettings: React.FC<BrandingSettingsProps> = ({
  initialLogoUrl = '',
  initialPrimaryColor = '#16a34a',
  initialCompanyName = '',
}) => {
  const [companyName, setCompanyName] = useState(initialCompanyName);
  const [logoUrl, setLogoUrl] = useState(initialLogoUrl);
  const [primaryColor, setPrimaryColor] = useState(initialPrimaryColor);
  const [allowSelfReg, setAllowSelfReg] = useState(false);
  const [requireEmailVerify, setRequireEmailVerify] = useState(true);
  const [sessionTimeout, setSessionTimeout] = useState('60');

  const { showToast } = useToast();

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    // Persist branding / security preferences locally or via tenant metadata
    localStorage.setItem(
      'tenant_branding_prefs',
      JSON.stringify({
        companyName,
        logoUrl,
        primaryColor,
        allowSelfReg,
        requireEmailVerify,
        sessionTimeout,
      })
    );
    showToast('Organization customization and security settings saved', 'success');
  };

  return (
    <form onSubmit={handleSave} className="space-y-6 pt-4 border-t border-[#e4e9e6]">
      <div>
        <h4 className="text-sm font-bold text-gray-900 mb-1 flex items-center gap-2">
          <Palette className="w-4 h-4 text-[#16a34a]" />
          Visual Branding & Customization
        </h4>
        <p className="text-xs text-gray-500">
          Customize portal display parameters and corporate identity for your organization members.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Display Company Name
          </label>
          <Input
            type="text"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            placeholder="e.g. Standard Chartered / Acme Corp"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">Corporate Logo URL</label>
          <Input
            type="url"
            value={logoUrl}
            onChange={(e) => setLogoUrl(e.target.value)}
            placeholder="https://assets.example.com/logo.png"
            leftIcon={<Globe className="w-4 h-4" />}
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Primary Accent Color
          </label>
          <div className="flex items-center gap-3">
            <input
              type="color"
              value={primaryColor}
              onChange={(e) => setPrimaryColor(e.target.value)}
              className="w-10 h-10 rounded-lg border border-[#e4e9e6] cursor-pointer p-0.5 bg-white"
            />
            <Input
              type="text"
              value={primaryColor}
              onChange={(e) => setPrimaryColor(e.target.value)}
              className="font-mono text-xs uppercase"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 mb-1.5">
            Session Inactivity Timeout (Minutes)
          </label>
          <Input
            type="number"
            min={5}
            max={1440}
            value={sessionTimeout}
            onChange={(e) => setSessionTimeout(e.target.value)}
            helperText="Default session lifespan before re-authentication is required."
          />
        </div>
      </div>

      <div className="pt-4 border-t border-[#e4e9e6]">
        <h4 className="text-sm font-bold text-gray-900 mb-1 flex items-center gap-2">
          <Shield className="w-4 h-4 text-purple-600" />
          Security & Access Controls
        </h4>
        <p className="text-xs text-gray-500 mb-4">
          Control access policies and invitation requirements.
        </p>

        <div className="space-y-3">
          <label className="flex items-start gap-3 p-3 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl cursor-pointer hover:bg-gray-50/50">
            <input
              type="checkbox"
              checked={allowSelfReg}
              onChange={(e) => setAllowSelfReg(e.target.checked)}
              className="w-4 h-4 text-[#16a34a] rounded border-gray-300 focus:ring-[#16a34a] mt-0.5"
            />
            <div>
              <span className="text-xs font-bold text-gray-900">
                Allow Organization Self-Registration
              </span>
              <p className="text-xs text-gray-500 mt-0.5">
                Users with matching domain emails can register directly without an explicit invitation.
              </p>
            </div>
          </label>

          <label className="flex items-start gap-3 p-3 bg-[#fbfdfc] border border-[#e4e9e6] rounded-xl cursor-pointer hover:bg-gray-50/50">
            <input
              type="checkbox"
              checked={requireEmailVerify}
              onChange={(e) => setRequireEmailVerify(e.target.checked)}
              className="w-4 h-4 text-[#16a34a] rounded border-gray-300 focus:ring-[#16a34a] mt-0.5"
            />
            <div>
              <span className="text-xs font-bold text-gray-900">
                Enforce Mandatory Email Verification
              </span>
              <p className="text-xs text-gray-500 mt-0.5">
                New accounts must verify their corporate inbox before access is granted.
              </p>
            </div>
          </label>
        </div>
      </div>

      <div className="flex items-center justify-end pt-4 border-t border-[#e4e9e6]">
        <Button type="submit" variant="primary" size="md" icon={<Save className="w-4 h-4" />}>
          Save Settings
        </Button>
      </div>
    </form>
  );
};

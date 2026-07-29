import React, { useState } from 'react';
import { NavLink, useParams } from 'react-router-dom';
import { MESSAGE_SPECS, MessageCategory, MessageTypeSpec } from '../../types/workbench';
import {
  ChevronLeft,
  ChevronRight,
  Send,
  Zap,
  FileCheck,
  CreditCard,
  ArrowRightLeft,
  Search,
} from 'lucide-react';

const CATEGORY_ICONS: Record<MessageCategory, React.FC<{ className?: string }>> = {
  'Payment Activation': Zap,
  'Payment Initiation': Send,
  'Mandate Management': FileCheck,
  'Direct Debit Operations': CreditCard,
  'Credit Transfer': ArrowRightLeft,
  'Account Services': Search,
};

const CATEGORIES: MessageCategory[] = [
  'Payment Activation',
  'Payment Initiation',
  'Mandate Management',
  'Direct Debit Operations',
  'Credit Transfer',
  'Account Services',
];

export const Sidebar: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { messageId } = useParams<{ messageId?: string }>();
  const activeId = messageId || 'pain013';

  return (
    <aside
      className={`bg-[#0b1120] border-r border-[#1e293b] flex flex-col justify-between transition-all duration-300 select-none z-20 ${
        collapsed ? 'w-16' : 'w-64'
      }`}
    >
      {/* Top Header & Search / Nav List */}
      <div className="flex-1 overflow-y-auto py-4 px-2 space-y-5 custom-scrollbar">
        {CATEGORIES.map((category) => {
          const items = MESSAGE_SPECS.filter((spec) => spec.category === category);
          if (items.length === 0) return null;
          const CategoryIcon = CATEGORY_ICONS[category];

          return (
            <div key={category} className="space-y-1">
              {!collapsed ? (
                <div className="px-3 py-1 flex items-center space-x-2 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  <CategoryIcon className="w-3 h-3 text-emerald-400" />
                  <span>{category}</span>
                </div>
              ) : (
                <div className="w-full flex justify-center py-1">
                  <CategoryIcon
                    className="w-4 h-4 text-slate-400"
                  />
                </div>
              )}

              <div className="space-y-0.5">
                {items.map((spec) => {
                  const isActive = activeId === spec.id;

                  return (
                    <NavLink
                      key={spec.id}
                      to={`/workbench/${spec.id}`}
                      className={`group relative flex items-center px-3 py-2 rounded-xl text-xs font-medium transition-all ${
                        isActive
                          ? 'bg-slate-800/90 text-emerald-400 font-semibold border-l-2 border-emerald-400 shadow-sm pl-2.5'
                          : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                      }`}
                      title={collapsed ? `${spec.name} (${spec.code})` : undefined}
                    >
                      <div className="flex items-center justify-between w-full min-w-0">
                        <div className="flex items-center space-x-2.5 truncate">
                          <span
                            className={`w-1.5 h-1.5 rounded-full ${
                              isActive ? 'bg-emerald-400 shadow-sm shadow-emerald-500' : 'bg-slate-600'
                            }`}
                          />
                          {!collapsed && (
                            <span className="truncate tracking-tight">{spec.name}</span>
                          )}
                        </div>

                        {!collapsed && (
                          <span
                            className={`px-1.5 py-0.5 rounded text-[10px] font-mono shrink-0 ${
                              isActive
                                ? 'bg-emerald-500/20 text-emerald-300 font-semibold border border-emerald-500/30'
                                : 'bg-slate-800/80 text-slate-400 border border-slate-700/50'
                            }`}
                          >
                            {spec.code.split('.').slice(0, 2).join('.')}
                          </span>
                        )}
                      </div>
                    </NavLink>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>

      {/* Footer */}
      <div className="p-3 border-t border-[#1e293b] bg-[#090d16] flex items-center justify-between">
        {!collapsed && (
          <div className="text-[10px] text-slate-400 font-mono">
            <div>© 2026 NIBSS Sandbox</div>
            <div className="text-emerald-500">ISO 20022 Engine</div>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition-colors ml-auto"
          title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          {collapsed ? (
            <ChevronRight className="w-4 h-4" />
          ) : (
            <ChevronLeft className="w-4 h-4" />
          )}
        </button>
      </div>
    </aside>
  );
};

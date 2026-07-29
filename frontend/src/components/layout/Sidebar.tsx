import React, { useState } from 'react';
import { SIDEBAR_GROUPS } from '../workbench/messageConfigs';

interface SidebarProps {
  activeMessage: string;
  onSelect: (key: string) => void;
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeMessage,
  onSelect,
  collapsed,
  onToggleCollapse,
}) => {
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>(
    Object.fromEntries(SIDEBAR_GROUPS.map(g => [g.label, true]))
  );

  const toggleGroup = (label: string) => {
    setExpandedGroups(prev => ({ ...prev, [label]: !prev[label] }));
  };

  if (collapsed) {
    return (
      <div
        className="flex-shrink-0 flex flex-col items-center py-4 cursor-pointer"
        style={{ width: 48, background: 'linear-gradient(180deg, #0b2818, #081f14)' }}
        onClick={onToggleCollapse}
      >
        <div
          className="w-7 h-7 rounded-lg flex items-center justify-center text-white font-bold text-[10px] mb-4"
          style={{ background: 'linear-gradient(135deg, #22a05a, #15803d)' }}
        >
          NPS
        </div>
        <span className="text-[#9fd8b3] text-[11px] rotate-90 mt-8 whitespace-nowrap">Expand</span>
      </div>
    );
  }

  return (
    <div
      className="flex-shrink-0 flex flex-col overflow-y-auto"
      style={{ width: 250, background: 'linear-gradient(180deg, #0b2818, #081f14)', color: '#dff3e6' }}
    >
      <div className="flex-1 overflow-y-auto">
        {SIDEBAR_GROUPS.map(group => (
          <div key={group.label}>
            <div
              className="flex items-center justify-between mx-1.5 mt-3.5 mb-2 px-2 cursor-pointer"
              onClick={() => toggleGroup(group.label)}
            >
              <span
                className="text-[10.5px] font-bold tracking-[0.6px] uppercase"
                style={{ color: '#7fb894', letterSpacing: '0.6px' }}
              >
                {group.label}
              </span>
              <span className="text-[#7fb894] text-[10px]">
                {expandedGroups[group.label] ? '▾' : '▸'}
              </span>
            </div>

            {expandedGroups[group.label] && group.items.map(item => (
              <div
                key={item.key}
                onClick={() => onSelect(item.key)}
                className="flex items-center gap-2.5 mx-1.5 px-2.5 py-2 rounded-[7px] mb-0.5 cursor-pointer transition-all"
                style={{
                  fontFamily: "'JetBrains Mono', 'Courier New', monospace",
                  fontSize: 12,
                  color: activeMessage === item.key ? '#fff' : '#c9e8d4',
                  background: activeMessage === item.key ? '#22a05a' : 'transparent',
                  boxShadow: activeMessage === item.key ? '0 2px 8px rgba(34,160,90,0.35)' : 'none',
                  fontWeight: activeMessage === item.key ? 600 : 400,
                }}
                onMouseEnter={e => {
                  if (activeMessage !== item.key) {
                    (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.05)';
                    (e.currentTarget as HTMLElement).style.color = '#fff';
                  }
                }}
                onMouseLeave={e => {
                  if (activeMessage !== item.key) {
                    (e.currentTarget as HTMLElement).style.background = 'transparent';
                    (e.currentTarget as HTMLElement).style.color = '#c9e8d4';
                  }
                }}
              >
                <span
                  className="w-1.5 h-1.5 rounded-[2px] flex-shrink-0"
                  style={{
                    background: activeMessage === item.key ? '#fff' : '#22a05a',
                    opacity: 0.6,
                  }}
                />
                {item.isoCode}
              </div>
            ))}
          </div>
        ))}
      </div>

      <div
        className="text-[10.5px] px-4 py-4 border-t"
        style={{ color: '#5d8a6f', borderColor: 'rgba(255,255,255,0.08)' }}
      >
        NPS Play Box Engine &copy; 2026 &middot; v2.4.0
      </div>

      <div
        className="flex items-center gap-1.5 px-4 py-3 cursor-pointer border-t text-[12px]"
        style={{ color: '#9fd8b3', borderColor: 'rgba(255,255,255,0.08)' }}
        onClick={onToggleCollapse}
      >
        <span className="text-[11px]">&#171;</span> Collapse
      </div>
    </div>
  );
};

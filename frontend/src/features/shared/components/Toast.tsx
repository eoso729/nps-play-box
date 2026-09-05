import React from 'react';
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastItem {
  id: string;
  message: string;
  type: ToastType;
}

interface ToastProps {
  toasts: ToastItem[];
  onClose: (id: string) => void;
}

export const Toast: React.FC<ToastProps> = ({ toasts, onClose }) => {
  if (toasts.length === 0) return null;

  const getIcon = (type: ToastType) => {
    switch (type) {
      case 'success':
        return <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />;
      case 'error':
        return <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />;
      default:
        return <Info className="w-5 h-5 text-blue-600 flex-shrink-0" />;
    }
  };

  const getStyles = (type: ToastType) => {
    switch (type) {
      case 'success':
        return 'bg-white border-emerald-200 text-emerald-950 shadow-emerald-500/10';
      case 'error':
        return 'bg-white border-red-200 text-red-950 shadow-red-500/10';
      case 'warning':
        return 'bg-white border-amber-200 text-amber-950 shadow-amber-500/10';
      default:
        return 'bg-white border-blue-200 text-blue-950 shadow-blue-500/10';
    }
  };

  return (
    <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`pointer-events-auto flex items-start gap-3 p-3.5 rounded-xl border shadow-lg transition-all animate-in fade-in slide-in-from-top-2 duration-200 ${getStyles(
            toast.type
          )}`}
        >
          {getIcon(toast.type)}
          <div className="flex-1 text-xs font-medium leading-relaxed">{toast.message}</div>
          <button
            type="button"
            onClick={() => onClose(toast.id)}
            className="text-gray-400 hover:text-gray-600 p-0.5 rounded transition-colors"
            aria-label="Dismiss"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      ))}
    </div>
  );
};

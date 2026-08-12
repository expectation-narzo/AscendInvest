import React, { useState, useEffect } from 'react';
import { Search, Command, Zap, ArrowRight, Shield, Wallet, Users, Layout } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const CommandPalette: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsOpen(prev => !prev);
      }
      if (e.key === 'Escape') setIsOpen(false);
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  if (!isOpen) return null;

  const actions = [
    { label: 'Go to Dashboard', icon: Layout, path: '/' },
    { label: 'Deposit USDT (BEP20)', icon: Zap, path: '/deposits' },
    { label: 'Withdrawal Portal', icon: Wallet, path: '/withdraw' },
    { label: 'Network Intelligence', icon: Users, path: '/team' },
    { label: 'Security Audit', icon: Shield, path: '/support' },
  ];

  return (
    <div className="fixed inset-0 z-[200] flex items-start justify-center pt-[15vh] px-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-md" onClick={() => setIsOpen(false)} />

      <div className="bg-white w-full max-w-[600px] rounded-[32px] shadow-2xl border border-[#F1F5F9] overflow-hidden relative z-10 animate-in zoom-in-95 duration-200">
        <div className="p-6 border-b border-[#F1F5F9] flex items-center gap-4">
           <Search size={22} className="text-primary" />
           <input
            autoFocus
            placeholder="Type a command or search..."
            className="flex-1 bg-transparent border-none outline-none text-[18px] font-bold text-[#1E293B] placeholder:text-[#CBD5E1]"
           />
           <div className="px-3 py-1 bg-[#F8FAFC] border border-[#E2E8F0] rounded-lg flex items-center gap-1">
              <span className="text-[10px] font-black text-[#94A3B8]">ESC</span>
           </div>
        </div>

        <div className="p-4 max-h-[400px] overflow-y-auto custom-scrollbar">
           <p className="px-4 py-2 text-[10px] font-black text-[#94A3B8] uppercase tracking-[0.2em]">Quick Navigation</p>
           <div className="mt-2 space-y-1">
              {actions.map((action, i) => (
                <div
                  key={i}
                  onClick={() => { navigate(action.path); setIsOpen(false); }}
                  className="flex items-center justify-between p-4 rounded-2xl hover:bg-[#F5F3FF] group cursor-pointer transition-all"
                >
                  <div className="flex items-center gap-4">
                     <div className="w-10 h-10 bg-[#F8FAFC] rounded-xl flex items-center justify-center text-[#64748B] group-hover:bg-white group-hover:text-primary transition-all shadow-sm">
                        <action.icon size={20} />
                     </div>
                     <span className="text-[15px] font-[700] text-[#1E293B]">{action.label}</span>
                  </div>
                  <ArrowRight size={18} className="text-[#CBD5E1] group-hover:text-primary transition-all transform group-hover:translate-x-1" />
                </div>
              ))}
           </div>
        </div>

        <div className="bg-[#F8FAFC] p-4 border-t border-[#F1F5F9] flex justify-between items-center px-8">
           <div className="flex items-center gap-4">
              <div className="flex items-center gap-1.5">
                 <kbd className="px-1.5 py-0.5 bg-white border border-gray-200 rounded-md text-[10px] font-bold shadow-sm">↑↓</kbd>
                 <span className="text-[10px] font-bold text-[#94A3B8] uppercase">Navigate</span>
              </div>
              <div className="flex items-center gap-1.5">
                 <kbd className="px-1.5 py-0.5 bg-white border border-gray-200 rounded-md text-[10px] font-bold shadow-sm">↵</kbd>
                 <span className="text-[10px] font-bold text-[#94A3B8] uppercase">Select</span>
              </div>
           </div>
           <p className="text-[10px] font-black text-primary uppercase tracking-widest">Ascend AI Core</p>
        </div>
      </div>
    </div>
  );
};

export default CommandPalette;

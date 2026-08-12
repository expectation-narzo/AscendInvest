import React, { useState, useEffect } from 'react';
import { X, Cpu, ShieldCheck, Activity, Zap, CheckCircle2 } from 'lucide-react';

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

const NodeRecalibrationModal: React.FC<Props> = ({ isOpen, onClose }) => {
  const [step, setStep] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setStep(0);
      setIsFinished(false);
      const timers = [
        setTimeout(() => setStep(1), 1500),
        setTimeout(() => setStep(2), 3500),
        setTimeout(() => setStep(3), 5500),
        setTimeout(() => setIsFinished(true), 7000),
      ];
      return () => timers.forEach(clearTimeout);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[250] flex items-center justify-center px-4">
      <div className="absolute inset-0 bg-[#0F172A]/90 backdrop-blur-xl" />

      <div className="bg-white w-full max-w-[500px] rounded-[40px] shadow-2xl overflow-hidden relative z-10 p-10 flex flex-col items-center text-center">
        {!isFinished ? (
          <>
            <div className="w-24 h-24 bg-primary/10 rounded-full flex items-center justify-center relative mb-8">
               <div className="absolute inset-0 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
               <Cpu size={40} className="text-primary" />
            </div>

            <h3 className="text-[22px] font-black text-[#1E293B] uppercase tracking-tight">Recalibrating Alpha Nodes</h3>
            <p className="text-[14px] text-[#64748B] font-medium mt-2">Optimizing regional arbitrage density for max yield.</p>

            <div className="w-full mt-10 space-y-4">
               {[
                 { id: 1, label: 'Analyzing liquidity pools...', active: step >= 0 },
                 { id: 2, label: 'Optimizing APAC routing...', active: step >= 1 },
                 { id: 3, label: 'Securing multi-sig link...', active: step >= 2 },
                 { id: 4, label: 'Finalizing protocol sync...', active: step >= 3 },
               ].map((s) => (
                 <div key={s.id} className={`flex items-center gap-4 p-4 rounded-2xl border transition-all ${s.active ? 'bg-[#F8FAFC] border-primary/20' : 'bg-transparent border-gray-100 opacity-30'}`}>
                    {s.active ? <Activity size={18} className="text-secondary animate-pulse" /> : <div className="w-[18px] h-[18px] rounded-full border-2 border-gray-200" />}
                    <span className={`text-[13px] font-black uppercase tracking-widest ${s.active ? 'text-[#1E293B]' : 'text-[#64748B]'}`}>{s.label}</span>
                 </div>
               ))}
            </div>
          </>
        ) : (
          <div className="animate-in zoom-in-95 duration-500 flex flex-col items-center">
             <div className="w-24 h-24 bg-secondary/20 rounded-full flex items-center justify-center text-secondary mb-8 shadow-xl shadow-secondary/20">
                <CheckCircle2 size={50} strokeWidth={3} />
             </div>
             <h3 className="text-[26px] font-black text-[#1E293B] uppercase tracking-tight">System Optimized</h3>
             <p className="text-[14px] text-[#64748B] font-medium mt-2">All nodes are now performing at peak institutional levels.</p>
             <div className="mt-8 bg-secondary/10 px-6 py-2 rounded-full border border-secondary/20">
                <span className="text-[11px] font-black text-secondary uppercase tracking-[0.2em]">Yield Performance: +12.5% Optimal</span>
             </div>
             <button
              onClick={onClose}
              className="mt-12 w-full h-[64px] bg-[#1E293B] text-white font-black rounded-[20px] shadow-2xl transition-all hover:bg-primary"
             >
                RETURN TO TERMINAL
             </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default NodeRecalibrationModal;

import React, { useState } from 'react';
import { Headphones, MessageCircle, X, Send } from 'lucide-react';

const FloatingSupport: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="fixed bottom-12 right-6 z-[100] flex flex-col items-end">
      {/* Support Chat Box */}
      {isOpen && (
        <div className="mb-4 w-[320px] bg-white rounded-[24px] shadow-2xl border border-[#F1F5F9] overflow-hidden animate-in slide-in-from-bottom-5 duration-300">
          <div className="bg-[#6C5CE7] p-5 text-white flex justify-between items-center">
             <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                   <Headphones size={20} />
                </div>
                <div>
                   <p className="text-[14px] font-black">Portal Support</p>
                   <p className="text-[10px] opacity-80 uppercase tracking-widest font-bold">Always Online</p>
                </div>
             </div>
             <button onClick={() => setIsOpen(false)} className="p-1 hover:bg-white/10 rounded-lg">
                <X size={20} />
             </button>
          </div>
          <div className="p-6">
             <p className="text-[13px] text-[#64748B] font-medium leading-relaxed">
               Welcome to Ascend Invest support. How can we help you coordinate your liquidity today?
             </p>
             <button className="w-full mt-6 bg-[#0284C7] text-white py-3 rounded-xl font-black text-[13px] flex items-center justify-center gap-2 shadow-lg shadow-sky-200">
                <MessageCircle size={18} /> OPEN TELEGRAM CHAT
             </button>
          </div>
          <div className="bg-[#F8FAFC] p-4 border-t border-gray-100">
             <p className="text-[10px] text-center text-[#94A3B8] font-bold uppercase tracking-widest">End-to-End Encrypted</p>
          </div>
        </div>
      )}

      {/* Floating Action Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`w-14 h-14 rounded-full flex items-center justify-center shadow-2xl transition-all ${isOpen ? 'bg-[#1E293B] rotate-90' : 'bg-[#6C5CE7] hover:scale-110 active:scale-95 shadow-[#6C5CE7]/30'}`}
      >
        {isOpen ? <X className="text-white" size={24} /> : <Headphones className="text-white" size={24} />}
      </button>
    </div>
  );
};

export default FloatingSupport;

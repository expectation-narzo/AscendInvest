import React, { useState } from 'react';
import { Calculator, ArrowRight, DollarSign } from 'lucide-react';

const InvestmentCalculator: React.FC = () => {
  const [amount, setAmount] = useState(1000);
  const [days, setDays] = useState(30);

  const profit = (amount * 0.025 * days).toFixed(2);
  const total = (amount + parseFloat(profit)).toFixed(2);

  return (
    <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-6 shadow-sm overflow-hidden relative">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-8 h-8 bg-orange-50 rounded-lg flex items-center justify-center text-orange-500">
           <Calculator size={18} />
        </div>
        <h3 className="text-[16px] font-[800] text-[#1E293B]">Yield Estimator</h3>
      </div>

      <div className="space-y-5">
         <div className="space-y-2">
            <div className="flex justify-between text-[11px] font-black uppercase text-[#94A3B8]">
               <span>Capital Amount</span>
               <span className="text-[#1E293B]">${amount}</span>
            </div>
            <input
              type="range" min="100" max="50000" step="100" value={amount}
              onChange={(e) => setAmount(parseInt(e.target.value))}
              className="w-full h-1.5 bg-[#F1F5F9] rounded-lg appearance-none cursor-pointer accent-[#6C5CE7]"
            />
         </div>

         <div className="space-y-2">
            <div className="flex justify-between text-[11px] font-black uppercase text-[#94A3B8]">
               <span>Cycle Duration</span>
               <span className="text-[#1E293B]">{days} Days</span>
            </div>
            <input
              type="range" min="7" max="365" step="1" value={days}
              onChange={(e) => setDays(parseInt(e.target.value))}
              className="w-full h-1.5 bg-[#F1F5F9] rounded-lg appearance-none cursor-pointer accent-[#6C5CE7]"
            />
         </div>

         <div className="mt-8 grid grid-cols-2 gap-4">
            <div className="bg-[#F8FAFC] p-4 rounded-2xl border border-[#F1F5F9]">
               <p className="text-[9px] font-black text-[#94A3B8] uppercase">Est. Profit</p>
               <p className="text-[16px] font-black text-[#28C76F] mt-1 tracking-tight">+${profit}</p>
            </div>
            <div className="bg-[#F1F0FF] p-4 rounded-2xl border border-transparent">
               <p className="text-[9px] font-black text-[#6C5CE7] uppercase">Final Balance</p>
               <p className="text-[16px] font-black text-[#1E293B] mt-1 tracking-tight">${total}</p>
            </div>
         </div>

         <button className="w-full mt-2 h-[48px] bg-[#1E293B] text-white text-[12px] font-black uppercase tracking-widest rounded-xl hover:bg-[#6C5CE7] transition-all flex items-center justify-center gap-2 group">
            Optimize Plan <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
         </button>
      </div>
    </div>
  );
};

export default InvestmentCalculator;

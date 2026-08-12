import React from 'react';
import { Activity, Cpu, ShieldCheck } from 'lucide-react';

const NodeStatus: React.FC = () => {
  return (
    <div className="mt-6 p-4 mx-4 bg-[#F8FAFC] rounded-[16px] border border-[#F1F5F9]">
      <div className="flex items-center justify-between mb-3">
         <p className="text-[10px] font-black text-[#94A3B8] uppercase tracking-[0.1em]">Protocol Node Health</p>
         <div className="w-1.5 h-1.5 rounded-full bg-[#22C55E] animate-ping"></div>
      </div>

      <div className="space-y-2">
         {[
           { label: 'Latency', value: '14ms', icon: Activity, color: 'text-primary' },
           { label: 'Node Load', value: '24%', icon: Cpu, color: 'text-orange-500' },
           { label: 'Encryption', value: 'AES-256', icon: ShieldCheck, color: 'text-secondary' }
         ].map((item, i) => (
           <div key={i} className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                 <item.icon size={12} className={item.color} />
                 <span className="text-[11px] font-bold text-[#64748B]">{item.label}</span>
              </div>
              <span className="text-[11px] font-black text-[#1E293B]">{item.value}</span>
           </div>
         ))}
      </div>
    </div>
  );
};

export default NodeStatus;

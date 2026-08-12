import React from 'react';
import { Target, ChevronRight } from 'lucide-react';

const MilestoneTracker: React.FC = () => {
  return (
    <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-8 shadow-sm">
      <div className="flex justify-between items-center mb-8">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center text-primary">
            <Target size={20} />
          </div>
          <div>
            <h3 className="text-[16px] font-[900] text-[#1E293B]">Growth Milestones</h3>
            <p className="text-[12px] text-[#64748B] font-bold">Protocol Tier Upgrade</p>
          </div>
        </div>
        <button className="text-[11px] font-black text-primary uppercase tracking-widest hover:underline">Full Rewards</button>
      </div>

      <div className="space-y-8">
        <div className="relative">
           <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-[#F1F5F9] -z-10"></div>
           <div className="space-y-6">
              {[
                { label: 'Starter Node', v: 'Completed', c: 'text-secondary', b: 'bg-secondary' },
                { label: 'Network Pro (50 Nodes)', v: '32 / 50 Nodes', c: 'text-primary', b: 'bg-primary' },
                { label: 'Alpha Whale (200 Nodes)', v: 'Locked', c: 'text-[#94A3B8]', b: 'bg-[#CBD5E1]' }
              ].map((m, i) => (
                <div key={i} className="flex items-center gap-6">
                   <div className={`w-8 h-8 rounded-full border-4 border-white shadow-md flex items-center justify-center text-white ${m.b}`}>
                      <span className="text-[10px] font-black">{i + 1}</span>
                   </div>
                   <div className="flex-1">
                      <p className={`text-[13px] font-[800] ${m.c}`}>{m.label}</p>
                      <p className="text-[11px] text-[#64748B] font-medium">{m.v}</p>
                   </div>
                </div>
              ))}
           </div>
        </div>

        <div className="bg-[#F8FAFC] p-4 rounded-2xl border border-[#F1F5F9] flex items-center justify-between group cursor-pointer hover:bg-white transition-all">
           <div className="flex items-center gap-3">
              <div className="w-2 h-2 rounded-full bg-secondary animate-pulse"></div>
              <span className="text-[12px] font-[800] text-[#1E293B]">Next Reward: $500 Bonus</span>
           </div>
           <ChevronRight size={16} className="text-[#CBD5E1] group-hover:text-primary transition-all" />
        </div>
      </div>
    </div>
  );
};

export default MilestoneTracker;

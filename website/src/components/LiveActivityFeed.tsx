import React from 'react';
import { Activity, ArrowUpRight, ArrowDownRight } from 'lucide-react';

const LiveActivityFeed: React.FC = () => {
  const feed = [
    { type: 'trade', msg: 'BTC/USDT Arbitrage executed', node: 'Node-HK42', time: '2s ago', up: true },
    { type: 'yield', msg: 'Daily profit cycle complete', node: 'System', time: '1m ago', up: true },
    { type: 'network', msg: 'New node initialized', node: 'Node-NY01', time: '5m ago', up: true },
  ];

  return (
    <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-6 shadow-sm">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-secondary/10 rounded-lg flex items-center justify-center text-secondary">
             <Activity size={18} />
          </div>
          <h3 className="text-[16px] font-[800] text-[#1E293B]">Live Network Activity</h3>
        </div>
        <span className="text-[10px] font-black text-[#94A3B8] uppercase tracking-widest animate-pulse">Scanning...</span>
      </div>

      <div className="space-y-4">
        {feed.map((item, i) => (
          <div key={i} className="flex items-start gap-4 p-3 rounded-xl hover:bg-[#F8FAFC] transition-colors cursor-default">
             <div className={`mt-1 w-2 h-2 rounded-full ${item.up ? 'bg-secondary' : 'bg-error'}`}></div>
             <div className="flex-1">
                <p className="text-[13px] font-bold text-[#1E293B] leading-snug">{item.msg}</p>
                <div className="flex items-center gap-3 mt-1">
                   <span className="text-[10px] font-black text-[#94A3B8] uppercase">{item.node}</span>
                   <span className="text-[10px] font-medium text-[#CBD5E1]">•</span>
                   <span className="text-[10px] font-bold text-[#CBD5E1] uppercase">{item.time}</span>
                </div>
             </div>
             {item.up ? <ArrowUpRight size={14} className="text-secondary" /> : <ArrowDownRight size={14} className="text-error" />}
          </div>
        ))}
      </div>

      <button className="w-full mt-6 py-2 text-[11px] font-black text-[#94A3B8] uppercase tracking-[0.2em] border border-dashed border-[#E2E8F0] rounded-xl hover:text-primary hover:border-primary transition-all">
         Enter Command Center
      </button>
    </div>
  );
};

export default LiveActivityFeed;

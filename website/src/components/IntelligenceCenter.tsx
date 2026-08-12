import React, { useEffect, useState } from 'react';
import { Cpu, Globe, Shield, Terminal as TerminalIcon, Wifi } from 'lucide-react';

const IntelligenceCenter: React.FC = () => {
  const [stats, setStats] = useState({
    resolution: `${window.screen.width}x${window.screen.height}`,
    platform: navigator.platform,
    latency: '0ms',
    packets: '0/0',
    secureLink: 'Active'
  });

  useEffect(() => {
    const itv = setInterval(() => {
      setStats(prev => ({
        ...prev,
        latency: Math.floor(Math.random() * 20 + 10) + 'ms',
        packets: Math.floor(Math.random() * 1000 + 5000) + '/' + Math.floor(Math.random() * 5)
      }));
    }, 2000);
    return () => clearInterval(itv);
  }, []);

  return (
    <div className="bg-[#0F172A] rounded-[24px] p-6 text-white shadow-2xl relative overflow-hidden group">
      <div className="absolute top-0 right-0 w-32 h-32 bg-primary/10 rounded-full blur-3xl -mr-16 -mt-16"></div>

      <div className="flex items-center gap-3 mb-6 relative z-10">
         <div className="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center text-primary">
            <TerminalIcon size={18} />
         </div>
         <h3 className="text-[14px] font-black uppercase tracking-[0.2em]">Intelligence Node Stats</h3>
      </div>

      <div className="grid grid-cols-2 gap-4 relative z-10">
         <div className="space-y-4">
            <div>
               <p className="text-[9px] font-black text-white/40 uppercase tracking-widest">Interface Resolution</p>
               <p className="text-[13px] font-bold terminal-text text-secondary mt-1">{stats.resolution}</p>
            </div>
            <div>
               <p className="text-[9px] font-black text-white/40 uppercase tracking-widest">Gateway Latency</p>
               <p className="text-[13px] font-bold terminal-text text-[#28C76F] mt-1 flex items-center gap-2">
                  <Wifi size={14} /> {stats.latency}
               </p>
            </div>
         </div>
         <div className="space-y-4">
            <div>
               <p className="text-[9px] font-black text-white/40 uppercase tracking-widest">Client Platform</p>
               <p className="text-[13px] font-bold terminal-text text-white mt-1 truncate">{stats.platform}</p>
            </div>
            <div>
               <p className="text-[9px] font-black text-white/40 uppercase tracking-widest">Security Layer</p>
               <div className="flex items-center gap-2 mt-1">
                  <Shield size={14} className="text-primary" />
                  <p className="text-[11px] font-black text-white uppercase bg-primary/20 px-2 py-0.5 rounded border border-primary/20 tracking-tighter">GCM-256</p>
               </div>
            </div>
         </div>
      </div>

      <div className="mt-6 pt-6 border-t border-white/5 flex items-center justify-between relative z-10">
         <div className="flex items-center gap-2">
            <Cpu size={14} className="text-secondary opacity-50" />
            <span className="text-[10px] font-black text-white/30 uppercase tracking-[0.1em]">Packets Scanned: {stats.packets}</span>
         </div>
         <div className="w-1.5 h-1.5 rounded-full bg-secondary animate-pulse shadow-[0_0_8px_rgba(40,199,111,0.5)]"></div>
      </div>
    </div>
  );
};

export default IntelligenceCenter;

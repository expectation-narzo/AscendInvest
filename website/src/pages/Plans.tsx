import React, { useEffect, useState } from 'react';
import {
  Zap,
  ShieldCheck,
  BarChart4,
  ArrowRight,
  Cpu,
  TrendingUp,
  Clock,
  Layers,
  Activity,
  ChevronRight
} from 'lucide-react';
import { api } from '../services/api';

const Plans: React.FC = () => {
  const [plans, setPlans] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const data = await api.get('/plans');
        // Match mobile app tier logic
        const enhancedPlans = data.map((p: any) => ({
          ...p,
          tier: p.min_amount >= 5000 ? 'Premium' : p.min_amount >= 1000 ? 'Standard' : 'Beginner',
          color: p.min_amount >= 5000 ? '#FF9F43' : p.min_amount >= 1000 ? '#28C76F' : '#6C5CE7',
          desc: p.min_amount >= 5000 ? 'Institutional grade portfolio management.' :
                p.min_amount >= 1000 ? 'Optimized compounding for serious growth.' :
                'Ideal for those exploring the alpha tier.'
        }));
        setPlans(enhancedPlans);
      } catch (err) {
        console.error('Failed to sync node protocols:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchPlans();
  }, []);

  if (loading) return (
    <div className="flex flex-col items-center justify-center min-h-[500px]">
      <div className="w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
      <p className="mt-4 text-[10px] font-black text-primary uppercase tracking-[0.3em]">Syncing Alpha Nodes...</p>
    </div>
  );

  return (
    <div className="p-6 md:p-10 space-y-10 font-sans max-w-[1400px] mx-auto min-h-screen">

      {/* 1. Institutional Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-4 md:px-0">
        <div className="space-y-2">
           <p className="text-primary text-[12px] font-[800] uppercase tracking-[0.25em]">Alpha Opportunities</p>
           <h1 className="text-[34px] md:text-[42px] font-[1000] text-text-dark tracking-tighter leading-none italic uppercase">
             Market <span className="text-primary not-italic">Nodes</span>
           </h1>
           <p className="text-[15px] text-text-gray font-semibold">Select a high-fidelity compounding protocol to automate your wealth growth.</p>
        </div>
        <div className="flex items-center gap-4">
           <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex items-center gap-4 group hover:shadow-md transition-all">
              <div className="w-10 h-10 bg-orange-50 rounded-xl flex items-center justify-center text-[#FF9F43] group-hover:rotate-12 transition-transform">
                 <Zap size={20} fill="#FF9F43" />
              </div>
              <div>
                <p className="text-[10px] font-[800] text-[#94A3B8] uppercase tracking-wider">Protocol Status</p>
                <p className="text-[13px] font-[800] text-[#FF9F43] uppercase flex items-center gap-1.5">
                   <div className="w-1.5 h-1.5 rounded-full bg-[#FF9F43] animate-pulse"></div> High Performance
                </p>
              </div>
           </div>
        </div>
      </div>

      {/* 2. Alpha Node Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8 px-4 md:px-0">
        {plans.map((plan, i) => (
          <div key={i} className="group bg-white rounded-[40px] border border-[#E2E8F0] p-8 md:p-10 shadow-sm hover:shadow-2xl hover:shadow-primary/10 transition-all flex flex-col relative overflow-hidden h-full">

            {/* Background Architecture */}
            <div className={`absolute top-0 right-0 w-40 h-40 opacity-[0.03] group-hover:opacity-[0.1] transition-opacity duration-1000 rounded-full -mr-20 -mt-20`} style={{ backgroundColor: plan.color }}></div>
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 opacity-[0.01] pointer-events-none group-hover:scale-110 transition-transform duration-1000">
               <Cpu size={300} />
            </div>

            <div className="flex justify-between items-start mb-10 relative z-10">
              <div>
                <h3 className="text-[24px] font-[1000] text-text-dark leading-tight tracking-tight group-hover:text-primary transition-colors">{plan.name}</h3>
                <div className="flex items-center gap-2 mt-1.5">
                   <span className="text-[10px] font-black uppercase tracking-[0.2em]" style={{ color: plan.color }}>{plan.tier} NODE PROTOCOL</span>
                </div>
              </div>
              <div className="w-14 h-14 rounded-2xl flex items-center justify-center shadow-xl transition-all duration-500 group-hover:rotate-[360deg]" style={{ backgroundColor: `${plan.color}15`, color: plan.color }}>
                 <BarChart4 size={28} strokeWidth={2.5} />
              </div>
            </div>

            <p className="text-[15px] text-text-gray font-medium leading-relaxed mb-10 relative z-10">{plan.desc}</p>

            <div className="space-y-8 mb-12 relative z-10 flex-1">
               <div className="flex justify-between items-end">
                  <div>
                    <p className="text-[11px] font-black text-text-light uppercase tracking-[0.2em]">Expected Return</p>
                    <div className="flex items-baseline gap-1 mt-2">
                       <p className="text-[36px] font-[1000] text-text-dark tracking-tighter leading-none">{(plan.daily_interest * plan.duration_days).toFixed(0)}%</p>
                       <span className="text-[14px] font-black text-secondary tracking-tighter uppercase italic">Yield</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-[14px] font-[900] text-secondary tracking-tight">+{plan.daily_interest}%</p>
                    <p className="text-[10px] font-black text-text-light uppercase tracking-widest leading-none mt-1">Daily Cap</p>
                  </div>
               </div>

               {/* Institutional Metric Grid */}
               <div className="grid grid-cols-3 gap-0 p-6 bg-[#F8FAFC] rounded-[28px] border border-[#F1F5F9] group-hover:border-primary/20 transition-all shadow-inner">
                  <div className="flex flex-col items-center border-r border-gray-100">
                    <p className="text-[10px] font-black text-text-light uppercase tracking-widest">MIN</p>
                    <p className="text-[16px] font-[1000] text-text-dark mt-1.5 tracking-tight">${plan.min_amount.toLocaleString()}</p>
                  </div>
                  <div className="flex flex-col items-center">
                    <p className="text-[10px] font-black text-text-light uppercase tracking-widest">CYCLE</p>
                    <p className="text-[16px] font-[1000] text-text-dark mt-1.5 tracking-tight">{plan.duration_days}D</p>
                  </div>
                  <div className="flex flex-col items-center border-l border-gray-100">
                    <p className="text-[10px] font-black text-text-light uppercase tracking-widest">MAX</p>
                    <p className="text-[16px] font-[1000] text-text-dark mt-1.5 tracking-tight">${plan.max_amount >= 1000000 ? '1M+' : plan.max_amount.toLocaleString()}</p>
                  </div>
               </div>
            </div>

            <div className="mt-auto space-y-5">
              <button className="w-full h-[72px] bg-[#1E293B] hover:bg-primary text-white font-[1000] text-[17px] rounded-[24px] shadow-2xl transition-all active:scale-[0.98] flex items-center justify-center gap-4 group/btn relative overflow-hidden uppercase tracking-widest">
                <div className="absolute inset-0 bg-white/10 translate-x-[-100%] group-hover/btn:translate-x-[100%] transition-transform duration-1000"></div>
                <span>Initialize Node</span>
                <ArrowRight size={22} className="group-hover/btn:translate-x-1.5 transition-transform" />
              </button>
              <div className="flex items-center justify-center gap-3">
                 <ShieldCheck size={16} className="text-secondary" />
                 <span className="text-[11px] font-black text-text-light uppercase tracking-[0.25em]">SLA BACKED GUARANTEE</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 3. Technology Insight Panel */}
      <div className="px-4 md:px-0 mt-16">
        <div className="bg-[#0F172A] rounded-[48px] p-10 md:p-14 text-white relative overflow-hidden shadow-2xl group border border-white/5">
           <div className="absolute top-0 right-0 w-96 h-96 bg-primary/10 rounded-full blur-[120px] -mr-40 -mt-40 group-hover:bg-primary/20 transition-all duration-1000"></div>

           <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
              <div className="lg:col-span-8 space-y-8">
                 <div className="flex items-center gap-5">
                    <div className="w-16 h-16 bg-white/5 rounded-3xl flex items-center justify-center border border-white/10 shadow-inner group-hover:scale-110 transition-transform duration-500">
                       <Activity size={32} className="text-primary" />
                    </div>
                    <div>
                       <h3 className="text-[26px] font-[1000] tracking-tighter uppercase italic">Alpha Compounding <span className="text-primary not-italic">v4.2</span></h3>
                       <p className="text-[15px] text-white/40 font-bold uppercase tracking-widest mt-1">Automated Arbitrage Management</p>
                    </div>
                 </div>
                 <p className="text-[17px] text-white/70 leading-relaxed font-medium max-w-2xl">
                    Our nodes utilize sub-millisecond execution to identify market inefficiencies across <span className="text-white font-black underline decoration-primary decoration-2 underline-offset-8">24 global mainnets</span>.
                    By deploying capital into segregated nodes, you benefit from a risk-isolated environment with non-custodial yield settlement.
                 </p>
                 <div className="flex flex-wrap gap-10">
                    {[
                      { l: 'Ledger Type', v: 'SQL-Distributed' },
                      { l: 'Network Reach', v: 'Global Edge' },
                      { l: 'Auth Protocol', v: 'Multi-Sig 3/5' }
                    ].map((item, i) => (
                      <div key={i} className="space-y-1.5">
                         <p className="text-[10px] text-white/30 uppercase font-black tracking-[0.2em]">{item.l}</p>
                         <p className="text-[15px] font-black text-secondary uppercase tracking-tight">{item.v}</p>
                      </div>
                    ))}
                 </div>
              </div>

              <div className="lg:col-span-4 hidden lg:block">
                 <div className="bg-white/5 border border-white/10 rounded-[40px] p-8 space-y-6 backdrop-blur-sm">
                    <div className="flex items-center justify-between">
                       <span className="text-[11px] font-black text-white/40 uppercase tracking-widest">Real-time Performance</span>
                       <TrendingUp size={16} className="text-secondary" />
                    </div>
                    <div className="space-y-4">
                       {[70, 85, 45, 90, 60].map((h, i) => (
                         <div key={i} className="space-y-1.5">
                            <div className="w-full bg-white/5 h-1.5 rounded-full overflow-hidden">
                               <div className="h-full bg-primary rounded-full transition-all duration-1000 delay-500" style={{ width: `${h}%` }}></div>
                            </div>
                         </div>
                       ))}
                    </div>
                    <p className="text-[10px] text-center text-white/20 font-black uppercase tracking-[0.3em]">Network Integrity: 100%</p>
                 </div>
              </div>
           </div>

           <img src="/logo.png" className="absolute left-[-60px] bottom-[-60px] w-64 h-64 opacity-[0.03] rotate-[20deg]" alt="" />
        </div>
      </div>

    </div>
  );
};

export default Plans;

import React, { useState } from 'react';
import {
  ShieldCheck,
  Plus,
  Search,
  User,
  Lock,
  RefreshCcw,
  ArrowRight,
  TrendingUp,
  AlertCircle,
  MessageSquare
} from 'lucide-react';

const P2P: React.FC = () => {
  const [activeTab, setActiveTab] = useState('market');

  return (
    <div className="p-4 md:p-10 space-y-10 font-sans max-w-[1400px] mx-auto bg-auth-gradient min-h-screen">

      {/* Header Area */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-4 md:px-0">
        <div>
           <p className="text-primary text-[12px] font-[800] uppercase tracking-[0.2em] mb-2">Internal Ledger</p>
           <h1 className="text-[32px] font-[900] text-[#1E293B] tracking-tight">Trade & Transfer</h1>
           <p className="text-[15px] text-[#64748B] mt-2 font-medium">Instant asset movements and merchant liquidity marketplace.</p>
        </div>
        <button className="flex items-center gap-2 bg-primary text-white px-6 py-3 rounded-2xl text-[14px] font-[800] shadow-xl shadow-primary/20 hover:scale-105 active:scale-95 transition-all">
          <Plus size={20} strokeWidth={3} />
          POST ADVERTISEMENT
        </button>
      </div>

      {/* Hero Asset Card */}
      <div className="mx-4 md:mx-0 bg-[#1E293B] rounded-[32px] p-8 md:p-12 relative overflow-hidden shadow-2xl group">
        <div className="absolute inset-0 bg-gradient-to-br from-primary to-[#1E293B] opacity-90 transition-opacity group-hover:opacity-80"></div>

        <div className="relative z-10 h-full flex flex-col md:flex-row md:items-center justify-between gap-8">
          <div>
            <div className="flex items-center gap-3 mb-4">
              <div className="w-2 h-2 rounded-full bg-secondary animate-pulse"></div>
              <p className="text-[11px] font-[800] text-white/50 uppercase tracking-[0.2em]">Liquid Unlocked Profit</p>
            </div>
            <h2 className="text-[42px] font-[900] text-white tracking-tighter">$2,450.00</h2>
            <div className="mt-8 flex items-center gap-4">
               <div className="bg-white/10 px-4 py-2 rounded-xl border border-white/10 flex items-center gap-2">
                  <ShieldCheck size={16} className="text-secondary" />
                  <span className="text-[12px] font-[800] text-white uppercase tracking-wider">Verified Liquidity</span>
               </div>
               <div className="bg-white/10 px-4 py-2 rounded-xl border border-white/10 flex items-center gap-2">
                  <TrendingUp size={16} className="text-primary" />
                  <span className="text-[12px] font-[800] text-white uppercase tracking-wider">24H Active Nodes</span>
               </div>
            </div>
          </div>
          <div className="hidden md:flex flex-col items-end gap-3 text-right">
             <div className="w-16 h-16 bg-white/10 rounded-2xl flex items-center justify-center border border-white/10">
                <RefreshCcw size={32} className="text-white/30 group-hover:rotate-180 transition-transform duration-1000" />
             </div>
             <p className="text-[13px] text-white/40 font-bold max-w-[200px]">Internal transfers are processed off-chain with zero network fees.</p>
          </div>
        </div>

        <img src="/logo.png" className="absolute right-[-60px] bottom-[-60px] w-[240px] h-[240px] opacity-[0.05] rotate-[-20deg] group-hover:rotate-0 transition-transform duration-1000" alt="" />
      </div>

      {/* Professional Tabs UI */}
      <div className="mx-4 md:mx-0 bg-white rounded-[20px] border border-[#E2E8F0] p-1.5 flex h-[64px] shadow-sm">
        {[
          { id: 'market', label: 'Marketplace' },
          { id: 'my_ads', label: 'My Listings' },
          { id: 'quick', label: 'Instant Transfer' }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex-1 h-full flex items-center justify-center rounded-[16px] text-[14px] font-[800] transition-all ${
              activeTab === tab.id
                ? 'bg-primary text-white shadow-xl shadow-primary/20 scale-[1.02]'
                : 'text-[#64748B] hover:bg-surface'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Section Content */}
      <div className="px-4 md:px-0">
        {activeTab === 'market' && (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
            {[1, 2, 3, 4, 5, 6].map(i => (
              <div key={i} className="group bg-white rounded-[32px] border border-[#E2E8F0] p-8 flex flex-col shadow-sm hover:shadow-2xl transition-all relative overflow-hidden">
                <div className="flex items-center justify-between mb-8">
                   <div className="flex items-center gap-4">
                      <div className="w-14 h-14 bg-surface rounded-2xl flex items-center justify-center border border-[#F1F5F9] group-hover:border-primary/30 transition-all">
                        <span className="text-primary font-[900] text-[20px]">U{i}</span>
                      </div>
                      <div>
                        <p className="text-[16px] font-[800] text-[#1E293B]">LiquidityNode_{i}9</p>
                        <div className="flex items-center gap-1.5 mt-1">
                           <CheckCircle2 size={12} className="text-secondary" />
                           <p className="text-[11px] font-[800] text-secondary uppercase tracking-wider">Verified Merchant</p>
                        </div>
                      </div>
                   </div>
                   <div className="text-right">
                      <p className="text-[11px] font-[800] text-[#94A3B8] uppercase">Completion</p>
                      <p className="text-[15px] font-[800] text-[#1E293B]">98.4%</p>
                   </div>
                </div>

                <div className="space-y-4 mb-10 p-6 bg-[#F8F9FA] rounded-[24px] border border-[#F1F5F9]">
                   <div className="flex justify-between items-center">
                     <span className="text-[13px] font-[700] text-[#64748B] uppercase tracking-wide">Rate</span>
                     <span className="text-[20px] font-[900] text-primary">$1.02</span>
                   </div>
                   <div className="flex justify-between items-center">
                     <span className="text-[13px] font-[700] text-[#64748B] uppercase tracking-wide">Available</span>
                     <span className="text-[16px] font-[800] text-[#1E293B]">1,240.00 USDT</span>
                   </div>
                </div>

                <div className="mt-auto grid grid-cols-2 gap-4">
                   <button className="h-[52px] border-2 border-[#E2E8F0] text-[#1E293B] font-[800] text-[13px] rounded-xl hover:bg-surface transition-all flex items-center justify-center gap-2">
                      <MessageSquare size={16} /> CHAT
                   </button>
                   <button className="h-[52px] bg-[#1E293B] text-white font-[800] text-[13px] rounded-xl hover:bg-primary shadow-lg transition-all flex items-center justify-center gap-2">
                      BUY <ArrowRight size={16} />
                   </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'quick' && (
          <div className="max-w-4xl mx-auto lg:mx-0">
            <div className="bg-white rounded-[32px] border border-[#E2E8F0] p-8 md:p-12 shadow-sm hover:shadow-xl transition-all relative overflow-hidden">
               <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -mr-32 -mt-32"></div>

               <div className="relative z-10">
                <div className="flex items-center gap-4 mb-10">
                   <div className="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary">
                      <RefreshCcw size={24} strokeWidth={2.5} />
                   </div>
                   <div>
                     <h3 className="text-[20px] font-[900] text-[#1E293B]">Internal Liquidity Dispatch</h3>
                     <p className="text-[14px] text-[#64748B] font-medium mt-1">Authorized zero-fee peer-to-peer settlement protocol.</p>
                   </div>
                </div>

                <div className="space-y-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="space-y-3 group/input">
                      <label className="text-[11px] font-[800] text-[#64748B] uppercase ml-1 tracking-widest">Receiver Protocol UID</label>
                      <div className="relative">
                        <User className="absolute left-6 top-1/2 -translate-y-1/2 text-[#94A3B8] group-focus-within/input:text-primary transition-colors" size={20} />
                        <input
                          type="text"
                          placeholder="AS-XXXXX"
                          className="w-full h-[72px] pl-14 pr-32 bg-surface border-2 border-transparent focus:bg-white focus:border-primary rounded-[24px] text-[16px] font-[700] text-[#1E293B] outline-none transition-all shadow-sm"
                        />
                        <button className="absolute right-4 top-1/2 -translate-y-1/2 bg-primary/10 text-primary text-[11px] font-[900] px-4 py-2 rounded-xl hover:bg-primary hover:text-white transition-all">VERIFY</button>
                      </div>
                    </div>

                    <div className="space-y-3 group/input">
                      <label className="text-[11px] font-[800] text-[#64748B] uppercase ml-1 tracking-widest">Dispatch Quantum (USDT)</label>
                      <div className="relative">
                        <Lock className="absolute left-6 top-1/2 -translate-y-1/2 text-[#94A3B8] group-focus-within/input:text-primary transition-colors" size={20} />
                        <input
                          type="number"
                          placeholder="0.00"
                          className="w-full h-[72px] pl-14 pr-24 bg-surface border-2 border-transparent focus:bg-white focus:border-primary rounded-[24px] text-[20px] font-[900] text-[#1E293B] outline-none transition-all shadow-sm"
                        />
                        <button className="absolute right-4 top-1/2 -translate-y-1/2 text-primary text-[11px] font-[900] px-4 py-2 rounded-xl hover:bg-primary/10 transition-all uppercase">MAX</button>
                      </div>
                    </div>
                  </div>

                  {/* Transfer Note */}
                  <div className="bg-surface/50 rounded-[24px] p-6 border border-gray-100 flex gap-4 items-start">
                     <AlertCircle className="text-[#94A3B8] mt-0.5" size={20} />
                     <p className="text-[13px] text-[#64748B] font-medium leading-relaxed">
                       This transfer is finalized instantly. Ensure the recipient UID corresponds to the intended peer.
                       <strong> Zero fees</strong> are applied to this protocol.
                     </p>
                  </div>

                  <button className="w-full h-[72px] bg-primary hover:bg-[#5A4AD1] text-white font-[900] text-[18px] rounded-[24px] shadow-2xl shadow-primary/30 flex items-center justify-center gap-4 group/btn relative overflow-hidden active:scale-[0.98] transition-all mt-6">
                    <div className="absolute inset-0 bg-white/20 translate-x-[-100%] group-hover/btn:translate-x-[100%] transition-transform duration-1000"></div>
                    <RefreshCcw size={24} strokeWidth={2.5} />
                    <span>Authorize Instant Dispatch</span>
                    <ArrowRight size={20} className="group-hover/btn:translate-x-1 transition-transform" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default P2P;

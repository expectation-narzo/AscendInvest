import React from 'react';
import {
  Trophy,
  Star,
  Link as LinkIcon,
  Share2,
  DollarSign,
  Users,
  Award,
  Zap,
  ShieldCheck,
  TrendingUp,
  Copy
} from 'lucide-react';
import MilestoneTracker from '../components/MilestoneTracker';

const Referrals: React.FC = () => {
  const referralCode = "AS99201";
  const referralLink = `https://ascendinvest.com/portal?ref=${referralCode}`;

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    alert('Referral link copied to secure clipboard!');
  };

  return (
    <div className="p-4 md:p-10 space-y-10 font-sans max-w-[1400px] mx-auto bg-auth-gradient min-h-screen">

      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-4 md:px-0">
        <div>
           <p className="text-primary text-[12px] font-[800] uppercase tracking-[0.2em] mb-2">Affiliate Program</p>
           <h1 className="text-[32px] font-[900] text-[#1E293B] tracking-tight">Invite & Earn Rewards</h1>
           <p className="text-[15px] text-[#64748B] mt-2 font-medium">Build your financial network and earn recursive compounding commissions.</p>
        </div>
        <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex items-center gap-4">
           <div className="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-primary">
              <Zap size={20} fill="currentColor" />
           </div>
           <div>
             <p className="text-[10px] font-[800] text-[#94A3B8] uppercase tracking-wider">Referral Multiplier</p>
             <p className="text-[18px] font-[900] text-primary tracking-tight">Active (Tier 1)</p>
           </div>
        </div>
      </div>

      {/* Hero Promotional Card */}
      <div className="mx-4 md:mx-0 bg-[#6C5CE7] rounded-[32px] p-8 md:p-14 text-white relative overflow-hidden shadow-2xl shadow-primary/30 group">
        <div className="absolute inset-0 bg-gradient-to-br from-primary to-[#5A4AD1] opacity-95"></div>
        <div className="relative z-10 max-w-3xl">
          <div className="flex items-center gap-3 mb-6">
             <div className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/10">
                <Trophy size={22} className="text-secondary" />
             </div>
             <p className="text-[13px] font-[800] text-white/70 uppercase tracking-[0.2em]">Growth Protocol</p>
          </div>
          <h2 className="text-[36px] md:text-[42px] font-[900] leading-tight mb-6">Build Your Financial<br className="hidden md:block" /> Legacy Network</h2>
          <p className="text-[18px] text-white/80 leading-relaxed font-medium max-w-2xl">
            Earn up to <span className="text-secondary font-black">10% commission</span> across 20 depths. The larger your network, the higher your passive yield velocity.
          </p>
          <div className="mt-12 flex flex-wrap gap-6">
             <div className="flex items-center gap-2">
                <ShieldCheck size={20} className="text-secondary" />
                <span className="text-[14px] font-[800]">Multi-Level Tracking</span>
             </div>
             <div className="flex items-center gap-2">
                <TrendingUp size={20} className="text-white" />
                <span className="text-[14px] font-[800]">Instant Revenue Settlement</span>
             </div>
          </div>
        </div>
        <img src="/logo.png" className="absolute right-[-40px] top-1/2 -translate-y-1/2 w-[280px] h-[280px] opacity-[0.05] rotate-[-15deg] group-hover:rotate-0 transition-transform duration-1000" alt="" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 px-4 md:px-0">
        <div className="lg:col-span-8 space-y-10">

          {/* ADD ON: Milestone Progress */}
          <MilestoneTracker />

          {/* Invitation Control Panel */}
          <div className="bg-white rounded-[32px] border border-[#E2E8F0] p-8 md:p-12 shadow-sm relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -mr-32 -mt-32"></div>

            <div className="relative z-10">
              <h3 className="text-[20px] font-[900] text-[#1E293B] mb-8">Personal Invitation Protocol</h3>

              <div className="space-y-8">
                 <div className="space-y-3">
                    <label className="text-[11px] font-[800] text-[#94A3B8] uppercase ml-1 tracking-[0.1em]">Your Secure Network Link</label>
                    <div className="bg-surface rounded-[24px] border-2 border-transparent focus-within:border-primary focus-within:bg-white p-2 flex items-center gap-3 transition-all shadow-inner">
                       <span className="flex-1 text-[15px] font-[700] text-[#334155] px-4 truncate font-mono">{referralLink}</span>
                       <button onClick={() => copyToClipboard(referralLink)} className="h-[52px] px-6 bg-white rounded-[18px] text-primary font-[800] text-[13px] shadow-sm border border-gray-100 hover:bg-primary hover:text-white transition-all flex items-center gap-2">
                          <Copy size={18} /> COPY
                       </button>
                    </div>
                 </div>

                 <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <button className="h-[68px] bg-primary text-white rounded-[20px] font-[900] text-[16px] shadow-2xl shadow-primary/30 flex items-center justify-center gap-4 hover:opacity-90 active:scale-[0.98] transition-all">
                       <Share2 size={24} /> INVITE FRIENDS
                    </button>
                    <button className="h-[68px] bg-[#1E293B] text-white rounded-[20px] font-[900] text-[16px] shadow-2xl shadow-black/10 flex items-center justify-center gap-4 hover:bg-black active:scale-[0.98] transition-all">
                       <Users size={24} /> VIEW NETWORK
                    </button>
                 </div>
              </div>
            </div>
          </div>

          {/* Performance Dashboard */}
          <div className="bg-white rounded-[32px] border border-[#E2E8F0] shadow-sm overflow-hidden">
             <div className="p-8 border-b border-[#F1F5F9] flex items-center justify-between">
                <h3 className="text-[18px] font-[900] text-[#1E293B]">Leaderboard Status</h3>
                <span className="text-[11px] font-[800] bg-orange-50 text-orange-500 px-3 py-1.5 rounded-lg uppercase tracking-wider">LIVE UPDATES</span>
             </div>
             <div className="p-12 text-center flex flex-col items-center">
                <div className="w-20 h-20 bg-surface rounded-[24px] flex items-center justify-center mb-6 border border-gray-50">
                   <Trophy size={40} className="text-[#CBD5E1]" />
                </div>
                <h4 className="text-[18px] font-[800] text-[#1E293B]">Loading Global Rankings...</h4>
                <p className="text-[14px] text-[#94A3B8] mt-2 font-medium max-w-[300px]">We are synchronizing top network growth data from the decentralized ledger.</p>
             </div>
          </div>
        </div>

        {/* Right Sidebar Stats */}
        <div className="lg:col-span-4 space-y-10">

           <div className="grid grid-cols-2 gap-6">
              <div className="bg-white rounded-[28px] border border-[#E2E8F0] p-6 shadow-sm group hover:-translate-y-1 transition-all">
                 <div className="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center text-primary mb-5 group-hover:scale-110 transition-transform">
                    <DollarSign size={20} />
                 </div>
                 <p className="text-[11px] font-[800] text-[#94A3B8] uppercase tracking-widest">Total Earnings</p>
                 <h4 className="text-[22px] font-[900] text-[#1E293B] mt-1">$0.00</h4>
              </div>
              <div className="bg-white rounded-[28px] border border-[#E2E8F0] p-6 shadow-sm group hover:-translate-y-1 transition-all">
                 <div className="w-10 h-10 bg-secondary/10 rounded-xl flex items-center justify-center text-secondary mb-5 group-hover:scale-110 transition-transform">
                    <Users size={20} />
                 </div>
                 <p className="text-[11px] font-[800] text-[#94A3B8] uppercase tracking-widest">Nodes Active</p>
                 <h4 className="text-[22px] font-[900] text-[#1E293B] mt-1">0</h4>
              </div>
           </div>

           {/* Achievements */}
           <div className="bg-white rounded-[32px] border border-[#E2E8F0] p-8 shadow-sm">
              <h3 className="text-[16px] font-[900] text-[#1E293B] mb-8 uppercase tracking-widest text-center">Achievements</h3>
              <div className="grid grid-cols-3 gap-4">
                 {[
                   { l: 'Starter', c: 'text-gray-400', bg: 'bg-gray-50' },
                   { l: 'PRO', c: 'text-primary', bg: 'bg-primary/5' },
                   { l: 'Whale', c: 'text-secondary', bg: 'bg-secondary/5' }
                 ].map((badge, i) => (
                   <div key={i} className="flex flex-col items-center gap-3">
                      <div className={`w-16 h-16 rounded-[20px] ${badge.bg} flex items-center justify-center border border-gray-100 opacity-40 grayscale group hover:opacity-100 hover:grayscale-0 transition-all`}>
                         <Award className={badge.c} size={32} strokeWidth={2.5} />
                      </div>
                      <span className="text-[10px] font-[900] text-[#94A3B8] uppercase tracking-widest">{badge.l}</span>
                   </div>
                 ))}
              </div>
           </div>

           {/* Commission Card */}
           <div className="bg-[#1E293B] rounded-[32px] p-8 text-white relative overflow-hidden shadow-2xl">
              <h3 className="text-[16px] font-[800] mb-8 border-b border-white/10 pb-6">Commission Protocol</h3>
              <div className="space-y-6">
                 {[
                   { l: 'Direct Tier (L1)', p: '10%', c: 'text-secondary' },
                   { l: 'Sub-Tier (L2)', p: '5%', c: 'text-white' },
                   { l: 'Sub-Tier (L3)', p: '3%', c: 'text-white' },
                   { l: 'Micro-Tiers (L4-20)', p: '1%', c: 'text-white/40' }
                 ].map((row, i) => (
                   <div key={i} className="flex justify-between items-center group cursor-default">
                      <span className={`text-[13px] font-[700] ${row.c} transition-all group-hover:translate-x-1`}>{row.l}</span>
                      <div className="flex-1 mx-4 border-b border-white/5 border-dashed"></div>
                      <span className={`text-[15px] font-[900] ${row.c}`}>{row.p}</span>
                   </div>
                 ))}
              </div>
              <img src="/logo.png" className="absolute left-[-40px] bottom-[-40px] w-[140px] h-[140px] opacity-[0.05] rotate-[20deg]" alt="" />
           </div>
        </div>
      </div>
    </div>
  );
};

export default Referrals;

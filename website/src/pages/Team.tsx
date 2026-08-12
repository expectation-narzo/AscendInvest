import React, { useState } from 'react';
import {
  Users,
  Search,
  ChevronRight,
  TrendingUp,
  Filter,
  Activity,
  Award,
  Zap,
  MoreVertical,
  ExternalLink
} from 'lucide-react';

const Team: React.FC = () => {
  const [activeFilter, setActiveFilter] = useState('all');

  return (
    <div className="p-4 md:p-10 space-y-10 font-sans max-w-[1400px] mx-auto bg-auth-gradient min-h-screen">

      {/* Header Area */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-4 md:px-0">
        <div>
           <p className="text-primary text-[12px] font-[800] uppercase tracking-[0.2em] mb-2">Network Hub</p>
           <h1 className="text-[32px] font-[900] text-[#1E293B] tracking-tight">Team Management</h1>
           <p className="text-[15px] text-[#64748B] mt-2 font-medium">Coordinate and scale your network nodes across the alpha infrastructure.</p>
        </div>
        <div className="flex items-center gap-3">
           <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex items-center gap-4">
              <div className="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-primary">
                 <Activity size={20} />
              </div>
              <div>
                <p className="text-[10px] font-[800] text-[#94A3B8] uppercase tracking-wider">Network Status</p>
                <p className="text-[13px] font-[800] text-primary uppercase tracking-tight">Synchronized</p>
              </div>
           </div>
        </div>
      </div>

      {/* Analytics Summary Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 px-4 md:px-0">
        {[
          { label: 'Cumulative Nodes', value: '0', icon: Users, color: '#6C5CE7', bg: 'bg-indigo-50' },
          { label: 'Direct Protocol (L1)', value: '0', icon: Zap, color: '#28C76F', bg: 'bg-emerald-50' },
          { label: 'Recursive Reach (L2)', value: '0', icon: Award, color: '#FF9F43', iconColor: 'text-orange-500', bg: 'bg-orange-50' }
        ].map((stat, idx) => (
          <div key={idx} className="bg-white rounded-[28px] border border-[#E2E8F0] p-8 shadow-sm group hover:-translate-y-1 hover:shadow-xl transition-all relative overflow-hidden">
            <div className={`absolute top-0 right-0 w-24 h-24 ${stat.bg} opacity-20 rounded-full -mr-12 -mt-12 group-hover:scale-110 transition-transform`}></div>
            <div className={`w-12 h-12 rounded-2xl ${stat.bg} flex items-center justify-center mb-6 shadow-sm`} style={{ color: stat.color }}>
               <stat.icon size={24} strokeWidth={2.5} />
            </div>
            <p className="text-[12px] font-[700] text-[#94A3B8] uppercase tracking-[0.1em]">{stat.label}</p>
            <h3 className="text-[32px] font-[900] text-[#1E293B] mt-2 tracking-tighter">{stat.value}</h3>
          </div>
        ))}
      </div>

      {/* Control & List Section */}
      <div className="space-y-8">

        {/* Search & Intelligence Controls */}
        <div className="bg-white rounded-[32px] border border-[#E2E8F0] p-6 md:p-8 shadow-sm flex flex-col md:flex-row gap-6 items-center">
          <div className="relative flex-1 w-full group">
            <Search className="absolute left-6 top-1/2 -translate-y-1/2 text-[#94A3B8] group-focus-within:text-primary transition-colors" size={20} />
            <input
              type="text"
              placeholder="Search protocol UID or name..."
              className="w-full h-[64px] pl-14 pr-6 bg-surface border-2 border-transparent focus:bg-white focus:border-primary rounded-[20px] text-[15px] font-[600] text-[#1E293B] outline-none transition-all shadow-inner"
            />
          </div>
          <div className="flex gap-2 w-full md:w-auto overflow-x-auto pb-2 md:pb-0 scrollbar-hide px-2">
            {[
              { id: 'all', label: 'Complete Network' },
              { id: 'lvl1', label: 'Primary Tier' },
              { id: 'lvl2', label: 'Sub-Tier' },
              { id: 'active', label: 'Operational Only' }
            ].map(filter => (
              <button
                key={filter.id}
                onClick={() => setActiveFilter(filter.id)}
                className={`h-[52px] px-8 rounded-2xl text-[13px] font-[800] transition-all whitespace-nowrap shadow-sm ${
                  activeFilter === filter.id
                    ? 'bg-primary text-white shadow-primary/20 scale-105'
                    : 'bg-surface text-[#64748B] hover:bg-gray-200'
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>
        </div>

        {/* High Performance Insights */}
        <div className="bg-[#F8F9FF] rounded-[32px] border border-[#DDE4FF] p-8 flex flex-col md:flex-row items-center gap-8 cursor-pointer hover:bg-[#F1F3FF] transition-all relative overflow-hidden group shadow-sm">
           <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -mr-32 -mt-32 group-hover:bg-primary/10 transition-all"></div>
           <div className="w-16 h-16 bg-white rounded-[20px] flex items-center justify-center shadow-xl border border-indigo-50 group-hover:scale-110 transition-transform">
              <TrendingUp className="text-primary" size={28} strokeWidth={2.5} />
           </div>
           <div className="flex-1 text-center md:text-left relative z-10">
              <h3 className="text-[20px] font-[900] text-[#1E293B] tracking-tight">Recursive Network Insights</h3>
              <p className="text-[15px] text-[#64748B] mt-1 font-medium">Analyze recursive growth patterns and automate commission flow across all sub-tiers.</p>
           </div>
           <div className="flex items-center gap-4 relative z-10">
              <button className="h-[48px] px-6 bg-white rounded-xl text-primary font-[800] text-[13px] shadow-sm hover:shadow-md transition-all">ANALYZE NODES</button>
              <ChevronRight className="text-[#CBD5E1] group-hover:translate-x-1 transition-all" size={28} />
           </div>
        </div>

        {/* Ledger Header */}
        <div className="flex justify-between items-center px-4">
           <div>
              <h3 className="text-[22px] font-[900] text-[#1E293B] tracking-tight">Active Network Ledger</h3>
              <p className="text-[13px] text-[#94A3B8] font-bold uppercase tracking-widest mt-1">Live status protocol</p>
           </div>
           <button className="p-3 hover:bg-surface rounded-xl text-[#94A3B8] transition-all"><Filter size={20} /></button>
        </div>

        {/* Protocol Empty State - matched and enhanced */}
        <div className="bg-white rounded-[40px] border border-[#E2E8F0] p-20 flex flex-col items-center text-center shadow-sm relative overflow-hidden">
           <div className="absolute inset-0 bg-auth-gradient opacity-30"></div>
           <div className="relative z-10">
              <div className="w-[120px] h-[120px] bg-surface rounded-full flex items-center justify-center mb-10 border-4 border-white shadow-2xl relative">
                 <Users className="text-[#CBD5E1]" size={56} strokeWidth={1.5} />
                 <div className="absolute -bottom-2 -right-2 w-10 h-10 bg-white rounded-2xl flex items-center justify-center shadow-lg border border-gray-100">
                    <Activity className="text-primary animate-pulse" size={20} />
                 </div>
              </div>
              <h4 className="text-[24px] font-[900] text-[#1E293B] tracking-tight">Network is Isolated</h4>
              <p className="text-[16px] text-[#64748B] mt-4 max-w-sm font-medium leading-relaxed">
                 You haven't initialized any network nodes yet. Deploy your invitation protocol to begin earning recursive yield.
              </p>
              <button className="mt-12 h-[64px] px-12 bg-primary text-white rounded-[20px] font-[900] text-[16px] shadow-2xl shadow-primary/30 hover:opacity-90 active:scale-[0.98] transition-all flex items-center gap-4">
                 DEPLOY INVITATION LINK <ExternalLink size={20} />
              </button>
           </div>
        </div>
      </div>

      {/* Support Visual Footer */}
      <div className="pt-10 flex flex-col items-center">
         <div className="flex -space-x-4 mb-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="w-12 h-12 rounded-full border-4 border-white bg-surface overflow-hidden shadow-lg">
                 <img src={`https://i.pravatar.cc/150?u=${i}`} alt="" className="w-full h-full object-cover grayscale hover:grayscale-0 transition-all cursor-pointer" />
              </div>
            ))}
            <div className="w-12 h-12 rounded-full border-4 border-white bg-primary flex items-center justify-center text-white text-[12px] font-black shadow-lg">+50k</div>
         </div>
         <p className="text-[14px] text-[#94A3B8] font-black uppercase tracking-[0.2em]">Join the Global Network Elite</p>
      </div>

    </div>
  );
};

export default Team;

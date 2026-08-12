import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  ArrowDownCircle,
  ArrowUpCircle,
  RefreshCcw,
  PieChart,
  Link as LinkIcon,
  Users,
  Headphones,
  LogOut,
  ChevronDown,
  Menu,
  X,
  Search,
  Bell,
  Command,
  ShieldCheck,
  Zap,
  Activity,
  Cpu
} from 'lucide-react';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const menuItems = [
    { label: 'Intelligence', icon: LayoutDashboard, path: '/' },
    { label: 'Treasury', icon: ArrowDownCircle, dropdown: [
      { label: 'Asset Funding', path: '/deposits', icon: ArrowDownCircle },
      { label: 'Capital Exit', path: '/withdraw', icon: ArrowUpCircle },
      { label: 'Internal Market', path: '/p2p', icon: RefreshCcw },
    ]},
    { label: 'Ecosystem', icon: Users, dropdown: [
      { label: 'Yield Nodes', path: '/plans', icon: PieChart },
      { label: 'Network Engine', path: '/referrals', icon: LinkIcon },
      { label: 'Protocol Ledger', path: '/team', icon: Users },
    ]},
    { label: 'Compliance', icon: ShieldCheck, path: '/support' },
  ];

  return (
    <nav className={`sticky top-0 z-50 transition-all duration-500 ${scrolled ? 'py-2' : 'py-4'}`}>
      <div className="max-w-[1500px] mx-auto px-6">
        <div className={`bg-white/80 backdrop-blur-xl rounded-[28px] border border-white/60 shadow-2xl shadow-black/5 flex items-center justify-between px-8 h-[76px] transition-all duration-500 ${scrolled ? 'mx-4' : ''}`}>

          {/* Institutional Branding */}
          <div className="flex items-center gap-12">
            <div className="flex items-center gap-3.5 cursor-pointer group" onClick={() => navigate('/')}>
              <div className="relative">
                 <div className="w-12 h-12 bg-[#6C5CE7] rounded-[18px] flex items-center justify-center shadow-2xl shadow-primary/30 group-hover:rotate-[10deg] transition-all duration-500 relative z-10 border border-white/20">
                    <img src="/logo.png" className="w-7 h-7 brightness-0 invert" alt="" />
                 </div>
              </div>
              <div className="flex flex-col">
                <span className="text-[20px] font-[1000] text-[#1E293B] tracking-tighter leading-none uppercase italic">Ascend</span>
                <div className="flex items-center gap-1.5 mt-1.5">
                   <div className="w-1 h-1 rounded-full bg-[#6C5CE7] animate-pulse"></div>
                   <span className="text-[9px] font-black text-[#6C5CE7] tracking-[0.25em] uppercase">Authorized</span>
                </div>
              </div>
            </div>

            {/* Desktop Navigation */}
            <div className="hidden lg:flex items-center gap-1.5 bg-[#F8FAFC] p-1.5 rounded-[20px] border border-[#F1F5F9]">
              {menuItems.map((item, idx) => (
                <div key={idx} className="relative group">
                  <button
                    onClick={() => item.path && navigate(item.path)}
                    className={`flex items-center gap-2.5 px-6 py-2.5 rounded-[16px] text-[13px] font-[800] tracking-tight transition-all duration-300 ${
                      (item.path === location.pathname || (item.dropdown && item.dropdown.some(d => d.path === location.pathname)))
                      ? 'bg-white text-[#6C5CE7] shadow-sm border border-gray-100'
                      : 'text-[#64748B] hover:text-[#1E293B] hover:bg-white/60'
                    }`}
                  >
                    <item.icon size={17} strokeWidth={2.5} className={(item.path === location.pathname || (item.dropdown && item.dropdown.some(d => d.path === location.pathname))) ? 'text-[#6C5CE7]' : 'opacity-40'} />
                    <span>{item.label}</span>
                    {item.dropdown && <ChevronDown size={14} className="opacity-30 group-hover:rotate-180 transition-transform" />}
                  </button>

                  {item.dropdown && (
                    <div className="absolute top-[calc(100%+12px)] left-0 w-[240px] bg-white rounded-[24px] shadow-2xl border border-[#E2E8F0] p-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all transform translate-y-3 group-hover:translate-y-0 z-50">
                      {item.dropdown.map((sub, sIdx) => (
                        <div
                          key={sIdx}
                          onClick={() => navigate(sub.path)}
                          className={`p-3.5 flex items-center gap-4 text-[13px] font-[750] rounded-[16px] cursor-pointer transition-all ${location.pathname === sub.path ? 'bg-[#6C5CE7]/5 text-[#6C5CE7]' : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'}`}
                        >
                          <div className={`w-9 h-9 rounded-xl flex items-center justify-center shadow-sm ${location.pathname === sub.path ? 'bg-[#6C5CE7] text-white' : 'bg-white border border-gray-100'}`}>
                            <sub.icon size={18} strokeWidth={2.5} />
                          </div>
                          {sub.label}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Tools Area */}
          <div className="flex items-center gap-5">
             <div className="hidden xl:flex items-center gap-3 bg-[#F8FAFC] border border-[#F1F5F9] rounded-[18px] px-5 py-2.5">
                <Search size={18} className="text-[#94A3B8]" />
                <input type="text" placeholder="Intelligence search..." className="bg-transparent text-[13px] font-bold outline-none w-32 focus:w-48 transition-all" />
                <div className="flex items-center gap-1.5 bg-white px-2 py-1 rounded-lg border border-gray-100 opacity-60">
                   <Command size={10} className="text-[#94A3B8]" /> <span className="text-[10px] font-black text-[#94A3B8]">K</span>
                </div>
             </div>

             <div className="flex items-center gap-4 pl-5 border-l border-[#F1F5F9]">
                <button className="relative p-2.5 text-[#64748B] hover:text-[#6C5CE7] transition-all">
                   <Bell size={21} strokeWidth={2.5} />
                   <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-[#E6656A] rounded-full border-2 border-white shadow-sm animate-pulse"></span>
                </button>

                <div className="hidden md:flex items-center gap-3.5 p-1.5 pr-5 bg-[#F8FAFC] hover:bg-white rounded-[22px] border border-transparent hover:border-[#E2E8F0] cursor-pointer transition-all">
                   <div className="w-10 h-10 rounded-[16px] border-2 border-primary/20 p-0.5 shadow-md overflow-hidden relative">
                      <img src="/ic_launcher.png" className="w-full h-full rounded-[12px] object-cover" alt="" />
                   </div>
                   <div className="flex flex-col text-left">
                      <p className="text-[13px] font-[900] text-[#1E293B] leading-none">Alpha Pro</p>
                      <p className="text-[9px] font-[900] text-[#28C76F] uppercase tracking-widest mt-1">Active Node</p>
                   </div>
                </div>

                <button
                  onClick={() => navigate('/login')}
                  className="p-3 text-[#E6656A] bg-[#E6656A]/5 hover:bg-[#E6656A]/10 rounded-[18px] transition-all"
                >
                  <LogOut size={21} strokeWidth={2.5} />
                </button>

                <button
                  onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                  className="lg:hidden p-3 bg-white border border-[#E2E8F0] rounded-[18px] text-text-dark"
                >
                  {isMobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
                </button>
             </div>
          </div>
        </div>
      </div>

      {/* Basic Mobile Drawer */}
      {isMobileMenuOpen && (
        <div className="lg:hidden fixed inset-0 z-[60]" onClick={() => setIsMobileMenuOpen(false)}>
           <div className="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
           <div className="absolute inset-y-0 left-0 w-[300px] bg-white flex flex-col p-8" onClick={e => e.stopPropagation()}>
              <div className="flex justify-between items-center mb-12">
                 <h2 className="font-black text-2xl italic tracking-tighter">ASCEND</h2>
                 <X size={24} onClick={() => setIsMobileMenuOpen(false)} />
              </div>
              <div className="space-y-6">
                 {menuItems.map((item, idx) => (
                   <div key={idx} className="space-y-4">
                      <div className="font-black text-[16px] flex items-center gap-4 text-[#1E293B]" onClick={() => { if(!item.dropdown) { navigate(item.path!); setIsMobileMenuOpen(false); } }}>
                         <item.icon size={22} strokeWidth={2.5} className="text-[#6C5CE7]" />
                         {item.label}
                      </div>
                      {item.dropdown && (
                        <div className="ml-10 space-y-4 border-l-2 border-gray-100 pl-6">
                           {item.dropdown.map((sub, sIdx) => (
                             <div key={sIdx} className="text-[14px] font-bold text-[#64748B]" onClick={() => { navigate(sub.path); setIsMobileMenuOpen(false); }}>{sub.label}</div>
                           ))}
                        </div>
                      )}
                   </div>
                 ))}
              </div>
           </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;

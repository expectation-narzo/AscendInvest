import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import NodeStatus from './NodeStatus';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const location = useLocation();
  const navigate = useNavigate();

  const menuSections = [
    {
      title: 'MAIN MENU',
      items: [
        { id: 'overview', icon: '/ic_dashboard.png', label: 'Overview', path: '/' },
        { id: 'deposits', icon: '/ic_deposit.png', label: 'Deposits', path: '/deposits' },
        { id: 'withdraw', icon: '/ic_withdraw.png', label: 'Withdrawal', path: '/withdraw' },
        { id: 'p2p', icon: '/ic_p2p.png', label: 'P2P Transfer', path: '/p2p' },
      ]
    },
    {
      title: 'EARNINGS',
      items: [
        { id: 'plans', icon: '/ic_plan.png', label: 'Market Plans', path: '/plans' },
        { id: 'referrals', icon: '/ic_link.png', label: 'Referrals', path: '/referrals' },
        { id: 'team', icon: '/ic_team.png', label: 'My Network', path: '/team' },
      ]
    },
    {
      title: 'OTHERS',
      items: [
        { id: 'support', icon: '/ic_support.png', label: 'Help Center', path: '/support' },
      ]
    }
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-[60] lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar - Exact 280dp from nav_drawer.xml */}
      <div className={`fixed lg:static inset-y-0 left-0 w-[280px] bg-white h-screen flex flex-col z-[70] transition-transform duration-300 transform ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'} border-r border-[#F1F5F9]`}>

        {/* Premium Header Area - RelativeLayout with bg_auth */}
        <div className="p-[24px] bg-auth-gradient relative overflow-hidden flex flex-col">
          <div className="w-[60px] h-[60px] rounded-full border-[2.5px] border-[#6C5CE7] p-[2px] shadow-lg bg-white relative z-10">
             <img src="/logo.png" className="w-full h-full rounded-full object-cover" alt="" />
          </div>

          <h2 className="mt-[16px] text-[18px] font-[800] text-[#1E293B] tracking-tight relative z-10">User Alpha</h2>

          <div className="flex items-center mt-[2px] relative z-10">
             <div className="w-[8px] h-[8px] rounded-full bg-[#22C55E]"></div>
             <span className="ml-[8px] text-[12px] font-[600] text-[#64748B]">ID: AS99201</span>
          </div>

          {/* Watermark logo -15deg */}
          <img src="/logo.png" className="absolute right-[-20px] top-1/2 -translate-y-1/2 w-[80px] h-[80px] opacity-[0.05] rotate-[-15deg] pointer-events-none" alt="" />
        </div>

        {/* Menu Items - ScrollView */}
        <div className="flex-1 overflow-y-auto px-[14px] pt-[20px] pb-6 custom-scrollbar">
          {menuSections.map((section, idx) => (
            <div key={idx} className="mb-[12px]">
              <h3 className="px-[16px] mb-[12px] text-[11px] font-[700] text-[#94A3B8] tracking-[0.1em] uppercase">
                {section.title}
              </h3>

              <div className="space-y-[4px]">
                {section.items.map((item) => {
                  const isActive = location.pathname === item.path;
                  return (
                    <div
                      key={item.id}
                      onClick={() => {
                        navigate(item.path);
                        if (window.innerWidth < 1024) onClose();
                      }}
                      className={`h-[46px] px-[16px] flex items-center rounded-[12px] cursor-pointer transition-all duration-200 ${
                        isActive
                          ? 'bg-[#F5F3FF] text-[#6C5CE7]'
                          : 'text-[#1E293B] hover:bg-[#F8FAFC]'
                      }`}
                    >
                      <div className="w-[24px] h-[24px] flex items-center justify-center">
                        <img
                          src={item.icon}
                          className={`w-full h-full object-contain transition-all ${isActive ? '' : 'opacity-40 grayscale'}`}
                          style={isActive ? { filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' } : {}}
                          alt=""
                          onError={(e) => {
                             (e.target as any).style.display = 'none';
                             // Handle support icon which is XML
                             if(item.id === 'support') (e.target as any).parentNode.innerHTML = '<div style="width:20px;height:20px;background:#64748B;border-radius:4px;opacity:0.4;"></div>';
                          }}
                        />
                      </div>
                      <span className={`ml-[16px] text-[14px] ${isActive ? 'font-[700]' : 'font-[500]'}`}>
                        {item.label}
                      </span>
                    </div>
                  );
                })}
              </div>

              {idx < menuSections.length - 1 && <div className="mx-[16px] my-[12px] border-b border-[#F1F5F9]" />}
            </div>
          ))}

          {/* Node Health Status - Add-on */}
          <NodeStatus />
        </div>

        {/* Footer - Sign Out */}
        <div className="p-[16px]">
          <button
            onClick={() => navigate('/login')}
            className="h-[46px] w-full px-[16px] flex items-center rounded-[12px] bg-[#FFF1F2] text-[#F43F5E] hover:bg-[#FFE4E6] transition-all"
          >
            <div className="w-[24px] h-[24px] flex items-center justify-center">
              <img src="/ic_logout.png" className="w-full h-full object-contain" style={{ filter: 'invert(37%) sepia(93%) saturate(3000%) hue-rotate(330deg) brightness(100%) contrast(100%)' }} alt="" />
            </div>
            <span className="ml-[16px] text-[14px] font-[700]">Sign Out</span>
          </button>
        </div>
      </div>
    </>
  );
};

export default Sidebar;

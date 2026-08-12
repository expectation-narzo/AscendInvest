import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock } from 'lucide-react';
import NotificationDrawer from './NotificationDrawer';

interface TopbarProps {
  onMenuClick: () => void;
}

const Topbar: React.FC<TopbarProps> = ({ onMenuClick }) => {
  const navigate = useNavigate();
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [time, setTime] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date().toLocaleTimeString()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="h-[64px] bg-white border-b border-[#F1F5F9] px-[16px] flex items-center justify-between sticky top-0 z-50 font-sans">

      {/* LEFT: Menu Button & Server Time */}
      <div className="flex items-center gap-6">
        <button
          onClick={onMenuClick}
          className="w-[32px] h-[32px] flex items-center justify-center hover:bg-[#F8FAFC] rounded-lg transition-all"
        >
          <img
            src="/ic_menu.png"
            className="w-[24px] h-[24px] object-contain opacity-60"
            style={{ filter: 'invert(43%) sepia(15%) saturate(1000%) hue-rotate(180deg) brightness(90%) contrast(90%)' }}
            alt="Menu"
            onError={(e) => {
               (e.target as any).src = "https://img.icons8.com/material-rounded/24/64748B/menu--v1.png";
            }}
          />
        </button>

        {/* ADD ON: Live Server Time */}
        <div className="hidden md:flex items-center gap-2 px-4 py-2 bg-[#F8FAFC] rounded-xl border border-[#F1F5F9] shadow-inner">
           <Clock size={14} className="text-[#94A3B8]" />
           <span className="text-[11px] font-black text-[#64748B] uppercase tracking-widest">{time}</span>
        </div>
      </div>

      {/* RIGHT: Notification and Profile */}
      <div className="flex items-center gap-[16px]">

        <div className="lg:hidden flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
           <span className="text-[18px] font-[900] text-[#1E293B] tracking-tight">Ascend</span>
           <div className="w-8 h-8 bg-[#6C5CE7] rounded-[8px] flex items-center justify-center shadow-md">
              <img src="/logo.png" className="w-5 h-5 brightness-0 invert" alt="" />
           </div>
        </div>

        {/* notification_button - Now opens Drawer */}
        <div
          onClick={() => setIsNotifOpen(true)}
          className="relative w-[32px] h-[32px] flex items-center justify-center cursor-pointer hover:bg-[#F8FAFC] rounded-full transition-all"
        >
          <img
            src="/ic_notification.png"
            className="w-[24px] h-[24px] object-contain opacity-60"
            style={{ filter: 'invert(43%) sepia(15%) saturate(1000%) hue-rotate(180deg) brightness(90%) contrast(90%)' }}
            alt=""
            onError={(e) => {
               (e.target as any).src = "https://img.icons8.com/material-rounded/24/64748B/appointment-reminders--v1.png";
            }}
          />
          {/* notification_dot */}
          <div className="absolute top-[4px] right-[4px] w-[8px] h-[8px] bg-[#E6656A] rounded-full border-2 border-white"></div>
        </div>

        <div className="w-[36px] h-[36px] rounded-full border border-[#E2E8F0] overflow-hidden cursor-pointer shadow-sm">
          <img src="/ic_launcher.png" className="w-full h-full object-cover rounded-full" alt="" />
        </div>
      </div>

      {/* Notification Drawer - ADD ON */}
      <NotificationDrawer isOpen={isNotifOpen} onClose={() => setIsNotifOpen(false)} />

    </div>
  );
};

export default Topbar;

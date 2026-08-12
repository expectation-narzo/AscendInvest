import React from 'react';
import { X, Bell, ShieldCheck, Zap, Info } from 'lucide-react';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

const NotificationDrawer: React.FC<NotificationDrawerProps> = ({ isOpen, onClose }) => {
  const notifications = [
    { id: 1, title: 'Yield Credited', msg: 'Daily profit node LN-88 finalized: +$12.50', time: 'Just now', icon: Zap, color: 'text-secondary', bg: 'bg-secondary/10' },
    { id: 2, title: 'Security Protocol', msg: 'New login authorized from Chrome (Windows)', time: '2h ago', icon: ShieldCheck, color: 'text-primary', bg: 'bg-primary/10' },
    { id: 3, title: 'Network Update', msg: 'Nodes in EU region are undergoing optimization', time: '5h ago', icon: Info, color: 'text-orange-500', bg: 'bg-orange-50' },
  ];

  return (
    <>
      {/* Backdrop */}
      {isOpen && <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-[110]" onClick={onClose} />}

      {/* Drawer */}
      <div className={`fixed top-0 right-0 h-screen w-full sm:w-[400px] bg-white z-[120] shadow-2xl transition-transform duration-500 transform ${isOpen ? 'translate-x-0' : 'translate-x-full'} flex flex-col`}>
        <div className="p-8 border-b border-[#F1F5F9] flex items-center justify-between bg-[#F8FAFC]">
           <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm text-[#1E293B]">
                 <Bell size={20} />
              </div>
              <h3 className="text-[18px] font-black text-[#1E293B]">Intelligence Feed</h3>
           </div>
           <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
              <X size={20} className="text-[#64748B]" />
           </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-4 custom-scrollbar">
           {notifications.map((n) => (
             <div key={n.id} className="p-5 rounded-[24px] bg-white border border-[#F1F5F9] hover:border-primary/20 hover:shadow-lg hover:shadow-primary/5 transition-all group cursor-default">
                <div className="flex gap-4">
                   <div className={`w-11 h-11 ${n.bg} ${n.color} rounded-2xl flex items-center justify-center flex-shrink-0 group-hover:rotate-12 transition-transform`}>
                      <n.icon size={22} strokeWidth={2.5} />
                   </div>
                   <div className="flex-1">
                      <div className="flex justify-between items-start">
                         <h4 className="text-[14px] font-[800] text-[#1E293B]">{n.title}</h4>
                         <span className="text-[10px] font-bold text-[#94A3B8] uppercase">{n.time}</span>
                      </div>
                      <p className="text-[12px] text-[#64748B] mt-1 font-medium leading-relaxed">{n.msg}</p>
                   </div>
                </div>
             </div>
           ))}
        </div>

        <div className="p-6 border-t border-[#F1F5F9] bg-[#F8FAFC]">
           <button className="w-full py-4 text-[11px] font-black text-[#6C5CE7] uppercase tracking-[0.25em] border-2 border-dashed border-[#E2E8F0] rounded-2xl hover:bg-white hover:border-[#6C5CE7] transition-all">
              Mark all as recognized
           </button>
        </div>
      </div>
    </>
  );
};

export default NotificationDrawer;

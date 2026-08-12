import React, { useState } from 'react';
import { Megaphone, X, ArrowRight } from 'lucide-react';

const GlobalAnnouncement: React.FC = () => {
  const [isVisible, setIsOpen] = useState(true);

  if (!isVisible) return null;

  return (
    <div className="mx-[20px] mt-[20px] mb-[-10px] bg-[#1E293B] rounded-[16px] p-4 flex items-center justify-between shadow-xl relative overflow-hidden group border border-white/5">
      <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-transparent opacity-50"></div>

      <div className="flex items-center gap-4 relative z-10">
        <div className="w-10 h-10 bg-primary/20 rounded-full flex items-center justify-center text-primary animate-pulse">
          <Megaphone size={18} />
        </div>
        <div>
          <p className="text-[13px] font-[800] text-white">System Protocol Update v4.2.1</p>
          <p className="text-[11px] text-white/60 font-medium mt-0.5">New high-yield arbitrage nodes are now operational in the APAC region. <span className="text-secondary font-black ml-1 cursor-pointer hover:underline">View Details</span></p>
        </div>
      </div>

      <div className="flex items-center gap-4 relative z-10">
         <button className="hidden md:flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white text-[11px] font-[800] px-4 py-2 rounded-lg uppercase tracking-wider transition-all">
            Recalibrate Nodes <ArrowRight size={14} />
         </button>
         <button onClick={() => setIsOpen(false)} className="p-1 text-white/40 hover:text-white transition-colors">
            <X size={20} />
         </button>
      </div>
    </div>
  );
};

export default GlobalAnnouncement;

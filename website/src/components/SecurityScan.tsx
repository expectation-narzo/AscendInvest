import React, { useEffect, useState } from 'react';
import { ShieldCheck } from 'lucide-react';

const SecurityScan: React.FC = () => {
  const [status, setStatus] = useState('Initializing link...');

  useEffect(() => {
    const statuses = ['Analyzing firewall...', 'Vault link secure', 'GCM-256 Active', 'Institutional Protected'];
    let i = 0;
    const interval = setInterval(() => {
      setStatus(statuses[i % statuses.length]);
      i++;
    }, 2500);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex items-center gap-3 bg-white/40 backdrop-blur-md px-4 py-2 rounded-full border border-white/60 shadow-sm transition-all hover:bg-white hover:shadow-lg">
      <div className="relative">
         <ShieldCheck size={16} className="text-secondary" />
         <div className="absolute inset-0 bg-secondary/20 rounded-full animate-ping"></div>
      </div>
      <span className="text-[10px] font-black text-[#1E293B] uppercase tracking-[0.2em]">{status}</span>
    </div>
  );
};

export default SecurityScan;

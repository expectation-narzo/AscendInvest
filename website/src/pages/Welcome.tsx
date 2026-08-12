import React from 'react';
import { useNavigate } from 'react-router-dom';
import SecurityScan from '../components/SecurityScan';

const Welcome: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#F8FAFC] flex flex-col items-center justify-center font-sans text-[#1E293B] px-6 selection:bg-[#6C5CE7]/20 relative overflow-hidden">

      {/* Background patterns */}
      <div className="absolute inset-0 bg-dot-pattern opacity-[0.2] pointer-events-none"></div>

      {/* ADD ON: Security Scan */}
      <div className="absolute top-10 flex justify-center w-full">
         <SecurityScan />
      </div>

      <div className="w-full max-w-md flex flex-col items-center z-10">

        <div className="w-[120px] h-[120px] bg-[#EEF2FF] rounded-[40px] flex items-center justify-center mb-8 shadow-2xl shadow-primary/20 rotate-3 transition-transform hover:rotate-0 duration-500">
           <img src="/logo.png" className="w-[72px] h-[72px] object-contain" alt="Logo" />
        </div>

        <h1 className="text-[32px] font-[1000] text-[#1E293B] tracking-tighter uppercase leading-none">Ascend <span className="text-[#6C5CE7]">Invest</span></h1>
        <p className="text-[11px] font-[800] text-[#94A3B8] uppercase tracking-[0.4em] mt-3">Elite Capital Management</p>

        <div className="w-full bg-white rounded-[48px] p-10 mt-12 shadow-[0_32px_64px_-12px_rgba(0,0,0,0.12)] border border-white">
          <h2 className="text-[34px] font-[1000] text-[#1E293B] leading-[0.9] tracking-tighter">Secure Your<br /><span className="text-[#6C5CE7]">Wealth</span></h2>

          <p className="text-[15px] leading-relaxed text-[#64748B] mt-6 font-medium">
            Institutional grade infrastructure for the modern investor. Smart, simple, and secured by encryption.
          </p>

          <div className="mt-12 space-y-4">
            <button
              onClick={() => navigate('/login')}
              className="w-full h-[72px] bg-[#1E293B] hover:bg-[#6C5CE7] text-white font-black text-[16px] rounded-[24px] shadow-2xl transition-all active:scale-[0.98] uppercase tracking-widest"
            >
              Access Portal
            </button>

            <button
              onClick={() => navigate('/register')}
              className="w-full h-[72px] bg-gray-50 border border-gray-100 text-[#1E293B] font-black text-[16px] rounded-[24px] hover:border-[#6C5CE7] transition-all uppercase tracking-widest"
            >
              Register Node
            </button>
          </div>
        </div>

      </div>

      <div className="mt-20 flex items-center gap-10 opacity-30 grayscale pointer-events-none">
         <img src="/logo.png" className="h-6 w-auto" alt="" />
         <img src="/logo.png" className="h-6 w-auto" alt="" />
         <img src="/logo.png" className="h-6 w-auto" alt="" />
      </div>
    </div>
  );
};

export default Welcome;

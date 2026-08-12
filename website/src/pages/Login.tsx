import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Fingerprint } from 'lucide-react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [authStep, setAuthStep] = useState(0);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const data = await api.post('/auth/login', { email, password });
      setAuthStep(1); // Show Biometric Scan

      setTimeout(() => setAuthStep(2), 2000); // Verify
      setTimeout(() => {
        login(data.token, data.user);
        setLoading(false);
        navigate('/');
      }, 3500);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] flex flex-col items-center justify-center font-sans text-[#1E293B] px-6 relative">

      {loading && (
        <div className="fixed inset-0 z-[300] bg-[#1E293B] flex flex-col items-center justify-center text-white animate-in fade-in duration-500">
           <div className="relative">
              <div className="w-32 h-32 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                 {authStep === 1 ? <Fingerprint size={48} className="text-primary animate-pulse" /> : <ShieldCheck size={48} className="text-secondary" />}
              </div>
           </div>
           <h3 className="text-[20px] font-black mt-10 tracking-widest uppercase italic text-center px-6">
             {authStep === 1 ? 'Initializing Biometric Scan' : 'Vault Access Authorized'}
           </h3>
           <p className="text-[12px] text-white/40 mt-2 font-mono uppercase tracking-[0.3em]">
             {authStep === 1 ? 'Verifying Neural Signature...' : 'Decrypting Session Keys...'}
           </p>
        </div>
      )}

      <div className="w-full max-w-md flex flex-col items-center">
        <div className="w-16 h-16 bg-[#EEF2FF] rounded-[20px] flex items-center justify-center mb-6 shadow-xl -rotate-6 cursor-pointer" onClick={() => navigate('/welcome')}>
           <img src="/logo.png" className="w-8 h-8 object-contain" alt="" />
        </div>
        <h1 className="text-[26px] font-[1000] text-[#1E293B] tracking-tighter uppercase leading-none mb-10">Ascend <span className="text-[#6C5CE7]">Portal</span></h1>

        <div className="w-full bg-white rounded-[40px] p-10 shadow-2xl border border-white">
          <h2 className="text-[24px] font-[1000] text-[#1E293B] tracking-tight uppercase">Authorized Login</h2>
          {error && <p className="mt-4 text-xs font-bold text-error bg-error/5 p-3 rounded-xl border border-error/10 uppercase tracking-widest">{error}</p>}

          <form onSubmit={handleLogin} className="mt-8 space-y-5">
            <div className="space-y-1.5">
               <label className="text-[10px] font-black text-[#94A3B8] uppercase tracking-[0.2em] ml-1">Identity Protocol</label>
               <input
                type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                placeholder="Secure ID (Email)"
                className="w-full h-[64px] px-6 bg-[#F8FAFC] border border-gray-100 focus:border-[#6C5CE7] focus:bg-white rounded-[20px] text-[15px] font-bold outline-none transition-all shadow-inner"
                required
               />
            </div>

            <div className="space-y-1.5">
               <label className="text-[10px] font-black text-[#94A3B8] uppercase tracking-[0.2em] ml-1">Vault Key</label>
               <input
                type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                placeholder="Access Password"
                className="w-full h-[64px] px-6 bg-[#F8FAFC] border border-gray-100 focus:border-[#6C5CE7] focus:bg-white rounded-[20px] text-[15px] font-bold outline-none transition-all shadow-inner"
                required
               />
            </div>

            <button
              type="submit" disabled={loading}
              className="w-full h-[68px] bg-[#1E293B] hover:bg-[#6C5CE7] text-white font-[1000] text-[16px] rounded-[24px] mt-8 shadow-2xl transition-all uppercase tracking-widest active:scale-[0.98]"
            >
              Initialize Secure Link
            </button>

            <div className="text-center pt-8 border-t border-gray-50 flex flex-col gap-6">
              <p className="text-[14px] text-[#64748B] font-bold">
                Not operational? <button type="button" onClick={() => navigate('/register')} className="text-[#6C5CE7] font-black ml-1 uppercase hover:underline">Register Node</button>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;

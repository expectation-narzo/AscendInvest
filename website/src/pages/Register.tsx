import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { User, Mail, Lock, Link as LinkIcon, ArrowRight, ShieldCheck, Zap } from 'lucide-react';
import { api } from '../services/api';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    referralCode: ''
  });

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Use email prefix as temporary username to satisfy DB unique constraint
      const username = formData.email.split('@')[0] + Math.floor(Math.random() * 1000);

      await api.post('/auth/register', {
        ...formData,
        username
      });

      alert('Registration successful! Please login.');
      navigate('/login');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] relative overflow-hidden flex flex-col items-center justify-center font-sans text-text-dark p-6">

      <div className="absolute inset-0 bg-dot-pattern opacity-[0.3]"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] bg-secondary/5 rounded-full blur-[120px] animate-glow"></div>
      <div className="absolute top-[-5%] left-[-5%] w-[400px] h-[400px] bg-primary/5 rounded-full blur-[100px] animate-glow" style={{ animationDelay: '1.5s' }}></div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-lg z-10"
      >
        <div className="flex flex-col items-center mb-8 group cursor-pointer" onClick={() => navigate('/welcome')}>
           <div className="relative">
              <div className="absolute inset-0 bg-secondary/20 blur-xl group-hover:bg-secondary/40 transition-all"></div>
              <div className="w-16 h-16 bg-white border-2 border-gray-100 rounded-[22px] flex items-center justify-center shadow-xl relative z-10 transition-transform group-hover:-rotate-12 duration-500">
                 <img src="/logo.png" className="w-10 h-10" alt="Logo" />
              </div>
           </div>
           <h1 className="text-[26px] font-[1000] text-text-dark tracking-tighter uppercase mt-5">Alpha <span className="text-secondary">Node Registry</span></h1>
        </div>

        <div className="bg-white rounded-[48px] p-10 md:p-14 shadow-[0_40px_80px_-15px_rgba(0,0,0,0.1)] border border-white relative overflow-hidden">
          <div className="absolute top-0 right-0 w-40 h-40 bg-secondary/5 rounded-full blur-3xl -mr-20 -mt-20"></div>

          <div className="relative z-10">
            <div className="flex items-center gap-3 mb-3">
               <Zap size={22} className="text-secondary" fill="currentColor" />
               <h2 className="text-[24px] font-[1000] text-text-dark tracking-tight leading-none uppercase">Join the Protocol</h2>
            </div>
            <p className="text-[14px] text-text-gray font-semibold mb-8 italic underline decoration-secondary decoration-2 underline-offset-4">Secure Network Initialization</p>

            {error && <p className="mb-6 text-xs font-bold text-error bg-error/5 p-3 rounded-xl border border-error/10 uppercase tracking-widest">{error}</p>}

            <form onSubmit={handleRegister} className="space-y-6">

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2 group">
                  <label className="text-[11px] font-black text-text-light uppercase ml-1 tracking-[0.1em]">Full Name</label>
                  <div className="relative">
                     <User className="absolute left-6 top-1/2 -translate-y-1/2 text-text-light group-focus-within:text-secondary transition-colors" size={18} />
                     <input
                      type="text" value={formData.fullName} onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                      placeholder="Investor Name"
                      className="w-full h-[64px] pl-14 pr-6 bg-[#F8FAFC] border-2 border-transparent focus:border-secondary/40 focus:bg-white rounded-[24px] text-[15px] font-[750] text-text-dark outline-none transition-all shadow-inner"
                      required
                     />
                  </div>
                </div>

                <div className="space-y-2 group">
                  <label className="text-[11px] font-black text-text-light uppercase ml-1 tracking-[0.1em]">Email</label>
                  <div className="relative">
                     <Mail className="absolute left-6 top-1/2 -translate-y-1/2 text-text-light group-focus-within:text-secondary transition-colors" size={18} />
                     <input
                      type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})}
                      placeholder="Node Identifier"
                      className="w-full h-[64px] pl-14 pr-6 bg-[#F8FAFC] border-2 border-transparent focus:border-secondary/40 focus:bg-white rounded-[24px] text-[15px] font-[750] text-text-dark outline-none transition-all shadow-inner"
                      required
                     />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2 group">
                  <label className="text-[11px] font-black text-text-light uppercase ml-1 tracking-[0.1em]">Vault key</label>
                  <div className="relative">
                     <Lock className="absolute left-6 top-1/2 -translate-y-1/2 text-text-light group-focus-within:text-secondary transition-colors" size={18} />
                     <input
                      type="password" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})}
                      placeholder="Access Password"
                      className="w-full h-[64px] pl-14 pr-6 bg-[#F8FAFC] border-2 border-transparent focus:border-secondary/40 focus:bg-white rounded-[24px] text-[15px] font-[750] text-text-dark outline-none transition-all shadow-inner"
                      required
                     />
                  </div>
                </div>

                <div className="space-y-2 group">
                  <label className="text-[11px] font-black text-text-light uppercase ml-1 tracking-[0.1em]">Ref Protocol</label>
                  <div className="relative">
                     <LinkIcon className="absolute left-6 top-1/2 -translate-y-1/2 text-text-light group-focus-within:text-secondary transition-colors" size={18} />
                     <input
                      type="text" value={formData.referralCode} onChange={(e) => setFormData({...formData, referralCode: e.target.value})}
                      placeholder="Affiliate Link"
                      className="w-full h-[64px] pl-14 pr-6 bg-[#F8FAFC] border-2 border-transparent focus:border-secondary/40 focus:bg-white rounded-[24px] text-[15px] font-[750] text-text-dark outline-none transition-all shadow-inner"
                     />
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="group/btn w-full h-[72px] bg-[#1E293B] hover:bg-secondary text-white font-[900] text-[17px] rounded-[24px] mt-10 shadow-2xl shadow-black/10 flex items-center justify-center gap-4 transition-all relative overflow-hidden active:scale-[0.98] disabled:opacity-70"
              >
                <div className="absolute inset-0 bg-white/10 translate-x-[-100%] group-hover/btn:translate-x-[100%] transition-transform duration-700"></div>
                <ShieldCheck size={24} />
                <span>{loading ? 'DEPLOYING NODE...' : 'INITIALIZE REGISTRY'}</span>
                <ArrowRight size={20} className="group-hover/btn:translate-x-1.5 transition-transform" />
              </button>

              <div className="text-center pt-8 border-t border-gray-50 flex flex-col gap-6">
                 <p className="text-[14px] font-bold text-text-gray">
                   Node already operational? <button type="button" onClick={() => navigate('/login')} className="text-secondary font-black ml-1 hover:underline">SYNC ACCESS</button>
                 </p>
              </div>
            </form>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default Register;

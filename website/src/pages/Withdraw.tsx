import React, { useState, useEffect } from 'react';
import {
  ArrowUpCircle,
  Wallet,
  ShieldAlert,
  CheckCircle2,
  ArrowRight,
  Lock,
  History,
  Activity,
  ExternalLink,
  Info,
  ShieldCheck,
  Cpu,
  RefreshCcw,
  DollarSign
} from 'lucide-react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { io } from 'socket.io-client';

const socket = io('http://localhost:5000');

const Withdraw: React.FC = () => {
  const { user } = useAuth();
  const [amount, setAmount] = useState('');
  const [userAddress, setUserAddress] = useState('');
  const [availableBalance, setAvailableBalance] = useState(0);
  const [history, setHistory] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentStep, setCurrentStep] = useState(1);

  const fetchStatus = async () => {
    try {
      const dashData = await api.get('/user/dashboard');
      if (dashData) {
        setAvailableBalance(Number(dashData.unlocked_balance) || 0);
      }

      const historyData = await api.get('/user/transactions?type=withdraw');
      if (Array.isArray(historyData)) {
        setHistory(historyData);
      }
    } catch (err) {
      console.error('Failed to sync liquidation data:', err);
    }
  };

  useEffect(() => {
    fetchStatus();

    if (user?.id) {
      socket.emit('join', user.id);

      socket.on('withdrawal_approved', (data) => {
        console.log('Withdrawal approved:', data);
        fetchStatus();
        alert('Your withdrawal has been approved!');
      });
    }

    return () => {
      socket.off('withdrawal_approved');
    };
  }, [user]);

  const initiateWithdrawal = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(amount);
    if (isNaN(val) || val < 20) {
      setError('Minimum liquidation threshold is $20.00');
      return;
    }
    if (val > availableBalance) {
      setError('Insufficient liquid yield in your node.');
      return;
    }
    setError('');
    setCurrentStep(2);
  };

  const handleAuthorization = async () => {
    setLoading(true);
    setError('');
    try {
      await api.post('/transactions/withdraw', { amount, userWalletAddress: userAddress });
      setCurrentStep(3);
      setTimeout(() => {
        setAmount('');
        setUserAddress('');
        fetchStatus();
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Authorization failed');
      setCurrentStep(1);
    } finally {
      setLoading(false);
    }
  };

  const safeFormatDate = (dateStr: string) => {
    try {
      if (!dateStr) return 'Pending...';
      return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    } catch (e) {
      return 'Recent';
    }
  };

  return (
    <div className="p-6 md:p-10 space-y-10 font-sans max-w-[1400px] mx-auto min-h-full">

      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-4 md:px-0">
        <div>
           <p className="text-error text-[12px] font-[800] uppercase tracking-[0.25em] mb-2">Liquidity Exit Protocol</p>
           <h1 className="text-[32px] font-[900] text-[#1E293B] tracking-tight">Withdraw Funds</h1>
           <p className="text-[15px] text-[#64748B] mt-2 font-medium">Extract optimized alpha yields and matured capital nodes back to your secure vault.</p>
        </div>
        <div className="bg-white rounded-3xl p-5 shadow-sm border border-gray-100 flex items-center gap-5 group hover:shadow-xl transition-all">
           <div className="w-12 h-12 bg-error/10 rounded-2xl flex items-center justify-center text-error group-hover:rotate-12 transition-transform">
              <Activity size={24} />
           </div>
           <div>
             <p className="text-[10px] font-[900] text-[#94A3B8] uppercase tracking-widest">Available Liquidity</p>
             <p className="text-[22px] font-[1000] text-[#1E293B] tracking-tighter">${(availableBalance || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
           </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 px-4 md:px-0">

        <div className="lg:col-span-8 space-y-8">
          <div className="bg-white rounded-[40px] border border-[#E2E8F0] p-8 md:p-14 shadow-sm relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-80 h-80 bg-error/5 rounded-full blur-[100px] -mr-40 -mt-40 group-hover:bg-error/10 transition-all duration-1000"></div>

            <div className="relative z-10">

              <div className="flex items-center gap-4 mb-14 overflow-x-auto pb-4 scrollbar-hide">
                 {[
                   { id: 1, label: 'Configuration' },
                   { id: 2, label: 'Security Check' },
                   { id: 3, label: 'Finalization' }
                 ].map((s) => (
                   <div key={s.id} className="flex items-center gap-3 flex-shrink-0">
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center text-[12px] font-black border-2 transition-all ${currentStep >= s.id ? 'bg-error border-error text-white' : 'bg-white border-gray-100 text-[#CBD5E1]'}`}>
                         {currentStep > s.id ? <CheckCircle2 size={16} /> : s.id}
                      </div>
                      <span className={`text-[11px] font-black uppercase tracking-widest ${currentStep >= s.id ? 'text-error' : 'text-[#CBD5E1]'}`}>{s.label}</span>
                      {s.id < 3 && <div className="w-8 h-px bg-gray-100 mx-2"></div>}
                   </div>
                 ))}
              </div>

              {currentStep === 1 && (
                <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
                  <div className="flex justify-between items-center mb-10">
                    <h3 className="text-[20px] font-[900] text-[#1E293B]">Payout Parameters</h3>
                    <div className="flex items-center gap-2 bg-error/5 px-4 py-2 rounded-xl border border-error/10 shadow-inner">
                       <Lock size={14} className="text-error" />
                       <span className="text-[11px] font-[800] text-error uppercase tracking-widest text-center">SECURE GCM-256</span>
                    </div>
                  </div>

                  <div className="bg-[#FFF1F2] rounded-[28px] p-6 flex items-center mb-12 border border-[#FFE4E6]">
                     <div className="w-14 h-14 bg-white rounded-2xl flex items-center justify-center shadow-sm border border-gray-100">
                        <img src="/ic_usdt.png" className="w-8 h-8 object-contain" alt="USDT" />
                     </div>
                     <div className="ml-5 flex-1">
                       <p className="text-[16px] font-[900] text-[#1E293B]">USDT Settlement</p>
                       <p className="text-[13px] text-error font-[700] mt-1 tracking-tight italic">Layer 2 Transfer Protocol: BEP20 (BSC)</p>
                     </div>
                  </div>

                  <form onSubmit={initiateWithdrawal} className="space-y-10">
                    {error && <p className="p-4 bg-error/5 border border-error/10 text-error text-xs font-black uppercase tracking-widest rounded-2xl">{error}</p>}

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                       <div className="space-y-3">
                          <label className="text-[11px] font-black text-[#94A3B8] uppercase ml-1 tracking-widest">Liquidation Quantum (USD)</label>
                          <div className="relative">
                             <DollarSign className="absolute left-6 top-1/2 -translate-y-1/2 text-[#CBD5E1]" size={22} />
                             <input
                               type="number" value={amount} onChange={(e) => setAmount(e.target.value)}
                               placeholder="Min 20.00"
                               className="w-full h-[76px] pl-16 pr-24 bg-[#F8FAFC] border-2 border-transparent focus:bg-white focus:border-error rounded-[28px] text-[22px] font-[1000] text-[#1E293B] outline-none transition-all shadow-inner"
                               required
                             />
                             <button type="button" onClick={() => setAmount(availableBalance.toString())} className="absolute right-4 top-1/2 -translate-y-1/2 bg-error/5 text-error text-[11px] font-black px-4 py-2 rounded-xl hover:bg-error hover:text-white transition-all">MAX</button>
                          </div>
                       </div>
                       <div className="space-y-3">
                          <label className="text-[11px] font-black text-[#94A3B8] uppercase ml-1 tracking-widest">Destination Node Address</label>
                          <div className="relative">
                             <Wallet className="absolute left-6 top-1/2 -translate-y-1/2 text-[#CBD5E1]" size={22} />
                             <input
                               type="text" value={userAddress} onChange={(e) => setUserAddress(e.target.value)}
                               placeholder="Authorized Address"
                               className="w-full h-[76px] pl-16 pr-6 bg-[#F8FAFC] border-2 border-transparent focus:bg-white focus:border-error rounded-[28px] text-[15px] font-[800] text-[#1E293B] outline-none transition-all shadow-inner"
                               required
                             />
                          </div>
                       </div>
                    </div>

                    <button type="submit" className="w-full h-[76px] bg-error hover:bg-[#D45459] text-white font-[1000] text-[18px] rounded-[28px] shadow-2xl shadow-error/30 flex items-center justify-center gap-4 group/btn relative overflow-hidden active:scale-[0.98] transition-all">
                       <div className="absolute inset-0 bg-white/20 translate-x-[-100%] group-hover/btn:translate-x-[100%] transition-transform duration-1000"></div>
                       <span>INITIALIZE EXIT PROTOCOL</span>
                       <ArrowRight size={22} strokeWidth={2.5} className="group-hover/btn:translate-x-2 transition-transform" />
                    </button>
                  </form>
                </div>
              )}

              {currentStep === 2 && (
                <div className="animate-in zoom-in-95 duration-500 flex flex-col items-center py-10">
                   <div className="w-24 h-24 bg-error/10 rounded-full flex items-center justify-center relative mb-10">
                      <div className="absolute inset-0 border-4 border-error border-t-transparent rounded-full animate-spin"></div>
                      <ShieldCheck size={40} className="text-error" />
                   </div>
                   <h3 className="text-[24px] font-[1000] text-[#1E293B] uppercase tracking-tighter text-center">Security Authorization</h3>
                   <p className="text-[15px] text-[#64748B] mt-2 font-medium max-w-sm text-center px-4">We are verifying your neural session signature and checking node liquidity status.</p>

                   <div className="w-full max-w-md mt-12 bg-[#F8FAFC] p-8 rounded-[32px] border border-gray-100 space-y-6">
                      <div className="flex justify-between items-center pb-4 border-b border-gray-100">
                         <span className="text-[12px] font-black text-[#94A3B8] uppercase">Authorization Quantum</span>
                         <span className="text-[16px] font-[900] text-[#1E293B]">${(Number(amount) || 0).toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between items-center pb-4 border-b border-gray-100">
                         <span className="text-[12px] font-black text-[#94A3B8] uppercase">Exit Gas Fee</span>
                         <span className="text-[14px] font-[800] text-error">$1.50</span>
                      </div>
                      <div className="flex justify-between items-center">
                         <span className="text-[12px] font-black text-[#94A3B8] uppercase tracking-widest">Link Status</span>
                         <span className="text-[12px] font-black text-secondary flex items-center gap-2 italic uppercase"><div className="w-1.5 h-1.5 bg-secondary rounded-full animate-pulse"></div> Link Active</span>
                      </div>
                   </div>

                   <div className="flex gap-4 w-full max-w-md mt-12">
                      <button onClick={() => setCurrentStep(1)} className="flex-1 h-16 bg-[#F8FAFC] text-[#64748B] font-black rounded-2xl hover:bg-gray-100 transition-all uppercase tracking-widest">CANCEL</button>
                      <button onClick={handleAuthorization} disabled={loading} className="flex-[2] h-16 bg-error text-white font-black rounded-2xl shadow-xl shadow-error/20 hover:opacity-90 active:scale-95 transition-all uppercase tracking-widest">
                         {loading ? 'Processing...' : 'AUTHORIZE PAYOUT'}
                      </button>
                   </div>
                </div>
              )}

              {currentStep === 3 && (
                <div className="animate-in zoom-in-95 duration-500 flex flex-col items-center py-20">
                   <div className="w-24 h-24 bg-secondary/20 rounded-full flex items-center justify-center text-secondary mb-10 shadow-2xl shadow-secondary/30 scale-125">
                      <CheckCircle2 size={50} strokeWidth={3} />
                   </div>
                   <h3 className="text-[28px] font-[1000] text-[#1E293B] uppercase tracking-tighter text-center">Liquidation Initiated</h3>
                   <p className="text-[16px] text-[#64748B] mt-3 font-medium text-center max-w-sm leading-relaxed px-6">
                      Your asset exit request has been added to the distributed ledger.
                      Audit completion expected within 24 business hours.
                   </p>
                   <button
                    onClick={() => setCurrentStep(1)}
                    className="mt-12 h-16 px-12 bg-[#1E293B] text-white font-black rounded-2xl shadow-xl transition-all hover:bg-[#6C5CE7] uppercase tracking-[0.2em]"
                   >
                      RETURN TO TERMINAL
                   </button>
                </div>
              )}

            </div>
          </div>

          <div className="bg-[#FEF2F2] rounded-[32px] p-8 border border-[#FEE2E2] flex gap-6 items-start group/warn relative overflow-hidden">
             <div className="absolute top-0 right-0 w-32 h-32 bg-error/5 rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform duration-1000"></div>
             <div className="w-14 h-14 bg-white rounded-2xl flex items-center justify-center shadow-sm flex-shrink-0 relative z-10">
                <ShieldAlert className="text-error" size={28} />
             </div>
             <div className="relative z-10">
                <p className="text-[15px] font-[900] text-[#991B1B] uppercase tracking-wide mb-1.5 italic underline decoration-error/30 underline-offset-4">Distributed Ledger Protocol</p>
                <p className="text-[14px] text-[#B91C1C] leading-relaxed font-semibold opacity-80">
                  Please strictly verify your destination address. Liquidation to the decentralized ledger is irreversible.
                  Assets sent to incorrect node identifiers cannot be recovered by the protocol.
                </p>
             </div>
          </div>
        </div>

        <div className="lg:col-span-4 space-y-10">

          <div className="bg-white rounded-[40px] border border-[#E2E8F0] shadow-sm overflow-hidden flex flex-col group hover:shadow-xl transition-all h-fit">
            <div className="p-8 border-b border-[#F1F5F9] flex items-center justify-between bg-[#F8FAFC]">
              <div className="flex items-center gap-3">
                 <div className="w-10 h-10 bg-error/10 rounded-xl flex items-center justify-center text-error border border-error/5">
                    <History size={22} />
                 </div>
                 <h3 className="text-[18px] font-[900] text-[#1E293B]">Payout Ledger</h3>
              </div>
              <Info size={16} className="text-[#CBD5E1]" />
            </div>

            <div className="p-5 space-y-4 max-h-[600px] overflow-y-auto custom-scrollbar">
              {history.map((item) => (
                <div key={item.id} className="p-6 rounded-[28px] bg-[#F8F9FA] border border-transparent hover:border-[#E2E8F0] hover:bg-white transition-all group/item cursor-pointer shadow-sm">
                  <div className="flex justify-between items-start">
                    <div className="flex gap-4">
                       <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-sm border border-gray-50">
                          <Cpu size={22} className="text-error opacity-70 group-hover/item:opacity-100" />
                       </div>
                       <div>
                          <p className="text-[15px] font-[900] text-[#1E293B] leading-tight group-hover/item:text-error transition-colors tracking-tight">{item.title}</p>
                          <p className="text-[11px] text-[#64748B] font-black uppercase tracking-widest mt-1">{safeFormatDate(item.created_at)}</p>
                       </div>
                    </div>
                    <span className={`text-[10px] font-black px-3 py-1.5 rounded-full uppercase tracking-tighter shadow-sm border border-transparent ${item.status === 'Success' ? 'bg-[#ECFDF5] text-[#28C76F]' : 'bg-[#FFF7ED] text-[#FF9F43]'}`}>
                      {item.status}
                    </span>
                  </div>
                  <div className="mt-8 flex items-center justify-between border-t border-gray-100 pt-4">
                     <p className="text-[20px] font-[1000] text-[#1E293B] tracking-tighter">${item.amount.toString().replace('-$', '')}</p>
                     <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-sm hover:bg-error hover:text-white transition-all">
                        <ExternalLink size={16} />
                     </div>
                  </div>
                </div>
              ))}
              {history.length === 0 && (
                <div className="py-20 text-center flex flex-col items-center">
                   <div className="w-16 h-16 bg-surface rounded-full flex items-center justify-center mb-6 opacity-40">
                      <RefreshCcw size={32} />
                   </div>
                   <p className="text-[13px] font-black text-[#94A3B8] uppercase tracking-[0.2em]">Synchronizing Ledger...</p>
                </div>
              )}
            </div>

            <div className="p-8 bg-[#F8FAFC] border-t border-gray-100 text-center">
               <button className="text-[12px] font-black text-[#94A3B8] hover:text-error transition-all uppercase tracking-[0.3em] flex items-center justify-center gap-2 mx-auto">
                  View Full Audit Trail <ArrowRight size={14} />
               </button>
            </div>
          </div>

          <div className="bg-[#1E293B] rounded-[40px] p-10 text-white relative overflow-hidden shadow-2xl border border-white/5 group h-fit">
             <div className="absolute inset-0 bg-error/10 opacity-0 group-hover:opacity-100 transition-opacity duration-1000"></div>
             <div className="relative z-10">
                <div className="flex gap-5 items-center mb-8">
                   <div className="w-14 h-14 bg-white/5 rounded-2xl flex items-center justify-center text-error border border-white/10 group-hover:scale-110 transition-transform">
                      <Lock size={32} strokeWidth={1.5} />
                   </div>
                   <div>
                      <h4 className="text-[18px] font-[900] tracking-tight uppercase italic leading-none">Vault Security</h4>
                      <p className="text-[10px] font-black text-error uppercase tracking-[0.2em] mt-1.5">Strict Liquidity Protocol</p>
                   </div>
                </div>
                <p className="text-[15px] text-white/40 leading-relaxed font-medium">To maintain node cluster stability, liquidations are audited against historical arbitrage performance and node maturation cycles.</p>
                <div className="mt-10 pt-10 border-t border-white/5 flex items-center justify-between">
                   <div className="space-y-1">
                      <p className="text-[10px] text-white/30 uppercase font-black tracking-widest">Encryption</p>
                      <p className="text-[13px] font-black text-secondary uppercase tracking-[0.1em]">AES-256 GCM</p>
                   </div>
                   <ShieldCheck size={48} className="text-secondary opacity-10" strokeWidth={1} />
                </div>
             </div>
             <img src="/logo.png" className="absolute right-[-60px] bottom-[-60px] w-56 h-56 opacity-[0.03] rotate-[-20deg]" alt="" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Withdraw;

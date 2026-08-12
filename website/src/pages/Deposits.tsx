import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { io } from 'socket.io-client';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Wallet, ArrowRight, CheckCircle2, Copy, History, ShieldCheck, Zap, Clock, ExternalLink,
  ChevronRight, AlertCircle, QrCode, Lock, ArrowLeft, BadgeCheck, Globe,
  Loader2, TrendingUp, Shield, Activity, Fingerprint, RefreshCcw, MousePointer2, Cpu,
  Network, Gauge, Terminal, Workflow, Info, Check, Timer, BarChart3, Radio, ShieldAlert,
  Search, Filter, ListRestart, HelpCircle, X, Share2, Download, ListChecks, ShieldQuestion, Printer, FileDown, SlidersHorizontal, Eye, Server,
  Command, Layers, Target, Binary, Box, HardDrive, ShieldPlus, Radar, Signal, Share, ScanLine,
  ChevronDown, Settings, Database, Waves
} from 'lucide-react';

const socket = io('http://localhost:5000');

const cryptoOptions = [
  { id: 'USDT', name: 'Tether', network: 'BEP20 (BSC)', icon: 'https://cryptologos.cc/logos/tether-usdt-logo.png?v=040', color: '#26A17B', explorer: 'https://bscscan.com/tx/', arrival: '2-5m', confirmations: 15, price: 1.00, health: 98, volatility: 'Stable', sparkline: [1, 1, 1, 1, 1, 1, 1], networkLoad: 24, marketCap: '$112B', shard: 'SHARD-01' },
  { id: 'BTC', name: 'Bitcoin', network: 'BTC Network', icon: 'https://cryptologos.cc/logos/bitcoin-btc-logo.png?v=040', color: '#F7931A', explorer: 'https://www.blockchain.com/explorer/transactions/btc/', arrival: '30-60m', confirmations: 2, price: 64231.50, health: 100, volatility: 'High', sparkline: [62, 63, 61, 65, 64, 66, 64], networkLoad: 88, marketCap: '$1.3T', shard: 'CORE-L1' },
  { id: 'ETH', name: 'Ethereum', network: 'ERC20', icon: 'https://cryptologos.cc/logos/ethereum-eth-logo.png?v=040', color: '#627EEA', explorer: 'https://etherscan.io/tx/', arrival: '5-15m', confirmations: 12, price: 3421.20, health: 95, volatility: 'Medium', sparkline: [3.2, 3.4, 3.1, 3.5, 3.4, 3.6, 3.4], networkLoad: 65, marketCap: '$450B', shard: 'SHARD-09' },
  { id: 'BNB', name: 'BNB', network: 'BEP20 (BSC)', icon: 'https://cryptologos.cc/logos/bnb-bnb-logo.png?v=040', color: '#F3BA2F', explorer: 'https://bscscan.com/tx/', arrival: '2-5m', confirmations: 15, price: 582.40, health: 99, volatility: 'Medium', sparkline: [550, 580, 570, 600, 590, 610, 582], networkLoad: 31, marketCap: '$90B', shard: 'SHARD-03' }
];

const validatorNodes = [
  { id: 'vNode-Alpha', location: 'Singapore', load: '12%', ping: '42ms' },
  { id: 'vNode-Zion', location: 'Frankfurt', load: '45%', ping: '118ms' },
  { id: 'vNode-Tokyo', location: 'Tokyo', load: '8%', ping: '24ms' }
];

const Deposits: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [selectedCrypto, setSelectedCrypto] = useState('USDT');
  const [selectedNode, setSelectedNode] = useState(validatorNodes[0]);
  const [amount, setAmount] = useState('');
  const [userAddress, setUserAddress] = useState('');
  const [txId, setTxId] = useState('');
  const [depositAddress, setDepositAddress] = useState('Fetching...');
  const [history, setHistory] = useState<any[]>([]);
  const [ongoing, setOngoing] = useState<any[]>([]);
  const [walletBalance, setWalletBalance] = useState('$0.00');
  const [loading, setLoading] = useState(false);
  const [initLoading, setInitLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);
  const [priority, setPriority] = useState('High');
  const [terminalLogs, setTerminalLogs] = useState<string[]>(['> Treasury Node v4.0 Active.', '> AES-GCM Encrypted Link...']);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTx, setSelectedTx] = useState<any>(null);
  const [showChecklist, setShowChecklist] = useState(false);
  const [filterStatus, setFilterStatus] = useState('All');
  const [nodeCount, setNodeCount] = useState(0);
  const [nodeStats, setNodeStats] = useState({ latency: '--', consensus: '99.99%', identifier: 'SCANNING...' });
  const [showSettings, setShowSettings] = useState(false);

  const currentCrypto = useMemo(() => cryptoOptions.find(c => c.id === selectedCrypto) || cryptoOptions[0], [selectedCrypto]);

  const cryptoAmount = useMemo(() => {
    const val = parseFloat(amount);
    if (isNaN(val)) return '0.0000';
    return (val / currentCrypto.price).toFixed(selectedCrypto === 'BTC' ? 8 : 4);
  }, [amount, currentCrypto, selectedCrypto]);

  const filteredHistory = useMemo(() => {
    return history.filter(tx => {
      const matchesSearch = (tx.transaction_id || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                            (tx.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                            (tx.amount || '').toString().includes(searchQuery);
      const matchesFilter = filterStatus === 'All' || tx.status === filterStatus;
      return matchesSearch && matchesFilter;
    });
  }, [history, searchQuery, filterStatus]);

  const addLog = (msg: string) => setTerminalLogs(prev => [...prev.slice(-3), `> ${msg}`]);

  const fetchStatus = useCallback(async () => {
    try {
      const addrData = await api.get(`/settings/deposit-address?crypto=${selectedCrypto}`);
      setDepositAddress(addrData.address || '0x...');
      const dashData = await api.get('/user/dashboard');
      setWalletBalance(`$${dashData.wallet_balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}`);

      const [historyData, ongoingData] = await Promise.all([
        api.get('/user/transactions?type=deposit'),
        api.get('/user/ongoing-deposits')
      ]);
      setHistory(historyData || []);
      setOngoing(ongoingData || []);
    } catch (err) {
      console.error(err);
    } finally {
      setInitLoading(false);
    }
  }, [selectedCrypto]);

  useEffect(() => {
    fetchStatus();
    if (user?.id) {
      socket.emit('join', user.id);
      socket.on('deposit_approved', () => {
        fetchStatus();
        addLog("PROTOCOL ALERT: Ledger finalized.");
      });
    }
    return () => { socket.off('deposit_approved'); };
  }, [user, fetchStatus]);

  useEffect(() => {
    if (step === 3) {
      const interval = setInterval(() => {
        setNodeCount(prev => prev < 12 ? prev + 1 : 12);
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [step]);

  const initiateHandshake = async () => {
    setLoading(true);
    addLog(`Handshaking: ${currentCrypto.id} @ ${selectedNode.id}...`);
    try {
      const handshake = await api.post('/protocol/handshake', { crypto: selectedCrypto, priority, nodeId: selectedNode.id });
      setNodeStats({
        latency: handshake.node_latency || selectedNode.ping,
        consensus: handshake.validator_consensus || '99.9%',
        identifier: handshake.node_identifier || selectedNode.id
      });
      setShowChecklist(true);
    } catch (err: any) {
      addLog("Node Error: Handshake timed out.");
      alert("Handshake Failed: Ensure your node server is online.");
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(depositAddress);
    setCopied(true);
    addLog("Buffer: Vault address isolated.");
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    addLog(`Broadcasting injection stack to ${selectedNode.id}...`);
    try {
      const numAmount = parseFloat(amount);
      if (isNaN(numAmount) || numAmount < 10) throw new Error('Min $10.00');

      const response = await api.post('/transactions/deposit', {
        amount: numAmount.toFixed(2),
        userWalletAddress: userAddress,
        transactionId: txId,
        crypto: selectedCrypto,
        priority: priority,
        validatorNode: selectedNode.id
      });

      addLog(`Signature: ${(response.signature || '').substring(0, 12)}`);
      setStep(3);
      setAmount(''); setUserAddress(''); setTxId('');
      fetchStatus();
    } catch (err: any) {
      setError(err.message);
      addLog(`CRITICAL: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleExportHistory = (format: 'csv' | 'json') => {
    if (format === 'csv') {
      const csvContent = "data:text/csv;charset=utf-8,"
        + ["Date,ID,Asset,Amount,Status,Node"].join(",") + "\n"
        + history.map(tx => `${new Date(tx.created_at).toLocaleString()},${tx.transaction_id},${tx.title},${tx.amount},${tx.status},${tx.validator_node}`).join("\n");
      const link = document.createElement("a");
      link.setAttribute("href", encodeURI(csvContent));
      link.setAttribute("download", `ledger_${Date.now()}.csv`);
      document.body.appendChild(link); link.click(); document.body.removeChild(link);
    } else {
      const blob = new Blob([JSON.stringify(history, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `ledger_${Date.now()}.json`;
      document.body.appendChild(a); a.click(); window.URL.revokeObjectURL(url); document.body.removeChild(a);
    }
  };

  const downloadReceipt = (tx: any) => {
    const content = `ASCEND INVEST - AUDIT RECEIPT\n--------------------------\nID: ${tx.transaction_id}\nAsset: ${tx.title || tx.crypto}\nAmount: $${tx.amount}\nNode: ${tx.validator_node || 'N/A'}\nStatus: ${tx.status || 'Syncing'}\n--------------------------`;
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = `receipt_${tx.transaction_id}.txt`;
    document.body.appendChild(a); a.click(); window.URL.revokeObjectURL(url); document.body.removeChild(a);
  };

  const Sparkline = ({ data, color }: { data: number[], color: string }) => (
    <svg width="40" height="16" viewBox="0 0 60 24" className="opacity-40">
      <polyline
        fill="none" stroke={color} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"
        points={data.map((val, i) => `${(i * 60) / (data.length - 1)},${24 - (val / Math.max(...data)) * 20}`).join(' ')}
      />
    </svg>
  );

  if (initLoading) {
    return <div className="flex items-center justify-center min-h-screen bg-white"><Loader2 size={24} className="animate-spin text-primary" /></div>;
  }

  return (
    <div className="flex flex-col min-h-screen bg-[#FDFDFF] font-sans text-slate-800 pb-8 selection:bg-primary/10 relative overflow-hidden">

      {/* Background Layers */}
      <div className="absolute inset-0 z-0 opacity-[0.03] pointer-events-none" style={{ backgroundImage: 'radial-gradient(#1E293B 1px, transparent 1px)', backgroundSize: '24px 24px' }} />

      <header className="sticky top-0 z-[100] bg-white/70 backdrop-blur-md border-b border-slate-100 px-4 md:px-6 py-2 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <motion.button onClick={() => window.history.back()} whileTap={{ scale: 0.95 }} className="w-8 h-8 rounded-lg bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-primary transition-all">
            <ArrowLeft size={16} />
          </motion.button>
          <div className="flex flex-col">
             <h2 className="text-[10px] font-black text-slate-800 uppercase tracking-widest leading-none">Treasury <span className="text-primary">X-Node</span></h2>
             <span className="text-[6px] font-black text-slate-300 uppercase tracking-widest mt-1">v4.0 Protocol</span>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden md:flex items-center gap-6 px-6 border-r border-slate-100 mr-2">
             <div className="flex flex-col">
                <span className="text-[7px] font-black text-slate-300 uppercase tracking-widest">Network Gas</span>
                <span className="text-[10px] font-black text-slate-600 flex items-center gap-1"><Zap size={10} className="text-amber-400" /> 24 Gwei</span>
             </div>
             <div className="flex flex-col">
                <span className="text-[7px] font-black text-slate-300 uppercase tracking-widest">Global Nodes</span>
                <span className="text-[10px] font-black text-slate-600 flex items-center gap-1"><Globe size={10} className="text-primary" /> 1,402 Online</span>
             </div>
          </div>
          <div className="flex flex-col items-end">
            <span className="text-[7px] font-black text-slate-400 uppercase tracking-widest leading-none">Net Balance</span>
            <p className="text-[13px] font-black text-slate-900 tracking-tight mt-0.5">{walletBalance}</p>
          </div>
          <div className="w-8 h-8 bg-slate-950 text-white rounded-lg flex items-center justify-center border-2 border-white rotate-3 shadow-lg">
             <Fingerprint size={16} />
          </div>
        </div>
      </header>

      {/* Sub-Header Telemetry Bar */}
      <div className="bg-slate-950 text-white py-1.5 overflow-hidden border-b border-white/5 relative z-[90]">
         <motion.div
            animate={{ x: [0, -1000] }}
            transition={{ duration: 30, repeat: Infinity, ease: 'linear' }}
            className="flex items-center gap-12 whitespace-nowrap px-4"
         >
            {[...Array(3)].map((_, i) => (
               <React.Fragment key={i}>
                  <span className="text-[7px] font-black uppercase tracking-[0.2em] flex items-center gap-2"><div className="w-1 h-1 bg-primary rounded-full" /> BTC_SHARD: $64,231.50 (+1.2%)</span>
                  <span className="text-[7px] font-black uppercase tracking-[0.2em] flex items-center gap-2"><div className="w-1 h-1 bg-emerald-500 rounded-full" /> USDT_PROTOCOL: STABLE @ 1.00</span>
                  <span className="text-[7px] font-black uppercase tracking-[0.2em] flex items-center gap-2"><div className="w-1 h-1 bg-primary rounded-full" /> ETH_INDEX: $3,421.20 (-0.4%)</span>
                  <span className="text-[7px] font-black uppercase tracking-[0.2em] flex items-center gap-2"><div className="w-1 h-1 bg-amber-500 rounded-full" /> BNB_SHARD: $582.40 (+0.8%)</span>
               </React.Fragment>
            ))}
         </motion.div>
      </div>

      <main className="max-w-[1200px] mx-auto w-full px-4 md:px-6 mt-6 grid grid-cols-1 lg:grid-cols-12 gap-6 relative z-10">

        <div className="lg:col-span-8 space-y-4">
          <section className="bg-white rounded-3xl border border-slate-100 shadow-sm p-5 md:p-8 relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-48 h-48 bg-primary/5 rounded-full blur-[60px] -mr-24 -mt-24 pointer-events-none" />

            <div className="relative z-10">
              <header className="mb-6 flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                <div className="space-y-1">
                   <div className="flex items-center gap-2">
                      <span className="px-2 py-0.5 bg-slate-950 text-white text-[7px] font-black uppercase tracking-widest rounded-full flex items-center gap-1">
                         <ShieldPlus size={8} className="text-primary" /> Multi-Sig
                      </span>
                      <span className="px-2 py-0.5 bg-emerald-50 text-emerald-600 text-[7px] font-black uppercase tracking-widest rounded-full border border-emerald-100">
                         Nodes Active
                      </span>
                   </div>
                   <h1 className="text-[24px] md:text-[28px] font-black text-slate-900 tracking-tight leading-none italic">
                      Asset <span className="text-primary underline decoration-slate-100 decoration-4 underline-offset-4">Injection</span>
                   </h1>
                </div>
                <div className="flex items-center gap-2">
                   <button onClick={() => setShowSettings(true)} className="p-2 bg-slate-50 rounded-xl text-slate-400 hover:text-primary transition-all border border-slate-100"><Settings size={14} /></button>
                   <div className="flex items-center gap-3 bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-100">
                      <div className="flex flex-col text-right">
                         <span className="text-[7px] font-black text-slate-400 uppercase tracking-widest">{currentCrypto.id} Shard</span>
                         <p className="text-[11px] font-black text-slate-800 leading-none mt-0.5">${currentCrypto.price.toLocaleString()}</p>
                      </div>
                      <TrendingUp size={14} className="text-emerald-500" />
                   </div>
                </div>
              </header>

              <div className="flex items-center gap-3 mb-8 overflow-x-auto pb-2 scrollbar-hide">
                {[1, 2, 3].map((s, i) => (
                  <React.Fragment key={s}>
                    <div className="flex items-center gap-2 flex-shrink-0 cursor-pointer" onClick={() => step > s && setStep(s)}>
                      <div className={`w-7 h-7 rounded-lg flex items-center justify-center text-[10px] font-black border transition-all ${step >= s ? 'bg-slate-900 border-slate-900 text-white rotate-3 shadow-md' : 'bg-white border-slate-100 text-slate-200'}`}>
                        {step > s ? <Check size={14} strokeWidth={4} /> : `0${s}`}
                      </div>
                      <span className={`text-[9px] font-black uppercase tracking-widest ${step >= s ? 'text-slate-900' : 'text-slate-300'}`}>
                        {s === 1 ? 'Asset' : s === 2 ? 'Vault' : 'Audit'}
                      </span>
                    </div>
                    {i < 2 && <div className={`w-6 h-px rounded-full transition-all ${step > i+1 ? 'bg-primary' : 'bg-slate-100'}`} />}
                  </React.Fragment>
                ))}
              </div>

              <AnimatePresence mode="wait">
                {step === 1 && (
                  <motion.div key="st1" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }} className="space-y-6">
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                      {cryptoOptions.map((crypto) => (
                        <button
                          key={crypto.id} onClick={() => setSelectedCrypto(crypto.id)}
                          className={`flex flex-col items-center p-3 rounded-2xl border-2 transition-all ${
                            selectedCrypto === crypto.id ? 'border-primary bg-primary/[0.02] shadow-sm' : 'border-slate-50 bg-white hover:border-slate-200'
                          }`}
                        >
                          <div className="w-full flex justify-between items-start mb-2 px-1">
                             <img src={crypto.icon} className="w-6 h-6 object-contain" alt="" />
                             <Sparkline data={crypto.sparkline} color={crypto.color} />
                          </div>
                          <div className="w-full text-left px-1">
                             <span className={`text-[12px] font-black uppercase tracking-tight ${selectedCrypto === crypto.id ? 'text-slate-900' : 'text-slate-400'}`}>{crypto.id}</span>
                             <div className="flex items-center justify-between mt-0.5">
                                <span className="text-[6px] font-black text-slate-300 uppercase tracking-widest">{crypto.shard}</span>
                                <div className={`w-1 h-1 rounded-full ${crypto.health > 95 ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
                             </div>
                          </div>
                        </button>
                      ))}
                    </div>

                    <div className="bg-slate-900 rounded-2xl p-4 text-white grid md:grid-cols-2 gap-6 relative overflow-hidden border border-white/5">
                       <div className="space-y-4 relative z-10">
                          <div className="flex items-center justify-between">
                             <div className="flex items-center gap-3">
                                <div className="w-10 h-10 bg-white/5 rounded-xl flex items-center justify-center text-primary border border-white/10 shadow-inner"><Binary size={20} /></div>
                                <div>
                                   <h4 className="text-[13px] font-black tracking-tight uppercase italic leading-none">{currentCrypto.name} Gateway</h4>
                                   <p className="text-[8px] text-white/40 font-black uppercase tracking-widest mt-1.5">{currentCrypto.network}</p>
                                </div>
                             </div>
                          </div>

                          <div className="space-y-2">
                             <label className="text-[7px] font-black text-white/30 uppercase tracking-[0.2em] ml-1">Connect to Validator Node</label>
                             <div className="grid grid-cols-3 gap-2">
                                {validatorNodes.map(node => (
                                  <button key={node.id} onClick={() => setSelectedNode(node)} className={`p-2 rounded-xl border transition-all text-left group ${selectedNode.id === node.id ? 'bg-primary border-primary' : 'bg-white/5 border-white/10 hover:border-white/20'}`}>
                                     <p className={`text-[8px] font-black uppercase ${selectedNode.id === node.id ? 'text-white' : 'text-white/40'}`}>{node.id}</p>
                                     <div className="flex items-center justify-between mt-1">
                                        <span className={`text-[6px] font-black ${selectedNode.id === node.id ? 'text-white/70' : 'text-white/20'}`}>{node.ping}</span>
                                        <div className={`w-1 h-1 rounded-full ${parseFloat(node.load) < 20 ? 'bg-emerald-400' : 'bg-amber-400'}`} />
                                     </div>
                                  </button>
                                ))}
                             </div>
                          </div>
                       </div>
                       <div className="flex flex-col justify-center space-y-2 relative z-10 font-mono text-[9px] text-white/30 uppercase tracking-widest">
                          <div className="flex justify-between items-center py-1.5 border-b border-white/5">
                             <span className="flex items-center gap-2"><Signal size={10} className="text-primary" /> Health</span>
                             <span className="text-emerald-400 font-black">{currentCrypto.health}.98%</span>
                          </div>
                          <div className="flex justify-between items-center py-1.5 border-b border-white/5">
                             <span className="flex items-center gap-2"><Clock size={10} className="text-primary" /> Arrival</span>
                             <span className="text-white font-black">{currentCrypto.arrival}</span>
                          </div>
                          <div className="flex justify-between items-center">
                             <span className="flex items-center gap-2"><Layers size={10} className="text-primary" /> Protocol</span>
                             <span className="text-emerald-400 font-black italic">Optimized</span>
                          </div>
                       </div>
                    </div>

                    <button onClick={initiateHandshake} disabled={loading} className="w-full h-12 bg-primary hover:bg-[#5B4BC9] text-white font-black text-[13px] rounded-xl shadow-lg flex items-center justify-center gap-3 active:scale-95 border-b-4 border-black/10 uppercase tracking-widest">
                      {loading ? <Loader2 size={18} className="animate-spin" /> : <>ESTABLISH HANDSHAKE <ArrowRight size={16} strokeWidth={3} /></>}
                    </button>
                  </motion.div>
                )}

                {step === 2 && (
                  <motion.div key="st2" initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
                       <div className="bg-slate-50 border border-slate-100 rounded-[32px] p-6 flex flex-col items-center shadow-inner relative overflow-hidden group/qr">
                          <div className="relative bg-white border border-slate-200 p-4 rounded-2xl shadow-lg transition-transform group-hover/qr:scale-105 duration-500">
                             <img src={`https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${depositAddress}`} className="w-28 h-28 mix-blend-multiply" alt="" />
                             <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-8 h-8 bg-white rounded-lg shadow-md flex items-center justify-center p-1.5 border border-slate-50 rotate-12"><img src={currentCrypto.icon} className="w-full h-full object-contain" alt="" /></div>
                          </div>
                          <div className="flex gap-2 mt-4">
                            <button className="w-8 h-8 bg-white rounded-lg text-slate-400 hover:text-primary transition-all border border-slate-200 shadow-sm flex items-center justify-center"><Download size={14} /></button>
                            <button className="w-8 h-8 bg-white rounded-lg text-slate-400 hover:text-primary transition-all border border-slate-200 shadow-sm flex items-center justify-center"><Share2 size={14} /></button>
                          </div>
                       </div>
                       <div className="space-y-6">
                          <div className="space-y-1.5">
                             <label className="text-[8px] font-black text-slate-400 uppercase tracking-widest ml-2 italic">Vault Address</label>
                             <div className="relative group">
                                <div className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl p-4 pr-12 font-mono text-[10px] text-slate-600 break-all leading-relaxed shadow-inner min-h-[90px] flex items-center transition-all">{depositAddress}</div>
                                <button onClick={handleCopy} className="absolute right-2 top-1/2 -translate-y-1/2 w-9 h-9 bg-white rounded-lg flex items-center justify-center text-primary shadow-md hover:bg-slate-900 hover:text-white transition-all active:scale-90 border border-slate-100">{copied ? <Check size={16} strokeWidth={4} /> : <Copy size={16} />}</button>
                             </div>
                          </div>
                          <div className="bg-slate-900 rounded-xl p-4 border border-white/5 flex gap-4 items-center shadow-lg">
                             <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-primary"><Server size={16} /></div>
                             <div>
                                <p className="text-[7px] font-black text-white/30 uppercase tracking-widest leading-none">Validator ID</p>
                                <p className="text-[10px] font-black text-white leading-none mt-1 tracking-widest">{nodeStats.identifier}</p>
                             </div>
                             <div className="ml-auto text-right">
                                <p className="text-[7px] font-black text-white/30 uppercase tracking-widest leading-none">Latency</p>
                                <p className="text-[10px] font-black text-emerald-400 leading-none mt-1">{nodeStats.latency}</p>
                             </div>
                          </div>
                       </div>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4 bg-slate-50 rounded-3xl p-5 md:p-6 border border-slate-100 shadow-inner">
                       <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                         <div className="space-y-1.5">
                            <label className="text-[8px] font-black text-slate-400 uppercase tracking-widest ml-2">Injection Quantum (USD)</label>
                            <div className="relative group">
                               <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300 font-black group-focus-within:text-primary transition-colors text-lg">$</div>
                               <input type="number" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0.00" className="w-full h-11 pl-8 pr-4 bg-white border-2 border-slate-100 focus:border-primary rounded-xl text-[18px] font-black text-slate-900 outline-none" required />
                            </div>
                         </div>
                         <div className="space-y-1.5">
                            <label className="text-[8px] font-black text-slate-400 uppercase tracking-widest ml-2">TxHash / Signature</label>
                            <div className="relative group">
                               <input type="text" value={txId} onChange={(e) => setTxId(e.target.value)} placeholder="Ox... Signature" className="w-full h-11 px-4 bg-white border-2 border-slate-100 focus:border-primary rounded-xl text-[12px] font-black text-slate-900 outline-none font-mono" required />
                               <div className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-200"><Signal size={14} /></div>
                            </div>
                         </div>
                       </div>

                       <div className="grid grid-cols-3 gap-3">
                          {['Standard', 'High', 'Critical'].map(p => (
                            <button key={p} type="button" onClick={() => setPriority(p)} className={`h-9 rounded-xl text-[9px] font-black uppercase tracking-widest transition-all border-2 relative overflow-hidden ${priority === p ? 'bg-slate-900 text-white border-slate-900' : 'bg-white text-slate-300 border-slate-100'}`}>
                               {priority === p && <motion.div layoutId="prio_v4" className="absolute inset-0 bg-primary/20" />}
                               <span className="relative z-10 flex items-center justify-center gap-1.5">{priority === p && <Zap size={10} fill="currentColor" />} {p}</span>
                            </button>
                          ))}
                       </div>

                       <div className="bg-[#0F172A] rounded-xl p-4 font-mono text-[9px] text-primary/70 space-y-1 border border-white/5 shadow-xl">
                          {terminalLogs.map((log, i) => (<motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }} key={i} className="leading-tight">{log}</motion.p>))}
                          <div className="flex items-center gap-1.5 text-white/10 mt-1 italic"><span className="animate-pulse">_</span><span>Handshake sync persistent...</span></div>
                       </div>

                       <button type="submit" disabled={loading} className="w-full h-12 bg-slate-900 hover:bg-black text-white font-black text-[13px] rounded-xl shadow-lg transition-all active:scale-95 uppercase tracking-widest flex items-center justify-center gap-3 border-b-4 border-slate-700/50">
                         {loading ? <Loader2 size={20} className="animate-spin" /> : <>SYNCHRONIZE ASSET <Command size={16} /></>}
                       </button>
                    </form>
                  </motion.div>
                )}

                {step === 3 && (
                  <motion.div key="st3" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="flex flex-col items-center py-12 text-center">
                    <div className="w-20 h-20 bg-emerald-500/10 rounded-2xl flex items-center justify-center text-emerald-500 mb-6 shadow-xl border border-emerald-100 rotate-12 relative overflow-hidden group"><motion.div initial={{ scale: 0 }} animate={{ scale: 2 }} transition={{ type: 'spring' }} className="absolute inset-0 bg-emerald-500/5" /><CheckCircle2 size={40} strokeWidth={3} className="relative z-10" /></div>
                    <h3 className="text-[20px] md:text-[24px] font-black text-slate-900 uppercase tracking-tighter italic leading-none">Injection <span className="text-slate-200">Authorized.</span></h3>
                    <div className="w-full max-w-sm mt-8 bg-slate-50/50 rounded-2xl p-6 border border-slate-100 space-y-4 shadow-inner">
                       <div className="flex justify-between items-center text-[9px] font-black uppercase tracking-widest text-slate-400"><span>Nodes Reconciled</span><span className="text-slate-900">{nodeCount} / 12</span></div>
                       <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden shadow-inner"><motion.div animate={{ width: `${(nodeCount/12)*100}%` }} className="h-full bg-emerald-500 shadow-[0_0_10px_rgba(16,185,129,0.5)]" /></div>
                       <div className="flex gap-2 justify-center">{[...Array(12)].map((_, i) => (
                         <div key={i} className={`w-1.5 h-1.5 rounded-full transition-all duration-500 ${i < nodeCount ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)] animate-pulse' : 'bg-slate-200'}`} />
                       ))}</div>
                    </div>
                    <div className="flex gap-3 w-full max-w-xs mt-10"><button onClick={() => setStep(1)} className="flex-1 h-11 bg-white border border-slate-200 text-slate-400 font-black rounded-lg text-[10px] uppercase shadow-sm">New Link</button><button onClick={() => window.location.href='/dashboard'} className="flex-[1.5] h-11 bg-slate-900 text-white font-black rounded-lg text-[10px] uppercase shadow-lg hover:bg-black transition-all">Command Hub</button></div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </section>

          {/* Ongoing Injections Table */}
          <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden flex flex-col">
            <div className="p-4 border-b border-slate-50 flex items-center justify-between bg-slate-50/20 backdrop-blur-md">
              <div className="flex items-center gap-3"><div className="w-8 h-8 bg-primary text-white rounded-lg flex items-center justify-center shadow-md"><Radio size={16} strokeWidth={2.5} className="animate-pulse" /></div><h3 className="text-[13px] font-black text-slate-900 uppercase leading-none italic">Active Handshakes</h3></div>
              <span className="text-[8px] font-black text-primary uppercase tracking-widest bg-primary/5 px-2 py-1 rounded-full border border-primary/10">{ongoing.length} Active</span>
            </div>
            <div className="p-0 overflow-x-auto"><table className="w-full text-left border-collapse"><thead className="bg-slate-50/50"><tr><th className="px-5 py-3 text-[8px] font-black text-slate-400 uppercase tracking-widest border-b border-slate-100">Handshake ID</th><th className="px-5 py-3 text-[8px] font-black text-slate-400 uppercase tracking-widest border-b border-slate-100">Quantum (USD)</th><th className="px-5 py-3 text-[8px] font-black text-slate-400 uppercase tracking-widest border-b border-slate-100">Directive</th><th className="px-5 py-3 text-[8px] font-black text-slate-400 uppercase tracking-widest border-b border-slate-100 text-right">Node State</th></tr></thead><tbody className="divide-y divide-slate-50">
              {ongoing.map((item, idx) => (
                <tr key={idx} onClick={() => setSelectedTx({ ...item, type: 'ongoing' })} className="group hover:bg-slate-50/50 transition-colors cursor-pointer">
                  <td className="px-5 py-4"><div className="flex items-center gap-3"><div className="w-7 h-7 bg-white rounded-lg flex items-center justify-center border border-slate-200 shadow-sm"><Terminal size={14} className="text-primary" /></div><span className="text-[11px] font-black text-slate-900 uppercase font-mono tracking-tighter">#{(item.transaction_id || '').substring(0,10)}</span></div></td>
                  <td className="px-5 py-4"><span className="text-[13px] font-black text-slate-700 tracking-tight">${parseFloat(item.amount).toLocaleString()}</span></td>
                  <td className="px-5 py-4 flex flex-col"><span className={`text-[8px] font-black px-2 py-0.5 rounded-md border w-fit ${item.priority === 'Critical' ? 'bg-red-50 text-red-600 border-red-100' : item.priority === 'High' ? 'bg-primary/5 text-primary border-primary/10' : 'bg-slate-50 text-slate-500 border-slate-100'}`}>{item.priority || 'Standard'}</span><span className="text-[7px] font-black text-slate-300 uppercase mt-1">Via: {item.validator_node || 'vNode-Alpha'}</span></td>
                  <td className="px-5 py-4 text-right"><div className="flex items-center justify-end gap-2"><div className="w-1.5 h-1.5 bg-amber-500 rounded-full animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.5)]" /><span className="text-[9px] font-black text-amber-600 uppercase tracking-widest italic">Syncing</span></div></td>
                </tr>
              ))}
              {ongoing.length === 0 && (<tr><td colSpan={4} className="px-5 py-12 text-center"><div className="flex flex-col items-center opacity-20"><Box size={24} className="text-slate-400 mb-2" /><p className="text-[9px] font-black uppercase tracking-widest">No handshake signals detected</p></div></td></tr>)}
            </tbody></table></div>
          </div>
        </div>

        {/* Audit Sidebar */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-100 shadow-lg overflow-hidden flex flex-col h-fit max-h-[750px] sticky top-[70px]">
            <div className="p-4 border-b border-slate-50 flex flex-col gap-4 bg-slate-50/30 backdrop-blur-md">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3"><div className="w-8 h-8 bg-slate-950 rounded-lg flex items-center justify-center text-white shadow-lg rotate-3"><History size={16} strokeWidth={2.5} /></div><h3 className="text-[13px] font-black text-slate-900 uppercase leading-none italic">Audit Hub</h3></div>
                <div className="flex items-center gap-2"><button onClick={() => handleExportHistory('csv')} title="Export Ledger" className="p-1.5 bg-white rounded-lg text-slate-400 border border-slate-200 shadow-sm transition-all"><FileDown size={14} /></button><div className="flex items-center gap-1.5 bg-emerald-50 px-2 py-1 rounded-full border border-emerald-100"><div className="w-1 h-1 bg-emerald-500 rounded-full animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.5)]" /><span className="text-[8px] font-black text-emerald-600 uppercase tracking-widest">Live</span></div></div>
              </div>
              <div className="flex gap-2">
                <div className="relative flex-1"><Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" /><input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Search Ledger..." className="w-full h-9 pl-9 pr-4 bg-white border-2 border-slate-100 rounded-xl text-[10px] font-bold outline-none focus:border-primary transition-all shadow-inner" /></div>
                <button onClick={() => setFilterStatus(prev => prev === 'All' ? 'Success' : prev === 'Success' ? 'Pending' : 'All')} className="w-9 h-9 bg-white border-2 border-slate-100 rounded-xl flex items-center justify-center text-slate-400 hover:text-primary transition-all shadow-sm"><SlidersHorizontal size={14} /></button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto custom-scrollbar p-4 space-y-3">
              {filteredHistory.map((item: any, idx) => (
                <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: idx * 0.05 }} key={item.id} onClick={() => setSelectedTx({ ...item, type: 'history' })} className="p-4 rounded-xl bg-white border border-slate-50 hover:border-primary/20 hover:shadow-xl transition-all group/item cursor-pointer relative overflow-hidden shadow-sm">
                  <div className="absolute top-0 right-0 w-16 h-16 bg-slate-50 rounded-full blur-2xl -mr-8 -mt-8 group-hover/item:bg-primary/5 transition-colors" /><div className="flex justify-between items-start mb-3 relative z-10"><div className="flex gap-3"><div className="w-8 h-8 bg-slate-50 rounded-lg flex items-center justify-center border border-slate-100 group-hover/item:bg-white group-hover/item:rotate-12 transition-all shadow-sm"><img src={cryptoOptions.find(c => (item.title || '').includes(c.id))?.icon || cryptoOptions[0].icon} className="w-5 h-5 object-contain" alt="" /></div><div><p className="text-[12px] font-black text-slate-900 leading-none group-hover/item:text-primary transition-colors tracking-tight italic">{item.title}</p><p className="text-[9px] text-slate-400 font-black mt-2 uppercase tracking-widest flex items-center gap-1.5"><Target size={10} className="text-primary" /> Node: {item.validator_node || 'N/A'}</p></div></div><span className={`text-[8px] font-black px-2 py-1 rounded-full uppercase border-2 shadow-sm ${item.status === 'Success' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-amber-50 text-amber-600 border-amber-100'}`}>{item.status}</span></div>
                  <div className="flex items-center justify-between pt-3 border-t border-slate-50 relative z-10"><p className="text-[17px] font-black text-slate-900 tracking-tighter leading-none">${parseFloat(item.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</p><motion.div whileHover={{ scale: 1.1, rotate: 10, backgroundColor: '#1E293B', color: '#fff' }} className="w-7 h-7 rounded-lg bg-slate-50 flex items-center justify-center text-slate-400 border border-slate-100 transition-all shadow-sm"><ChevronRight size={14} strokeWidth={2.5} /></motion.div></div>
                </motion.div>
              ))}
              {filteredHistory.length === 0 && (<div className="py-12 text-center flex flex-col items-center opacity-30"><div className="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center mb-4 shadow-inner border border-slate-100"><Search size={24} className="text-slate-300" /></div><p className="text-[9px] font-black text-slate-400 uppercase tracking-widest italic">Ledger Node Empty</p></div>)}
            </div>
          </div>

          {/* Support Directive */}
          <motion.div onClick={() => navigate('/support')} whileHover={{ y: -3, backgroundColor: '#0F172A' }} className="bg-white rounded-[28px] border border-slate-100 p-5 shadow-lg flex items-center gap-4 group cursor-pointer transition-all duration-300">
             <div className="w-10 h-10 bg-primary text-white rounded-xl flex items-center justify-center shadow-xl shadow-primary/20 group-hover:rotate-12 transition-all"><MousePointer2 size={18} strokeWidth={2.5} /></div>
             <div className="flex-1">
                <p className="text-[13px] font-black text-slate-900 group-hover:text-white uppercase italic leading-none transition-colors">Support Directive</p>
                <p className="text-[9px] text-slate-400 font-medium leading-none transition-colors mt-1.5 uppercase tracking-widest italic">L1 Assistance Active</p>
             </div>
             <ChevronRight size={18} className="text-slate-300 group-hover:text-primary transition-all" />
          </motion.div>
        </div>
      </main>

      {/* Transaction Details Modal */}
      <AnimatePresence>
        {selectedTx && (
          <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setSelectedTx(null)} className="absolute inset-0 bg-slate-950/70 backdrop-blur-sm" />
            <motion.div initial={{ opacity: 0, scale: 0.95, y: 20 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.95, y: 20 }} className="relative w-full max-w-lg bg-white rounded-[40px] shadow-2xl border border-slate-200 overflow-hidden">
              <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50"><div className="flex items-center gap-3"><div className="w-10 h-10 bg-slate-900 rounded-xl flex items-center justify-center text-white shadow-lg"><Binary size={20} /></div><div><h3 className="text-[16px] font-black text-slate-900 uppercase tracking-tighter italic">Ledger Details</h3><p className="text-[9px] text-slate-400 font-black uppercase tracking-widest">Index: #{(selectedTx.transaction_id || '').substring(0,14)}</p></div></div><button onClick={() => setSelectedTx(null)} className="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-red-500 transition-all shadow-sm"><X size={16} strokeWidth={3} /></button></div>
              <div className="p-8 space-y-8"><div className="flex flex-col items-center text-center"><div className={`w-20 h-20 rounded-[28px] flex items-center justify-center mb-4 shadow-xl border-4 border-white ${selectedTx.status === 'Success' ? 'bg-emerald-500 text-white' : 'bg-amber-500 text-white'}`}>{selectedTx.status === 'Success' ? <CheckCircle2 size={40} /> : <Timer size={40} className="animate-pulse" />}</div><h2 className="text-[32px] font-black text-slate-900 tracking-tighter leading-none">${parseFloat(selectedTx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</h2><p className={`mt-2 text-[10px] font-black px-4 py-1.5 rounded-full uppercase tracking-[0.2em] border-2 shadow-inner ${selectedTx.status === 'Success' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-amber-50 text-amber-600 border-amber-100'}`}>Protocol Node Verified</p></div>
                 <div className="grid grid-cols-2 gap-4"><div className="p-5 bg-slate-50 rounded-[24px] border border-slate-100 space-y-1 shadow-inner"><p className="text-[8px] font-black text-slate-400 uppercase tracking-widest">Protocol Hub</p><p className="text-[13px] font-black text-slate-900 italic flex items-center gap-2"><img src={cryptoOptions.find(c => (selectedTx.title || '').includes(c.id))?.icon || cryptoOptions[0].icon} className="w-4 h-4 object-contain" alt="" />{selectedTx.title || 'L1 Injection'}</p></div><div className="p-5 bg-slate-50 rounded-[24px] border border-slate-100 space-y-1 shadow-inner"><p className="text-[8px] font-black text-slate-400 uppercase tracking-widest">Validator Signal</p><p className="text-[13px] font-black text-slate-900 uppercase tracking-tighter">{selectedTx.priority || 'Standard Tier'}</p></div><div className="col-span-2 p-5 bg-slate-50 rounded-[24px] border border-slate-100 space-y-1 shadow-inner"><p className="text-[8px] font-black text-slate-400 uppercase tracking-widest">Network TxHash</p><p className="text-[11px] font-mono text-slate-600 break-all leading-relaxed font-bold">{(selectedTx.transaction_hash || selectedTx.tx_hash || 'SHA256:AUTH').toUpperCase()}</p></div><div className="col-span-2 p-5 bg-slate-50 rounded-[24px] border border-slate-100 space-y-1 shadow-inner"><p className="text-[8px] font-black text-slate-400 uppercase tracking-widest">Zero-Proof Signature</p><p className="text-[11px] font-mono text-primary break-all leading-relaxed font-black">{selectedTx.protocol_data || selectedTx.protocol_signature || 'AES-GCM-AUTH-256'}</p></div></div>
                 <div className="flex gap-4"><motion.a whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} href={`${cryptoOptions.find(c => (selectedTx.title || '').includes(c.id))?.explorer}${selectedTx.transaction_hash || selectedTx.tx_hash}`} target="_blank" rel="noreferrer" className="flex-1 h-14 bg-slate-900 text-white rounded-2xl flex items-center justify-center gap-3 text-[12px] font-black uppercase tracking-widest shadow-xl"><ExternalLink size={18} /> Explorer Link</motion.a><motion.button onClick={() => downloadReceipt(selectedTx)} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} className="w-14 h-14 bg-slate-50 border border-slate-200 rounded-2xl flex items-center justify-center text-slate-400 hover:text-primary transition-all shadow-sm"><Download size={20} /></motion.button></div></div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Safety Directive Modal */}
      <AnimatePresence>
        {showChecklist && (
          <div className="fixed inset-0 z-[250] flex items-center justify-center p-4">
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setShowChecklist(false)} className="absolute inset-0 bg-slate-950/80 backdrop-blur-md" />
            <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }} className="relative w-full max-w-md bg-white rounded-[40px] shadow-2xl p-8 border-4 border-slate-100 overflow-hidden">
               <div className="absolute top-0 right-0 w-32 h-32 bg-primary/10 rounded-full blur-3xl -mr-16 -mt-16" />
               <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-6 shadow-inner border border-primary/20"><ShieldPlus size={32} /></div>
               <h3 className="text-[22px] font-black text-slate-900 uppercase tracking-tighter italic leading-none">Safety Directive</h3>
               <p className="text-[14px] text-slate-400 mt-4 leading-relaxed font-medium">Verify protocol isolation parameters before node exposure. Errors result in capital dissociation.</p>
               <div className="mt-8 space-y-4">{[ `Deploy ${currentCrypto.id} protocol shard`, `Network sync via ${currentCrypto.network}`, 'Acknowledge validator priority' ].map((text, i) => (<div key={i} className="flex items-center gap-4 p-4 bg-slate-50 rounded-2xl border-2 border-slate-100 shadow-inner group hover:bg-white transition-all"><div className="w-6 h-6 rounded-lg bg-white border-2 border-slate-200 flex items-center justify-center text-emerald-500 group-hover:border-emerald-500 transition-all shadow-sm"><Check size={14} strokeWidth={4} /></div><span className="text-[11px] font-black text-slate-500 uppercase tracking-widest">{text}</span></div>))}</div>
               <button onClick={() => { setShowChecklist(false); setStep(2); addLog("Safety verified. revealing vault..."); }} className="w-full h-14 bg-slate-900 text-white font-black rounded-2xl mt-8 shadow-xl hover:bg-black transition-all uppercase tracking-[0.2em] text-[12px] border-b-4 border-slate-700">AUTHORIZE NODE LINK</button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Settings Modal */}
      <AnimatePresence>
        {showSettings && (
          <div className="fixed inset-0 z-[300] flex items-center justify-center p-4">
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setShowSettings(false)} className="absolute inset-0 bg-slate-950/50 backdrop-blur-sm" />
            <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }} className="relative w-full max-w-sm bg-white rounded-[32px] shadow-2xl p-6 border border-slate-100">
               <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-3"><Settings size={18} className="text-primary" /><h3 className="text-[14px] font-black text-slate-900 uppercase tracking-widest">Protocol Config</h3></div>
                  <button onClick={() => setShowSettings(false)} className="text-slate-400 hover:text-red-500 transition-colors"><X size={18} /></button>
               </div>
               <div className="space-y-4">
                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
                     <span className="text-[10px] font-black text-slate-500 uppercase">Auto-Reconcile</span>
                     <div className="w-10 h-5 bg-emerald-500 rounded-full relative p-1"><div className="w-3 h-3 bg-white rounded-full absolute right-1" /></div>
                  </div>
                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
                     <span className="text-[10px] font-black text-slate-500 uppercase">Multi-Sig Guard</span>
                     <div className="w-10 h-5 bg-emerald-500 rounded-full relative p-1"><div className="w-3 h-3 bg-white rounded-full absolute right-1" /></div>
                  </div>
                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
                     <span className="text-[10px] font-black text-slate-500 uppercase">L1 Sync Mode</span>
                     <span className="text-[9px] font-black text-primary uppercase">Optimized</span>
                  </div>
               </div>
               <button onClick={() => setShowSettings(false)} className="w-full h-11 bg-slate-900 text-white font-black rounded-xl mt-6 uppercase text-[10px] tracking-widest">Apply Parameters</button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default Deposits;

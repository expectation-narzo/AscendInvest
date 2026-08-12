import React, { useEffect, useState } from 'react';
import Chart from 'react-apexcharts';
import GlobalAnnouncement from '../components/GlobalAnnouncement';
import LiveActivityFeed from '../components/LiveActivityFeed';
import PortfolioDiversity from '../components/PortfolioDiversity';
import InvestmentCalculator from '../components/InvestmentCalculator';
import NodeRecalibrationModal from '../components/NodeRecalibrationModal';
import LiveMarketGrid from '../components/LiveMarketGrid';
import IntelligenceCenter from '../components/IntelligenceCenter';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { io } from 'socket.io-client';

const socket = io('http://localhost:5000');

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [data, setData] = useState<any>(null);
  const [isRecalibrateOpen, setIsRecalibrateOpen] = useState(false);

  const fetchDashboard = async () => {
    try {
      const result = await api.get('/user/dashboard');
      setData(result);
    } catch (err) {
      console.error('Failed to sync intelligence:', err);
    }
  };

  useEffect(() => {
    fetchDashboard();

    if (user?.id) {
      socket.emit('join', user.id);

      const handleUpdate = () => {
        console.log('Balance update triggered');
        fetchDashboard();
      };

      socket.on('deposit_approved', handleUpdate);
      socket.on('withdrawal_approved', handleUpdate);

      return () => {
        socket.off('deposit_approved', handleUpdate);
        socket.off('withdrawal_approved', handleUpdate);
      };
    }
  }, [user]);

  if (!data) return <div className="flex items-center justify-center min-h-[400px]"><div className="w-12 h-12 border-4 border-[#6C5CE7]/20 border-t-[#6C5CE7] rounded-full animate-spin"></div></div>;

  const getChartOptions = (color: string) => ({
    chart: { type: 'area', toolbar: { show: false }, sparkline: { enabled: true } },
    stroke: { curve: 'smooth', width: 3, colors: [color] },
    fill: {
      type: 'gradient',
      gradient: {
        shadeIntensity: 1,
        type: "vertical",
        opacityFrom: 0.5,
        opacityTo: 0.0,
        stops: [0, 100]
      }
    },
    tooltip: { enabled: false },
    colors: [color]
  });

  return (
    <div className="flex flex-col font-sans pb-10">
      <GlobalAnnouncement />

      <div className="flex flex-col px-[24px] pt-[32px] pb-[16px]">
        <p className="text-[#6C5CE7] text-[12px] font-[700] uppercase tracking-[0.1em] leading-tight">Institutional Overview</p>
        <h1 className="text-[24px] font-[900] text-[#1E293B] mt-[4px]">Command Center</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-[24px] px-[20px]">
        <div className="lg:col-span-8 space-y-[20px]">

          <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-[24px] shadow-sm">
            <div className="flex justify-between items-center">
              <div>
                <p className="text-[14px] font-[500] text-[#64748B]">Wallet Balance</p>
                <h3 className="text-[20px] font-[800] text-[#1E293B] mt-[4px] tracking-tight">${data.wallet_balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</h3>
              </div>
              <div className="w-[48px] h-[48px] bg-[#F1F0FF] rounded-full flex items-center justify-center p-[12px]">
                <img src="/ic_wallet.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
              </div>
            </div>
            <p className="text-[12px] font-[600] text-[#94A3B8] uppercase tracking-[0.05em] mt-[16px]">Asset Distribution</p>

            <div className="mt-[20px] space-y-[12px]">
               <div className="bg-[#F8F9FA] p-[16px] rounded-[12px] border border-transparent">
                 <div className="flex items-center justify-between mb-3">
                   <div className="flex items-center gap-2">
                      <img src="/ic_lock.png" className="w-[20px] h-[20px] opacity-40" alt="" />
                      <span className="text-[12px] font-[600] text-[#64748B] uppercase tracking-[0.05em]">Locked Assets</span>
                   </div>
                   <span className="text-[14px] font-[700] text-[#1E293B]">${data.locked_balance.toLocaleString()}</span>
                 </div>
                 <div className="w-full bg-[#E2E8F0] h-[6px] rounded-full overflow-hidden">
                   <div className="bg-[#FF8C42] h-full rounded-full transition-all duration-1000" style={{ width: '80%' }}></div>
                 </div>
               </div>

               <div className="bg-[#F8F9FA] p-[16px] rounded-[12px] border border-transparent">
                 <div className="flex items-center justify-between mb-3">
                   <div className="flex items-center gap-2">
                      <img src="/ic_unlock.png" className="w-[20px] h-[20px]" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
                      <span className="text-[12px] font-[600] text-[#64748B] uppercase tracking-[0.05em]">Available for Withdrawal</span>
                   </div>
                   <span className="text-[14px] font-[700] text-[#1E293B]">${data.unlocked_balance.toLocaleString()}</span>
                 </div>
                 <div className="w-full bg-[#E2E8F0] h-[6px] rounded-full overflow-hidden">
                   <div className="bg-[#6C5CE7] h-full rounded-full transition-all duration-1000" style={{ width: '20%' }}></div>
                 </div>
               </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-[20px]">
            <div className="bg-white rounded-[24px] border border-[#E2E8F0] flex flex-col overflow-hidden shadow-sm">
              <div className="p-[24px]">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="text-[14px] font-[500] text-[#64748B]">Total Capital</p>
                    <h3 className="text-[20px] font-[800] text-[#1E293B] mt-[4px] tracking-tight">${data.total_deposit.toLocaleString('en-US', { minimumFractionDigits: 2 })}</h3>
                  </div>
                  <div className="w-[48px] h-[48px] bg-[#FFF8F1] rounded-full flex items-center justify-center p-[12px]">
                    <img src="/ic_dollar.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(65%) sepia(85%) saturate(1242%) hue-rotate(330deg) brightness(96%) contrast(92%)' }} alt="" />
                  </div>
                </div>
                <p className="text-[12px] font-[600] text-[#94A3B8] uppercase tracking-[0.05em] mt-[16px]">Funding History</p>
              </div>
              <div className="px-[2px] mt-auto">
                <Chart options={getChartOptions('#FF8C42')} series={[{ data: data.deposit_series || [400, 300, 500, 350, 600, 550, 700] }]} type="area" height={140} />
              </div>
            </div>

            <div className="bg-white rounded-[24px] border border-[#E2E8F0] flex flex-col overflow-hidden shadow-sm">
              <div className="p-[24px]">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="text-[14px] font-[500] text-[#64748B]">Net Earnings</p>
                    <h3 className="text-[20px] font-[800] text-[#1E293B] mt-[4px] tracking-tight">${data.total_profit.toLocaleString('en-US', { minimumFractionDigits: 2 })}</h3>
                  </div>
                  <div className="w-[48px] h-[48px] bg-[#F1F0FF] rounded-full flex items-center justify-center p-[12px]">
                    <img src="/ic_money_bag.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
                  </div>
                </div>
                <p className="text-[12px] font-[600] text-[#94A3B8] uppercase tracking-[0.05em] mt-[16px]">Profit Trend</p>
              </div>
              <div className="px-[2px] mt-auto">
                <Chart options={getChartOptions('#6C5CE7')} series={[{ data: data.profit_series || [240, 139, 480, 390, 680, 580, 730] }]} type="area" height={140} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-[24px] shadow-sm">
            <div className="flex justify-between items-center">
              <div>
                <p className="text-[14px] font-[500] text-[#64748B]">Yield Analytics</p>
                <div className="flex items-center gap-2 mt-[4px]">
                  <h3 className="text-[20px] font-[800] text-[#1E293B] tracking-tight">${data.total_profit.toLocaleString('en-US', { minimumFractionDigits: 2 })}</h3>
                  <span className="bg-[#E8F9F1] text-[#28C76F] text-[12px] font-[700] px-[8px] py-[2px] rounded-[6px] border border-[#DCFCE7]">
                    {data.yield_percentage}
                  </span>
                </div>
              </div>
              <div className="w-[48px] h-[48px] bg-[#F0FDF4] rounded-full flex items-center justify-center p-[12px]">
                <img src="/ic_plan.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(53%) sepia(85%) saturate(1242%) hue-rotate(100deg) brightness(96%) contrast(92%)' }} alt="" />
              </div>
            </div>

            <Chart options={getChartOptions('#28C76F')} series={[{ data: data.profit_series || [240, 139, 480, 390, 680, 580, 730] }]} type="area" height={180} className="mt-[24px]" />

            <div className="mt-[16px] p-[16px] bg-[#F8F9FA] rounded-[12px] border border-transparent space-y-[16px]">
               <div>
                 <div className="flex items-center gap-3 mb-[4px]">
                   <div className="w-[32px] h-[32px] bg-[#F1F0FF] rounded-lg flex items-center justify-center p-[6px]">
                     <img src="/ic_dollar.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
                   </div>
                   <span className="text-[14px] font-[500] text-[#666666]">Earnings</span>
                 </div>
                 <h4 className="text-[20px] font-[600] text-[#333333] tracking-tight">${data.total_deposit.toLocaleString()}</h4>
                 <div className="w-full bg-[#F0F0F0] h-[6px] rounded-full overflow-hidden mt-[8px]">
                   <div className="bg-[#6C5CE7] h-full rounded-full" style={{ width: '65%' }}></div>
                 </div>
               </div>

               <div>
                 <div className="flex items-center gap-3 mb-[4px]">
                   <div className="w-[32px] h-[32px] bg-[#E0FCFC] rounded-lg flex items-center justify-center p-[6px]">
                     <img src="/ic_clock.png" className="w-full h-full object-contain opacity-80" style={{ filter: 'invert(65%) sepia(85%) saturate(2000%) hue-rotate(150deg) brightness(96%) contrast(92%)' }} alt="" />
                   </div>
                   <span className="text-[14px] font-[500] text-[#666666]">Profit</span>
                 </div>
                 <h4 className="text-[20px] font-[600] text-[#333333] tracking-tight">${data.total_profit.toLocaleString()}</h4>
                 <div className="w-full bg-[#F0F0F0] h-[6px] rounded-full overflow-hidden mt-[8px]">
                   <div className="bg-[#00CED1] h-full rounded-full" style={{ width: '40%' }}></div>
                 </div>
               </div>
            </div>
          </div>
        </div>

        <div className="lg:col-span-4 space-y-[24px]">
           <IntelligenceCenter />
           <LiveMarketGrid />
           <InvestmentCalculator />
           <PortfolioDiversity />
           <LiveActivityFeed />

           <div className="bg-[#1E293B] rounded-[24px] p-8 text-white relative overflow-hidden shadow-2xl">
              <h4 className="text-[16px] font-[800] mb-4">Institutional Shield</h4>
              <p className="text-[13px] text-white/60 font-medium leading-relaxed">
                 Your node assets are strictly isolated using segregated multi-signature cold storage vaults.
              </p>
              <div className="mt-8 pt-8 border-t border-white/10 flex items-center justify-between">
                 <div>
                    <p className="text-[10px] text-white/40 uppercase font-black">Link Status</p>
                    <p className="text-[13px] text-secondary font-black uppercase mt-1">Authorized</p>
                 </div>
                 <button
                  onClick={() => setIsRecalibrateOpen(true)}
                  className="bg-primary/20 hover:bg-primary/40 text-primary text-[10px] font-black px-4 py-2 rounded-xl border border-primary/20 transition-all"
                 >
                    RECALIBRATE
                 </button>
              </div>
           </div>
        </div>

      </div>

      <NodeRecalibrationModal isOpen={isRecalibrateOpen} onClose={() => setIsRecalibrateOpen(false)} />
    </div>
  );
};

export default Dashboard;

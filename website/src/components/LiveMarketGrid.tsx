import React, { useEffect, useState } from 'react';
import { TrendingUp, TrendingDown, ArrowRight, Activity } from 'lucide-react';

const LiveMarketGrid: React.FC = () => {
  const [marketData, setMarketData] = useState<any[]>([]);

  useEffect(() => {
    const fetchMarket = async () => {
      try {
        const symbols = ['BTCUSDT', 'ETHUSDT', 'SOLUSDT', 'BNBUSDT'];
        const response = await fetch('https://api.binance.com/api/v3/ticker/24hr');
        const data = await response.json();

        const filtered = data
          .filter((item: any) => symbols.includes(item.symbol))
          .map((item: any) => ({
            symbol: item.symbol.replace('USDT', ''),
            fullName: item.symbol === 'BTCUSDT' ? 'Bitcoin' : item.symbol === 'ETHUSDT' ? 'Ethereum' : item.symbol === 'SOLUSDT' ? 'Solana' : 'Binance Coin',
            price: parseFloat(item.lastPrice).toLocaleString(undefined, { minimumFractionDigits: 2 }),
            change: parseFloat(item.priceChangePercent).toFixed(2),
            volume: (parseFloat(item.quoteVolume) / 1000000).toFixed(2) + 'M',
            up: parseFloat(item.priceChangePercent) >= 0
          }));

        setMarketData(filtered);
      } catch (error) {
        console.error('Market fetch error:', error);
      }
    };

    fetchMarket();
    const itv = setInterval(fetchMarket, 5000);
    return () => clearInterval(itv);
  }, []);

  return (
    <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-6 shadow-sm">
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-primary/10 rounded-lg flex items-center justify-center text-primary">
            <Activity size={18} />
          </div>
          <h3 className="text-[16px] font-[800] text-[#1E293B]">Live Market Nodes</h3>
        </div>
        <button className="text-[10px] font-black text-primary uppercase tracking-widest hover:underline flex items-center gap-1">
          Full Market <ArrowRight size={12} />
        </button>
      </div>

      <div className="space-y-4">
        {marketData.map((coin, i) => (
          <div key={i} className="flex items-center justify-between p-4 bg-[#F8FAFC] rounded-2xl border border-transparent hover:border-primary/20 hover:bg-white transition-all group">
            <div className="flex items-center gap-4">
               <div className={`w-10 h-10 rounded-xl flex items-center justify-center font-black text-[12px] shadow-sm ${coin.up ? 'bg-secondary/10 text-secondary' : 'bg-error/10 text-error'}`}>
                  {coin.symbol[0]}
               </div>
               <div>
                  <p className="text-[14px] font-black text-[#1E293B]">{coin.symbol}<span className="text-[10px] text-[#94A3B8] ml-2">/USDT</span></p>
                  <p className="text-[11px] text-[#64748B] font-medium">{coin.fullName}</p>
               </div>
            </div>
            <div className="text-right">
               <p className="text-[14px] font-black text-[#1E293B]">${coin.price}</p>
               <div className={`flex items-center justify-end text-[10px] font-black mt-0.5 ${coin.up ? 'text-secondary' : 'text-error'}`}>
                  {coin.up ? '+' : ''}{coin.change}% {coin.up ? <TrendingUp size={10} className="ml-1" /> : <TrendingDown size={10} className="ml-1" />}
               </div>
            </div>
          </div>
        ))}

        {marketData.length === 0 && (
           <div className="py-10 text-center text-[12px] text-[#94A3B8] font-bold uppercase tracking-widest animate-pulse">
              Syncing Ledger...
           </div>
        )}
      </div>
    </div>
  );
};

export default LiveMarketGrid;

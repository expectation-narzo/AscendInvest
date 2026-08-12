import React, { useEffect, useState } from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';

const MarketTicker: React.FC = () => {
  const [prices, setPrices] = useState<any[]>([]);

  useEffect(() => {
    const fetchPrices = async () => {
      try {
        const symbols = ['BTCUSDT', 'ETHUSDT', 'BNBUSDT', 'SOLUSDT', 'ADAUSDT', 'XRPUSDT', 'DOTUSDT'];
        const response = await fetch('https://api.binance.com/api/v3/ticker/24hr');
        const data = await response.json();

        const filtered = data
          .filter((item: any) => symbols.includes(item.symbol))
          .map((item: any) => ({
            symbol: item.symbol.replace('USDT', '/USDT'),
            price: parseFloat(item.lastPrice).toLocaleString(undefined, { minimumFractionDigits: 2 }),
            change: parseFloat(item.priceChangePercent).toFixed(2) + '%',
            up: parseFloat(item.priceChangePercent) >= 0
          }));

        setPrices(filtered);
      } catch (error) {
        console.error('Failed to fetch crypto prices:', error);
      }
    };

    fetchPrices();
    const interval = setInterval(fetchPrices, 10000); // Update every 10s
    return () => clearInterval(interval);
  }, []);

  if (prices.length === 0) return (
    <div className="bg-white/50 backdrop-blur-sm border-b border-[#F1F5F9] h-10 flex items-center px-6">
       <span className="text-[10px] font-black text-[#94A3B8] uppercase tracking-widest animate-pulse">Establishing Market Link...</span>
    </div>
  );

  return (
    <div className="bg-white/50 backdrop-blur-sm border-b border-[#F1F5F9] h-10 flex items-center overflow-hidden">
      <div className="flex animate-ticker whitespace-nowrap gap-10 px-6">
        {[...prices, ...prices].map((coin, i) => (
          <div key={i} className="flex items-center gap-2">
            <span className="text-[11px] font-black text-[#1E293B]">{coin.symbol}</span>
            <span className="text-[11px] font-bold text-[#64748B]">${coin.price}</span>
            <div className={`flex items-center text-[10px] font-black ${coin.up ? 'text-[#28C76F]' : 'text-[#E6656A]'}`}>
              {coin.up ? <TrendingUp size={12} className="mr-1" /> : <TrendingDown size={12} className="mr-1" />}
              {coin.change}
            </div>
          </div>
        ))}
      </div>
      <style>{`
        @keyframes ticker {
          0% { transform: translateX(0); }
          100% { transform: translateX(-50%); }
        }
        .animate-ticker {
          animation: ticker 40s linear infinite;
        }
      `}</style>
    </div>
  );
};

export default MarketTicker;

import React from 'react';
import Chart from 'react-apexcharts';
import { Layers } from 'lucide-react';

const PortfolioDiversity: React.FC = () => {
  const options: any = {
    chart: { type: 'donut' },
    colors: ['#6C5CE7', '#28C76F', '#FF9F43'],
    labels: ['Capital', 'Yield', 'Network'],
    legend: { show: false },
    stroke: { width: 0 },
    plotOptions: {
      pie: {
        donut: {
          size: '80%',
          labels: {
            show: true,
            total: {
              show: true,
              label: 'Diversity',
              fontSize: '11px',
              fontWeight: 800,
              color: '#94A3B8'
            }
          }
        }
      }
    }
  };

  return (
    <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-6 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-[16px] font-[800] text-[#1E293B] uppercase tracking-tight">Portfolio Mix</h3>
        <Layers size={18} className="text-primary opacity-50" />
      </div>

      <div className="flex items-center justify-center">
         <Chart options={options} series={[60, 25, 15]} type="donut" width="220" />
      </div>

      <div className="mt-8 space-y-3">
         {[
           { l: 'Invested', v: '60%', c: 'bg-primary' },
           { l: 'Compounding', v: '25%', c: 'bg-secondary' },
           { l: 'Network Reward', v: '15%', c: 'bg-orange-400' }
         ].map((item, i) => (
           <div key={i} className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                 <div className={`w-2 h-2 rounded-full ${item.c}`}></div>
                 <span className="text-[11px] font-bold text-[#64748B]">{item.l}</span>
              </div>
              <span className="text-[11px] font-black text-[#1E293B]">{item.v}</span>
           </div>
         ))}
      </div>
    </div>
  );
};

export default PortfolioDiversity;

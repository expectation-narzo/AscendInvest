import React from 'react';

interface MetricCardProps {
  title: string;
  value: string;
  icon: React.ElementType;
  children?: React.ReactNode;
}

const MetricCard: React.FC<MetricCardProps> = ({ title, value, icon: Icon, children }) => {
  return (
    <div className="bg-white rounded-[24px] border border-gray-100 p-5 md:p-6 card-shadow">
      <div className="flex justify-between items-start">
        <div className="min-w-0 flex-1">
          <p className="text-[10px] md:text-xs font-bold text-text-secondary uppercase tracking-wider truncate">{title}</p>
          <h3 className="text-xl md:text-2xl font-black text-text-primary mt-1 truncate">{value}</h3>
        </div>
        <div className="p-2 md:p-3 bg-primary/5 rounded-full flex-shrink-0 ml-2">
          <Icon className="text-primary w-5 h-5 md:w-6 md:h-6" />
        </div>
      </div>
      {children}
    </div>
  );
};

export default MetricCard;

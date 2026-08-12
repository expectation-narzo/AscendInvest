import React from 'react';

const Support: React.FC = () => {
  const tickets = [
    { id: '#TK-202301', title: 'Deposit not credited', status: 'Pending', date: 'Oct 24, 2023', color: '#FF9F43' },
    { id: '#TK-202298', title: 'KYC Verification', status: 'Resolved', date: 'Oct 20, 2023', color: '#28C76F' },
  ];

  return (
    <div className="flex flex-col min-h-full font-sans pb-[40px]">

      {/* Header Area - matching section_support.xml */}
      <div className="px-[24px] pt-[32px] pb-[24px]">
        <p className="text-[#6C5CE7] text-[12px] font-[700] uppercase tracking-[0.1em]">Help Center</p>
        <h1 className="text-[24px] font-[700] text-[#1E293B] mt-1">Customer Support</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-[24px] px-[20px]">
        <div className="lg:col-span-1 space-y-[24px]">

          {/* Email Support Card */}
          <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-[24px] shadow-sm">
             <div className="w-[48px] h-[48px] bg-[#F5F3FF] rounded-full flex items-center justify-center mb-[16px]">
                <img src="/logo.png" className="w-[24px] h-[24px] object-contain opacity-40" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
             </div>
             <h3 className="text-[18px] font-[800] text-[#1E293B]">Email Support</h3>
             <p className="text-[14px] text-[#64748B] mt-[8px]">Get a response within 24 hours.</p>
             <p className="text-[#6C5CE7] font-[700] mt-[16px] text-[15px]">support@ascendinvest.com</p>
          </div>

          {/* Telegram Card */}
          <div className="bg-[#E0F2FE] rounded-[24px] p-[24px] border border-[#BAE6FD]">
             <div className="w-[48px] h-[48px] bg-white rounded-full flex items-center justify-center mb-[16px] shadow-sm">
                <img src="/ic_launcher.png" className="w-[24px] h-[24px] object-contain opacity-60" alt="" />
             </div>
             <h3 className="text-[18px] font-[800] text-[#0369A1]">Telegram Group</h3>
             <p className="text-[14px] text-[#0369A1] opacity-70 mt-[8px]">Join our active community of 50k+ investors.</p>
             <button className="w-full mt-[24px] bg-[#0284C7] text-white h-[48px] rounded-[12px] font-bold text-[14px] shadow-sm hover:opacity-90 transition-all">Join Channel</button>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-[24px]">
          {/* Create Support Ticket Card */}
          <div className="bg-white rounded-[24px] border border-[#E2E8F0] p-[30px] shadow-sm">
            <h3 className="text-[20px] font-[900] text-[#1E293B] mb-[24px]">Create Support Ticket</h3>
            <form className="space-y-[24px]">
               <div className="grid grid-cols-1 md:grid-cols-2 gap-[24px]">
                 <div>
                   <p className="text-[12px] font-[700] text-[#64748B] uppercase tracking-[0.05em] mb-[8px]">Subject</p>
                   <input type="text" placeholder="e.g. Deposit Issue" className="w-full h-[56px] px-[16px] bg-[#F8FAFC] border border-[#E2E8F0] rounded-[12px] text-[14px] focus:border-[#6C5CE7] outline-none" />
                 </div>
                 <div>
                   <p className="text-[12px] font-[700] text-[#64748B] uppercase tracking-[0.05em] mb-[8px]">Priority</p>
                   <select className="w-full h-[56px] px-[16px] bg-[#F8FAFC] border border-[#E2E8F0] rounded-[12px] text-[14px] focus:border-[#6C5CE7] outline-none">
                     <option>Normal</option>
                     <option>High</option>
                     <option>Urgent</option>
                   </select>
                 </div>
               </div>
               <div>
                 <p className="text-[12px] font-[700] text-[#64748B] uppercase tracking-[0.05em] mb-[8px]">Detailed Description</p>
                 <textarea rows={6} placeholder="How can we help you today?" className="w-full p-[16px] bg-[#F8FAFC] border border-[#E2E8F0] rounded-[12px] text-[14px] focus:border-[#6C5CE7] outline-none resize-none"></textarea>
               </div>
               <button className="h-[56px] px-[32px] bg-[#6C5CE7] text-white font-bold text-[16px] rounded-[12px] shadow-lg shadow-[#6C5CE7]/20 flex items-center justify-center gap-[12px] active:scale-[0.98] transition-all">
                 Submit Ticket
               </button>
            </form>
          </div>

          {/* Ticket History matching item_support_ticket.xml */}
          <div className="space-y-[16px]">
            <h3 className="text-[18px] font-[700] text-[#1E293B] px-[4px]">Previous Tickets</h3>
            {tickets.map(ticket => (
              <div key={ticket.id} className="bg-white rounded-[16px] border border-[#E2E8F0] p-[16px] flex items-center justify-between hover:bg-[#F8FAFC] transition-all cursor-pointer">
                 <div className="flex items-center gap-[16px]">
                    <div className="w-[44px] h-[44px] bg-[#F8FAFC] rounded-full flex items-center justify-center">
                       <img src="/logo.png" className="w-[20px] h-[20px] opacity-20" style={{ filter: 'invert(43%) sepia(85%) saturate(1242%) hue-rotate(218deg) brightness(96%) contrast(92%)' }} alt="" />
                    </div>
                    <div>
                       <p className="text-[14px] font-[700] text-[#1E293B]">{ticket.title}</p>
                       <p className="text-[11px] text-[#64748B] mt-[2px]">{ticket.id} • {ticket.date}</p>
                    </div>
                 </div>
                 <span className={`text-[11px] font-[800] px-[10px] py-[4px] rounded-full uppercase`} style={{ backgroundColor: `${ticket.color}15`, color: ticket.color }}>
                    {ticket.status}
                 </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Support;

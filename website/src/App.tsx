import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import MarketTicker from './components/MarketTicker';
import FloatingSupport from './components/FloatingSupport';
import CommandPalette from './components/CommandPalette';
import Dashboard from './pages/Dashboard';
import P2P from './pages/P2P';
import Plans from './pages/Plans';
import Welcome from './pages/Welcome';
import Login from './pages/Login';
import Register from './pages/Register';
import Support from './pages/Support';
import Deposits from './pages/Deposits';
import Withdraw from './pages/Withdraw';
import Referrals from './pages/Referrals';
import Team from './pages/Team';
import { AuthProvider, useAuth } from './context/AuthContext';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/welcome" />;
};

const AuthenticatedLayout = ({ children }: { children: React.ReactNode }) => {
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  return (
    <ProtectedRoute>
      <div className="flex bg-[#FFFFFF] h-screen w-screen overflow-hidden">
        <CommandPalette />

        {/* SIDEBAR: Independent scroll internally */}
        <Sidebar isOpen={isSidebarOpen} onClose={() => setSidebarOpen(false)} />

        {/* MAIN CONTAINER: Fixed Height, internal scrolling only for <main> */}
        <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden">

          {/* FIXED TOP: Does not scroll */}
          <div className="flex-shrink-0 z-50 shadow-sm">
             <Topbar onMenuClick={() => setSidebarOpen(true)} />
             <MarketTicker />
          </div>

          {/* SCROLLABLE CONTENT: Independent scroll */}
          <main className="flex-1 overflow-y-auto bg-auth-gradient custom-scrollbar relative">
            <div className="max-w-[1400px] mx-auto pb-20">
              {children}
            </div>
            <FloatingSupport />
          </main>

        </div>
      </div>
    </ProtectedRoute>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/welcome" element={<Welcome />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route path="/" element={<AuthenticatedLayout><Dashboard /></AuthenticatedLayout>} />
          <Route path="/deposits" element={<AuthenticatedLayout><Deposits /></AuthenticatedLayout>} />
          <Route path="/withdraw" element={<AuthenticatedLayout><Withdraw /></AuthenticatedLayout>} />
          <Route path="/p2p" element={<AuthenticatedLayout><P2P /></AuthenticatedLayout>} />
          <Route path="/plans" element={<AuthenticatedLayout><Plans /></AuthenticatedLayout>} />
          <Route path="/referrals" element={<AuthenticatedLayout><Referrals /></AuthenticatedLayout>} />
          <Route path="/team" element={<AuthenticatedLayout><Team /></AuthenticatedLayout>} />
          <Route path="/support" element={<AuthenticatedLayout><Support /></AuthenticatedLayout>} />

          <Route path="*" element={<Navigate to="/welcome" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

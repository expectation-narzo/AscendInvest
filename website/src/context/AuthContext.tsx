import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
  user: any;
  token: string | null;
  login: (token: string, user: any) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<any>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('ascend_token'));

  useEffect(() => {
    const savedUser = localStorage.getItem('ascend_user');
    if (savedUser) setUser(JSON.parse(savedUser));
  }, []);

  const login = (newToken: string, userData: any) => {
    setToken(newToken);
    setUser(userData);
    localStorage.setItem('ascend_token', newToken);
    localStorage.setItem('ascend_user', JSON.stringify(userData));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('ascend_token');
    localStorage.removeItem('ascend_user');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

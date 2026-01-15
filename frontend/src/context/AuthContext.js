import React, { createContext, useState, useEffect, useContext } from 'react';
import { storage } from '../utils/storage';
import api from '../api/axios';
import { setLogoutCallback } from '../api/axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // 앱 시작 시 1회만 토큰 검증
  useEffect(() => {
    checkLoginStatus();
  }, []);

  // axios 인터셉터에 로그아웃 콜백 등록
  useEffect(() => {
    setLogoutCallback(logout);
  }, []);

  const checkLoginStatus = async () => {
    const token = await storage.getAccessToken();

    if (!token) {
      setIsLoggedIn(false);
      setIsLoading(false);
      return;
    }

    // 서버 검증 (앱 시작 시 1회만)
    try {
      await api.get('/api/auth/verify');
      const userData = await storage.getUser();
      setUser(userData);
      setIsLoggedIn(true);
    } catch (error) {
      // 토큰 무효 - 로컬 저장소 정리
      await storage.clear();
      setIsLoggedIn(false);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (userData, tokens) => {
    await storage.saveTokens(tokens.accessToken, tokens.refreshToken);
    await storage.saveUser(userData);
    setUser(userData);
    setIsLoggedIn(true);  // 즉시 반영!
  };

  const logout = async () => {
    await storage.clear();
    setUser(null);
    setIsLoggedIn(false);  // 즉시 반영!
  };

  return (
    <AuthContext.Provider
      value={{
        isLoggedIn,
        user,
        isLoading,
        login,
        logout,
        checkLoginStatus
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

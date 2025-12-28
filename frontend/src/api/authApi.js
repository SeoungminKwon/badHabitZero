import api from './axios';
import { storage } from '../utils/storage';
import { config } from '../constants/config';
import axios from 'axios';

export const authApi = {

  // 카카오 로그인 URL 가져오기
  async getKakaoLoginUrl() {
    try {
      const response = await api.get('/api/auth/kakao/login-url');
      return response.data.data;
    } catch (error) {
      console.error('카카오 로그인 URL 가져오기 실패:', error);
      throw error;
    }
  },

  // 카카오 로그인 처리
  async kakaoCallback(code) {
    try {
      const response = await api.get(`/api/auth/kakao/callback?code=${code}`);
      const data = response.data;

      if (data.success) {
        await storage.saveTokens(
          data.data.accessToken,
          data.data.refreshToken
        );

        await storage.saveUser({
          userId: data.data.userId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }

      return data;
    } catch (error) {
      console.error('카카오 로그인 처리 실패:', error);
      throw error;
    }
  },

  // 토큰 갱신 (인터셉터에서 자동으로 처리하지만, 수동 호출용)
  async refreshToken() {
    try {
      const refreshToken = await storage.getRefreshToken();
      
      // 인터셉터를 타지 않도록 기본 axios 사용
      const response = await axios.post(
        `${config.API_BASE_URL}/api/auth/refresh`,
        { refreshToken }
      );

      if (response.data.success) {
        await storage.saveTokens(
          response.data.data.accessToken,
          response.data.data.refreshToken
        );
      }

      return response.data;
    } catch (error) {
      console.error('토큰 갱신 실패:', error);
      throw error;
    }
  },

  // 로그아웃
  async logout() {
    try {
      const refreshToken = await storage.getRefreshToken();
      await api.post('/api/auth/logout', { refreshToken });
    } catch (error) {
      console.error('로그아웃 API 실패:', error);
    } finally {
      // 에러가 나도 로컬 저장소는 비우기
      await storage.clear();
    }
  },
};
import axios from 'axios';
import { config } from '../constants/config';
import { storage } from '../utils/storage';

// axios 인스턴스 생성
const api = axios.create({
  baseURL: config.API_BASE_URL,
  timeout: 10000, // 10초 타임아웃
  headers: {
    'Content-Type': 'application/json',
  },
});

// ========== 요청 인터셉터 ==========
// 모든 요청 전에 실행됨
api.interceptors.request.use(
  async (config) => {
    // 저장된 토큰 가져오기
    const token = await storage.getAccessToken();

    // 토큰이 있으면 헤더에 추가
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    console.log(`📤 요청: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ========== 응답 인터셉터 ==========
// 모든 응답 후에 실행됨
api.interceptors.response.use(
  (response) => {
    console.log(`📥 응답: ${response.status} ${response.config.url}`);
    return response;
  },
  async (error) => {
    console.log(`❌ 에러: ${error.response?.status} ${error.config?.url}`);

    const originalRequest = error.config;

    // 401 에러 (토큰 만료) && 재시도 안 한 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 토큰 갱신 시도
        const refreshToken = await storage.getRefreshToken();

        const response = await axios.post(
          `${config.API_BASE_URL}/api/auth/refresh`,
          { refreshToken }
        );

        if (response.data.success) {
          // 새 토큰 저장
          await storage.saveTokens(
            response.data.data.accessToken,
            response.data.data.refreshToken
          );

          // 원래 요청 재시도
          originalRequest.headers.Authorization =
            `Bearer ${response.data.data.accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // 토큰 갱신 실패 → 로그아웃 처리
        await storage.clear();
        // 여기서 로그인 화면으로 이동하는 로직 추가 가능
      }
    }

    return Promise.reject(error);
  }
);

export default api;
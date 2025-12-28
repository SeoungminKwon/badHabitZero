// AsyncStorage: 앱의 로컬 저장소 (웹의 localStorage 같은 것)
import AsyncStorage from '@react-native-async-storage/async-storage';

// 저장할 때 사용하는 키 이름들
const KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER: 'user',
};

// storage 객체: 토큰과 사용자 정보를 저장/조회/삭제하는 함수들
export const storage = {

  // ========== 토큰 관련 ==========

  // 토큰 저장 (로그인 성공 시 호출)
  async saveTokens(accessToken, refreshToken) {
    try {
      await AsyncStorage.setItem(KEYS.ACCESS_TOKEN, accessToken);
      await AsyncStorage.setItem(KEYS.REFRESH_TOKEN, refreshToken);
    } catch (error) {
      console.error('토큰 저장 실패:', error);
    }
  },

  // 액세스 토큰 가져오기 (API 요청 시 사용)
  async getAccessToken() {
    try {
      return await AsyncStorage.getItem(KEYS.ACCESS_TOKEN);
    } catch (error) {
      console.error('액세스 토큰 조회 실패:', error);
      return null;
    }
  },

  // 리프레시 토큰 가져오기 (토큰 갱신 시 사용)
  async getRefreshToken() {
    try {
      return await AsyncStorage.getItem(KEYS.REFRESH_TOKEN);
    } catch (error) {
      console.error('리프레시 토큰 조회 실패:', error);
      return null;
    }
  },

  // ========== 사용자 정보 관련 ==========

  // 사용자 정보 저장 (로그인 성공 시 호출)
  async saveUser(user) {
    try {
      // 객체는 문자열로 변환해서 저장해야 함
      await AsyncStorage.setItem(KEYS.USER, JSON.stringify(user));
    } catch (error) {
      console.error('사용자 정보 저장 실패:', error);
    }
  },

  // 사용자 정보 가져오기
  async getUser() {
    try {
      const user = await AsyncStorage.getItem(KEYS.USER);
      // 문자열을 다시 객체로 변환
      return user ? JSON.parse(user) : null;
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error);
      return null;
    }
  },

  // ========== 전체 삭제 ==========

  // 모든 데이터 삭제 (로그아웃 시 호출)
  async clear() {
    try {
      await AsyncStorage.multiRemove([
        KEYS.ACCESS_TOKEN,
        KEYS.REFRESH_TOKEN,
        KEYS.USER,
      ]);
    } catch (error) {
      console.error('데이터 삭제 실패:', error);
    }
  },
};
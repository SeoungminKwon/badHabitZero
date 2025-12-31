export const config = {
  // 백엔드 API 주소
  // 개발할 때는 localhost, 배포할 때는 실제 서버 주소로 변경
  API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080',
};

// 디버깅용 로그
console.log('API_BASE_URL:', config.API_BASE_URL);
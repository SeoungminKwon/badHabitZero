import api from './axios';

// 악습 목록 조회
export const getHabits = async () => {
  const response = await api.get('/api/habits');
  return response.data;
};

// 악습 등록
export const createHabit = async (habitData) => {
  const response = await api.post('/api/habits', habitData);
  return response.data;
};

// 악습 상세 조회
export const getHabit = async (habitId) => {
  const response = await api.get(`/api/habits/${habitId}`);
  return response.data;
};

// 악습 수정
export const updateHabit = async (habitId, habitData) => {
  const response = await api.put(`/api/habits/${habitId}`, habitData);
  return response.data;
};

// 악습 삭제
export const deleteHabit = async (habitId) => {
  const response = await api.delete(`/api/habits/${habitId}`);
  return response.data;
};

// AI 분석 (추가 질문 생성)
export const analyzeHabit = async (data) => { 
  const response = await api.post('/api/ai/analyze', data);
  return response.data;
};

// AI 가치 산정
export const calculateValue = async (data) => {
  const response = await api.post('/api/ai/analyze/complete', data);
  return response.data;
};

// ============================================
// 챗봇 기반 가치 산정 API
// ============================================

// 챗봇 세션 시작
export const startChatSession = async (data) => {
  const response = await api.post('/api/ai/chat/start', data);
  return response.data;
};

// 챗봇 메시지 전송
export const sendChatMessage = async (sessionId, message) => {
  const response = await api.post('/api/ai/chat/message', { sessionId, message });
  return response.data;
};
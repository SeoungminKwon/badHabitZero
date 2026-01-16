package org.example.badhabitzero.domain.ai.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatSessionManager {

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    @Data
    public static class ChatSession {
        private String sessionId;
        private String habitName;
        private String category;
        private String reason;
        private List<String> questions;         // AI가 생성한 질문 목록
        private List<String> answers;           // 사용자 답변 목록
        private int currentQuestionIndex;       // 현재 질문 인덱스 (0부터)
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;

        public ChatSession() {
            this.questions = new ArrayList<>();
            this.answers = new ArrayList<>();
            this.currentQuestionIndex = 0;
        }
    }

    public ChatSession createSession(String habitName, String category, String reason, List<String> questions) {
        String sessionId = UUID.randomUUID().toString();

        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setHabitName(habitName);
        session.setCategory(category);
        session.setReason(reason);
        session.setQuestions(questions);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));

        sessions.put(sessionId, session);
        log.info("챗봇 세션 생성: sessionId={}, habitName={}", sessionId, habitName);

        return session;
    }

    public ChatSession getSession(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("세션을 찾을 수 없음: sessionId={}", sessionId);
            return null;
        }

        // 만료 체크
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            log.warn("세션 만료됨: sessionId={}", sessionId);
            sessions.remove(sessionId);
            return null;
        }

        return session;
    }

    public void addAnswer(String sessionId, String answer) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.getAnswers().add(answer);
            session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);
            log.debug("답변 추가: sessionId={}, questionIndex={}", sessionId, session.getCurrentQuestionIndex());
        }
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("세션 제거: sessionId={}", sessionId);
    }

    public boolean isComplete(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) return false;
        return session.getCurrentQuestionIndex() >= session.getQuestions().size();
    }

    public String getNextQuestion(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) return null;

        int index = session.getCurrentQuestionIndex();
        if (index < session.getQuestions().size()) {
            return session.getQuestions().get(index);
        }
        return null;
    }

    // 5분마다 만료된 세션 정리
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int removedCount = 0;

        for (Map.Entry<String, ChatSession> entry : sessions.entrySet()) {
            if (now.isAfter(entry.getValue().getExpiresAt())) {
                sessions.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("만료된 세션 정리: {}개 제거", removedCount);
        }
    }
}

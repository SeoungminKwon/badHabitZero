package org.example.badhabitzero.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSessionResponse {
    private String sessionId;           // 세션 ID
    private String message;             // AI 첫 질문
    private int questionNumber;         // 현재 질문 번호 (1부터 시작)
    private int totalQuestions;         // 총 질문 수
    private boolean isComplete;         // 대화 완료 여부
}

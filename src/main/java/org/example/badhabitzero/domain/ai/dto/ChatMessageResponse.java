package org.example.badhabitzero.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {
    private String message;             // AI 다음 질문 또는 완료 메시지
    private int questionNumber;         // 현재 질문 번호
    private int totalQuestions;         // 총 질문 수
    private boolean isComplete;         // 대화 완료 여부
    private ValueResult valueResult;    // 완료 시에만 포함
}

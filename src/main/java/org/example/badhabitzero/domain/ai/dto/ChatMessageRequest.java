package org.example.badhabitzero.domain.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private String sessionId;           // 세션 ID
    private String message;             // 사용자 자연어 답변
}

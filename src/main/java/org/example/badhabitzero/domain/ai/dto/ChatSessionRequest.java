package org.example.badhabitzero.domain.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionRequest {
    private String habitName;      // 악습 이름
    private String category;       // 카테고리
    private String reason;         // 고치고 싶은 이유 (선택)
}

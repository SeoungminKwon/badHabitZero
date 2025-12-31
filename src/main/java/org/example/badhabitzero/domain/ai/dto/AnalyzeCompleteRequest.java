package org.example.badhabitzero.domain.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AnalyzeCompleteRequest {
    private String habitName;              // 악습 이름
    private String category;               // 카테고리
    private String reason;                 // 고치고 싶은 이유
    private Map<String, Object> answers;   // 사용자 답변 (질문ID: 답변)
}
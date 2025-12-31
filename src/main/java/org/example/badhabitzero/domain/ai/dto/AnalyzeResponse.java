package org.example.badhabitzero.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalyzeResponse {
    private boolean needMoreInfo;          // 추가 정보 필요 여부
    private List<Question> questions;      // 추가 질문 목록

    @Getter
    @Builder
    public static class Question {
        private String id;                 // 질문 ID (예: "price")
        private String question;           // 질문 내용
        private String type;               // 타입: "number", "text", "choice"
        private List<String> options;      // choice인 경우 선택지
    }
}
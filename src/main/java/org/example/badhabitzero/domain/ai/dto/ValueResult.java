package org.example.badhabitzero.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValueResult {
    private int value;                     // 1회당 가치 (원)
    private Breakdown breakdown;           // 상세 내역
    private String explanation;            // 설명
    private List<String> sources;          // 출처 목록

    @Getter
    @Builder
    public static class Breakdown {
        private int directCost;            // 직접 비용
        private int healthCost;            // 건강 비용
        private int opportunityCost;       // 기회 비용
        private int psychologicalCost;     // 심리 비용
    }
}
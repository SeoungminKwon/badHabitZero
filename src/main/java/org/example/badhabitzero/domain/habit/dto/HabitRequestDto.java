package org.example.badhabitzero.domain.habit.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.badhabitzero.domain.habit.entity.Habit;

@Getter
@Setter
public class HabitRequestDto {

    private String name;           // 악습 이름
    private String category;       // 카테고리 (SMOKING, DRINKING 등)
    private String reason;         // 고치고 싶은 이유
    private String icon;           // 아이콘
    private Integer baseValue;     // 기본 가치 (사용자 입력)

    // AI 가치 산정 결과 (프론트에서 AI 산정 후 전달)
    private Integer aiValue;
    private String aiDescription;

    public Habit toEntity(Long userId) {
        return Habit.builder()
                .userId(userId)
                .name(name)
                .category(Habit.Category.valueOf(category))
                .reason(reason)
                .icon(icon)
                .baseValue(baseValue != null ? baseValue : 0)
                .aiValue(aiValue)
                .aiDescription(aiDescription)
                .isActive(true)
                .build();
    }
}
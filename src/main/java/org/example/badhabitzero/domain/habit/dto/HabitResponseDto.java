package org.example.badhabitzero.domain.habit.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.badhabitzero.domain.habit.entity.Habit;

import java.time.LocalDateTime;

@Getter
@Builder
public class HabitResponseDto {

    private Long id;
    private String name;
    private String category;
    private String reason;
    private String icon;
    private Integer baseValue;
    private Integer aiValue;
    private String aiDescription;
    private Integer effectiveValue;    // 실제 적용 가치
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime lastSavedAt;
    private LocalDateTime createdAt;

    public static HabitResponseDto from(Habit habit) {
        return HabitResponseDto.builder()
                .id(habit.getId())
                .name(habit.getName())
                .category(habit.getCategory().name())
                .reason(habit.getReason())
                .icon(habit.getIcon())
                .baseValue(habit.getBaseValue())
                .aiValue(habit.getAiValue())
                .aiDescription(habit.getAiDescription())
                .effectiveValue(habit.getEffectiveValue())
                .isActive(habit.getIsActive())
                .displayOrder(habit.getDisplayOrder())
                .lastSavedAt(habit.getLastSavedAt())
                .createdAt(habit.getCreatedAt())
                .build();
    }
}
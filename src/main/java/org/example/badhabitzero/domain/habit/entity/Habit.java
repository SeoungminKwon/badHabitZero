package org.example.badhabitzero.domain.habit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.badhabitzero.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "habits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Habit extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(length = 500)
    private String reason;

    @Column(length = 50)
    private String icon;

    @Column(name = "base_value", nullable = false)
    private Integer baseValue;

    @Column(name = "ai_value")
    private Integer aiValue;

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt;

    @Builder
    private Habit(Long userId, String name, Category category, String reason, String icon,
                  Integer baseValue, Integer aiValue, String aiDescription,
                  Boolean isActive, Integer displayOrder) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.reason = reason;
        this.icon = icon;
        this.baseValue = baseValue;
        this.aiValue = aiValue;
        this.aiDescription = aiDescription;
        this.isActive = isActive != null ? isActive : true;
        this.displayOrder = displayOrder;
    }

    // AI 가치 업데이트
    public void updateAiValue(Integer aiValue, String aiDescription) {
        this.aiValue = aiValue;
        this.aiDescription = aiDescription;
    }

    // 악습 정보 수정
    public void update(String name, Category category, String reason, String icon, Integer baseValue) {
        this.name = name;
        this.category = category;
        this.reason = reason;
        this.icon = icon;
        this.baseValue = baseValue;
    }

    // 활성화/비활성화
    public void updateActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // 순서 변경
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // 마지막 참은 시간 업데이트
    public void updateLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    // 실제 적용할 가치 (AI 값 우선, 없으면 기본 값)
    public Integer getEffectiveValue() {
        return aiValue != null ? aiValue : baseValue;
    }

    // 카테고리
    public enum Category {
        SMOKING,    // 흡연
        DRINKING,   // 음주
        EATING,     // 과식/야식/간식
        SPENDING,   // 과소비/충동구매
        LAZINESS,   // 게으름/미루기
        DIGITAL,    // SNS/유튜브/게임
        CAFFEINE,   // 카페인
        GAMBLING,   // 도박
        OTHER       // 기타
    }
}
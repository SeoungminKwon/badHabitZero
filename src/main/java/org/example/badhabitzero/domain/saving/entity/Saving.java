package org.example.badhabitzero.domain.saving.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.badhabitzero.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "savings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Saving extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "habit_id", nullable = false)
    private Long habitId;

    @Column(name = "saved_value", nullable = false)  // value → saved_value로 변경
    private Integer savedValue;

    @Column(length = 255)
    private String memo;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    @Builder
    private Saving(Long userId, Long habitId, Integer savedValue, String memo, LocalDateTime savedAt) {
        this.userId = userId;
        this.habitId = habitId;
        this.savedValue = savedValue;
        this.memo = memo;
        this.savedAt = savedAt != null ? savedAt : LocalDateTime.now();
    }
}
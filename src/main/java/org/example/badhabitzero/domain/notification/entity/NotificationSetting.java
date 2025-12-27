package org.example.badhabitzero.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.badhabitzero.global.common.BaseEntity;

import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // 매일 알림
    @Column(name = "daily_enabled", nullable = false)
    private Boolean dailyEnabled;

    @Column(name = "daily_time")
    private LocalTime dailyTime;

    // 매주 알림
    @Column(name = "weekly_enabled", nullable = false)
    private Boolean weeklyEnabled;

    @Column(name = "weekly_day")
    private Integer weeklyDay;

    @Column(name = "weekly_time")
    private LocalTime weeklyTime;

    // 매달 알림
    @Column(name = "monthly_enabled", nullable = false)
    private Boolean monthlyEnabled;

    @Column(name = "monthly_day")
    private Integer monthlyDay;

    @Column(name = "monthly_time")
    private LocalTime monthlyTime;

    // 매년 알림
    @Column(name = "yearly_enabled", nullable = false)
    private Boolean yearlyEnabled;

    // 복귀 알림
    @Column(name = "inactive_enabled", nullable = false)
    private Boolean inactiveEnabled;

    @Column(name = "inactive_days")
    private Integer inactiveDays;

    @Builder
    private NotificationSetting(Long userId, Boolean dailyEnabled, LocalTime dailyTime,
                                Boolean weeklyEnabled, Integer weeklyDay, LocalTime weeklyTime,
                                Boolean monthlyEnabled, Integer monthlyDay, LocalTime monthlyTime,
                                Boolean yearlyEnabled, Boolean inactiveEnabled, Integer inactiveDays) {
        this.userId = userId;
        this.dailyEnabled = dailyEnabled != null ? dailyEnabled : true;
        this.dailyTime = dailyTime != null ? dailyTime : LocalTime.of(21, 0);
        this.weeklyEnabled = weeklyEnabled != null ? weeklyEnabled : true;
        this.weeklyDay = weeklyDay != null ? weeklyDay : 1;
        this.weeklyTime = weeklyTime != null ? weeklyTime : LocalTime.of(10, 0);
        this.monthlyEnabled = monthlyEnabled != null ? monthlyEnabled : true;
        this.monthlyDay = monthlyDay != null ? monthlyDay : 1;
        this.monthlyTime = monthlyTime != null ? monthlyTime : LocalTime.of(10, 0);
        this.yearlyEnabled = yearlyEnabled != null ? yearlyEnabled : true;
        this.inactiveEnabled = inactiveEnabled != null ? inactiveEnabled : true;
        this.inactiveDays = inactiveDays != null ? inactiveDays : 3;
    }

    // 기본 설정으로 생성
    public static NotificationSetting createDefault(Long userId) {
        return NotificationSetting.builder()
                .userId(userId)
                .build();
    }

    // 매일 알림 설정 업데이트
    public void updateDaily(Boolean enabled, LocalTime time) {
        this.dailyEnabled = enabled;
        this.dailyTime = time;
    }

    // 매주 알림 설정 업데이트
    public void updateWeekly(Boolean enabled, Integer day, LocalTime time) {
        this.weeklyEnabled = enabled;
        this.weeklyDay = day;
        this.weeklyTime = time;
    }

    // 매달 알림 설정 업데이트
    public void updateMonthly(Boolean enabled, Integer day, LocalTime time) {
        this.monthlyEnabled = enabled;
        this.monthlyDay = day;
        this.monthlyTime = time;
    }

    // 매년 알림 설정 업데이트
    public void updateYearly(Boolean enabled) {
        this.yearlyEnabled = enabled;
    }

    // 복귀 알림 설정 업데이트
    public void updateInactive(Boolean enabled, Integer days) {
        this.inactiveEnabled = enabled;
        this.inactiveDays = days;
    }
}

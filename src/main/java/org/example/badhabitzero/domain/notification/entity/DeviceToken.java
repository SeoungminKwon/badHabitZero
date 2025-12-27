package org.example.badhabitzero.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.badhabitzero.global.common.BaseEntity;

@Entity
@Table(name = "device_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private DeviceToken(Long userId, String fcmToken, String deviceInfo, Boolean isActive) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceInfo = deviceInfo;
        this.isActive = isActive != null ? isActive : true;
    }

    // 토큰 업데이트
    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // 활성화/비활성화
    public void updateActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
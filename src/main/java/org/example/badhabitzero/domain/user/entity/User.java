package org.example.badhabitzero.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.badhabitzero.global.common.BaseEntity;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column
    private Integer income;

    @Builder
    private User(String email, String nickname, Provider provider, Long providerId,
                 String profileImage, Role role, Integer birthYear, Gender gender, Integer income) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
        this.birthYear = birthYear;
        this.gender = gender;
        this.income = income;
    }

    // 프로필 업데이트
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    // 추가 정보 업데이트
    public void updateAdditionalInfo(Integer birthYear, Gender gender, Integer income) {
        this.birthYear = birthYear;
        this.gender = gender;
        this.income = income;
    }

    // 소셜 로그인 제공자
    public enum Provider {
        KAKAO, GOOGLE, APPLE
    }

    // 권한
    public enum Role {
        USER, PREMIUM, ADMIN
    }

    // 성별
    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
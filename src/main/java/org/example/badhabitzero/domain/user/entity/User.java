package org.example.badhabitzero.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    @Column(nullable = false, length = 20)
    private String nickname;

    @Builder
    private User(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}
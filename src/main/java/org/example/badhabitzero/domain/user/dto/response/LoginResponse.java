package org.example.badhabitzero.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.badhabitzero.domain.user.entity.User;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String profileImage;
    private boolean isNewUser;
    private String accessToken;
    private String refreshToken;

    public static LoginResponse of(User user, boolean isNewUser, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
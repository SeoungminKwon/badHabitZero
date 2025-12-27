package org.example.badhabitzero.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.user.dto.response.KakaoTokenResponse;
import org.example.badhabitzero.domain.user.dto.response.KakaoUserInfo;
import org.example.badhabitzero.domain.user.dto.response.LoginResponse;
import org.example.badhabitzero.domain.user.dto.response.TokenResponse;
import org.example.badhabitzero.domain.user.entity.RefreshToken;
import org.example.badhabitzero.domain.user.entity.User;
import org.example.badhabitzero.domain.user.repository.RefreshTokenRepository;
import org.example.badhabitzero.domain.user.repository.UserRepository;
import org.example.badhabitzero.global.error.BusinessException;
import org.example.badhabitzero.global.error.ErrorCode;
import org.example.badhabitzero.global.security.JwtProperties;
import org.example.badhabitzero.global.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * 카카오 로그인 URL 반환
     */
    public String getKakaoLoginUrl() {
        return kakaoOAuthService.getKakaoLoginUrl();
    }

    /**
     * 카카오 로그인 처리
     */
    @Transactional
    public LoginResponse kakaoLogin(String code, String deviceInfo) {
        // 1. 카카오 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getAccessToken(code);

        // 2. 카카오 사용자 정보 조회
        KakaoUserInfo userInfo = kakaoOAuthService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 조회 또는 생성
        boolean isNewUser = false;
        User user = userRepository.findByProviderAndProviderId(User.Provider.KAKAO, userInfo.getId())
                .orElse(null);

        if (user == null) {
            // 신규 사용자 생성
            user = User.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getNickname())
                    .provider(User.Provider.KAKAO)
                    .providerId(userInfo.getId())
                    .profileImage(userInfo.getProfileImage())
                    .role(User.Role.USER)
                    .build();
            userRepository.save(user);
            isNewUser = true;
            log.info("신규 사용자 가입: userId={}, email={}", user.getId(), user.getEmail());
        } else {
            // 기존 사용자 프로필 업데이트
            user.updateProfile(userInfo.getNickname(), userInfo.getProfileImage());
            log.info("기존 사용자 로그인: userId={}, email={}", user.getId(), user.getEmail());
        }

        // 4. JWT 토큰 발급
        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId(), deviceInfo);

        return LoginResponse.of(user, isNewUser, accessToken, refreshToken);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponse refreshToken(String refreshTokenValue) {
        // 1. 리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 2. 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // 3. 사용자 조회
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail());
        String newRefreshToken = UUID.randomUUID().toString();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        // 5. 리프레시 토큰 업데이트
        refreshToken.updateToken(newRefreshToken, newExpiresAt);

        log.info("토큰 갱신: userId={}", user.getId());

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
        log.info("로그아웃 처리 완료");
    }

    /**
     * 리프레시 토큰 생성
     */
    private String createRefreshToken(Long userId, String deviceInfo) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        // 기존 토큰이 있으면 업데이트, 없으면 생성
        RefreshToken refreshToken = refreshTokenRepository.findByUserIdAndDeviceInfo(userId, deviceInfo)
                .map(existing -> {
                    existing.updateToken(tokenValue, expiresAt);
                    return existing;
                })
                .orElse(RefreshToken.builder()
                        .userId(userId)
                        .token(tokenValue)
                        .deviceInfo(deviceInfo)
                        .expiresAt(expiresAt)
                        .build());

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }
}
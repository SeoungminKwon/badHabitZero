package org.example.badhabitzero.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.badhabitzero.domain.user.dto.request.TokenRefreshRequest;
import org.example.badhabitzero.domain.user.dto.response.LoginResponse;
import org.example.badhabitzero.domain.user.dto.response.TokenResponse;
import org.example.badhabitzero.domain.user.service.AuthService;
import org.example.badhabitzero.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인 URL", description = "카카오 로그인 페이지 URL을 반환합니다.")
    @GetMapping("/kakao/login-url")
    public ResponseEntity<ApiResponse<String>> getKakaoLoginUrl() {
        String loginUrl = authService.getKakaoLoginUrl();
        return ResponseEntity.ok(ApiResponse.success(loginUrl));
    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인가 코드로 로그인을 처리합니다.")
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(
            @RequestParam String code,
            HttpServletRequest request) {

        String deviceInfo = request.getHeader("User-Agent");
        LoginResponse response = authService.kakaoLogin(code, deviceInfo);

        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "토큰 갱신 성공"));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody TokenRefreshRequest request) {

        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }

    @Operation(summary = "토큰 검증", description = "액세스 토큰의 유효성을 검증합니다.")
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyToken() {
        // JwtAuthenticationFilter에서 이미 토큰 검증을 완료했으므로
        // 이 엔드포인트에 도달했다는 것은 토큰이 유효하다는 의미
        return ResponseEntity.ok(ApiResponse.success(true, "토큰이 유효합니다"));
    }
}

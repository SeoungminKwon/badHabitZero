package org.example.badhabitzero.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.user.config.KakaoProperties;
import org.example.badhabitzero.domain.user.dto.response.KakaoTokenResponse;
import org.example.badhabitzero.domain.user.dto.response.KakaoUserInfo;
import org.example.badhabitzero.global.error.BusinessException;
import org.example.badhabitzero.global.error.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final KakaoProperties kakaoProperties;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";


    /**
     * 카카오 로그인 URL 생성
     */
    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoProperties.getClientId()
                + "&redirect_uri=" + kakaoProperties.getRedirectUri()
                + "&response_type=code";
    }

    /**
     * 인가 코드로 액세스 토큰 요청
     */
    public KakaoTokenResponse getAccessToken(String code) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("client_secret", kakaoProperties.getClientSecret());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", code);

        try {
            return restClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (Exception e) {
            log.error("카카오 토큰 요청 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 액세스 토큰으로 사용자 정보 요청
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        RestClient restClient = RestClient.create();

        try {
            return restClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserInfo.class);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }
}

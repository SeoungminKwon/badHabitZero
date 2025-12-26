package org.example.badhabitzero.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    // API 전용 로거 (logback-spring.xml의 API_LOG와 연결)
    private static final Logger apiLog = LoggerFactory.getLogger("API_LOG");
    // 요청 본문 최대 크기 (10KB)
    private static final int MAX_PAYLOAD_LENGTH = 10240;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 정적 리소스, H2 콘솔은 로깅 제외
        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청/응답 본문을 여러 번 읽을 수 있도록 래핑
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 요청 고유 ID (로그 추적용)
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        try {
            // 요청 로그
            logRequest(requestId, wrappedRequest);

            // 실제 요청 처리
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // 응답 로그
            long duration = System.currentTimeMillis() - startTime;
            logResponse(requestId, wrappedResponse, duration);

            // 응답 본문을 클라이언트에게 전달 (필수!)
            wrappedResponse.copyBodyToResponse();
        }
    }

    // 요청 로그
    private void logRequest(String requestId, ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        // 요청 본문 (POST, PUT 등)
        String body = getRequestBody(request);

        if (body.isEmpty()) {
            apiLog.info("[{}] ▶ {} {}", requestId, method, fullPath);
        } else {
            apiLog.info("[{}] ▶ {} {} | Body: {}", requestId, method, fullPath, body);
        }
    }

    // 응답 로그
    private void logResponse(String requestId, ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String body = getResponseBody(response);

        // 응답 본문이 길면 잘라서 표시
        String truncatedBody = body.length() > 500
                ? body.substring(0, 500) + "...(truncated)"
                : body;

        apiLog.info("[{}] ◀ {} | {}ms | Body: {}", requestId, status, duration, truncatedBody);
    }

    // 요청 본문 읽기
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
    }

    // 응답 본문 읽기
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
    }

    // 로깅 제외 경로
    private boolean isExcludedPath(String uri) {
        return uri.startsWith("/h2-console")
                || uri.startsWith("/favicon.ico")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg");
    }
}

// 로그 출력
//2024-12-23 17:45:30.123 [http-nio-8080-exec-1] INFO  API_LOG - [a1b2c3d4] ▶ POST /api/users | Body: {"email":"test@test.com","nickname":"테스트"}
//2024-12-23 17:45:30.456 [http-nio-8080-exec-1] INFO  API_LOG - [a1b2c3d4] ◀ 201 | 333ms | Body: {"success":true,"data":{"id":1,"email":"test@test.com"},"message":"회원가입이 완료되었습니다."}

// 로그 파일 구조
//logs/
//├── badhabitzero.log          # 일반 로그 (전체)
//├── badhabitzero-error.log    # 에러 로그만
//└── badhabitzero-api.log      # API 요청/응답 로그만
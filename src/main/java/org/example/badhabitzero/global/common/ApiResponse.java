package org.example.badhabitzero.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null인 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final boolean success;  // 성공 여부
    private final T data;           // 실제 데이터
    private final String message;   // 메시지 (선택)

    @Builder
    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // ========== 성공 응답 ==========

    // 데이터만 반환
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    // 데이터 + 메시지 반환
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    // 메시지만 반환 (데이터 없음)
    public static ApiResponse<Void> successWithMessage(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    // 데이터도 메시지도 없음 (단순 성공)
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }
}
package org.example.badhabitzero.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null인 필드는 JSON에서 제외
public class ErrorResponse {

    private final LocalDateTime timestamp;  // 에러 발생 시간
    private final int status;               // HTTP 상태 코드
    private final String code;              // 서비스 에러 코드
    private final String message;           // 에러 메시지
    private final List<FieldError> errors;  // 필드별 에러 (Validation용)

    @Builder
    private ErrorResponse(int status, String code, String message, List<FieldError> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    // ErrorCode로 생성 (일반적인 에러)
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // ErrorCode + 커스텀 메시지
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    // Validation 에러 (필드별 에러 포함)
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    // 필드 에러 (Validation 실패 시 각 필드별 에러 정보)
    @Getter
    public static class FieldError {
        private final String field;    // 에러 발생 필드명
        private final String value;    // 입력된 값
        private final String reason;   // 에러 이유

        @Builder
        private FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        // BindingResult에서 FieldError 리스트 생성
        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> FieldError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() == null ? "" : error.getRejectedValue().toString())
                            .reason(error.getDefaultMessage())
                            .build())
                    .toList();
        }
    }
}
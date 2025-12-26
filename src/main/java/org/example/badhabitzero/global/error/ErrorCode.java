package org.example.badhabitzero.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== Common (공통) ==========
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입의 값입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "리소스를 찾을 수 없습니다."),

    // ========== Auth (인증) ==========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),

    // ========== User (사용자) ==========
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U003", "이미 존재하는 닉네임입니다."),

    // ========== Habit (악습) ==========
    HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "악습을 찾을 수 없습니다."),
    HABIT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "H002", "악습 등록 개수를 초과했습니다."),

    // ========== Saving (금고) ==========
    SAVING_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "저축 기록을 찾을 수 없습니다.");

    private final HttpStatus status;   // HTTP 상태 코드
    private final String code;         // 우리 서비스 에러 코드 (프론트에서 분기용)
    private final String message;      // 사용자에게 보여줄 메시지
}
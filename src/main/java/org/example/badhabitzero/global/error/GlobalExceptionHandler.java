package org.example.badhabitzero.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j  // 로깅용
@RestControllerAdvice  // 모든 컨트롤러에서 발생하는 예외를 여기서 처리
public class GlobalExceptionHandler {

    // ========== 비즈니스 예외 ==========
    // 우리가 의도적으로 던진 예외 (ex: 사용자 없음, 권한 없음)
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // ========== Validation 예외 ==========
    // @Valid 검증 실패 시 (ex: @NotBlank, @Email 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // ========== 타입 불일치 예외 ==========
    // 파라미터 타입이 맞지 않을 때 (ex: id에 문자열 입력)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("Type Mismatch Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        return new ResponseEntity<>(response, ErrorCode.INVALID_TYPE_VALUE.getStatus());
    }

    // ========== HTTP Method 불일치 ==========
    // GET 요청해야 하는데 POST로 요청했을 때
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.error("Method Not Allowed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, ErrorCode.METHOD_NOT_ALLOWED.getStatus());
    }

    // ========== 필수 파라미터 누락 ==========
    // @RequestParam(required = true) 누락 시
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("Missing Parameter: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // ========== JSON 파싱 실패 ==========
    // 잘못된 JSON 형식으로 요청 시
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.error("JSON Parse Error: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "잘못된 JSON 형식입니다.");
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // ========== 404 Not Found ==========
    // 존재하지 않는 API 경로 호출 시
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("No Handler Found: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND);
        return new ResponseEntity<>(response, ErrorCode.NOT_FOUND.getStatus());
    }

    // ========== 그 외 모든 예외 ==========
    // 예상하지 못한 예외 (서버 에러)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);  // 스택트레이스 전체 출력
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
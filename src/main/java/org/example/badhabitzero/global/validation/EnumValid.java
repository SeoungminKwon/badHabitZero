package org.example.badhabitzero.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidator.class)  // 검증 로직 클래스 지정
@Target({ElementType.FIELD, ElementType.PARAMETER})  // 필드, 파라미터에 사용 가능
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValid {

    String message() default "유효하지 않은 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();  // 검증할 Enum 클래스

    boolean ignoreCase() default false;  // 대소문자 무시 여부
}

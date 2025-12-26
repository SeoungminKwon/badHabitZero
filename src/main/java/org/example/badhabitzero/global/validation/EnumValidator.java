package org.example.badhabitzero.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<EnumValid, String> {

    private Class<? extends Enum<?>> enumClass;
    private boolean ignoreCase;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null은 @NotNull로 처리하므로 여기서는 통과
        if (value == null) {
            return true;
        }

        // Enum 값들과 비교
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .anyMatch(enumValue ->
                        ignoreCase ? enumValue.equalsIgnoreCase(value) : enumValue.equals(value));
    }
}
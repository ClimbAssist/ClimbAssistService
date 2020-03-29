package com.climbassist.common;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * This annotation validates that exactly one of the fields specified is present.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneOf.Validator.class)
public @interface OneOf {

    class Validator implements ConstraintValidator<OneOf, Object> {

        private List<String> values;
        private String message;

        @Override
        public void initialize(OneOf constraintAnnotation) {
            values = Arrays.asList(constraintAnnotation.values());
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
            if (object == null) {
                return true;
            }
            if (!object.getClass()
                    .isAssignableFrom(String.class)) {
                throw new IllegalArgumentException("Field must be of type String.");
            }
            //noinspection SuspiciousMethodCalls
            if (values.contains(object)) {
                return true;
            }
            else {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
                return false;
            }
        }
    }

    String[] values();

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

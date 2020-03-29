package com.climbassist.common;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * This annotation validates that exactly one of the fields specified is present.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExactlyOnePresent.Validator.class)
public @interface ExactlyOnePresent {

    class Validator implements ConstraintValidator<ExactlyOnePresent, Object> {

        private List<String> values;
        private String message;

        @Override
        public void initialize(ExactlyOnePresent constraintAnnotation) {
            values = Arrays.asList(constraintAnnotation.fieldNames());
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
            if (values.stream()
                    .filter(fieldName -> {
                        try {
                            Field field = object.getClass()
                                    .getDeclaredField(fieldName);
                            field.setAccessible(true);
                            return field.get(object) != null;
                        } catch (NoSuchFieldException e) {
                            throw new IllegalArgumentException(
                                    String.format("Field %s does not exist in class %s.", fieldName, object.getClass()
                                            .getName()));
                        } catch (IllegalAccessException ignored) {
                            // this will never happen
                            return false;
                        }
                    })
                    .count() == 1) {
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

    String[] fieldNames();

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

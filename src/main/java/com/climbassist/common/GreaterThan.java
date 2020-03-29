package com.climbassist.common;

import org.apache.commons.lang3.reflect.FieldUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * This annotation validates that exactly one of the fields specified is present.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GreaterThan.Validator.class)
public @interface GreaterThan {

    class Validator implements ConstraintValidator<GreaterThan, Object> {

        private String lesserFieldName;
        private String greaterFieldName;
        private String message;

        @Override
        public void initialize(GreaterThan constraintAnnotation) {
            lesserFieldName = constraintAnnotation.lesserFieldName();
            greaterFieldName = constraintAnnotation.greaterFieldName();
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
            Field lesserField = FieldUtils.getField(object.getClass(), lesserFieldName, true);
            Field greaterField = FieldUtils.getField(object.getClass(), greaterFieldName, true);

            if (!Comparable.class.isAssignableFrom(lesserField.getType()) || !Comparable.class.isAssignableFrom(
                    greaterField.getType())) {
                throw new IllegalArgumentException("Both fields must be subclasses of Comparable.");
            }

            if (!lesserField.getClass()
                    .equals(greaterField.getClass())) {
                throw new IllegalArgumentException("Both fields must be of the same type.");
            }

            try {
                Comparable lesserFieldValue = (Comparable) lesserField.get(object);
                Comparable greaterFieldValue = (Comparable) greaterField.get(object);

                if (lesserFieldValue == null || greaterFieldValue == null) {
                    throw new IllegalArgumentException("Both fields must be present and non-null");
                }

                // we've already confirmed that both classes are the same type and implement Comparable
                @SuppressWarnings("unchecked") int comparisonResult = greaterFieldValue.compareTo(lesserFieldValue);

                if (comparisonResult <= 0) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                            .addConstraintViolation();
                    return false;
                }
            } catch (IllegalAccessException e) {
                // this will never happen
                return false;
            }
            return true;
        }
    }

    String lesserFieldName() default "";

    String greaterFieldName() default "";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

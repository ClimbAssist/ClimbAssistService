package com.climbassist.api.resource.common.grade;

import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.Range;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@ConstraintComposition
@Constraint(validatedBy = {})
@Range(min = 0, max = 16, message = "Grade must be between 0 and 16.")
public @interface ValidGrade {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

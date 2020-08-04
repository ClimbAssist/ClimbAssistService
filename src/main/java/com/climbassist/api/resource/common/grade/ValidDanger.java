package com.climbassist.api.resource.common.grade;

import com.climbassist.common.OneOf;
import org.hibernate.validator.constraints.ConstraintComposition;

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
@OneOf(values = {"R", "X", "PG13"}, message = "Danger must be one of R, X, or PG13.")
public @interface ValidDanger {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

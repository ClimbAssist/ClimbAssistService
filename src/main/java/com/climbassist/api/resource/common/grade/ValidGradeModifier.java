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
@OneOf(values = {"a", "a/b", "b", "b/c", "c", "c/d", "d", "+", "-"},
        message = "Grade modifier must be one of a, a/b, b, b/c, c, c/d, d, -, or +.")
public @interface ValidGradeModifier {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

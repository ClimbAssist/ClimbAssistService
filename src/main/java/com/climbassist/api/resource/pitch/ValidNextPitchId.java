package com.climbassist.api.resource.pitch;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@ConstraintComposition
@Constraint(validatedBy = {})
// route ID (max 111) + "-pitch" (6) + "-" (1) + slug (10)
@Size(min = 1, max = 128, message = "Next pitch ID must be between 1 and 128 characters.")
@Pattern(regexp = "([a-z0-9-]*)", message = "Next pitch ID must contain only lowercase letters, numbers, and hyphens.")
public @interface ValidNextPitchId {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

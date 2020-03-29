package com.climbassist.api.resource.point;

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
// route ID (max 110) + "-point" (6) + "-" (1) + slug (10)
@Size(min = 1, max = 127, message = "Next point ID must be between 1 and 127 characters.")
@Pattern(regexp = "([a-z0-9-]*)", message = "Next point ID must contain only lowercase letters, numbers, and hyphens.")
public @interface ValidNextPointId {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

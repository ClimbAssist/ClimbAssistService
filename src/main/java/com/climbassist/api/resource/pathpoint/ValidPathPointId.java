package com.climbassist.api.resource.pathpoint;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@ConstraintComposition
@Constraint(validatedBy = {})
@NotNull(message = "Path point ID must be present.")
@Size(min = 1, max = 111, message = "Path point ID must be between 1 and 111 characters.")
@Pattern(regexp = "([a-z0-9-]*)", message = "Path point ID must contain only lowercase letters, numbers, and hyphens.")
public @interface ValidPathPointId {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.climbassist.api.resource.point;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@ConstraintComposition
@Constraint(validatedBy = {})
@NotNull(message = "Y must be present.")
@DecimalMin(value = "" + (-Double.MAX_VALUE),
        message = "Y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
@DecimalMax(value = "" + Double.MAX_VALUE,
        message = "Y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
public @interface ValidY {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

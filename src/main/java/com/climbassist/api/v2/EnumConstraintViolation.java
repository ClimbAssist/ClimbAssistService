package com.climbassist.api.v2;

import lombok.Getter;
import lombok.NonNull;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

public class EnumConstraintViolation<T extends Enum<?>> implements ConstraintViolation<T> {

    @Getter
    private final String message;

    public EnumConstraintViolation(@NonNull String fieldName, @NonNull Class<T> enumClass) {
        message = fieldName + " must be one of " + SentenceGenerator.toOrList(enumClass.getEnumConstants()) + ".";
    }

    @Override
    public String getMessageTemplate() {
        return null;
    }

    @Override
    public T getRootBean() {
        return null;
    }

    @Override
    public Class<T> getRootBeanClass() {
        return null;
    }

    @Override
    public Object getLeafBean() {
        return null;
    }

    @Override
    public Object[] getExecutableParameters() {
        return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return null;
    }

    @Override
    public Object getInvalidValue() {
        return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
    }

    @Override
    public <U> U unwrap(Class<U> type) {
        return null;
    }
}

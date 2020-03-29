package com.climbassist.api.resource.common;

class InvalidFormatException extends RuntimeException {

    <T> InvalidFormatException(T object, Throwable cause) {
        super(String.format("Unable to serialize %s %s", object.getClass()
                .getSimpleName(), object), cause);
    }

    InvalidFormatException(String object, String className, Throwable cause) {
        super(String.format("Unable to deserialize %s %s", className, object), cause);
    }
}

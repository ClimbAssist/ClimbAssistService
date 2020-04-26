package com.climbassist.api;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

public abstract class RuntimeApiException extends RuntimeException {

    public RuntimeApiException(@NonNull String message) {
        super(message);
    }

    public abstract String getType();

    public abstract HttpStatus getHttpStatus();
}

package com.climbassist.api;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends Exception {

    public ApiException(@NonNull String message) {
        super(message);
    }

    public ApiException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    public abstract String getType();

    public abstract HttpStatus getHttpStatus();
}

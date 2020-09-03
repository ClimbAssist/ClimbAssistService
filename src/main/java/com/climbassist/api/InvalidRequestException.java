package com.climbassist.api;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ApiException {

    public InvalidRequestException(@NonNull String message) {
        super(message);
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

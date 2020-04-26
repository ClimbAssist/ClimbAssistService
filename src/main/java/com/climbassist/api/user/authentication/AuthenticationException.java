package com.climbassist.api.user.authentication;

import com.climbassist.api.ApiException;
import org.springframework.http.HttpStatus;

public abstract class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}

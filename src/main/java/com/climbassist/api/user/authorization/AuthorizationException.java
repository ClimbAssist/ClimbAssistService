package com.climbassist.api.user.authorization;

import com.climbassist.api.ApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApiException {

    AuthorizationException() {
        super("You are not authorized to access this resource.");
    }

    AuthorizationException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getType() {
        return "AuthorizationException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}

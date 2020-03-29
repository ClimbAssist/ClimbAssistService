package com.climbassist.api.user.authorization;

import lombok.NonNull;

public class UserAuthorizationException extends Exception {

    UserAuthorizationException() {
        super("You are not authorized to access this resource.");
    }

    UserAuthorizationException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}

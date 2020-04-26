package com.climbassist.api.user.authorization;

import lombok.NonNull;

public class SessionExpiredException extends AuthorizationException {

    public SessionExpiredException(@NonNull Throwable cause) {
        super("Session timed out. Please sign in again.", cause);
    }
}

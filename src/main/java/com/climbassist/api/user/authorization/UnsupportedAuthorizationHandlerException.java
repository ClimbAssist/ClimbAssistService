package com.climbassist.api.user.authorization;

public class UnsupportedAuthorizationHandlerException extends RuntimeException {

    public UnsupportedAuthorizationHandlerException(Class<? extends AuthorizationHandler> clazz) {
        super(String.format("Unsupported AuthorizationHandler %s", clazz));
    }
}

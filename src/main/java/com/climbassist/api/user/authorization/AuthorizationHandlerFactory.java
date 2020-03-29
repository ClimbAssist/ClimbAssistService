package com.climbassist.api.user.authorization;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthorizationHandlerFactory {

    @NonNull
    private Map<Class<? extends AuthorizationHandler>, AuthorizationHandler> authorizationHandlerMap;

    public AuthorizationHandlerFactory(AuthorizationHandler... authorizationHandlers) {
        authorizationHandlerMap = Arrays.stream(authorizationHandlers)
                .collect(Collectors.toMap(authorizationHandler -> authorizationHandler.getClass(),
                        authorizationHandler -> authorizationHandler));
    }

    public AuthorizationHandler create(Class<? extends AuthorizationHandler> clazz) {
        if (!authorizationHandlerMap.containsKey(clazz)) {
            throw new UnsupportedAuthorizationHandlerException(clazz);
        }
        return authorizationHandlerMap.get(clazz);
    }
}

package com.climbassist.api.user.authorization;

import com.climbassist.api.user.SessionUtils;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Builder
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @NonNull AuthorizationHandlerFactory authorizationHandlerFactory;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean preHandle(@NonNull HttpServletRequest httpServletRequest,
                             @NonNull HttpServletResponse httpServletResponse, @NonNull Object handler)
            throws AuthorizationException {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authorization authorization = handlerMethod.getMethodAnnotation(Authorization.class);
        if (authorization == null) {
            return true;
        }
        if (!SessionUtils.hasSessionCookies(httpServletRequest)) {
            throw new AuthorizationException();
        }

        AuthorizationHandler authorizationHandler = authorizationHandlerFactory.create(authorization.value());
        authorizationHandler.checkAuthorization(SessionUtils.getUserSessionData(httpServletRequest));
        return true;
    }
}

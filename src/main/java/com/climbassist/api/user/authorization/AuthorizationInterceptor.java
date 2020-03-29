package com.climbassist.api.user.authorization;

import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Builder
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @NonNull AuthorizationHandlerFactory authorizationHandlerFactory;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             Object handler) throws UserAuthorizationException {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authorization authorization = handlerMethod.getMethodAnnotation(Authorization.class);
        if (authorization == null) {
            return true;
        }
        if (!SessionUtils.hasSessionCookies(httpServletRequest)) {
            throw new UserAuthorizationException();
        }

        UserSessionData userSessionData = SessionUtils.getUserSessionData(httpServletRequest);
        AuthorizationHandler authorizationHandler = authorizationHandlerFactory.create(authorization.value());
        UserSessionData newUserSessionData = authorizationHandler.checkAuthorization(userSessionData);
        SessionUtils.setSessionCookies(httpServletResponse, newUserSessionData);
        httpServletRequest.getSession()
                .setAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME, newUserSessionData.getAccessToken());
        return true;
    }
}
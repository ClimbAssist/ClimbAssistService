package com.climbassist.api.user.authorization;

import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Builder
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @NonNull AuthorizationHandlerFactory authorizationHandlerFactory;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest httpServletRequest,
            @NonNull HttpServletResponse httpServletResponse, @NonNull Object handler) throws AuthorizationException {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authorization authorization = handlerMethod.getMethodAnnotation(Authorization.class);
        if (authorization == null) {
            return true;
        }

        //noinspection unchecked
        Optional<UserData> maybeUserData = ((Optional<UserData>) httpServletRequest.getSession()
                .getAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME));
        AuthorizationHandler authorizationHandler = authorizationHandlerFactory.create(authorization.value());
        authorizationHandler.checkAuthorization(maybeUserData);
        return true;
    }
}

package com.climbassist.api.user.authorization;

import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Builder
@Slf4j
public class UserDataDecorationFilter implements Filter {

    @NonNull
    private final UserManager userManager;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        httpServletRequest.getSession()
                .setAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME, Optional.empty());

        if (SessionUtils.hasSessionCookies(httpServletRequest)) {
            UserSessionData userSessionData = SessionUtils.getUserSessionData(httpServletRequest);
            try {
                userManager.isSignedIn(userSessionData.getAccessToken());
                setAttributes(httpServletRequest, userSessionData.getAccessToken());
            } catch (AccessTokenExpiredException e) {
                log.info("Refreshing access token.");
                try {
                    String newAccessToken = userManager.refreshAccessToken(userSessionData.getRefreshToken());
                    UserSessionData newUserSessionData = UserSessionData.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(userSessionData.getRefreshToken())
                            .build();
                    SessionUtils.setSessionCookies((HttpServletResponse) servletResponse, newUserSessionData);
                    setAttributes(httpServletRequest, newAccessToken);
                } catch (SessionExpiredException sessionExpiredException) {
                    log.info("Session has expired.");
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void setAttributes(HttpServletRequest httpServletRequest, String accessToken) {
        UserData userData = userManager.getUserData(accessToken);
        httpServletRequest.getSession()
                .setAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME, accessToken);
        httpServletRequest.getSession()
                .setAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME, Optional.of(userData));
    }
}

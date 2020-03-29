package com.climbassist.api.user;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@UtilityClass
public class SessionUtils {

    public static final String ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME = "accessToken";

    private static final String ACCESS_TOKEN_COOKIE_NAME = "climbassist_accesstoken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "climbassist_refreshtoken";
    private static final String J_SESSION_ID_COOKIE_NAME = "JSESSIONID";
    private static final String COOKIE_PATH = "/";

    // This uses output parameters which is a bad practice, but we can't get around it because this is how Spring
    // implements setting cookies within a controller
    public static void setSessionCookies(@NonNull HttpServletResponse httpServletResponse,
                                         @NonNull UserSessionData userSessionData) {
        httpServletResponse.addCookie(buildCookie(ACCESS_TOKEN_COOKIE_NAME, userSessionData.getAccessToken()));
        httpServletResponse.addCookie(buildCookie(REFRESH_TOKEN_COOKIE_NAME, userSessionData.getRefreshToken()));
    }

    // This uses output parameters which is a bad practice, but we can't get around it because this is how Spring
    // implements setting cookies within a controller
    public static void removeSessionCookies(@NonNull HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(buildEmptyCookie(ACCESS_TOKEN_COOKIE_NAME));
        httpServletResponse.addCookie(buildEmptyCookie(REFRESH_TOKEN_COOKIE_NAME));
    }

    public static void removeJSessionIdCookie(@NonNull HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(buildEmptyCookie(J_SESSION_ID_COOKIE_NAME));
    }

    public static boolean hasSessionCookies(@NonNull HttpServletRequest httpServletRequest) {
        return httpServletRequest.getCookies() != null && Arrays.stream(httpServletRequest.getCookies())
                .anyMatch(cookie -> cookie.getName()
                        .equals(ACCESS_TOKEN_COOKIE_NAME)) && Arrays.stream(httpServletRequest.getCookies())
                .anyMatch(cookie -> cookie.getName()
                        .equals(REFRESH_TOKEN_COOKIE_NAME));
    }

    public static UserSessionData getUserSessionData(@NonNull HttpServletRequest httpServletRequest) {
        return UserSessionData.builder()
                .accessToken(getCookieValue(httpServletRequest, ACCESS_TOKEN_COOKIE_NAME))
                .refreshToken(getCookieValue(httpServletRequest, REFRESH_TOKEN_COOKIE_NAME))
                .build();
    }

    private String getCookieValue(HttpServletRequest httpServletRequest, String cookieName) {
        return Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName()
                        .equals(cookieName))
                .findAny()
                .orElseThrow(() -> new CookieNotPresentException(cookieName))
                .getValue();
    }

    private Cookie buildCookie(String cookieName, String cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(COOKIE_PATH);
        return cookie;
    }

    private Cookie buildEmptyCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }
}

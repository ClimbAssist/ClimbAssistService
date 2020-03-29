package com.climbassist.api.user;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@UtilityClass
public class CookieTestUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "climbassist_accesstoken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "climbassist_refreshtoken";
    private static final String J_SESSION_ID_COOKIE_NAME = "JSESSIONID";
    private static final String COOKIE_PATH = "/";

    public static void verifySessionCookiesAreSet(HttpServletResponse mockHttpServletResponse,
                                                  UserSessionData userSessionData) {
        verifySessionCookiesAreSet(mockHttpServletResponse,
                buildCookie(ACCESS_TOKEN_COOKIE_NAME, userSessionData.getAccessToken()),
                buildCookie(REFRESH_TOKEN_COOKIE_NAME, userSessionData.getRefreshToken()));
    }

    public static Cookie buildCookie(String cookieName, String cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(COOKIE_PATH);
        return cookie;
    }

    public static void verifySessionCookiesAreRemoved(HttpServletResponse mockHttpServletResponse) {
        verifySessionCookiesAreSet(mockHttpServletResponse, buildEmptyCookie(ACCESS_TOKEN_COOKIE_NAME),
                buildEmptyCookie(REFRESH_TOKEN_COOKIE_NAME));
    }

    static void verifyJSessionIdCookieIsRemoved(HttpServletResponse mockHttpServletResponse) {
        verifySessionCookiesAreSet(mockHttpServletResponse, buildEmptyCookie(J_SESSION_ID_COOKIE_NAME));
    }

    private static Cookie buildEmptyCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }

    private static void verifySessionCookiesAreSet(HttpServletResponse mockHttpServletResponse,
                                                   Cookie... expectedCookies) {
        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(mockHttpServletResponse, times(expectedCookies.length)).addCookie(cookieArgumentCaptor.capture());
        List<Cookie> actualCookies = cookieArgumentCaptor.getAllValues();
        Arrays.stream(expectedCookies)
                .forEach(expectedCookie -> assertThat(actualCookies.stream()
                        .filter(actualCookie -> StringUtils.equals(actualCookie.getName(), expectedCookie.getName()) &&
                                StringUtils.equals(actualCookie.getValue(), expectedCookie.getValue()) &&
                                StringUtils.equals(actualCookie.getPath(), expectedCookie.getPath()) &&
                                actualCookie.getMaxAge() == expectedCookie.getMaxAge())
                        .count(), is(equalTo(1L))));
    }
}

package com.climbassist.api.user;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@UtilityClass
public class CookieTestUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "climbassist_accesstoken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "climbassist_refreshtoken";
    private static final String J_SESSION_ID_COOKIE_NAME = "JSESSIONID";
    private static final String COOKIE_PATH = "/";

    public static void verifySessionCookiesAreCorrect(@NonNull MockHttpServletResponse mockHttpServletResponse,
                                                      @NonNull UserSessionData userSessionData) {
        verifyResponseContainsCookies(mockHttpServletResponse,
                buildCookie(ACCESS_TOKEN_COOKIE_NAME, userSessionData.getAccessToken()),
                buildCookie(REFRESH_TOKEN_COOKIE_NAME, userSessionData.getRefreshToken()));
    }

    public static Cookie buildCookie(@NonNull String cookieName, @NonNull String cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(COOKIE_PATH);
        return cookie;
    }

    public static Cookie[] buildSessionCookies(@NonNull String accessToken, @NonNull String refreshToken) {
        return new Cookie[]{CookieTestUtils.buildCookie(CookieTestUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken),
                CookieTestUtils.buildCookie(CookieTestUtils.REFRESH_TOKEN_COOKIE_NAME, refreshToken)};
    }

    public static void verifySessionCookiesAreRemoved(@NonNull MockHttpServletResponse mockHttpServletResponse) {
        verifyResponseContainsCookies(mockHttpServletResponse, buildEmptyCookie(ACCESS_TOKEN_COOKIE_NAME),
                buildEmptyCookie(REFRESH_TOKEN_COOKIE_NAME));
    }

    static void verifyJSessionIdCookieIsRemoved(@NonNull MockHttpServletResponse mockHttpServletResponse) {
        verifyResponseContainsCookies(mockHttpServletResponse, buildEmptyCookie(J_SESSION_ID_COOKIE_NAME));
    }

    private static Cookie buildEmptyCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }

    private static void verifyResponseContainsCookies(MockHttpServletResponse mockHttpServletResponse,
                                                      Cookie... expectedCookies) {
        Cookie[] actualCookies = mockHttpServletResponse.getCookies();
        Arrays.stream(expectedCookies)
                .forEach(expectedCookie -> assertThat(Arrays.stream(actualCookies)
                        .filter(actualCookie -> areCookiesEqual(actualCookie, expectedCookie))
                        .count(), is(equalTo(1L))));
    }

    private boolean areCookiesEqual(Cookie cookie1, Cookie cookie2) {
        return StringUtils.equals(cookie1.getName(), cookie2.getName()) && StringUtils.equals(cookie1.getValue(),
                cookie2.getValue()) && StringUtils.equals(cookie1.getPath(), cookie2.getPath()) &&
                cookie1.getMaxAge() == cookie2.getMaxAge();
    }
}

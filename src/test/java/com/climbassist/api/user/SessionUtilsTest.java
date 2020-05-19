package com.climbassist.api.user;

import com.climbassist.api.user.authentication.UserSessionData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionUtilsTest {

    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access token")
            .refreshToken("refresh token")
            .build();
    private static final Cookie ACCESS_TOKEN_COOKIE = new Cookie(CookieTestUtils.ACCESS_TOKEN_COOKIE_NAME,
            USER_SESSION_DATA.getAccessToken());
    private static final Cookie REFRESH_TOKEN_COOKIE = new Cookie(CookieTestUtils.REFRESH_TOKEN_COOKIE_NAME,
            USER_SESSION_DATA.getRefreshToken());
    private static final Cookie[] COOKIES = new Cookie[]{ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE};

    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    public void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(UserSessionData.class, USER_SESSION_DATA);
        nullPointerTester.setDefault(HttpServletResponse.class, mockHttpServletResponse);
        nullPointerTester.setDefault(HttpServletRequest.class, mockHttpServletRequest);
        nullPointerTester.testStaticMethods(SessionUtils.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void setSessionCookies_setsSessionCookies() {
        SessionUtils.setSessionCookies(mockHttpServletResponse, USER_SESSION_DATA);
        CookieTestUtils.verifySessionCookiesAreCorrect(mockHttpServletResponse, USER_SESSION_DATA);
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesIsNull() {
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesIsEmpty() {
        mockHttpServletRequest.setCookies();
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasOnlyAccessToken() {
        mockHttpServletRequest.setCookies(ACCESS_TOKEN_COOKIE);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasOnlyRefreshToken() {
        mockHttpServletRequest.setCookies(REFRESH_TOKEN_COOKIE);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasBothCookies() {
        mockHttpServletRequest.setCookies(COOKIES);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(true)));
    }

    @Test
    void getUserSessionData_returnsUserSessionDataFromCookies() {
        mockHttpServletRequest.setCookies(COOKIES);
        assertThat(SessionUtils.getUserSessionData(mockHttpServletRequest), is(equalTo(USER_SESSION_DATA)));
    }

    @Test
    void getUserSessionData_throwCookieNotPresentException_whenAccessTokenIsMissing() {
        mockHttpServletRequest.setCookies(REFRESH_TOKEN_COOKIE);
        assertThrows(CookieNotPresentException.class, () -> SessionUtils.getUserSessionData(mockHttpServletRequest));
    }

    @Test
    void getUserSessionData_throwCookieNotPresentException_whenRefreshTokenIsMissing() {
        mockHttpServletRequest.setCookies(ACCESS_TOKEN_COOKIE);
        assertThrows(CookieNotPresentException.class, () -> SessionUtils.getUserSessionData(mockHttpServletRequest));
    }

    @Test
    void removeSessionCookies_setsCookiesToEmpty() {
        mockHttpServletResponse.addCookie(ACCESS_TOKEN_COOKIE);
        mockHttpServletResponse.addCookie(REFRESH_TOKEN_COOKIE);
        SessionUtils.removeSessionCookies(mockHttpServletResponse);
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void removeJSessionIdCookie_setsJSessionIdCookieToEmpty() {
        mockHttpServletResponse.addCookie(new Cookie("JSESSIONID", "j-session-id"));
        SessionUtils.removeJSessionIdCookie(mockHttpServletResponse);
        CookieTestUtils.verifyJSessionIdCookieIsRemoved(mockHttpServletResponse);
    }
}

package com.climbassist.api.user;

import com.climbassist.api.user.authentication.UserSessionData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private HttpServletResponse mockHttpServletResponse;
    @Mock
    private HttpServletRequest mockHttpServletRequest;

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
        CookieTestUtils.verifySessionCookiesAreSet(mockHttpServletResponse, USER_SESSION_DATA);
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesIsNull() {
        when(mockHttpServletRequest.getCookies()).thenReturn(null);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesIsEmpty() {
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[0]);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasOnlyAccessToken() {
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{ACCESS_TOKEN_COOKIE});
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasOnlyRefreshToken() {
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{REFRESH_TOKEN_COOKIE});
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(false)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void hasSessionCookies_returnsFalse_whenCookiesHasBothCookies() {
        when(mockHttpServletRequest.getCookies()).thenReturn(COOKIES);
        assertThat(SessionUtils.hasSessionCookies(mockHttpServletRequest), is(equalTo(true)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void getUserSessionData_returnsUserSessionDataFromCookies() {
        when(mockHttpServletRequest.getCookies()).thenReturn(COOKIES);
        assertThat(SessionUtils.getUserSessionData(mockHttpServletRequest), is(equalTo(USER_SESSION_DATA)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void getUserSessionData_throwCookieNotPresentException_whenAccessTokenIsMissing() {
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{REFRESH_TOKEN_COOKIE});
        assertThrows(CookieNotPresentException.class, () -> SessionUtils.getUserSessionData(mockHttpServletRequest));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void getUserSessionData_throwCookieNotPresentException_whenRefreshTokenIsMissing() {
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{ACCESS_TOKEN_COOKIE});
        assertThrows(CookieNotPresentException.class, () -> SessionUtils.getUserSessionData(mockHttpServletRequest));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
    }

    @Test
    void removeSessionCookies_setsCookiesToEmpty() {
        SessionUtils.removeSessionCookies(mockHttpServletResponse);
        CookieTestUtils.verifySessionCookiesAreRemoved(mockHttpServletResponse);
    }

    @Test
    void removeJSessionIdCookie_setsJSessionIdCookieToEmpty() {
        SessionUtils.removeJSessionIdCookie(mockHttpServletResponse);
        CookieTestUtils.verifyJSessionIdCookieIsRemoved(mockHttpServletResponse);
    }
}
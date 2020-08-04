package com.climbassist.api.user.authorization;

import com.climbassist.api.user.CookieTestUtils;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.UserSessionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataDecorationFilterTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken(ACCESS_TOKEN)
            .refreshToken(REFRESH_TOKEN)
            .build();
    private static final UserData USER_DATA = UserData.builder()
            .userId("king-koopa")
            .username("bowser")
            .email("bowser@mushroom-kingdom.com")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build();

    @Mock
    private UserManager mockUserManager;
    @Mock
    private FilterChain mockFilterChain;

    private UserDataDecorationFilter userDataDecorationFilter;

    @BeforeEach
    public void setUp() {
        userDataDecorationFilter = UserDataDecorationFilter.builder()
                .userManager(mockUserManager)
                .build();
    }

    @Test
    void doFilter_doesNotRefreshAccessTokenAndDoesNotSetRequestAttributes_whenRequestDoesNotHaveSessionCookies()
            throws IOException, ServletException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        userDataDecorationFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        assertThat(mockHttpServletResponse.getCookies(), is(emptyArray()));
        //noinspection ConstantConditions
        assertThat(Collections.list(mockHttpServletRequest.getSession()
                .getAttributeNames()), not(hasItem(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME)));
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME), is(equalTo(Optional.empty())));
    }

    @Test
    void doFilter_doesNotRefreshAccessTokenAndSetsAttributes_whenAccessTokenIsNotExpired()
            throws IOException, ServletException {
        MockHttpServletRequest mockHttpServletRequest = buildMockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = buildMockHttpServletResponse();
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);

        userDataDecorationFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        verify(mockUserManager).isSignedIn(ACCESS_TOKEN);
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
        verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        CookieTestUtils.verifySessionCookiesAreCorrect(mockHttpServletResponse, USER_SESSION_DATA);
        //noinspection ConstantConditions
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME), is(equalTo(ACCESS_TOKEN)));
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME), is(equalTo(Optional.of(USER_DATA))));
    }

    @Test
    void doFilter_refreshesAccessTokenAndSetsAttributes_whenAccessTokenIsExpiredAndRefreshTokenIsNotExpired()
            throws IOException, ServletException, SessionExpiredException {
        String newAccessToken = "new-access-token";
        MockHttpServletRequest mockHttpServletRequest = buildMockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        when(mockUserManager.isSignedIn(any())).thenThrow(new AccessTokenExpiredException(new Throwable()));
        when(mockUserManager.refreshAccessToken(any())).thenReturn(newAccessToken);
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);

        userDataDecorationFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        verify(mockUserManager).isSignedIn(ACCESS_TOKEN);
        verify(mockUserManager).refreshAccessToken(REFRESH_TOKEN);
        verify(mockUserManager).getUserData(newAccessToken);
        verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        CookieTestUtils.verifySessionCookiesAreCorrect(mockHttpServletResponse, UserSessionData.builder()
                .accessToken(newAccessToken)
                .refreshToken(REFRESH_TOKEN)
                .build());
        //noinspection ConstantConditions
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME), is(equalTo(newAccessToken)));
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME), is(equalTo(Optional.of(USER_DATA))));
    }

    @Test
    void doFilter_refreshesAccessTokenAndDoesNotSetAttributes_whenAccessTokenAndRefreshTokenAreBothExpired()
            throws SessionExpiredException, IOException, ServletException {
        MockHttpServletRequest mockHttpServletRequest = buildMockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = buildMockHttpServletResponse();
        when(mockUserManager.isSignedIn(any())).thenThrow(new AccessTokenExpiredException(new Throwable()));
        when(mockUserManager.refreshAccessToken(any())).thenThrow(new SessionExpiredException(new Throwable()));

        userDataDecorationFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        verify(mockUserManager).isSignedIn(ACCESS_TOKEN);
        verify(mockUserManager).refreshAccessToken(REFRESH_TOKEN);
        verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        CookieTestUtils.verifySessionCookiesAreCorrect(mockHttpServletResponse, USER_SESSION_DATA);
        assertThat(Collections.list(mockHttpServletRequest.getAttributeNames()), not(hasItem("userId")));
        //noinspection ConstantConditions
        assertThat(Collections.list(mockHttpServletRequest.getSession()
                .getAttributeNames()), not(hasItem(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME)));
        assertThat(mockHttpServletRequest.getSession()
                .getAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME), is(equalTo(Optional.empty())));
    }

    private static MockHttpServletRequest buildMockHttpServletRequest() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setCookies(CookieTestUtils.buildSessionCookies(ACCESS_TOKEN, REFRESH_TOKEN));
        return mockHttpServletRequest;
    }

    private static MockHttpServletResponse buildMockHttpServletResponse() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Arrays.stream(CookieTestUtils.buildSessionCookies(ACCESS_TOKEN, REFRESH_TOKEN))
                .forEach(mockHttpServletResponse::addCookie);
        return mockHttpServletResponse;
    }
}

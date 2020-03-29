package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.UserSessionData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedAuthorizationHandlerTest {

    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access token")
            .refreshToken("refresh token")
            .build();
    private static final UserSessionData NEW_USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("new access token")
            .refreshToken("refresh token")
            .build();
    @Mock
    private UserManager mockUserManager;

    private AuthenticatedAuthorizationHandler authenticatedAuthorizationHandler;

    @BeforeEach
    void setUp() {
        authenticatedAuthorizationHandler = AuthenticatedAuthorizationHandler.builder()
                .userManager(mockUserManager)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(UserSessionData.class, USER_SESSION_DATA);
        nullPointerTester.testInstanceMethods(authenticatedAuthorizationHandler, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void checkAuthorization_returnsOriginalSessionData_whenUserIsSignedIn() throws UserAuthorizationException {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        assertThat(authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA),
                is(equalTo(USER_SESSION_DATA)));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_refreshesTokenAndReturnsNewSessionData_whenUserIsSignedInButAccessTokenIsExpired()
            throws UserAuthorizationException {
        doThrow(new AccessTokenExpiredException(null)).when(mockUserManager)
                .isSignedIn(USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.refreshAccessToken(any())).thenReturn(NEW_USER_SESSION_DATA.getAccessToken());
        doReturn(true).when(mockUserManager)
                .isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());

        assertThat(authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA),
                is(equalTo(NEW_USER_SESSION_DATA)));

        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
        verify(mockUserManager).refreshAccessToken(USER_SESSION_DATA.getRefreshToken());
        verify(mockUserManager).isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsUserAuthorizationException_whenUserIsNotSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(false);
        assertThrows(UserAuthorizationException.class,
                () -> authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsUserAuthorizationException_whenUserIsNotSignedInAfterRefreshingToken()
            throws SessionExpiredException {
        doThrow(new AccessTokenExpiredException(null)).when(mockUserManager)
                .isSignedIn(USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.refreshAccessToken(any())).thenReturn(NEW_USER_SESSION_DATA.getAccessToken());
        doReturn(false).when(mockUserManager)
                .isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());

        assertThrows(UserAuthorizationException.class,
                () -> authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));

        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
        verify(mockUserManager).refreshAccessToken(USER_SESSION_DATA.getRefreshToken());
        verify(mockUserManager).isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());
    }
}
package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.UserSessionData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedAuthorizationHandlerTest {

    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access token")
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
    void checkAuthorization_returns_whenUserIsSignedIn() throws AuthorizationException {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA);
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(false);
        assertThrows(AuthorizationException.class,
                () -> authenticatedAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }
}

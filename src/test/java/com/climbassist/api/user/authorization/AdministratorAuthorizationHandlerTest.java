package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
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
class AdministratorAuthorizationHandlerTest {

    private static final String USERNAME = "captain-america";
    private static final String EMAIL = "cap@shield.com";
    private static final String USER_ID = "987654320";
    private static final UserData USER_DATA_ADMINISTRATOR = UserData.builder()
            .userId(USER_ID)
            .username(USERNAME)
            .email(EMAIL)
            .isEmailVerified(true)
            .isAdministrator(true)
            .build();
    private static final UserData USER_DATA_NOT_ADMINISTRATOR = UserData.builder()
            .userId(USER_ID)
            .username(USERNAME)
            .email(EMAIL)
            .isEmailVerified(true)
            .isAdministrator(false)
            .build();
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access token")
            .refreshToken("refresh token")
            .build();

    @Mock
    private UserManager mockUserManager;

    private AdministratorAuthorizationHandler administratorAuthorizationHandler;

    @BeforeEach
    void setUp() {
        administratorAuthorizationHandler = AdministratorAuthorizationHandler.builder()
                .userManager(mockUserManager)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(UserSessionData.class, USER_SESSION_DATA);
        nullPointerTester.testInstanceMethods(administratorAuthorizationHandler, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void checkAuthorization_returns_whenUserIsSignedInAndIsAdministrator() throws AuthorizationException {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_ADMINISTRATOR);
        administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA);
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsSignedInAndIsNotAdministrator() {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_NOT_ADMINISTRATOR);
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(false);
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }
}

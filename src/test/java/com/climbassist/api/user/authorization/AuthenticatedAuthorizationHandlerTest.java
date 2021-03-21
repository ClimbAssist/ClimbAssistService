package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedAuthorizationHandlerTest {

    private AuthenticatedAuthorizationHandler authenticatedAuthorizationHandler;

    @BeforeEach
    void setUp() {
        authenticatedAuthorizationHandler = AuthenticatedAuthorizationHandler.builder()
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(authenticatedAuthorizationHandler, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void checkAuthorization_returns_whenUserIsSignedIn() throws AuthorizationException {
        authenticatedAuthorizationHandler.checkAuthorization(Optional.of(UserData.builder()
                .userId("kirby")
                .username("popopo")
                .email("kirby@dreamland.com")
                .isEmailVerified(true)
                .isAdministrator(false)
                .build()));
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedIn() {
        assertThrows(AuthorizationException.class,
                () -> authenticatedAuthorizationHandler.checkAuthorization(Optional.empty()));
    }
}

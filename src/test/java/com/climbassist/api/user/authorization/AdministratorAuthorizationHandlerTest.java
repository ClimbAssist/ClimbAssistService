package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AdministratorAuthorizationHandlerTest {

    private AdministratorAuthorizationHandler administratorAuthorizationHandler;

    @BeforeEach
    void setUp() {
        administratorAuthorizationHandler = AdministratorAuthorizationHandler.builder()
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(administratorAuthorizationHandler, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void checkAuthorization_returns_whenUserIsSignedInAndIsAdministrator() throws AuthorizationException {
        administratorAuthorizationHandler.checkAuthorization(buildOptionalUserData(true));
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsSignedInAndIsNotAdministrator() {
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(buildOptionalUserData(false)));
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedIn() {
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(Optional.empty()));
    }

    private static Optional<UserData> buildOptionalUserData(boolean isAdministrator) {
        return Optional.of(UserData.builder()
                .userId("kirby")
                .username("popopo")
                .email("kirby@dreamland.com")
                .isEmailVerified(true)
                .isAdministrator(isAdministrator)
                .build());
    }
}

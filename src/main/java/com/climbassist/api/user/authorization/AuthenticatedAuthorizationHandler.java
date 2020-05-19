package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

// returns true for any authenticated (logged-in) user
@SuperBuilder
public class AuthenticatedAuthorizationHandler implements AuthorizationHandler {

    @NonNull
    protected final UserManager userManager;

    @Override
    public void checkAuthorization(@NonNull UserSessionData userSessionData) throws AuthorizationException {
        if (!userManager.isSignedIn(userSessionData.getAccessToken())) {
            throw new AuthorizationException();
        }
    }
}

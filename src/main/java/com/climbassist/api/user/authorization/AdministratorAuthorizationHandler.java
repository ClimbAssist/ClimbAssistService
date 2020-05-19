package com.climbassist.api.user.authorization;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

// returns true if the user is signed in and is an administrator
@SuperBuilder
public class AdministratorAuthorizationHandler extends AuthenticatedAuthorizationHandler {

    @Override
    public void checkAuthorization(@NonNull UserSessionData userSessionData) throws AuthorizationException {
        super.checkAuthorization(userSessionData);
        if (!userManager.getUserData(userSessionData.getAccessToken())
                .isAdministrator()) {
            throw new AuthorizationException();
        }
    }
}

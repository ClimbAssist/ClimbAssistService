package com.climbassist.api.user.authorization;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

// returns true if the user is signed in and is an administrator
@SuperBuilder
public class AdministratorAuthorizationHandler extends AuthenticatedAuthorizationHandler {

    @Override
    public UserSessionData checkAuthorization(@NonNull UserSessionData userSessionData)
            throws UserAuthorizationException {
        UserSessionData newUserSessionData = super.checkAuthorization(userSessionData);
        if (!userManager.getUserData(newUserSessionData.getAccessToken())
                .isAdministrator()) {
            throw new UserAuthorizationException();
        }
        return newUserSessionData;
    }
}

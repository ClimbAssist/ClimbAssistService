package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

// returns true for any authenticated (logged-in) user
@SuperBuilder
public class AuthenticatedAuthorizationHandler implements AuthorizationHandler {

    @NonNull
    protected final UserManager userManager;

    @Override
    public UserSessionData checkAuthorization(@NonNull UserSessionData userSessionData)
            throws AuthorizationException {
        UserSessionData newUserSessionData = userSessionData;
        try {
            if (!userManager.isSignedIn(userSessionData.getAccessToken())) {
                throw new AuthorizationException();
            }
        } catch (AccessTokenExpiredException e) {
            String newAccessToken = userManager.refreshAccessToken(userSessionData.getRefreshToken());
            if (!userManager.isSignedIn(newAccessToken)) {
                throw new AuthorizationException();
            }
            newUserSessionData = UserSessionData.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(userSessionData.getRefreshToken())
                    .build();
        }
        return newUserSessionData;
    }
}

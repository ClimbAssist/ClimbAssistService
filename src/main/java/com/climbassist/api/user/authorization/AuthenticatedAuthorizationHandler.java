package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

// returns true for any authenticated (logged-in) user
@SuperBuilder
public class AuthenticatedAuthorizationHandler implements AuthorizationHandler {

    @Override
    public void checkAuthorization(@NonNull Optional<UserData> maybeUserData) throws AuthorizationException {
        if (!maybeUserData.isPresent()) {
            throw new AuthorizationException();
        }
    }
}

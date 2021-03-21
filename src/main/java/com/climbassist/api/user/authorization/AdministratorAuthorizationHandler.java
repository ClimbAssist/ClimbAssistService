package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

// returns true if the user is signed in and is an administrator
@SuperBuilder
public class AdministratorAuthorizationHandler implements AuthorizationHandler {

    @Override
    public void checkAuthorization(@NonNull Optional<UserData> maybeUserData) throws AuthorizationException {
        if (!maybeUserData.isPresent() || !maybeUserData.get()
                .isAdministrator()) {
            throw new AuthorizationException();
        }
    }
}

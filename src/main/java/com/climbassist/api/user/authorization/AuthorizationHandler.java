package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import lombok.NonNull;

import java.util.Optional;

public interface AuthorizationHandler {

    void checkAuthorization(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NonNull Optional<UserData> maybeUserData)
            throws AuthorizationException;
}

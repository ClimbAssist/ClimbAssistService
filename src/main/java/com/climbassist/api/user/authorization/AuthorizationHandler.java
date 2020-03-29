package com.climbassist.api.user.authorization;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;

public interface AuthorizationHandler {

    UserSessionData checkAuthorization(@NonNull UserSessionData userSessionData) throws UserAuthorizationException;
}

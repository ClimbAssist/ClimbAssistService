package com.climbassist.api.user.authorization;

import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;

public interface AuthorizationHandler {

    void checkAuthorization(@NonNull UserSessionData userSessionData) throws AuthorizationException;
}

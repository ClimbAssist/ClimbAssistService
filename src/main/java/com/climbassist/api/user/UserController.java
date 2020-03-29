package com.climbassist.api.user;

import com.climbassist.metrics.Metrics;
import com.climbassist.api.user.authorization.AuthenticatedAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.Valid;

@Builder
@RestController
public class UserController {

    @NonNull
    private final UserManager userManager;

    @Metrics(api = "GetUser")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "/v1/user", method = RequestMethod.GET)
    public UserData getUser(
            @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME) @NonNull String accessToken) {
        return userManager.getUserData(accessToken);
    }

    @Metrics(api = "UpdateUser")
    @Authorization(AuthenticatedAuthorizationHandler.class)
    @RequestMapping(path = "/v1/user", method = RequestMethod.POST)
    public UserData updateUser(@NonNull @Valid @RequestBody UpdateUserRequest updateUserRequest,
                               @SessionAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME)
                               @NonNull String accessToken) {
        userManager.updateUser(accessToken, updateUserRequest.getEmail());
        return userManager.getUserData(accessToken);
    }
}

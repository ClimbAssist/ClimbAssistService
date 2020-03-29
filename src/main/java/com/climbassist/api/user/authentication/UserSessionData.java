package com.climbassist.api.user.authentication;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class UserSessionData {

    @NonNull
    private String accessToken;
    @NonNull
    private String refreshToken;
}

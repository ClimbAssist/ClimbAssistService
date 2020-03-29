package com.climbassist.test.integration.api.user;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TestUserContext {

    private String username;
    private String password;
    private TestEmailContext testEmailContext;
}

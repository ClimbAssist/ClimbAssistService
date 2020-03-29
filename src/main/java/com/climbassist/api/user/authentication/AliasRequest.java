package com.climbassist.api.user.authentication;


import com.climbassist.api.user.ValidOptionalEmail;
import com.climbassist.api.user.ValidOptionalUsername;
import com.climbassist.common.ExactlyOnePresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
@ExactlyOnePresent(fieldNames = {"username", "email"},
        message = "Either username or email must be present, but not both.")
public class AliasRequest {

    @ValidOptionalUsername
    @Nullable
    private String username;

    @ValidOptionalEmail
    @Nullable
    private String email;
}

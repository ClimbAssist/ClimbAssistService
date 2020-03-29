package com.climbassist.api.user.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class VerifyEmailRequest {

    @ValidVerificationCode
    private String verificationCode;
}

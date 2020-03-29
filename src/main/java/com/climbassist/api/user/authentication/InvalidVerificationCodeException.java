package com.climbassist.api.user.authentication;

public class InvalidVerificationCodeException extends UserAuthenticationException {

    public InvalidVerificationCodeException(Throwable cause) {
        super("The verification code provided is invalid. It may be expired.", cause);
    }
}

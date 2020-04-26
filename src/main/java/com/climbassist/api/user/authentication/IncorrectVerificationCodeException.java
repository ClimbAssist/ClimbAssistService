package com.climbassist.api.user.authentication;

public class IncorrectVerificationCodeException extends AuthenticationException {

    public IncorrectVerificationCodeException(Throwable cause) {
        super("The verification code provided is invalid. It may be expired.", cause);
    }

    @Override
    public String getType() {
        return "IncorrectVerificationCodeException";
    }
}

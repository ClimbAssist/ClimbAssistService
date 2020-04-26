package com.climbassist.api.user.authentication;

public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException() {
        super("Your email has not been verified. Please check your email for a verification code.");
    }

    @Override
    public String getType() {
        return "EmailNotVerifiedException";
    }
}

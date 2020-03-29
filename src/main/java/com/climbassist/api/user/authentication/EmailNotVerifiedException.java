package com.climbassist.api.user.authentication;

public class EmailNotVerifiedException extends UserAuthenticationException {

    public EmailNotVerifiedException() {
        super("Your email has not been verified. Please check your email for a verification code.");
    }
}

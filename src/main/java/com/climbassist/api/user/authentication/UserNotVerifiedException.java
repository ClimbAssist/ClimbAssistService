package com.climbassist.api.user.authentication;

public class UserNotVerifiedException extends UserAuthenticationException {

    public UserNotVerifiedException() {
        super("Your account has not been verified. Please check your email for a verification link.");
    }
}

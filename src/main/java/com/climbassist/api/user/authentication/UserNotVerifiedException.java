package com.climbassist.api.user.authentication;

public class UserNotVerifiedException extends AuthenticationException {

    public UserNotVerifiedException() {
        super("Your account has not been verified. Please check your email for a verification link.");
    }

    @Override
    public String getType() {
        return "UserNotVerifiedException";
    }
}

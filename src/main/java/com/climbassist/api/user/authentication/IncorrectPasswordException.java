package com.climbassist.api.user.authentication;

public class IncorrectPasswordException extends AuthenticationException {

    public IncorrectPasswordException() {
        super("The password you entered is incorrect.");
    }

    @Override
    public String getType() {
        return "IncorrectPasswordException";
    }
}

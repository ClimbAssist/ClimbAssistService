package com.climbassist.api.user.authentication;

public class InvalidPasswordException extends UserAuthenticationException {

    public InvalidPasswordException() {
        super("The password you entered is invalid.");
    }
}

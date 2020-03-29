package com.climbassist.api.user.authentication;

public class UserAuthenticationException extends Exception {

    public UserAuthenticationException(String message) {
        super(message);
    }

    public UserAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

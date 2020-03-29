package com.climbassist.api.user.authentication;

public class AccessTokenExpiredException extends RuntimeException {

    public AccessTokenExpiredException(Throwable cause) {
        super(cause);
    }

}

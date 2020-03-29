package com.climbassist.api.user;

public class CookieNotPresentException extends RuntimeException {

    public CookieNotPresentException(String cookieName) {
        super(String.format("Expected cookie %s not present.", cookieName));
    }
}

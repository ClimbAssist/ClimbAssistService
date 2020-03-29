package com.climbassist.api.user.authentication;

public class EmailAlreadyVerifiedException extends Exception {

    public EmailAlreadyVerifiedException() {
        super("Email address is already verified.");
    }
}

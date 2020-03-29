package com.climbassist.api.user;

class InvalidUserDataException extends RuntimeException {

    InvalidUserDataException() {
        super("User data is invalid.");
    }
}

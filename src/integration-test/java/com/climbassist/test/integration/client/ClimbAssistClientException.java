package com.climbassist.test.integration.client;

import lombok.NonNull;

public class ClimbAssistClientException extends RuntimeException {

    public ClimbAssistClientException(@NonNull Throwable cause) {
        super(cause);
    }
}

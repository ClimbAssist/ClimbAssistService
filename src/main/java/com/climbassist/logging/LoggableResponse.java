package com.climbassist.logging;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
class LoggableResponse {

    private long duration;
    private int status;
    private String headers;
    private String body;
}

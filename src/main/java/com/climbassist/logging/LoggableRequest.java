package com.climbassist.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
class LoggableRequest {

    private String protocol;
    private String sender;
    private String method;
    private String path;
    private String queryString;
    private Map<String, String> queryParameters;
    private String headers;
    private String body;
}

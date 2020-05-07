package com.climbassist.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class LoggableResponse {

    long duration;
    int status;
    Multimap<String, String> headers;
    // only one of body or jsonBody should ever be present, depending on if the body is JSON or not
    String body;
    @JsonRawValue
    String jsonBody;
}

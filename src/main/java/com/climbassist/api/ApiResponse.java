package com.climbassist.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This represents a REST API response that is returned from all APIs.
 */
@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    @AllArgsConstructor // required for @Builder, because of a bug
    @Builder
    @Data
    @NoArgsConstructor
    public static class Error {

        private String type;
        private String message;
    }

    public static final int EXTRA_CHARACTERS_FOR_ERROR = 10;

    @JsonRawValue // we need this because we know this string is a JSON object so we don't want Jackson to escape it
    private String data;
    @JsonRawValue // we need this because we know this string is a JSON object so we don't want Jackson to escape it
    private String error;
}

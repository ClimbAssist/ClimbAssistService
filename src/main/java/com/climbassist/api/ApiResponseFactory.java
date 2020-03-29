package com.climbassist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;

@Builder
class ApiResponseFactory {

    @NonNull
    private final ObjectMapper objectMapper;

    ApiResponse createFromData(String data) {
        return ApiResponse.builder()
                .data(data)
                .build();
    }

    ApiResponse createFromError(String error) {
        return ApiResponse.builder()
                .error(error)
                .build();
    }
}

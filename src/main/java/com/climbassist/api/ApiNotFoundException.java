package com.climbassist.api;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

class ApiNotFoundException extends ApiException {

    ApiNotFoundException(@NonNull String path, @NonNull String method) {
        super(String.format("No API exists at path %s with method %s.", path, method));
    }

    @Override
    public String getType() {
        return "ApiNotFoundException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

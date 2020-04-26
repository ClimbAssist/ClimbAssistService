package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.RuntimeApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class InvalidOrderingException extends RuntimeApiException {

    public InvalidOrderingException(@NonNull String parentId, @NonNull String message) {
        super(String.format("Unable to build ordered list for children of resource %s. %s", parentId, message));
    }

    @Override
    public String getType() {
        return "InvalidOrderingException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}

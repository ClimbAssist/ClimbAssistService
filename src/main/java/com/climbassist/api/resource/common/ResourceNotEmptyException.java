package com.climbassist.api.resource.common;

import com.climbassist.api.ApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class ResourceNotEmptyException extends ApiException {

    public ResourceNotEmptyException(@NonNull String resourceType, @NonNull String resourceId) {
        super(String.format(
                "Can't delete %s %s because it contains at least one child resource. Ensure that the %s contains no " +
                        "children before deleting.", resourceType, resourceId, resourceType));
    }

    @Override
    public String getType() {
        return "ResourceNotEmptyException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}

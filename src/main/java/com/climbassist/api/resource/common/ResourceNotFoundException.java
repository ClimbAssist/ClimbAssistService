package com.climbassist.api.resource.common;

import com.climbassist.api.ApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(@NonNull String resourceType, @NonNull String resourceId) {
        super(String.format("No %s with ID %s found.", resourceType, resourceId));
    }

    @Override
    public String getType() {
        return "ResourceNotFoundException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

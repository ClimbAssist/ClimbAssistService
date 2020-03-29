package com.climbassist.api.resource.common;

public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("No %s with ID %s found.", resourceType, resourceId));
    }
}

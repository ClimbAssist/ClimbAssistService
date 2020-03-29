package com.climbassist.api.resource.common;

import lombok.NonNull;

public class InvalidChildException extends RuntimeException {

    public InvalidChildException(@NonNull Class<?> resourceType, @NonNull Class<?> childResourceType) {
        super(String.format("Resource type %s is not a valid child of resource type %s.",
                childResourceType.getSimpleName(), resourceType.getSimpleName()));
    }
}

package com.climbassist.api.resource.common;

import lombok.NonNull;

public class ResourceNotEmptyException extends Exception {

    public ResourceNotEmptyException(@NonNull String resourceType, @NonNull String resourceId) {
        super(String.format(
                "Can't delete %s %s because it contains at least one child resource. Ensure that the %s contains no " +
                        "children before deleting.", resourceType, resourceId, resourceType));
    }
}

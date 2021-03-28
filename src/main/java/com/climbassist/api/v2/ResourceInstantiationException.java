package com.climbassist.api.v2;

import lombok.NonNull;

public class ResourceInstantiationException extends RuntimeException {

    public <T extends Resource> ResourceInstantiationException(@NonNull final Class<T> resourceType,
            @NonNull final Throwable cause) {
        super("Could not instantiate resource of type " + resourceType + ".", cause);
    }
}

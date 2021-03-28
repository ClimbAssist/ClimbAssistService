package com.climbassist.api.v2;

import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;

public class ResourceFactory {

    public <T extends Resource> T buildResource(@NonNull final Class<T> resourceTypeClass) {
        try {
            return resourceTypeClass.getDeclaredConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ResourceInstantiationException(resourceTypeClass, e);
        }
    }

    public <T extends Resource> T buildResource(@NonNull final Class<T> resourceTypeClass, @NonNull final String id) {
        T resource = buildResource(resourceTypeClass);
        resource.setId(id);
        return resource;
    }
}

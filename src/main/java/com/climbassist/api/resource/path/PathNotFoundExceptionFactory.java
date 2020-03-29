package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class PathNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Path> {

    @Override
    public ResourceNotFoundException create(@NonNull String pathId) {
        return new PathNotFoundException(pathId);
    }
}

package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class PathNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Path> {

    @Override
    public ResourceNotEmptyException create(@NonNull String pathId) {
        return new PathNotEmptyException(pathId);
    }
}

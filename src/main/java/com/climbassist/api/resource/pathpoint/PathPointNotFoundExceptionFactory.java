package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class PathPointNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<PathPoint> {

    @Override
    public ResourceNotFoundException create(@NonNull String pathPointId) {
        return new PathPointNotFoundException(pathPointId);
    }
}

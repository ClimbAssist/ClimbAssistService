package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class PointNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Point> {

    @Override
    public ResourceNotFoundException create(@NonNull String pointId) {
        return new PointNotFoundException(pointId);
    }
}
package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class WallNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Wall> {

    @Override
    public ResourceNotFoundException create(@NonNull String wallId) {
        return new WallNotFoundException(wallId);
    }
}

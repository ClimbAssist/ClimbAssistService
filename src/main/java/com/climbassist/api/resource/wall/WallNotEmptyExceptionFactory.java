package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class WallNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Wall> {

    @Override
    public ResourceNotEmptyException create(@NonNull String wallId) {
        return new WallNotEmptyException(wallId);
    }
}

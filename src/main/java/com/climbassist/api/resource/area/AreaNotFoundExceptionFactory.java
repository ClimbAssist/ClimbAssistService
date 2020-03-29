package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class AreaNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Area> {

    @Override
    public ResourceNotFoundException create(@NonNull String areaId) {
        return new AreaNotFoundException(areaId);
    }
}
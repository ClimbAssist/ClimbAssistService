package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class RegionNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Region> {

    @Override
    public ResourceNotFoundException create(@NonNull String regionId) {
        return new RegionNotFoundException(regionId);
    }
}
package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class RegionNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Region> {

    @Override
    public ResourceNotEmptyException create(@NonNull String regionId) {
        return new RegionNotEmptyException(regionId);
    }
}

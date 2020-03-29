package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class AreaNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Area> {

    @Override
    public ResourceNotEmptyException create(@NonNull String areaId) {
        return new AreaNotEmptyException(areaId);
    }
}

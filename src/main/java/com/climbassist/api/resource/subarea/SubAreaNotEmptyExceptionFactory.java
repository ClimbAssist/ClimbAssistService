package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class SubAreaNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<SubArea> {

    @Override
    public ResourceNotEmptyException create(@NonNull String subAreaId) {
        return new SubAreaNotEmptyException(subAreaId);
    }
}

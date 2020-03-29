package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class SubAreaNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<SubArea> {

    @Override
    public ResourceNotFoundException create(@NonNull String subAreaId) {
        return new SubAreaNotFoundException(subAreaId);
    }
}

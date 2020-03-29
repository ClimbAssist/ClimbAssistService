package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class CragNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Crag> {

    @Override
    public ResourceNotFoundException create(@NonNull String cragId) {
        return new CragNotFoundException(cragId);
    }
}
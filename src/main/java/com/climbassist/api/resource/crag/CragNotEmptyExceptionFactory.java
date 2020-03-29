package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class CragNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Crag> {

    @Override
    public ResourceNotEmptyException create(@NonNull String cragId) {
        return new CragNotEmptyException(cragId);
    }
}

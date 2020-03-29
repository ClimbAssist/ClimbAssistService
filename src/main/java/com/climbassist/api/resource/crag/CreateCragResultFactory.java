package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateCragResultFactory implements CreateResourceResultFactory<Crag> {

    @Override
    public CreateResourceResult<Crag> create(@NonNull String cragId) {
        return CreateCragResult.builder()
                .cragId(cragId)
                .build();
    }
}
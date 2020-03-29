package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateSubAreaResultFactory implements CreateResourceResultFactory<SubArea> {

    @Override
    public CreateResourceResult<SubArea> create(@NonNull String subAreaId) {
        return CreateSubAreaResult.builder()
                .subAreaId(subAreaId)
                .build();
    }
}
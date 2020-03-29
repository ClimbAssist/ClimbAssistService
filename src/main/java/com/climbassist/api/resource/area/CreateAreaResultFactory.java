package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateAreaResultFactory implements CreateResourceResultFactory<Area> {

    @Override
    public CreateResourceResult<Area> create(@NonNull String areaId) {
        return CreateAreaResult.builder()
                .areaId(areaId)
                .build();
    }
}
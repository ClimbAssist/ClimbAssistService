package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateRegionResultFactory implements CreateResourceResultFactory<Region> {

    @Override
    public CreateResourceResult<Region> create(@NonNull String regionId) {
        return CreateRegionResult.builder()
                .regionId(regionId)
                .build();
    }
}
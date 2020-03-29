package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateWallResultFactory implements CreateResourceResultFactory<Wall> {

    @Override
    public CreateResourceResult<Wall> create(@NonNull String wallId) {
        return CreateWallResult.builder()
                .wallId(wallId)
                .build();
    }
}
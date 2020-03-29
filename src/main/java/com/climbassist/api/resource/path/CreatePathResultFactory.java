package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreatePathResultFactory implements CreateResourceResultFactory<Path> {

    @Override
    public CreateResourceResult<Path> create(@NonNull String pathId) {
        return CreatePathResult.builder()
                .pathId(pathId)
                .build();
    }
}

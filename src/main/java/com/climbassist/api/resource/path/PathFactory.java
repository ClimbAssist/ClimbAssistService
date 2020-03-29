package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class PathFactory extends ResourceFactory<Path, NewPath> {

    @Builder
    private PathFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Path create(@NonNull NewPath newPath) {
        return Path.builder()
                .pathId(resourceIdGenerator.generateResourceId(String.format("%s-path", newPath.getCragId())))
                .cragId(newPath.getCragId())
                .build();
    }
}

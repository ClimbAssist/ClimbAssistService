package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class SubAreaFactory extends ResourceFactory<SubArea, NewSubArea> {

    @Builder
    private SubAreaFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public SubArea create(@NonNull NewSubArea newSubArea) {
        return SubArea.builder()
                .subAreaId(resourceIdGenerator.generateResourceId(newSubArea.getName()))
                .areaId(newSubArea.getAreaId())
                .name(newSubArea.getName())
                .description(newSubArea.getDescription())
                .build();
    }
}

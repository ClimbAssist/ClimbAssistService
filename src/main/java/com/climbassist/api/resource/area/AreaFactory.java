package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class AreaFactory extends ResourceFactory<Area, NewArea> {

    @Builder
    private AreaFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Area create(@NonNull NewArea newArea) {
        return Area.builder()
                .areaId(resourceIdGenerator.generateResourceId(newArea.getName()))
                .regionId(newArea.getRegionId())
                .name(newArea.getName())
                .description(newArea.getDescription())
                .build();
    }
}
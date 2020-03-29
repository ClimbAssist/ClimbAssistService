package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class WallFactory extends ResourceFactory<Wall, NewWall> {

    @Builder
    private WallFactory(@NonNull ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Wall create(@NonNull NewWall newWall) {
        return Wall.builder()
                .wallId(resourceIdGenerator.generateResourceId(newWall.getName()))
                .cragId(newWall.getCragId())
                .name(newWall.getName())
                .first(newWall.getFirst())
                .next(newWall.getNext())
                .build();
    }
}

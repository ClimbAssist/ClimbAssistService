package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchResourceFactory;
import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.pitch.Pitch;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

public class PointFactory extends ResourceFactory<Point, NewPoint>
        implements BatchResourceFactory<Point, NewPoint, Pitch, BatchNewPoint> {

    @Builder
    private PointFactory(@NonNull ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Point create(@NonNull NewPoint newPoint) {
        return Point.builder()
                .pointId(resourceIdGenerator.generateResourceId(String.format("%s-point", newPoint.getPitchId())))
                .pitchId(newPoint.getPitchId())
                .x(newPoint.getX())
                .y(newPoint.getY())
                .z(newPoint.getZ())
                .first(newPoint.getFirst())
                .next(newPoint.getNext())
                .build();
    }

    @Override
    public NewPoint create(String parentResourceId, BatchNewPoint batchNewResource, boolean first) {
        return create(parentResourceId, batchNewResource, first, Optional.empty());
    }

    @Override
    public NewPoint create(String parentResourceId, BatchNewPoint batchNewResource, boolean first, String next) {
        return create(parentResourceId, batchNewResource, first, Optional.of(next));
    }

    public NewPoint create(String parentResourceId, BatchNewPoint batchNewResource, boolean first,
                           @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext) {
        return NewPoint.builder()
                .pitchId(parentResourceId)
                .x(batchNewResource.getX())
                .y(batchNewResource.getY())
                .z(batchNewResource.getZ())
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
    }
}

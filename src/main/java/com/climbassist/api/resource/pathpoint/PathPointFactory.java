package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchResourceFactory;
import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.path.Path;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

public class PathPointFactory extends ResourceFactory<PathPoint, NewPathPoint>
        implements BatchResourceFactory<PathPoint, NewPathPoint, Path, BatchNewPathPoint> {

    @Builder
    private PathPointFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public PathPoint create(@NonNull NewPathPoint newPathPoint) {
        return PathPoint.builder()
                .pathPointId((resourceIdGenerator.generateResourceId(
                        String.format("%s-path-point", newPathPoint.getPathId()))))
                .pathId(newPathPoint.getPathId())
                .latitude(newPathPoint.getLatitude())
                .longitude(newPathPoint.getLongitude())
                .first(newPathPoint.getFirst())
                .next(newPathPoint.getNext())
                .build();
    }

    @Override
    public NewPathPoint create(String parentResourceId, BatchNewPathPoint batchNewResource, boolean first) {
        return create(parentResourceId, batchNewResource, first, Optional.empty());
    }

    @Override
    public NewPathPoint create(String parentResourceId, BatchNewPathPoint batchNewResource, boolean first,
                               String next) {
        return create(parentResourceId, batchNewResource, first, Optional.of(next));
    }

    public NewPathPoint create(String parentResourceId, BatchNewPathPoint batchNewResource, boolean first,
                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext) {
        return NewPathPoint.builder()
                .pathId(parentResourceId)
                .latitude(batchNewResource.getLatitude())
                .longitude(batchNewResource.getLongitude())
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
    }
}

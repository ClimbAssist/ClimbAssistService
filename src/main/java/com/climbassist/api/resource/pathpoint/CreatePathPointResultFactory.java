package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.common.batch.BatchCreateResourceResultFactory;
import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import com.climbassist.api.resource.path.Path;
import lombok.NonNull;

import java.util.List;

public class CreatePathPointResultFactory
        implements CreateResourceResultFactory<PathPoint>, BatchCreateResourceResultFactory<PathPoint, Path> {

    @Override
    public CreateResourceResult<PathPoint> create(@NonNull String pathId) {
        return CreatePathPointResult.builder()
                .pathPointId(pathId)
                .build();
    }

    @Override
    public BatchCreateResourcesResult<PathPoint, Path> create(@NonNull List<String> pathPointsIds) {
        return BatchCreatePathPointsResult.builder()
                .pathPointIds(pathPointsIds)
                .build();
    }
}

package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.common.batch.BatchCreateResourceResultFactory;
import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import com.climbassist.api.resource.pitch.Pitch;
import lombok.NonNull;

import java.util.List;

public class CreatePointResultFactory
        implements CreateResourceResultFactory<Point>, BatchCreateResourceResultFactory<Point, Pitch> {

    @Override
    public CreateResourceResult<Point> create(@NonNull String pointId) {
        return CreatePointResult.builder()
                .pointId(pointId)
                .build();
    }

    @Override
    public BatchCreateResourcesResult<Point, Pitch> create(List<String> resourceIds) {
        return BatchCreatePointsResult.builder()
                .pointIds(resourceIds)
                .build();
    }
}

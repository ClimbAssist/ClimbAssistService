package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.pitch.Pitch;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PointsDao extends ResourceWithParentDao<Point, Pitch> {

    @Override
    protected Point buildResourceForDeletion(@NonNull String resourceId) {
        return Point.builder()
                .pointId(resourceId)
                .build();
    }

    @Override
    protected Point buildIndexHashKey(@NonNull String parentId) {
        return Point.builder()
                .pitchId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Point.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Point> getResourceTypeClass() {
        return Point.class;
    }
}

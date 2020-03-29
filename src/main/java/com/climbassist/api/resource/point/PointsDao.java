package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PointsDao extends ResourceDao<Point> {

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

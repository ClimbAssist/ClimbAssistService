package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.path.Path;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PathPointsDao extends ResourceWithParentDao<PathPoint, Path> {

    @Override
    protected PathPoint buildResourceForDeletion(@NonNull String resourceId) {
        return PathPoint.builder()
                .pathPointId(resourceId)
                .build();
    }

    @Override
    protected PathPoint buildIndexHashKey(@NonNull String parentId) {
        return PathPoint.builder()
                .pathId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return PathPoint.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<PathPoint> getResourceTypeClass() {
        return PathPoint.class;
    }
}

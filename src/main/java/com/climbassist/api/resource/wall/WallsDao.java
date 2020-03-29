package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class WallsDao extends ResourceDao<Wall> {

    @Override
    protected Wall buildResourceForDeletion(@NonNull String resourceId) {
        return Wall.builder()
                .wallId(resourceId)
                .build();
    }

    @Override
    protected Wall buildIndexHashKey(@NonNull String parentId) {
        return Wall.builder()
                .cragId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Wall.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Wall> getResourceTypeClass() {
        return Wall.class;
    }
}

package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PathsDao extends ResourceDao<Path> {

    @Override
    protected Path buildResourceForDeletion(@NonNull String resourceId) {
        return Path.builder()
                .pathId(resourceId)
                .build();
    }

    @Override
    protected Path buildIndexHashKey(@NonNull String parentId) {
        return Path.builder()
                .cragId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Path.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Path> getResourceTypeClass() {
        return Path.class;
    }
}

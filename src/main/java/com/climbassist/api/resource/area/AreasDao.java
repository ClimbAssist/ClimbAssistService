package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AreasDao extends ResourceDao<Area> {

    @Override
    protected Area buildResourceForDeletion(@NonNull String resourceId) {
        return Area.builder()
                .areaId(resourceId)
                .build();
    }

    @Override
    protected Area buildIndexHashKey(@NonNull String parentId) {
        return Area.builder()
                .regionId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Area.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Area> getResourceTypeClass() {
        return Area.class;
    }
}
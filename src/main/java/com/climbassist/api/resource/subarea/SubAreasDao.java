package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SubAreasDao extends ResourceWithParentDao<SubArea, Area> {

    @Override
    protected SubArea buildResourceForDeletion(@NonNull String resourceId) {
        return SubArea.builder()
                .subAreaId(resourceId)
                .build();
    }

    @Override
    protected SubArea buildIndexHashKey(@NonNull String parentId) {
        return SubArea.builder()
                .areaId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return SubArea.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<SubArea> getResourceTypeClass() {
        return SubArea.class;
    }
}

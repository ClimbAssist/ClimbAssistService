package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class RegionsDao extends ResourceDao<Region> {

    @Override
    protected Region buildResourceForDeletion(@NonNull String resourceId) {
        return Region.builder()
                .regionId(resourceId)
                .build();
    }

    @Override
    protected Region buildIndexHashKey(@NonNull String parentId) {
        return Region.builder()
                .countryId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Region.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Region> getResourceTypeClass() {
        return Region.class;
    }
}

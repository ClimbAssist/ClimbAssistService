package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class RoutesDao extends ResourceDao<Route> {

    @Override
    protected Route buildResourceForDeletion(@NonNull String resourceId) {
        return Route.builder()
                .routeId(resourceId)
                .build();
    }

    @Override
    protected Route buildIndexHashKey(@NonNull String parentId) {
        return Route.builder()
                .wallId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Route.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Route> getResourceTypeClass() {
        return Route.class;
    }
}

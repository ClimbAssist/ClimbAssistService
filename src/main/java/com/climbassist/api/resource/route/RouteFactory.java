package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithImageFactory;
import lombok.Builder;
import lombok.NonNull;

public class RouteFactory extends ResourceFactory<Route, NewRoute> implements ResourceWithImageFactory<Route> {

    @Builder
    private RouteFactory(@NonNull ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Route create(@NonNull NewRoute newRoute) {
        return Route.builder()
                .routeId(resourceIdGenerator.generateResourceId(newRoute.getName()))
                .wallId(newRoute.getWallId())
                .name(newRoute.getName())
                .description(newRoute.getDescription())
                .center(newRoute.getCenter() == null ? null : Center.builder()
                        .x(newRoute.getCenter()
                                .getX())
                        .y(newRoute.getCenter()
                                .getY())
                        .z(newRoute.getCenter()
                                .getZ())
                        .build())
                .protection(newRoute.getProtection())
                .style(newRoute.getStyle())
                .first(newRoute.getFirst())
                .next(newRoute.getNext())
                .build();
    }

    @Override
    public Route create(@NonNull Route route, @NonNull String imageLocation) {
        return route.toBuilder()
                .mainImageLocation(imageLocation)
                .build();
    }
}

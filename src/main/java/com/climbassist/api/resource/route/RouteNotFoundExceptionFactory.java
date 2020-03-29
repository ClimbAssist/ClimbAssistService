package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class RouteNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Route> {

    @Override
    public ResourceNotFoundException create(@NonNull String routeId) {
        return new RouteNotFoundException(routeId);
    }
}

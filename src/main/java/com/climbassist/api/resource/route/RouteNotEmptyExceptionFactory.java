package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class RouteNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Route> {

    @Override
    public ResourceNotEmptyException create(@NonNull String route) {
        return new RouteNotEmptyException(route);
    }
}

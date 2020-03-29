package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceNotFoundException;

public class RouteNotFoundException extends ResourceNotFoundException {

    public RouteNotFoundException(String routeId) {
        super("route", routeId);
    }
}

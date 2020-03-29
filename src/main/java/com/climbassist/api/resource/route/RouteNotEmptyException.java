package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class RouteNotEmptyException extends ResourceNotEmptyException {

    RouteNotEmptyException(@NonNull String routeId) {
        super("route", routeId);
    }
}

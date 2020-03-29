package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateRouteResultFactory implements CreateResourceResultFactory<Route> {

    @Override
    public CreateResourceResult<Route> create(@NonNull String routeId) {
        return CreateRouteResult.builder()
                .routeId(routeId)
                .build();
    }
}
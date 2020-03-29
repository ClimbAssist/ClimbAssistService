package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class PointNotFoundException extends ResourceNotFoundException {

    PointNotFoundException(String pointId) {
        super("point", pointId);
    }
}
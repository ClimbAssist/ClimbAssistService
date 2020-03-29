package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class AreaNotFoundException extends ResourceNotFoundException {

    AreaNotFoundException(String areaId) {
        super("area", areaId);
    }
}
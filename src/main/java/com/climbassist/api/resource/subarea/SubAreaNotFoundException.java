package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class SubAreaNotFoundException extends ResourceNotFoundException {

    SubAreaNotFoundException(String cragId) {
        super("sub-area", cragId);
    }
}
package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class CragNotFoundException extends ResourceNotFoundException {

    CragNotFoundException(String cragId) {
        super("crag", cragId);
    }
}

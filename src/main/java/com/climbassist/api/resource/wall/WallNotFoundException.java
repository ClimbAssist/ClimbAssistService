package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class WallNotFoundException extends ResourceNotFoundException {

    WallNotFoundException(String wallId) {
        super("wall", wallId);
    }
}

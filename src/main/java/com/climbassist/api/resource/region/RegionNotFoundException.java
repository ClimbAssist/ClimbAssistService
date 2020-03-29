package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class RegionNotFoundException extends ResourceNotFoundException {

    RegionNotFoundException(String regionId) {
        super("region", regionId);
    }
}
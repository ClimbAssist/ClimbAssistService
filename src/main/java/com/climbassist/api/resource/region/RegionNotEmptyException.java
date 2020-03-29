package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class RegionNotEmptyException extends ResourceNotEmptyException {

    RegionNotEmptyException(@NonNull String regionId) {
        super("region", regionId);
    }
}

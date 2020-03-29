package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class AreaNotEmptyException extends ResourceNotEmptyException {

    AreaNotEmptyException(@NonNull String areaId) {
        super("area", areaId);
    }
}

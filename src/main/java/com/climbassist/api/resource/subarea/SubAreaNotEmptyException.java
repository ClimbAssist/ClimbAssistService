package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class SubAreaNotEmptyException extends ResourceNotEmptyException {

    SubAreaNotEmptyException(@NonNull String subAreaId) {
        super("sub-area", subAreaId);
    }
}

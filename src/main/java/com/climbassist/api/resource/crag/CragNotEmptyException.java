package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class CragNotEmptyException extends ResourceNotEmptyException {

    CragNotEmptyException(@NonNull String cragId) {
        super("crag", cragId);
    }
}

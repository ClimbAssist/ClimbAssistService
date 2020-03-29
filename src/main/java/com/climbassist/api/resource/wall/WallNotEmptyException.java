package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class WallNotEmptyException extends ResourceNotEmptyException {

    WallNotEmptyException(@NonNull String wallId) {
        super("wall", wallId);
    }
}

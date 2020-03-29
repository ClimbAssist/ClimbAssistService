package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class PathNotEmptyException extends ResourceNotEmptyException {

    PathNotEmptyException(@NonNull String pathId) {
        super("path", pathId);
    }
}

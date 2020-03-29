package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import lombok.NonNull;

public class PathNotFoundException extends ResourceNotFoundException {

    public PathNotFoundException(@NonNull String pathId) {
        super("path", pathId);
    }
}

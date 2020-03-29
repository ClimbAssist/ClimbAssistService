package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import lombok.NonNull;

public class PathPointNotFoundException extends ResourceNotFoundException {

    public PathPointNotFoundException(@NonNull String pathPointId) {
        super("path point", pathPointId);
    }
}

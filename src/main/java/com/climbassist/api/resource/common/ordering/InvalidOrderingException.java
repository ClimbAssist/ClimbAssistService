package com.climbassist.api.resource.common.ordering;

import lombok.NonNull;

public class InvalidOrderingException extends RuntimeException {

    public InvalidOrderingException(@NonNull String parentId, @NonNull String message) {
        super(String.format("Unable to build ordered list for children of resource %s. %s", parentId, message));
    }

}

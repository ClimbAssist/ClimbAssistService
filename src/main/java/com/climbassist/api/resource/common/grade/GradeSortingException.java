package com.climbassist.api.resource.common.grade;

import lombok.NonNull;

class GradeSortingException extends IllegalStateException {

    GradeSortingException(@NonNull String message) {
        super(message);
    }
}

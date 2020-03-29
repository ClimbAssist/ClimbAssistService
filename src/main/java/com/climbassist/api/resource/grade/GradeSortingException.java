package com.climbassist.api.resource.grade;

import lombok.NonNull;

class GradeSortingException extends IllegalStateException {

    GradeSortingException(@NonNull String message) {
        super(message);
    }
}

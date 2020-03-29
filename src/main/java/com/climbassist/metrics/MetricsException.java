package com.climbassist.metrics;

import lombok.NonNull;

public class MetricsException extends RuntimeException {

    public MetricsException(@NonNull Throwable cause) {
        super(cause);
    }
}

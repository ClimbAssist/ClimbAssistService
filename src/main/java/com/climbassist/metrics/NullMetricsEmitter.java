package com.climbassist.metrics;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class NullMetricsEmitter extends MetricsEmitter {

    @Override
    public void emitErrorMetric(@NonNull String api, boolean isError) {
    }

    @Override
    public void emitFaultMetric(@NonNull String api, boolean isFault) {
    }

    @Override
    public void emitDurationMetric(@NonNull String api, double duration) {
    }
}

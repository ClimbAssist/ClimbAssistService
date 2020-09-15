package com.climbassist.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Optional;
import java.util.Set;

@SuperBuilder
public class MetricsEmitter {

    private static final String API_DIMENSION_NAME = "api";

    @NonNull
    private final String metricsNamespace;
    @NonNull
    private final AmazonCloudWatch amazonCloudWatch;

    public void emitErrorMetric(@NonNull String api, boolean isError) {
        emitBooleanMetric(api, "errorCount", isError);
    }

    public void emitFaultMetric(@NonNull String api, boolean isFault) {
        emitBooleanMetric(api, "faultCount", isFault);
    }

    public void emitDurationMetric(@NonNull String api, double duration) {
        amazonCloudWatch.putMetricData(
                buildPutMetricDataRequest(api, "duration", duration, Optional.of(StandardUnit.Milliseconds)));
    }

    private void emitBooleanMetric(String api, String metricName, boolean booleanValue) {
        amazonCloudWatch.putMetricData(
                buildPutMetricDataRequest(api, metricName, booleanValue ? 1.0 : 0.0, Optional.empty()));
    }

    private PutMetricDataRequest buildPutMetricDataRequest(String api, String metricName, double value,
                                                           @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                                   Optional<StandardUnit> maybeUnit) {
        Set<Dimension> dimensions = ImmutableSet.of(new Dimension().withName(API_DIMENSION_NAME)
                .withValue(api));
        MetricDatum metricDatum = new MetricDatum().withDimensions(dimensions)
                .withMetricName(metricName)
                .withValue(value);
        maybeUnit.ifPresent(metricDatum::setUnit);
        return new PutMetricDataRequest().withNamespace(metricsNamespace)
                .withMetricData(metricDatum);
    }
}

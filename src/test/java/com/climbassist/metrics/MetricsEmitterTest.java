package com.climbassist.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricsEmitterTest {

    private static final String METRICS_NAMESPACE = "MetricsNamespace";
    private static final String API = "GetSomething";
    private static final String EXPECTED_ERROR_METRIC_NAME = "errorCount";
    private static final String EXPECTED_FAULT_METRIC_NAME = "faultCount";
    private static final String EXPECTED_DURATION_METRIC_NAME = "duration";
    private static final Set<Dimension> EXPECTED_DIMENSIONS = ImmutableSet.of(new Dimension().withName("api")
            .withValue(API));

    @Mock
    private AmazonCloudWatch mockAmazonCloudWatch;

    private MetricsEmitter metricsEmitter;

    @BeforeEach
    void setUp() {
        metricsEmitter = MetricsEmitter.builder()
                .metricsNamespace(METRICS_NAMESPACE)
                .amazonCloudWatch(mockAmazonCloudWatch)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(metricsEmitter, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void emitErrorMetric_emitsOne_whenErrorIsTrue() {
        metricsEmitter.emitErrorMetric(API, true);
        verify(mockAmazonCloudWatch).putMetricData(
                buildPutMetricDataRequest(EXPECTED_ERROR_METRIC_NAME, 1.0, Optional.empty()));
    }

    @Test
    void emitErrorMetric_emitsZero_whenErrorIsFalse() {
        metricsEmitter.emitErrorMetric(API, false);
        verify(mockAmazonCloudWatch).putMetricData(
                buildPutMetricDataRequest(EXPECTED_ERROR_METRIC_NAME, 0.0, Optional.empty()));
    }

    @Test
    void emitFaultMetric_emitsOne_whenFaultIsTrue() {
        metricsEmitter.emitFaultMetric(API, true);
        verify(mockAmazonCloudWatch).putMetricData(
                buildPutMetricDataRequest(EXPECTED_FAULT_METRIC_NAME, 1.0, Optional.empty()));
    }

    @Test
    void emitFaultMetric_emitsZero_whenFaultIsFalse() {
        metricsEmitter.emitFaultMetric(API, false);
        verify(mockAmazonCloudWatch).putMetricData(
                buildPutMetricDataRequest(EXPECTED_FAULT_METRIC_NAME, 0.0, Optional.empty()));
    }

    @Test
    void emitDurationMetric_emitsDurationMetric() {
        double duration = 420.69;
        metricsEmitter.emitDurationMetric(API, duration);
        verify(mockAmazonCloudWatch).putMetricData(buildPutMetricDataRequest(EXPECTED_DURATION_METRIC_NAME, duration,
                Optional.of(StandardUnit.Milliseconds)));
    }

    private PutMetricDataRequest buildPutMetricDataRequest(String metricName, double value,
                                                           @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                                   Optional<StandardUnit> maybeUnit) {
        MetricDatum metricDatum = new MetricDatum().withDimensions(EXPECTED_DIMENSIONS)
                .withMetricName(metricName)
                .withValue(value);
        maybeUnit.ifPresent(metricDatum::setUnit);
        return new PutMetricDataRequest().withNamespace(METRICS_NAMESPACE)
                .withMetricData(metricDatum);
    }
}

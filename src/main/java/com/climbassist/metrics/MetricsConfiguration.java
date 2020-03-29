package com.climbassist.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@Import(CommonConfiguration.class)
@PropertySource("classpath:application.properties")
public class MetricsConfiguration {

    @Bean
    public MetricsFilter metricsFilter(@NonNull String region,
                                       @NonNull @Value("${metricsNamespace}") String metricsNamespace,
                                       @NonNull RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return MetricsFilter.builder()
                .metricsEmitter(MetricsEmitter.builder()
                        .amazonCloudWatch(AmazonCloudWatchClientBuilder.standard()
                                .withRegion(region)
                                .build())
                        .metricsNamespace(metricsNamespace)
                        .build())
                .requestMappingHandlerMapping(requestMappingHandlerMapping)
                .build();
    }
}

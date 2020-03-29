package com.climbassist.logging;

import com.climbassist.common.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class LoggingConfiguration {

    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter(@NonNull ObjectMapper objectMapper) {
        return RequestResponseLoggingFilter.builder()
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }
}

package com.climbassist.test.integration.client;

import com.climbassist.test.integration.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class ClimbAssistClientConfiguration {

    @Bean
    public ClimbAssistClient climbAssistClient(@Value("${applicationEndpoint}") @NonNull String applicationEndpoint,
                                               @NonNull ObjectMapper objectMapper) {
        //noinspection UnstableApiUsage
        return ClimbAssistClient.builder()
                .applicationEndpoint(applicationEndpoint)
                .httpClientFactory(new HttpClientFactory())
                .objectMapper(objectMapper)
                // https://docs.aws.amazon.com/cognito/latest/developerguide/limits.html
                .rateLimiter(RateLimiter.create(2))
                .build();
    }
}

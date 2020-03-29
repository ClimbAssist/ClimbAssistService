package com.climbassist.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public String region(@Value("${region}") @NonNull String region) {
        return region;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create()
                .build();
    }
}

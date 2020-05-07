package com.climbassist.common;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.common.s3.S3Proxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public String region(@Value("${region}") @NonNull String region) {
        return region;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        return objectMapper;
    }

    @Bean
    public S3Proxy s3Proxy(@NonNull String region) {
        return S3Proxy.builder()
                .amazonS3(AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .build();
    }

    @Bean
    public ResourceIdGenerator resourceIdGenerator() {
        return new ResourceIdGenerator();
    }

    @Bean
    public String imagesBucketName(@Value("${imagesBucketName}") @NonNull String imagesBucketName) {
        return imagesBucketName;
    }
}

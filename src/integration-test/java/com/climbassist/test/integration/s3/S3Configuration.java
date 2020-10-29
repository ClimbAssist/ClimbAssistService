package com.climbassist.test.integration.s3;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.climbassist.test.integration.CommonConfiguration;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class S3Configuration {

    @Bean
    public S3Proxy s3Proxy(@NonNull String region) {
        return S3Proxy.builder()
                .amazonS3(AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .build();
    }
}

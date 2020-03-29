package com.climbassist.test.integration.s3;

import com.amazonaws.services.s3.AmazonS3;
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
    public AmazonS3 amazonS3(@NonNull String region) {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }
}

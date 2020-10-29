package com.climbassist.api.resource.common.image.webpconverter;

import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.climbassist.common.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class WebpConverterConfiguration {

    @Bean
    public WebpConverter webpConverter(@NonNull final String region,
            @NonNull @Value("${webpConverterLambdaFunctionName}") final String webpConverterLambdaFunctionName,
            @NonNull final ObjectMapper objectMapper) {
        return WebpConverter.builder()
                .awsLambda(AWSLambdaClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .webpConverterLambdaFunctionName(webpConverterLambdaFunctionName)
                .objectMapper(objectMapper)
                .build();
    }
}

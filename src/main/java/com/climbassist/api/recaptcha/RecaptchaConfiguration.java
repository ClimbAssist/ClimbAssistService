package com.climbassist.api.recaptcha;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.climbassist.common.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class RecaptchaConfiguration {

    @Bean
    public RecaptchaKeysRetriever recaptchaKeysRetriever(@NonNull @Value("${stage}") String stage,
                                                         @NonNull String region, @NonNull ObjectMapper objectMapper,
                                                         @NonNull @Value("${recaptchaBackDoorResponseSecretId}")
                                                                 String recaptchaBackDoorResponseSecretId) {
        String recaptchaKeysSecretIdSuffix;
        switch (stage) {
            case "prod":
                recaptchaKeysSecretIdSuffix = "";
                break;
            case "beta":
                recaptchaKeysSecretIdSuffix = "-beta";
                break;
            default:
                recaptchaKeysSecretIdSuffix = "-development";
                break;
        }

        return RecaptchaKeysRetriever.builder()
                .awsSecretsManager(AWSSecretsManagerClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaKeysSecretId("ClimbAssistRecaptchaKeys" + recaptchaKeysSecretIdSuffix)
                .recaptchaBackDoorResponseSecretId(recaptchaBackDoorResponseSecretId)
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public RecaptchaVerifier recaptchaVerifier(@NonNull ObjectMapper objectMapper,
                                               @NonNull RecaptchaKeysRetriever recaptchaKeysRetriever) {
        return RecaptchaVerifier.builder()
                .httpClient(HttpClientBuilder.create()
                        .build())
                .objectMapper(objectMapper)
                .recaptchaKeysRetriever(recaptchaKeysRetriever)
                .build();
    }

    @Bean
    public RecaptchaController recaptchaController(@NonNull RecaptchaKeysRetriever recaptchaKeysRetriever) {
        return RecaptchaController.builder()
                .recaptchaKeysRetriever(recaptchaKeysRetriever)
                .build();
    }
}

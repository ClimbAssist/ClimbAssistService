package com.climbassist.test.integration.api.recaptcha;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.climbassist.test.integration.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class RecaptchaBackDoorResponseRetrieverConfiguration {

    @Bean
    public RecaptchaBackDoorResponseRetriever recaptchaBackDoorResponseRetriever(@NonNull String region, @NonNull
    @Value("${recaptchaBackDoorResponseSecretId}") String recaptchaBackDoorResponseSecretId) {
        return RecaptchaBackDoorResponseRetriever.builder()
                .awsSecretsManager(AWSSecretsManagerClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaBackDoorResponseSecretId(recaptchaBackDoorResponseSecretId)
                .build();
    }
}

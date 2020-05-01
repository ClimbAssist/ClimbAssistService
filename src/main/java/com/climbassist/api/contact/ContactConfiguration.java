package com.climbassist.api.contact;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.climbassist.common.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class ContactConfiguration {

    @Bean
    public ContactController contactController(@NonNull String region, @NonNull @Value("${stage}") String stage,
                                               @NonNull @Value("${climbAssistEmail}") String climbAssistEmail,
                                               @NonNull ObjectMapper objectMapper) {
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

        return ContactController.builder()
                .climbAssistEmail(climbAssistEmail)
                .amazonSimpleEmailService(AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaKeysSecretId("ClimbAssistRecaptchaKeys" + recaptchaKeysSecretIdSuffix)
                .awsSecretsManager(AWSSecretsManagerClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .objectMapper(objectMapper)
                .build();
    }
}

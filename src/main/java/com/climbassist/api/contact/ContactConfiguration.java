package com.climbassist.api.contact;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.climbassist.api.contact.recaptcha.RecaptchaKeysRetriever;
import com.climbassist.api.contact.recaptcha.RecaptchaVerifier;
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

        RecaptchaKeysRetriever recaptchaKeysRetriever = RecaptchaKeysRetriever.builder()
                .awsSecretsManager(AWSSecretsManagerClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaKeysSecretId("ClimbAssistRecaptchaKeys" + recaptchaKeysSecretIdSuffix)
                .objectMapper(objectMapper)
                .build();

        return ContactController.builder()
                .climbAssistEmail(climbAssistEmail)
                .amazonSimpleEmailService(AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaKeysRetriever(recaptchaKeysRetriever)
                .recaptchaVerifier(RecaptchaVerifier.builder()
                        .httpClient(HttpClientBuilder.create()
                                .build())
                        .objectMapper(objectMapper)
                        .recaptchaKeysRetriever(recaptchaKeysRetriever)
                        .build())
                .build();
    }
}

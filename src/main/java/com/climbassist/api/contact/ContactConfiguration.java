package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.climbassist.common.CommonConfiguration;
import com.climbassist.common.recaptcha.RecaptchaConfiguration;
import com.climbassist.common.recaptcha.RecaptchaKeysRetriever;
import com.climbassist.common.recaptcha.RecaptchaVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, RecaptchaConfiguration.class})
public class ContactConfiguration {

    @Bean
    public ContactController contactController(@NonNull String region, @NonNull @Value("${stage}") String stage,
                                               @NonNull @Value("${climbAssistEmail}") String climbAssistEmail,
                                               @NonNull ObjectMapper objectMapper,
                                               @NonNull RecaptchaKeysRetriever recaptchaKeysRetriever,
                                               @NonNull RecaptchaVerifier recaptchaVerifier) {
        return ContactController.builder()
                .climbAssistEmail(climbAssistEmail)
                .amazonSimpleEmailService(AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaKeysRetriever(recaptchaKeysRetriever)
                .recaptchaVerifier(recaptchaVerifier)
                .build();
    }
}

package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.climbassist.api.recaptcha.RecaptchaConfiguration;
import com.climbassist.api.recaptcha.RecaptchaVerifier;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, RecaptchaConfiguration.class})
public class ContactConfiguration {

    @Bean
    public ContactController contactController(@NonNull String region,
                                               @NonNull @Value("${climbAssistEmail}") String climbAssistEmail,
                                               @NonNull RecaptchaVerifier recaptchaVerifier) {
        return ContactController.builder()
                .climbAssistEmail(climbAssistEmail)
                .amazonSimpleEmailService(AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .recaptchaVerifier(recaptchaVerifier)
                .build();
    }
}

package com.climbassist.test.integration.api.user;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.climbassist.test.integration.CommonConfiguration;
import com.climbassist.test.integration.api.recaptcha.RecaptchaBackDoorResponseRetriever;
import com.climbassist.test.integration.api.recaptcha.RecaptchaBackDoorResponseRetrieverConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import lombok.NonNull;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@Import({CommonConfiguration.class, RecaptchaBackDoorResponseRetrieverConfiguration.class})
public class TestUserManagerConfiguration {

    @Bean
    @Scope("prototype")
    public TestUserManager testUserManager(@NonNull String region, @NonNull ClimbAssistClient climbAssistClient,
                                           @Value("${userPoolId}") @NonNull String userPoolId,
                                           @NonNull HttpClient httpClient,
                                           @NonNull RecaptchaBackDoorResponseRetriever recaptchaBackDoorResponseRetriever) {
        return TestUserManager.builder()
                .amazonSimpleEmailService(AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .amazonSNS(AmazonSNSClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .amazonSQS(AmazonSQSClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .awsCognitoIdentityProvider(AWSCognitoIdentityProviderClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .climbAssistClient(climbAssistClient)
                .userPoolId(userPoolId)
                .httpClient(httpClient)
                .recaptchaBackDoorResponseRetriever(recaptchaBackDoorResponseRetriever)
                .build();
    }
}

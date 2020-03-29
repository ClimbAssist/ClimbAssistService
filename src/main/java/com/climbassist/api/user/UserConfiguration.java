package com.climbassist.api.user;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.climbassist.api.user.authentication.UserAuthenticationController;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfiguration.class)
public class UserConfiguration {

    @Bean
    public UserManager userManager(@NonNull String region, @Value("${userPoolId}") @NonNull String userPoolId,
                                   @Value("${userPoolClientId}") @NonNull String userPoolClientId) {
        return UserManager.builder()
                .awsCognitoIdentityProvider(AWSCognitoIdentityProviderClientBuilder.standard()
                        .withRegion(region)
                        .build())
                .userPoolId(userPoolId)
                .userPoolClientId(userPoolClientId)
                .build();
    }

    @Bean
    public UserAuthenticationController userAuthenticationController(@NonNull UserManager userManager) {
        return UserAuthenticationController.builder()
                .userManager(userManager)
                .build();
    }

    @Bean
    public UserController userController(@NonNull UserManager userManager) {
        return UserController.builder()
                .userManager(userManager)
                .build();
    }
}

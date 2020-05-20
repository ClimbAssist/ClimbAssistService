package com.climbassist.api.user;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.user.authentication.DeletedUsersDao;
import com.climbassist.api.user.authentication.UserAuthenticationController;
import com.climbassist.api.user.authorization.UserDataDecorationFilter;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.ZonedDateTime;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class})
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
    public UserAuthenticationController userAuthenticationController(@NonNull UserManager userManager,
                                                                     @NonNull DeletedUsersDao deletedUsersDao,
                                                                     @Value("${userDataRetentionTimeMinutes}")
                                                                             long userDataRetentionTimeMinutes) {
        return UserAuthenticationController.builder()
                .userManager(userManager)
                .deletedUsersDao(deletedUsersDao)
                .userDataRetentionTimeMinutes(userDataRetentionTimeMinutes)
                .currentZonedDateTimeSupplier(ZonedDateTime::now)
                .build();
    }

    @Bean
    public UserController userController(@NonNull UserManager userManager) {
        return UserController.builder()
                .userManager(userManager)
                .build();
    }

    @Bean
    public UserDataDecorationFilter userDataDecorationFilter(@NonNull UserManager userManager) {
        return UserDataDecorationFilter.builder()
                .userManager(userManager)
                .build();
    }
}

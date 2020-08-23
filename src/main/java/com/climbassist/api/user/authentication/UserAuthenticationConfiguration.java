package com.climbassist.api.user.authentication;

import com.climbassist.api.user.UserConfiguration;
import com.climbassist.api.user.UserManager;
import com.climbassist.common.recaptcha.RecaptchaConfiguration;
import com.climbassist.common.recaptcha.RecaptchaVerifier;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.ZonedDateTime;

@Configuration
@Import({UserConfiguration.class, RecaptchaConfiguration.class})
public class UserAuthenticationConfiguration {

    @Bean
    public UserAuthenticationController userAuthenticationController(@NonNull UserManager userManager,
                                                                     @NonNull DeletedUsersDao deletedUsersDao,
                                                                     @Value("${userDataRetentionTimeMinutes}")
                                                                             long userDataRetentionTimeMinutes,
                                                                     @NonNull RecaptchaVerifier recaptchaVerifier) {
        return UserAuthenticationController.builder()
                .userManager(userManager)
                .deletedUsersDao(deletedUsersDao)
                .userDataRetentionTimeMinutes(userDataRetentionTimeMinutes)
                .currentZonedDateTimeSupplier(ZonedDateTime::now)
                .recaptchaVerifier(recaptchaVerifier)
                .build();
    }
}

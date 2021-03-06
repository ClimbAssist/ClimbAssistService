package com.climbassist.api;

import com.climbassist.api.user.UserConfiguration;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.AuthenticatedAuthorizationHandler;
import com.climbassist.api.user.authorization.AuthorizationHandlerFactory;
import com.climbassist.api.user.authorization.AuthorizationInterceptor;
import com.climbassist.common.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
@Import({CommonConfiguration.class, UserConfiguration.class})
public class ApiConfiguration {

    static final String API_PATH = "/v1/**";

    @Bean
    public ApiResponseFilter apiResponseFilter(@NonNull ObjectMapper objectMapper) {
        return ApiResponseFilter.builder()
                .objectMapper(objectMapper)
                .apiResponseFactory(ApiResponseFactory.builder()
                        .objectMapper(objectMapper)
                        .build())
                .build();
    }

    @Bean
    public ApiExceptionHandler apiExceptionHandler(@NonNull ObjectMapper objectMapper) {
        return ApiExceptionHandler.builder()
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public MappedInterceptor mappedAuthorizationInterceptor(@NonNull UserManager userManager) {
        AuthorizationHandlerFactory authorizationHandlerFactory = new AuthorizationHandlerFactory(
                AuthenticatedAuthorizationHandler.builder()
                        .userManager(userManager)
                        .build(), AdministratorAuthorizationHandler.builder()
                .userManager(userManager)
                .build());
        return new MappedInterceptor(new String[]{API_PATH}, AuthorizationInterceptor.builder()
                .authorizationHandlerFactory(authorizationHandlerFactory)
                .build());
    }

    // allows validation on path variables and request parameters
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}

package com.climbassist.api;

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
@Import(CommonConfiguration.class)
public class ApiConfiguration {

    public static final String V1_VERSION = "v1";
    public static final String V2_VERSION = "v2";

    static final String V1_API_PATH = "/" + V1_VERSION + "/**";
    static final String V2_API_PATH = "/" + V2_VERSION + "/**";

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
    public MappedInterceptor mappedAuthorizationInterceptor() {
        AuthorizationHandlerFactory authorizationHandlerFactory = new AuthorizationHandlerFactory(
                AuthenticatedAuthorizationHandler.builder()
                        .build(), AdministratorAuthorizationHandler.builder()
                .build());
        return new MappedInterceptor(new String[]{V1_API_PATH, V2_API_PATH}, AuthorizationInterceptor.builder()
                .authorizationHandlerFactory(authorizationHandlerFactory)
                .build());
    }

    // allows validation on path variables and request parameters
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}

package com.climbassist.test.integration.api.resource;

import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@Import(ClimbAssistClientConfiguration.class)
public class ResourceManagerConfiguration {

    @Bean
    @Scope("prototype")
    public ResourceManager resourceManager(@NonNull ClimbAssistClient climbAssistClient) {
        return ResourceManager.builder()
                .climbAssistClient(climbAssistClient)
                .build();
    }
}

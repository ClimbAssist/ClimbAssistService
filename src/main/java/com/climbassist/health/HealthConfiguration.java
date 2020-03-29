package com.climbassist.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfiguration {

    @Bean
    public HealthController healthController() {
        return new HealthController();
    }
}

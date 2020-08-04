package com.climbassist;

import com.climbassist.api.ApiConfiguration;
import com.climbassist.api.contact.ContactConfiguration;
import com.climbassist.api.resource.area.AreaConfiguration;
import com.climbassist.api.resource.country.CountryConfiguration;
import com.climbassist.api.resource.crag.CragConfiguration;
import com.climbassist.api.resource.path.PathConfiguration;
import com.climbassist.api.resource.pathpoint.PathPointConfiguration;
import com.climbassist.api.resource.pitch.PitchConfiguration;
import com.climbassist.api.resource.point.PointConfiguration;
import com.climbassist.api.resource.region.RegionConfiguration;
import com.climbassist.api.resource.route.RouteConfiguration;
import com.climbassist.api.resource.subarea.SubAreaConfiguration;
import com.climbassist.api.resource.wall.WallConfiguration;
import com.climbassist.api.user.UserConfiguration;
import com.climbassist.api.user.authentication.UserAuthenticationConfiguration;
import com.climbassist.health.HealthConfiguration;
import com.climbassist.logging.LoggingConfiguration;
import com.climbassist.main.MainConfiguration;
import com.climbassist.metrics.MetricsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@EnableWebMvc
@Configuration
@Import({ApiConfiguration.class, AreaConfiguration.class, ContactConfiguration.class, CountryConfiguration.class,
        CragConfiguration.class, HealthConfiguration.class, LoggingConfiguration.class, MainConfiguration.class,
        MetricsConfiguration.class, PathConfiguration.class, PathPointConfiguration.class, PitchConfiguration.class,
        PointConfiguration.class, RegionConfiguration.class, RouteConfiguration.class, SubAreaConfiguration.class,
        UserAuthenticationConfiguration.class, UserConfiguration.class, WallConfiguration.class})
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
        resourceHandlerRegistry.addResourceHandler("/static/**")
                .addResourceLocations("/dist/static/");
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getMultipartResolver() {
        return new CommonsMultipartResolver();
    }

    // This will allow URLs containing "." to be passed without being truncated.
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
        requestMappingHandlerMapping.setAlwaysUseFullPath(true);
        requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
        return requestMappingHandlerMapping;
    }
}

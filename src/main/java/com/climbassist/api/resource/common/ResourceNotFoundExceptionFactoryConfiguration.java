package com.climbassist.api.resource.common;

import com.climbassist.api.resource.area.AreaNotFoundExceptionFactory;
import com.climbassist.api.resource.country.CountryNotFoundExceptionFactory;
import com.climbassist.api.resource.crag.CragNotFoundExceptionFactory;
import com.climbassist.api.resource.path.PathNotFoundExceptionFactory;
import com.climbassist.api.resource.pathpoint.PathPointNotFoundExceptionFactory;
import com.climbassist.api.resource.pitch.PitchNotFoundExceptionFactory;
import com.climbassist.api.resource.point.PointNotFoundExceptionFactory;
import com.climbassist.api.resource.region.RegionNotFoundExceptionFactory;
import com.climbassist.api.resource.route.RouteNotFoundExceptionFactory;
import com.climbassist.api.resource.subarea.SubAreaNotFoundExceptionFactory;
import com.climbassist.api.resource.wall.WallNotFoundExceptionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceNotFoundExceptionFactoryConfiguration {

    @Bean
    public CountryNotFoundExceptionFactory countryNotFoundExceptionFactory() {
        return new CountryNotFoundExceptionFactory();
    }

    @Bean
    public RegionNotFoundExceptionFactory regionNotFoundExceptionFactory() {
        return new RegionNotFoundExceptionFactory();
    }

    @Bean
    public AreaNotFoundExceptionFactory areaNotFoundExceptionFactory() {
        return new AreaNotFoundExceptionFactory();
    }

    @Bean
    public SubAreaNotFoundExceptionFactory subAreaNotFoundExceptionFactory() {
        return new SubAreaNotFoundExceptionFactory();
    }

    @Bean
    public CragNotFoundExceptionFactory cragNotFoundExceptionFactory() {
        return new CragNotFoundExceptionFactory();
    }

    @Bean
    public WallNotFoundExceptionFactory wallNotFoundExceptionFactory() {
        return new WallNotFoundExceptionFactory();
    }

    @Bean
    public RouteNotFoundExceptionFactory routeNotFoundExceptionFactory() {
        return new RouteNotFoundExceptionFactory();
    }

    @Bean
    public PitchNotFoundExceptionFactory pitchNotFoundExceptionFactory() {
        return new PitchNotFoundExceptionFactory();
    }

    @Bean
    public PointNotFoundExceptionFactory pointNotFoundExceptionFactory() {
        return new PointNotFoundExceptionFactory();
    }

    @Bean
    public PathNotFoundExceptionFactory pathNotFoundExceptionFactory() {
        return new PathNotFoundExceptionFactory();
    }

    @Bean
    public PathPointNotFoundExceptionFactory pathPointNotFoundExceptionFactory() {
        return new PathPointNotFoundExceptionFactory();
    }
}

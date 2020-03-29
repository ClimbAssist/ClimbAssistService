package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.wall.Wall;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderableListBuilderConfiguration {

    @Bean
    public OrderableListBuilder<Wall, Crag> wallOrderableListBuilder() {
        return new OrderableListBuilder<>();
    }

    @Bean
    public OrderableListBuilder<Route, Wall> routeOrderableListBuilder() {
        return new OrderableListBuilder<>();
    }

    @Bean
    public OrderableListBuilder<Pitch, Route> pitchOrderableListBuilder() {
        return new OrderableListBuilder<>();
    }

    @Bean
    public OrderableListBuilder<Point, Pitch> pointOrderableListBuilder() {
        return new OrderableListBuilder<>();
    }

    @Bean
    public OrderableListBuilder<PathPoint, Path> pathPointOrderableListBuilder() {
        return new OrderableListBuilder<>();
    }
}

package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RouteNotFoundExceptionFactory;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class PitchConfiguration {

    @Bean
    public PitchController pitchController(@NonNull PitchesDao pitchesDao, @NonNull RoutesDao routesDao,
                                           @NonNull PointsDao pointsDao,
                                           @NonNull ResourceIdGenerator resourceIdGenerator,
                                           @NonNull PitchNotFoundExceptionFactory pitchNotFoundExceptionFactory,
                                           @NonNull RouteNotFoundExceptionFactory routeNotFoundExceptionFactory,
                                           @NonNull RecursiveResourceRetriever<Point, Pitch> recursiveResourceRetriever) {
        PitchFactory pitchFactory = PitchFactory.builder()
                .resourceIdGenerator(resourceIdGenerator)
                .build();
        CreatePitchResultFactory createPitchResultFactory = new CreatePitchResultFactory();
        ResourceControllerDelegate<Pitch, NewPitch> resourceControllerDelegate =
                ResourceControllerDelegate.<Pitch, NewPitch>builder().resourceDao(pitchesDao)
                        .resourceFactory(pitchFactory)
                        .resourceNotFoundExceptionFactory(pitchNotFoundExceptionFactory)
                        .createResourceResultFactory(createPitchResultFactory)
                        .build();
        ResourceWithParentControllerDelegate<Pitch, NewPitch, Route> resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<Pitch, NewPitch, Route>builder().resourceDao(pitchesDao)
                        .parentResourceDao(routesDao)
                        .parentResourceNotFoundExceptionFactory(routeNotFoundExceptionFactory)
                        .resourceControllerDelegate(resourceControllerDelegate)
                        .build();
        PitchNotEmptyExceptionFactory pitchNotEmptyExceptionFactory = new PitchNotEmptyExceptionFactory();
        return PitchController.builder()
                .orderableResourceWithParentControllerDelegate(
                        OrderableResourceWithParentControllerDelegate.<Pitch, NewPitch, Route>builder().resourceWithParentControllerDelegate(
                                resourceWithParentControllerDelegate)
                                .orderableListBuilder(new OrderableListBuilder<>())
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Pitch, NewPitch>builder().childResourceDaos(
                                ImmutableSet.of(pointsDao))
                                .resourceNotEmptyExceptionFactory(pitchNotEmptyExceptionFactory)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .routesDao(routesDao)
                .pitchesDao(pitchesDao)
                .pointsDao(pointsDao)
                .pitchFactory(pitchFactory)
                .pitchNotFoundExceptionFactory(pitchNotFoundExceptionFactory)
                .routeNotFoundExceptionFactory(routeNotFoundExceptionFactory)
                .createPitchResultFactory(createPitchResultFactory)
                .pitchNotEmptyExceptionFactory(pitchNotEmptyExceptionFactory)
                .build();
    }
}

package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.*;
import com.climbassist.api.resource.common.image.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverter;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverterConfiguration;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.resource.wall.WallNotFoundExceptionFactory;
import com.climbassist.api.resource.wall.WallsDao;
import com.climbassist.common.CommonConfiguration;
import com.climbassist.common.s3.S3Proxy;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class,
        WebpConverterConfiguration.class})
public class RouteConfiguration {

    @Bean
    public RouteController routeController(@NonNull RoutesDao routesDao, @NonNull WallsDao wallsDao,
            @NonNull PitchesDao pitchesDao, @NonNull ResourceIdGenerator resourceIdGenerator,
            @NonNull RouteNotFoundExceptionFactory routeNotFoundExceptionFactory,
            @NonNull WallNotFoundExceptionFactory wallNotFoundExceptionFactory, @NonNull S3Proxy s3Proxy,
            @NonNull String imagesBucketName,
            @NonNull RecursiveResourceRetriever<Pitch, Route> recursiveResourceRetriever,
            @NonNull WebpConverter webpConverter) {
        RouteFactory routeFactory = RouteFactory.builder()
                .resourceIdGenerator(resourceIdGenerator)
                .build();
        ResourceControllerDelegate<Route, NewRoute> resourceControllerDelegate =
                ResourceControllerDelegate.<Route, NewRoute>builder().resourceDao(routesDao)
                        .resourceFactory(routeFactory)
                        .resourceNotFoundExceptionFactory(routeNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateRouteResultFactory())
                        .build();
        ResourceWithParentControllerDelegate<Route, NewRoute, Wall> resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<Route, NewRoute, Wall>builder().resourceDao(routesDao)
                        .parentResourceDao(wallsDao)
                        .parentResourceNotFoundExceptionFactory(wallNotFoundExceptionFactory)
                        .resourceControllerDelegate(resourceControllerDelegate)
                        .build();
        RouteNotEmptyExceptionFactory routeNotEmptyExceptionFactory = new RouteNotEmptyExceptionFactory();
        return RouteController.builder()
                .resourceWithParentControllerDelegate(resourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(
                        OrderableResourceWithParentControllerDelegate.<Route, NewRoute, Wall>builder().resourceWithParentControllerDelegate(
                                resourceWithParentControllerDelegate)
                                .orderableListBuilder(new OrderableListBuilder<>())
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Route, NewRoute>builder().childResourceDaos(
                                ImmutableSet.of(pitchesDao))
                                .resourceNotEmptyExceptionFactory(routeNotEmptyExceptionFactory)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithImageControllerDelegate(
                        ResourceWithImageControllerDelegate.<Route>builder().resourceDao(routesDao)
                                .resourceNotFoundExceptionFactory(routeNotFoundExceptionFactory)
                                .s3Proxy(s3Proxy)
                                .imagesBucketName(imagesBucketName)
                                .resourceFactory(routeFactory)
                                .webpConverter(webpConverter)
                                .build())
                .routesDao(routesDao)
                .routeNotFoundExceptionFactory(routeNotFoundExceptionFactory)
                .pitchesDao(pitchesDao)
                .routeNotEmptyExceptionFactory(routeNotEmptyExceptionFactory)
                .s3Proxy(s3Proxy)
                .build();
    }
}

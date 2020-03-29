package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CragNotFoundExceptionFactory;
import com.climbassist.api.resource.crag.CragsDao;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class WallConfiguration {

    @Bean
    public WallController wallController(@NonNull WallsDao wallsDao, @NonNull CragsDao cragsDao,
                                         @NonNull RoutesDao routesDao, @NonNull ResourceIdGenerator resourceIdGenerator,
                                         @NonNull WallNotFoundExceptionFactory wallNotFoundExceptionFactory,
                                         @NonNull CragNotFoundExceptionFactory cragNotFoundExceptionFactory,
                                         @NonNull RecursiveResourceRetriever<Route, Wall> recursiveResourceRetriever) {
        ResourceControllerDelegate<Wall, NewWall> resourceControllerDelegate =
                ResourceControllerDelegate.<Wall, NewWall>builder().resourceDao(wallsDao)
                        .resourceFactory(WallFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(wallNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateWallResultFactory())
                        .build();
        ResourceWithParentControllerDelegate<Wall, NewWall, Crag> resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<Wall, NewWall, Crag>builder().resourceDao(wallsDao)
                        .parentResourceDao(cragsDao)
                        .parentResourceNotFoundExceptionFactory(cragNotFoundExceptionFactory)
                        .resourceControllerDelegate(resourceControllerDelegate)
                        .build();
        return WallController.builder()
                .resourceWithParentControllerDelegate(resourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(
                        OrderableResourceWithParentControllerDelegate.<Wall, NewWall, Crag>builder().resourceWithParentControllerDelegate(
                                resourceWithParentControllerDelegate)
                                .orderableListBuilder(new OrderableListBuilder<>())
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Wall, NewWall>builder().childResourceDaos(
                                ImmutableSet.of(routesDao))
                                .resourceNotEmptyExceptionFactory(new WallNotEmptyExceptionFactory())
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .build();
    }
}

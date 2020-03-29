package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CragNotFoundExceptionFactory;
import com.climbassist.api.resource.crag.CragsDao;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.climbassist.api.resource.pathpoint.PathPointsDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class PathConfiguration {

    @Bean
    public PathController pathController(@NonNull PathsDao pathsDao, @NonNull CragsDao cragsDao,
                                         @NonNull PathPointsDao pathPointsDao,
                                         @NonNull RecursiveResourceRetriever<PathPoint, Path> recursiveResourceRetriever,
                                         @NonNull ResourceIdGenerator resourceIdGenerator,
                                         @NonNull PathNotFoundExceptionFactory pathNotFoundExceptionFactory,
                                         @NonNull CragNotFoundExceptionFactory cragNotFoundExceptionFactory) {
        ResourceControllerDelegate<Path, NewPath> resourceControllerDelegate =
                ResourceControllerDelegate.<Path, NewPath>builder().resourceDao(pathsDao)
                        .createResourceResultFactory(new CreatePathResultFactory())
                        .resourceFactory(PathFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(pathNotFoundExceptionFactory)
                        .build();

        return PathController.builder()
                .resourceControllerDelegate(resourceControllerDelegate)
                .resourceWithParentControllerDelegate(
                        ResourceWithParentControllerDelegate.<Path, NewPath, Crag>builder().resourceDao(pathsDao)
                                .parentResourceDao(cragsDao)
                                .parentResourceNotFoundExceptionFactory(cragNotFoundExceptionFactory)
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Path, NewPath>builder().resourceControllerDelegate(
                                resourceControllerDelegate)
                                .childResourceDaos(ImmutableSet.of(pathPointsDao))
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .resourceNotEmptyExceptionFactory(new PathNotEmptyExceptionFactory())
                                .build())
                .build();
    }
}

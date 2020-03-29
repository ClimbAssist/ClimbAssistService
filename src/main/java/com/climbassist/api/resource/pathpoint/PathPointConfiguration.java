package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactoryConfiguration;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.batch.BatchResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.PathNotFoundExceptionFactory;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class,
        ResourceNotFoundExceptionFactoryConfiguration.class})
public class PathPointConfiguration {

    @Bean
    public PathPointController pathPointController(@NonNull PathPointsDao pathPointsDao, @NonNull PathsDao pathsDao,
                                                   @NonNull ResourceIdGenerator resourceIdGenerator,
                                                   @NonNull PathPointNotFoundExceptionFactory pathPointNotFoundExceptionFactory,
                                                   @NonNull PathNotFoundExceptionFactory pathNotFoundExceptionFactory,
                                                   @NonNull OrderableListBuilder<PathPoint, Path> orderableListBuilder) {
        PathPointFactory pathPointFactory = PathPointFactory.builder()
                .resourceIdGenerator(resourceIdGenerator)
                .build();
        CreatePathPointResultFactory createPathPointResultFactory = new CreatePathPointResultFactory();
        ResourceControllerDelegate<PathPoint, NewPathPoint> resourceControllerDelegate =
                ResourceControllerDelegate.<PathPoint, NewPathPoint>builder().resourceDao(pathPointsDao)
                        .createResourceResultFactory(createPathPointResultFactory)
                        .resourceFactory(pathPointFactory)
                        .resourceNotFoundExceptionFactory(pathPointNotFoundExceptionFactory)
                        .build();
        ResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path> resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<PathPoint, NewPathPoint, Path>builder().resourceDao(pathPointsDao)
                        .parentResourceDao(pathsDao)
                        .parentResourceNotFoundExceptionFactory(pathNotFoundExceptionFactory)
                        .resourceControllerDelegate(resourceControllerDelegate)
                        .build();

        return PathPointController.builder()
                .resourceControllerDelegate(resourceControllerDelegate)
                .resourceWithParentControllerDelegate(resourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(
                        OrderableResourceWithParentControllerDelegate.<PathPoint, NewPathPoint, Path>builder().resourceWithParentControllerDelegate(
                                resourceWithParentControllerDelegate)
                                .orderableListBuilder(orderableListBuilder)
                                .build())
                .batchResourceWithParentControllerDelegate(
                        BatchResourceWithParentControllerDelegate.<PathPoint, NewPathPoint, Path, BatchNewPathPoint>builder().resourceControllerDelegate(
                                resourceControllerDelegate)
                                .resourceDao(pathPointsDao)
                                .parentResourceDao(pathsDao)
                                .resourceNotFoundExceptionFactory(pathPointNotFoundExceptionFactory)
                                .parentResourceNotFoundExceptionFactory(pathNotFoundExceptionFactory)
                                .batchResourceFactory(pathPointFactory)
                                .batchCreateResourceResultFactory(createPathPointResultFactory)
                                .build())
                .build();
    }
}

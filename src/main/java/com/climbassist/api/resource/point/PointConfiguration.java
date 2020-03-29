package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.batch.BatchResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.PitchNotFoundExceptionFactory;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class})
public class PointConfiguration {

    @Bean
    public PointController pointController(@NonNull PointsDao pointsDao, @NonNull PitchesDao pitchesDao,
                                           @NonNull ResourceIdGenerator resourceIdGenerator,
                                           @NonNull PointNotFoundExceptionFactory pointNotFoundExceptionFactory,
                                           @NonNull PitchNotFoundExceptionFactory pitchNotFoundExceptionFactory) {
        PointFactory pointFactory = PointFactory.builder()
                .resourceIdGenerator(resourceIdGenerator)
                .build();
        CreatePointResultFactory createPointResultFactory = new CreatePointResultFactory();
        ResourceControllerDelegate<Point, NewPoint> resourceControllerDelegate =
                ResourceControllerDelegate.<Point, NewPoint>builder().resourceDao(pointsDao)
                        .resourceFactory(pointFactory)
                        .resourceNotFoundExceptionFactory(pointNotFoundExceptionFactory)
                        .createResourceResultFactory(createPointResultFactory)
                        .build();
        ResourceWithParentControllerDelegate<Point, NewPoint, Pitch> resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<Point, NewPoint, Pitch>builder().resourceDao(pointsDao)
                        .parentResourceDao(pitchesDao)
                        .parentResourceNotFoundExceptionFactory(pitchNotFoundExceptionFactory)
                        .resourceControllerDelegate(resourceControllerDelegate)
                        .build();
        return PointController.builder()
                .resourceControllerDelegate(resourceControllerDelegate)
                .resourceWithParentControllerDelegate(resourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(
                        OrderableResourceWithParentControllerDelegate.<Point, NewPoint, Pitch>builder().resourceWithParentControllerDelegate(
                                resourceWithParentControllerDelegate)
                                .orderableListBuilder(new OrderableListBuilder<>())
                                .build())
                .batchResourceWithParentControllerDelegate(
                        BatchResourceWithParentControllerDelegate.<Point, NewPoint, Pitch, BatchNewPoint>builder().resourceControllerDelegate(
                                resourceControllerDelegate)
                                .resourceDao(pointsDao)
                                .parentResourceDao(pitchesDao)
                                .resourceNotFoundExceptionFactory(pointNotFoundExceptionFactory)
                                .parentResourceNotFoundExceptionFactory(pitchNotFoundExceptionFactory)
                                .batchResourceFactory(pointFactory)
                                .batchCreateResourceResultFactory(createPointResultFactory)
                                .build())
                .build();
    }
}

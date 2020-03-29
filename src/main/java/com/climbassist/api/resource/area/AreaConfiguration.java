package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactoryConfiguration;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.RegionNotFoundExceptionFactory;
import com.climbassist.api.resource.region.RegionsDao;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.SubAreasDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, ResourceNotFoundExceptionFactoryConfiguration.class})
public class AreaConfiguration {

    @Bean
    public AreaController areaController(@NonNull AreasDao areasDao, @NonNull RegionsDao regionsDao,
                                         @NonNull SubAreasDao subAreasDao,
                                         @NonNull ResourceIdGenerator resourceIdGenerator,
                                         @NonNull AreaNotFoundExceptionFactory areaNotFoundExceptionFactory,
                                         @NonNull RegionNotFoundExceptionFactory regionNotFoundExceptionFactory,
                                         @NonNull RecursiveResourceRetriever<SubArea, Area> recursiveResourceRetriever) {
        ResourceControllerDelegate<Area, NewArea> resourceControllerDelegate =
                ResourceControllerDelegate.<Area, NewArea>builder().resourceDao(areasDao)
                        .resourceFactory(AreaFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(areaNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateAreaResultFactory())
                        .build();
        return AreaController.builder()
                .resourceWithParentControllerDelegate(
                        ResourceWithParentControllerDelegate.<Area, NewArea, Region>builder().resourceDao(areasDao)
                                .parentResourceDao(regionsDao)
                                .parentResourceNotFoundExceptionFactory(regionNotFoundExceptionFactory)
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Area, NewArea>builder().childResourceDaos(
                                ImmutableSet.of(subAreasDao))
                                .resourceNotEmptyExceptionFactory(new AreaNotEmptyExceptionFactory())
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .build())
                .build();
    }
}

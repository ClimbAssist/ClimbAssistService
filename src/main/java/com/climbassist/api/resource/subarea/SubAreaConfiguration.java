package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.AreaNotFoundExceptionFactory;
import com.climbassist.api.resource.area.AreasDao;
import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CragsDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class SubAreaConfiguration {

    @Bean
    public SubAreaController subAreaController(@NonNull SubAreasDao subAreasDao, @NonNull AreasDao areasDao,
                                               @NonNull CragsDao cragsDao,
                                               @NonNull ResourceIdGenerator resourceIdGenerator,
                                               @NonNull SubAreaNotFoundExceptionFactory subAreaNotFoundExceptionFactory,
                                               @NonNull AreaNotFoundExceptionFactory areaNotFoundExceptionFactory,
                                               @NonNull RecursiveResourceRetriever<Crag, SubArea> recursiveResourceRetriever) {
        ResourceControllerDelegate<SubArea, NewSubArea> resourceControllerDelegate =
                ResourceControllerDelegate.<SubArea, NewSubArea>builder().resourceDao(subAreasDao)
                        .resourceFactory(SubAreaFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(subAreaNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateSubAreaResultFactory())
                        .build();
        return SubAreaController.builder()
                .resourceWithParentControllerDelegate(
                        ResourceWithParentControllerDelegate.<SubArea, NewSubArea, Area>builder().resourceDao(
                                subAreasDao)
                                .parentResourceDao(areasDao)
                                .parentResourceNotFoundExceptionFactory(areaNotFoundExceptionFactory)
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<SubArea, NewSubArea>builder().childResourceDaos(
                                ImmutableSet.of(cragsDao))
                                .resourceNotEmptyExceptionFactory(new SubAreaNotEmptyExceptionFactory())
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .build())
                .build();
    }
}

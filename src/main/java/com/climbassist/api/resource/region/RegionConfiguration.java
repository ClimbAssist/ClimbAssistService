package com.climbassist.api.resource.region;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.AreasDao;
import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.country.CountriesDao;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.CountryNotFoundExceptionFactory;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class RegionConfiguration {

    @Bean
    public RegionController regionController(@NonNull RegionsDao regionsDao, @NonNull CountriesDao countriesDao,
                                             @NonNull AreasDao areasDao,
                                             @NonNull ResourceIdGenerator resourceIdGenerator,
                                             @NonNull RegionNotFoundExceptionFactory regionNotFoundExceptionFactory,
                                             @NonNull CountryNotFoundExceptionFactory countryNotFoundExceptionFactory,
                                             @NonNull RecursiveResourceRetriever<Area, Region> recursiveResourceRetriever) {
        ResourceControllerDelegate<Region, NewRegion> resourceControllerDelegate =
                ResourceControllerDelegate.<Region, NewRegion>builder().resourceDao(regionsDao)
                        .resourceFactory(RegionFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(regionNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateRegionResultFactory())
                        .build();
        return RegionController.builder()
                .resourceWithParentControllerDelegate(
                        ResourceWithParentControllerDelegate.<Region, NewRegion, Country>builder().resourceDao(
                                regionsDao)
                                .parentResourceDao(countriesDao)
                                .parentResourceNotFoundExceptionFactory(countryNotFoundExceptionFactory)
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Region, NewRegion>builder().childResourceDaos(
                                ImmutableSet.of(areasDao))
                                .resourceNotEmptyExceptionFactory(new RegionNotEmptyExceptionFactory())
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .build())
                .build();
    }
}

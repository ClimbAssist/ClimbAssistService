package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.RegionsDao;
import com.climbassist.api.v2.ResourcesDao;
import com.climbassist.common.CommonConfiguration;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class CountryConfiguration {

    @Bean
    public CountryController countryController(@NonNull CountriesDao countriesDao, @NonNull RegionsDao regionsDao,
            @NonNull ResourceIdGenerator resourceIdGenerator,
            @NonNull CountryNotFoundExceptionFactory countryNotFoundExceptionFactory,
            @NonNull RecursiveResourceRetriever<Region, Country> recursiveResourceRetriever) {
        ResourceControllerDelegate<Country, NewCountry> resourceControllerDelegate =
                ResourceControllerDelegate.<Country, NewCountry>builder().resourceDao(countriesDao)
                        .resourceFactory(CountryFactory.builder()
                                .resourceIdGenerator(resourceIdGenerator)
                                .build())
                        .resourceNotFoundExceptionFactory(countryNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateCountryResultFactory())
                        .build();
        return CountryController.builder()
                .resourceControllerDelegate(resourceControllerDelegate)
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Country, NewCountry>builder().childResourceDaos(
                                ImmutableSet.of(regionsDao))
                                .resourceNotEmptyExceptionFactory(new CountryNotEmptyExceptionFactory())
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .recursiveResourceRetrievers(ImmutableSet.of(recursiveResourceRetriever))
                                .build())
                .countriesDao(countriesDao)
                .build();
    }

    @Bean
    public com.climbassist.api.v2.CountryController countryControllerV2(@NonNull final ResourcesDao resourcesDao,
            @NonNull final ResourceIdGenerator resourceIdGenerator) {
        return com.climbassist.api.v2.CountryController.builder()
                .resourcesDao(resourcesDao)
                .resourceIdGenerator(resourceIdGenerator)
                .build();
    }
}

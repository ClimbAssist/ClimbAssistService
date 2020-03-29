package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class CountryFactory extends ResourceFactory<Country, NewCountry> {

    @Builder
    private CountryFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Country create(@NonNull NewCountry newCountry) {
        return Country.builder()
                .countryId(resourceIdGenerator.generateResourceId(newCountry.getName()))
                .name(newCountry.getName())
                .build();
    }
}
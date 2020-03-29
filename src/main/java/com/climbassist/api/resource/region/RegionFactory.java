package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class RegionFactory extends ResourceFactory<Region, NewRegion> {

    @Builder
    private RegionFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Region create(@NonNull NewRegion newRegion) {
        return Region.builder()
                .regionId(resourceIdGenerator.generateResourceId(newRegion.getName()))
                .countryId(newRegion.getCountryId())
                .name(newRegion.getName())
                .build();
    }
}
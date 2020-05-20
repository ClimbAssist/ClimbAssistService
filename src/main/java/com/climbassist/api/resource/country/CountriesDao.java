package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceWithoutParentDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CountriesDao extends ResourceWithoutParentDao<Country> {

    @Override
    protected Class<Country> getResourceTypeClass() {
        return Country.class;
    }

    @Override
    protected Country buildResourceForDeletion(@NonNull String resourceId) {
        return Country.builder()
                .countryId(resourceId)
                .build();
    }
}

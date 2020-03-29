package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreateCountryResultFactory implements CreateResourceResultFactory<Country> {

    @Override
    public CreateResourceResult<Country> create(@NonNull String countryId) {
        return CreateCountryResult.builder()
                .countryId(countryId)
                .build();
    }
}
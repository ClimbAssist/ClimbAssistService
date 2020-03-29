package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class CountryNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Country> {

    @Override
    public ResourceNotFoundException create(@NonNull String countryId) {
        return new CountryNotFoundException(countryId);
    }
}
package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class CountryNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Country> {

    @Override
    public ResourceNotEmptyException create(@NonNull String countryId) {
        return new CountryNotEmptyException(countryId);
    }
}

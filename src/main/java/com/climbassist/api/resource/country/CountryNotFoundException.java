package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import lombok.NonNull;

public class CountryNotFoundException extends ResourceNotFoundException {

    public CountryNotFoundException(@NonNull final String countryId) {
        super("country", countryId);
    }
}

package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceNotFoundException;

class CountryNotFoundException extends ResourceNotFoundException {

    CountryNotFoundException(String countryId) {
        super("country", countryId);
    }
}
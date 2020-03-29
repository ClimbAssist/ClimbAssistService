package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class CountryNotEmptyException extends ResourceNotEmptyException {

    CountryNotEmptyException(@NonNull String countryId) {
        super("country", countryId);
    }
}

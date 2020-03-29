package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.ValidCountryId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewRegion implements NewResourceWithParent<Region, Country> {

    @ValidCountryId
    private String countryId;

    @ValidName
    private String name;

    @JsonIgnore
    @Override
    public String getParentId() {
        return countryId;
    }
}

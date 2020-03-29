package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.NewResource;
import com.climbassist.api.resource.common.ValidName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewCountry implements NewResource<Country> {

    @ValidName
    private String name;
}

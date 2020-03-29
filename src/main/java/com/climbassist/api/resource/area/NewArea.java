package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.ValidRegionId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewArea implements NewResourceWithParent<Area, Region> {

    @ValidRegionId
    private String regionId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @JsonIgnore
    @Override
    public String getParentId() {
        return regionId;
    }
}

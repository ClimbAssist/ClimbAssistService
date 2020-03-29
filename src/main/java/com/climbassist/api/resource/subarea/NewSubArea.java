package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.ValidAreaId;
import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewSubArea implements NewResourceWithParent<SubArea, Area> {

    @ValidAreaId
    private String areaId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @JsonIgnore
    @Override
    public String getParentId() {
        return areaId;
    }
}

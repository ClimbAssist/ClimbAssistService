package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.ValidSubAreaId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Set;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
public class NewCrag implements NewResourceWithParent<Crag, SubArea> {

    @ValidSubAreaId
    private String subAreaId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @Valid
    @ValidLocation
    private Location location;

    @Valid
    @Nullable
    @Size(min = 1, max = 10, message = "Parking must contain between 1 and 10 elements.")
    private Set<Parking> parking;

    @JsonIgnore
    @Override
    public String getParentId() {
        return subAreaId;
    }
}

package com.climbassist.api.resource.route;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.common.ValidOptionalDescription;
import com.climbassist.api.resource.wall.ValidWallId;
import com.climbassist.api.resource.wall.Wall;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewRoute implements NewResourceWithParent<Route, Wall> {

    @ValidWallId
    private String wallId;

    @ValidName
    private String name;

    @ValidOptionalDescription
    @Nullable
    private String description;

    @Valid
    @Nullable
    private Center center;

    @ValidProtection
    @Nullable
    private String protection;

    @ValidStyle
    private String style;

    @Nullable
    private Boolean first;

    @ValidNextRouteId
    @Nullable
    private String next;

    @JsonIgnore
    @Override
    public String getParentId() {
        return wallId;
    }
}

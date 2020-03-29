package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.ValidPathId;
import com.climbassist.api.resource.point.ValidNextPointId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewPathPoint implements NewResourceWithParent<PathPoint, Path> {

    @ValidPathId
    private String pathId;

    @ValidLatitude
    private Double latitude;

    @ValidLongitude
    private Double longitude;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Nullable
    private Boolean first;

    @ValidNextPointId
    @Nullable
    private String next;

    @JsonIgnore
    @Override
    public String getParentId() {
        return pathId;
    }
}

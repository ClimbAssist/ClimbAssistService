package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.grade.ValidDanger;
import com.climbassist.api.resource.common.grade.ValidGrade;
import com.climbassist.api.resource.common.grade.ValidGradeModifier;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.ValidRouteId;
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
public class NewPitch implements NewResourceWithParent<Pitch, Route> {

    @ValidRouteId
    private String routeId;

    @ValidDescription
    private String description;

    @ValidGrade
    private Integer grade;

    @ValidGradeModifier
    @Nullable
    private String gradeModifier;

    @ValidDanger
    @Nullable
    private String danger;

    @Valid
    @Nullable
    private Anchors anchors;

    @ValidDistance
    @Nullable
    private Double distance;

    @Nullable
    private Boolean first;

    @ValidNextPitchId
    @Nullable
    private String next;

    @JsonIgnore
    @Override
    public String getParentId() {
        return routeId;
    }
}

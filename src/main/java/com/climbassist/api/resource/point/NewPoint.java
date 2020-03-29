package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.ValidPitchId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewPoint implements NewResourceWithParent<Point, Pitch> {

    @ValidPitchId
    private String pitchId;

    @ValidX
    private Double x;

    @ValidY
    private Double y;

    @ValidZ
    private Double z;

    @Nullable
    private Boolean first;

    @ValidNextPointId
    private String next;

    @JsonIgnore
    @Override
    public String getParentId() {
        return pitchId;
    }
}

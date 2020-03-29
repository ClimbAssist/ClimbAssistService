package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchNewResource;
import com.climbassist.api.resource.pitch.Pitch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class BatchNewPoint implements BatchNewResource<Point, Pitch> {

    @ValidX
    private Double x;

    @ValidY
    private Double y;

    @ValidZ
    private Double z;
}

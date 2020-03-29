package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchNewResource;
import com.climbassist.api.resource.path.Path;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class BatchNewPathPoint implements BatchNewResource<PathPoint, Path> {

    @ValidLatitude
    private Double latitude;

    @ValidLongitude
    private Double longitude;
}

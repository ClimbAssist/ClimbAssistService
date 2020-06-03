package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchNewResources;
import com.climbassist.api.resource.path.Path;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
class BatchNewPathPoints implements BatchNewResources<PathPoint, Path, BatchNewPathPoint> {

    @NotNull(message = "New path points must be present.")
    @Size(min = 1, max = 100, message = "Request must contain between 1 and 100 new path points.")
    private List<@Valid BatchNewPathPoint> newPathPoints;

    @JsonIgnore
    @Override
    public List<BatchNewPathPoint> getBatchNewResources() {
        return newPathPoints;
    }
}

package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchNewResources;
import com.climbassist.api.resource.pitch.Pitch;
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
public class BatchNewPoints implements BatchNewResources<Point, Pitch, BatchNewPoint> {

    @NotNull(message = "New points must be present.")
    @Size(min = 1, max = 100, message = "Request must contain between 1 and 100 new points.")
    private List<@Valid BatchNewPoint> newPoints;

    @JsonIgnore
    @Override
    public List<BatchNewPoint> getBatchNewResources() {
        return newPoints;
    }
}

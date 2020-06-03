package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.pitch.Pitch;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class BatchCreatePointsResult implements BatchCreateResourcesResult<Point, Pitch> {

    @NonNull
    private List<String> pointIds;
}

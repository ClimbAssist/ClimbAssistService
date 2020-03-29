package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.pitch.Pitch;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class BatchCreatePointsResult implements BatchCreateResourcesResult<Point, Pitch> {

    @NonNull
    private List<String> pointIds;
}

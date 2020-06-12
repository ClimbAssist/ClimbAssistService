package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.path.Path;
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
public class BatchCreatePathPointsResult implements BatchCreateResourcesResult<PathPoint, Path> {

    @NonNull
    private List<String> pathPointIds;
}

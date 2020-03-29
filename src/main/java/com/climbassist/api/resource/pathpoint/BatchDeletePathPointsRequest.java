package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.batch.BatchDeleteResourcesRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
class BatchDeletePathPointsRequest implements BatchDeleteResourcesRequest {

    @NotNull(message = "Path point IDs must be present.")
    @Size(min = 1, max = 50, message = "Request must contain between 1 and 50 path point IDs.")
    private Set<@ValidPathPointId String> pathPointIds;

    @Override
    public Set<String> getResourceIds() {
        return pathPointIds;
    }
}

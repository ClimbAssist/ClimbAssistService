package com.climbassist.api.resource.point;

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
class BatchDeletePointsRequest implements BatchDeleteResourcesRequest {

    @NotNull(message = "Point IDs must be present.")
    @Size(min = 1, max = 50, message = "Request must contain between 1 and 50 point IDs.")
    private Set<@ValidPointId String> pointIds;

    @Override
    public Set<String> getResourceIds() {
        return pointIds;
    }
}

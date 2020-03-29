package com.climbassist.api.resource.pathpoint;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.pitch.ValidPitchId;
import com.climbassist.api.resource.point.ValidNextPointId;
import com.climbassist.api.resource.point.ValidPointId;
import com.climbassist.api.resource.point.ValidX;
import com.climbassist.api.resource.point.ValidY;
import com.climbassist.api.resource.point.ValidZ;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class PathPoint implements OrderableResourceWithParent<PathPoint, Path> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "PathIndex";

    @DynamoDBHashKey
    @ValidPointId
    private String pathPointId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidPitchId
    private String pathId;

    @NotNull(message = "Latitude must be present.")
    @DecimalMin(value = "-180", message = "Latitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Latitude must be between -180 and 180.")
    private Double latitude;

    @NotNull(message = "Longitude must be present.")
    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180.")
    private Double longitude;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Nullable
    private Boolean first;

    @ValidNextPointId
    @Nullable
    private String next;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return pathPointId;
    }

    @DynamoDBIgnore
    @Override
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isFirst() {
        return first == null ? false : first;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return pathId;
    }
}

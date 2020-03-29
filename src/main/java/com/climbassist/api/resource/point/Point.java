package com.climbassist.api.resource.point;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.ValidPitchId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Point implements OrderableResourceWithParent<Point, Pitch> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "PitchIndex";

    @DynamoDBHashKey
    @ValidPointId
    private String pointId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidPitchId
    private String pitchId;

    @ValidX
    private Double x;

    @ValidY
    private Double y;

    @ValidZ
    private Double z;

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
        return pointId;
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
        return pitchId;
    }
}

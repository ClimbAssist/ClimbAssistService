package com.climbassist.api.resource.wall;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
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
public class NewWall implements NewResourceWithParent<Wall, Crag> {

    @ValidCragId
    private String cragId;

    @ValidName
    private String name;

    @Nullable
    private Boolean first;

    @Nullable
    @ValidNextWallId
    private String next;

    @JsonIgnore
    @Override
    public String getParentId() {
        return cragId;
    }
}

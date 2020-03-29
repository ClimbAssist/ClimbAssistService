package com.climbassist.api.resource.path;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder(toBuilder = true)
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Path implements ResourceWithParentAndChildren<Path, Crag> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "CragIndex";

    @DynamoDBHashKey
    @ValidPathId
    private String pathId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidCragId
    private String cragId;

    @DynamoDBIgnore
    private List<PathPoint> pathPoints;

    @Override
    @DynamoDBIgnore
    @JsonIgnore
    public String getId() {
        return pathId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return cragId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Path>> void setChildResources(Collection<?> childResources,
                                                                                   Class<ChildResource> childResourceClass) {
        if (childResourceClass != PathPoint.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        pathPoints = childResources == null ? null : ImmutableList.copyOf((Collection<PathPoint>) childResources);
    }
}

package com.climbassist.api.resource.wall;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParentAndChildren;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
import com.climbassist.api.resource.route.Route;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Wall implements OrderableResourceWithParentAndChildren<Wall, Crag> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "CragIndex";

    @DynamoDBHashKey
    @ValidWallId
    private String wallId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidCragId
    private String cragId;

    @ValidName
    private String name;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Nullable
    private Boolean first;

    @Nullable
    @ValidNextWallId
    private String next;

    @DynamoDBIgnore
    private List<Route> routes;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return wallId;
    }

    @DynamoDBIgnore
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Override
    public boolean isFirst() {
        return first == null ? false : first;
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
    public <ChildResource extends ResourceWithParent<Wall>> void setChildResources(Collection<?> childResources,
                                                                                   Class<ChildResource> childResourceClass) {
        if (childResourceClass != Route.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        routes = childResources == null ? null : ImmutableList.copyOf((Collection<Route>) childResources);
    }
}

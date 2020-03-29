package com.climbassist.api.resource.subarea;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.ValidAreaId;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.crag.Crag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Set;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class SubArea implements ResourceWithParentAndChildren<SubArea, Area> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "AreaIndex";

    @DynamoDBHashKey
    @ValidSubAreaId
    private String subAreaId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidAreaId
    private String areaId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @DynamoDBIgnore
    private Set<Crag> crags;

    @Override
    @DynamoDBIgnore
    @JsonIgnore
    public String getId() {
        return subAreaId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return areaId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<SubArea>> void setChildResources(Collection<?> childResources,
                                                                                      Class<ChildResource> childResourceClass) {
        if (childResourceClass != Crag.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        crags = childResources == null ? null : ImmutableSet.copyOf((Collection<Crag>) childResources);
    }
}

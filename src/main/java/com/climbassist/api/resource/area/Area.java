package com.climbassist.api.resource.area;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.ValidRegionId;
import com.climbassist.api.resource.subarea.SubArea;
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
public class Area implements ResourceWithParentAndChildren<Area, Region> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "RegionIndex";

    @DynamoDBHashKey
    @ValidAreaId
    private String areaId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidRegionId
    private String regionId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @DynamoDBIgnore
    private Set<SubArea> subAreas;

    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public String getId() {
        return areaId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return regionId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Area>> void setChildResources(Collection<?> childResources,
                                                                                   Class<ChildResource> childResourceClass) {
        if (childResourceClass != SubArea.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        subAreas = childResources == null ? null : ImmutableSet.copyOf((Collection<SubArea>) childResources);
    }
}

package com.climbassist.api.resource.region;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.ValidCountryId;
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
public class Region implements ResourceWithParentAndChildren<Region, Country> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "CountryIndex";

    @DynamoDBHashKey
    @ValidRegionId
    private String regionId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidCountryId
    private String countryId;

    @ValidName
    private String name;

    @DynamoDBIgnore
    private Set<Area> areas;

    @Override
    @DynamoDBIgnore
    @JsonIgnore
    public String getId() {
        return regionId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return countryId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Region>> void setChildResources(Collection<?> childResources,
                                                                                     Class<ChildResource> childResourceClass) {
        if (childResourceClass != Area.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        areas = childResources == null ? null : ImmutableSet.copyOf((Collection<Area>) childResources);
    }
}

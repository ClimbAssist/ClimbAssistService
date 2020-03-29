package com.climbassist.api.resource.country;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.region.Region;
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
public class Country implements ResourceWithChildren<Country> {

    @DynamoDBHashKey
    @ValidCountryId
    private String countryId;

    @ValidName
    private String name;

    @DynamoDBIgnore
    private Set<Region> regions;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return countryId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Country>> void setChildResources(Collection<?> childResources,
                                                                                      Class<ChildResource> childResourceClass) {
        if (childResourceClass != Region.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        regions = childResources == null ? null : ImmutableSet.copyOf((Collection<Region>) childResources);
    }
}

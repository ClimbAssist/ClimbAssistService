package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@SuperBuilder
public abstract class ResourceWithoutParentDao<Resource extends com.climbassist.api.resource.common.Resource>
        extends ResourceDao<Resource> {

    public Set<Resource> getResources() {
        return new HashSet<>(
                dynamoDBMapper.scan(getResourceTypeClass(), new DynamoDBScanExpression(), dynamoDBMapperConfig));
    }
}

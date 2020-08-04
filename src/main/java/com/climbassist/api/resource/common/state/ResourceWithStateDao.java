package com.climbassist.api.resource.common.state;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuperBuilder
public abstract class ResourceWithStateDao<Resource extends ResourceWithParent<ParentResource> & ResourceWithState,
        ParentResource extends ResourceWithChildren<ParentResource>>
        extends ResourceWithParentDao<Resource, ParentResource> {

    @NonNull
    protected final UserManager userManager;

    @Override
    public Optional<Resource> getResource(@NonNull String resourceId, @NonNull Optional<UserData> maybeUserData) {
        Optional<Resource> maybeResource = Optional.ofNullable(
                dynamoDBMapper.load(getResourceTypeClass(), resourceId, dynamoDBMapperConfig));
        if (!maybeResource.isPresent() || maybeResource.get()
                .getState()
                .equals(State.PUBLIC.toString()) || maybeUserData.isPresent() && maybeUserData.get()
                .isAdministrator()) {
            return maybeResource;
        }
        return Optional.empty();
    }

    @Override
    public Set<Resource> getResources(@NonNull String parentId, Optional<UserData> maybeUserData) {
        Resource hashKey = buildIndexHashKey(parentId);
        DynamoDBQueryExpression<Resource> dynamoDBQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(hashKey)
                        .withConsistentRead(false)
                        .withIndexName(getIndexName());
        if (!maybeUserData.isPresent() || !maybeUserData.get()
                .isAdministrator()) {
            dynamoDBQueryExpression.setQueryFilter(ImmutableMap.of("state",
                    new Condition().withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue(State.PUBLIC.toString()))));
        }
        return new HashSet<>(
                dynamoDBMapper.query(getResourceTypeClass(), dynamoDBQueryExpression, dynamoDBMapperConfig));
    }
}

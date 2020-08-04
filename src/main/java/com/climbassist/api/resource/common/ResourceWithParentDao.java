package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.climbassist.api.user.UserData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuperBuilder
public abstract class ResourceWithParentDao<Resource extends ResourceWithParent<ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        extends ResourceDao<Resource> {

    public Set<Resource> getResources(@NonNull String parentId, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NonNull Optional<UserData> maybeUserData) {
        Resource hashKey = buildIndexHashKey(parentId);
        DynamoDBQueryExpression<Resource> dynamoDBQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(hashKey)
                        .withConsistentRead(false)
                        .withIndexName(getIndexName());
        return new HashSet<>(
                dynamoDBMapper.query(getResourceTypeClass(), dynamoDBQueryExpression, dynamoDBMapperConfig));
    }

    protected abstract Resource buildIndexHashKey(String parentId);

    protected abstract String getIndexName();
}

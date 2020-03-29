package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ResourceDao<Resource> {

    @NonNull
    protected final DynamoDBMapperConfig dynamoDBMapperConfig;
    @NonNull
    protected final DynamoDBMapper dynamoDBMapper;

    public Optional<Resource> getResource(@NonNull String resourceId) {
        return Optional.ofNullable(dynamoDBMapper.load(getResourceTypeClass(), resourceId, dynamoDBMapperConfig));
    }

    public Set<Resource> getResources(@NonNull String parentId) {
        Resource hashKey = buildIndexHashKey(parentId);
        DynamoDBQueryExpression<Resource> dynamoDBQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(hashKey)
                        .withConsistentRead(false)
                        .withIndexName(getIndexName());
        return new HashSet<>(
                dynamoDBMapper.query(getResourceTypeClass(), dynamoDBQueryExpression, dynamoDBMapperConfig));
    }

    public void saveResource(@NonNull Resource resource) {
        dynamoDBMapper.save(resource, dynamoDBMapperConfig);
    }

    public void deleteResource(@NonNull String resourceId) {
        dynamoDBMapper.delete(buildResourceForDeletion(resourceId), dynamoDBMapperConfig);
    }

    protected abstract Resource buildResourceForDeletion(String resourceId);

    protected abstract Resource buildIndexHashKey(String parentId);

    protected abstract String getIndexName();

    protected abstract Class<Resource> getResourceTypeClass();
}

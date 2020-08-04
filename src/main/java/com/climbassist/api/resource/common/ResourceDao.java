package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.user.UserData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ResourceDao<Resource extends com.climbassist.api.resource.common.Resource> {

    @NonNull
    protected final DynamoDBMapperConfig dynamoDBMapperConfig;
    @NonNull
    protected final DynamoDBMapper dynamoDBMapper;

    public Optional<Resource> getResource(@NonNull String resourceId,
                                          @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                          @NonNull Optional<UserData> maybeUserData) {
        return Optional.ofNullable(dynamoDBMapper.load(getResourceTypeClass(), resourceId, dynamoDBMapperConfig));
    }

    public void saveResource(@NonNull Resource resource) {
        dynamoDBMapper.save(resource, dynamoDBMapperConfig);
    }

    public void deleteResource(@NonNull String resourceId) {
        dynamoDBMapper.delete(buildResourceForDeletion(resourceId), dynamoDBMapperConfig);
    }

    protected abstract Resource buildResourceForDeletion(String resourceId);

    protected abstract Class<Resource> getResourceTypeClass();
}

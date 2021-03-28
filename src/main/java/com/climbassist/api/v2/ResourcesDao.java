package com.climbassist.api.v2;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder
public class ResourcesDao {

    @NonNull
    private final DynamoDBMapperConfig dynamoDbMapperConfig;
    @NonNull
    private final DynamoDBMapper dynamoDbMapper;
    @NonNull
    private final ResourceFactory resourceFactory;

    public <T extends Resource> Optional<T> getResource(@NonNull final String id,
            @NonNull final Class<T> resourceTypeClass) {
        return getResource(id, resourceTypeClass, Optional.empty());
    }

    public <T extends Resource> Optional<T> getResource(@NonNull final String id,
            @NonNull final Class<T> resourceTypeClass, @NonNull final UserData userData) {
        return getResource(id, resourceTypeClass, Optional.of(userData));
    }

    public <T extends Resource> Set<T> listResources(@NonNull final Class<T> resourceTypeClass) {
        return listResources(resourceTypeClass, Optional.empty());
    }

    public <T extends Resource> Set<T> listResources(@NonNull final Class<T> resourceTypeClass,
            @NonNull final UserData userData) {
        return listResources(resourceTypeClass, Optional.of(userData));
    }

    public <T extends Resource> void saveResource(@NonNull final T resource) {
        dynamoDbMapper.save(resource, dynamoDbMapperConfig);
    }

    public <T extends Resource> void deleteResource(final String id, final Class<T> resourceTypeClass) {
        dynamoDbMapper.delete(resourceFactory.buildResource(resourceTypeClass, id), dynamoDbMapperConfig);
    }

    private <T extends Resource> Optional<T> getResource(final String id, final Class<T> resourceTypeClass,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UserData> maybeUserData) {
        DynamoDBQueryExpression<T> dynamoDbQueryExpression = new DynamoDBQueryExpression<T>().withHashKeyValues(
                resourceFactory.buildResource(resourceTypeClass, id));
        if (isNotAdministrator(maybeUserData)) {
            dynamoDbQueryExpression = dynamoDbQueryExpression.withQueryFilterEntry("state",
                    new Condition().withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue(State.PUBLIC.toString())));
        }
        return dynamoDbMapper.query(resourceTypeClass, dynamoDbQueryExpression, dynamoDbMapperConfig)
                .stream()
                .findAny();
    }

    private <T extends Resource> Set<T> listResources(final Class<T> resourceTypeClass,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<UserData> maybeUserData) {
        DynamoDBQueryExpression<T> dynamoDbQueryExpression =
                new DynamoDBQueryExpression<T>().withHashKeyValues(resourceFactory.buildResource(resourceTypeClass))
                        .withConsistentRead(false)
                        .withIndexName("TypeIndex");
        if (isNotAdministrator(maybeUserData)) {
            dynamoDbQueryExpression = dynamoDbQueryExpression.withRangeKeyCondition("state",
                    new Condition().withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue(State.PUBLIC.toString())));
        }
        return new HashSet<>(dynamoDbMapper.query(resourceTypeClass, dynamoDbQueryExpression, dynamoDbMapperConfig));
    }

    private boolean isNotAdministrator(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<UserData> maybeUserData) {
        return !maybeUserData.isPresent() || !maybeUserData.get()
                .isAdministrator();
    }
}


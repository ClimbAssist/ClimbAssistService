package com.climbassist.api.resource.country;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@SuperBuilder
public class CountriesDao extends ResourceDao<Country> {

    private static final String NO_SECONDARY_INDEX_ERROR_MESSAGE = "Countries table has no secondary index.";

    @Override
    protected Country buildIndexHashKey(@NonNull String parentId) {
        throw new UnsupportedOperationException(NO_SECONDARY_INDEX_ERROR_MESSAGE);
    }

    @Override
    protected String getIndexName() {
        throw new UnsupportedOperationException(NO_SECONDARY_INDEX_ERROR_MESSAGE);
    }

    @Override
    protected Class<Country> getResourceTypeClass() {
        return Country.class;
    }

    @Override
    public Set<Country> getResources(@NonNull String parentId) {
        throw new UnsupportedOperationException(
                "Countries do not have a parent. The no-argument implementation of this method should be used instead" +
                        ".");
    }

    @Override
    protected Country buildResourceForDeletion(@NonNull String resourceId) {
        return Country.builder()
                .countryId(resourceId)
                .build();
    }

    public Set<Country> getResources() {
        return new HashSet<>(dynamoDBMapper.scan(Country.class, new DynamoDBScanExpression(), dynamoDBMapperConfig));
    }
}

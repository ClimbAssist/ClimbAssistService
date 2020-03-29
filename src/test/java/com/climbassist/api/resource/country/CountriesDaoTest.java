package com.climbassist.api.resource.country;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountriesDaoTest extends AbstractResourceDaoTest<Country> {

    private static final String NO_SECONDARY_INDEX_ERROR_MESSAGE = "Countries table has no secondary index.";
    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Countries"))
            .build();
    private static final Country COUNTRY_1 = Country.builder()
            .countryId("country-1")
            .name("Country 1")
            .build();
    private static final Country COUNTRY_2 = Country.builder()
            .countryId("country-2")
            .name("Country 2")
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;
    @Mock
    private PaginatedScanList<Object> mockPaginatedScanList;

    @Override
    protected CountriesDao buildResourceDao() {
        return CountriesDao.builder()
                .dynamoDBMapper(mockDynamoDbMapper)
                .dynamoDBMapperConfig(DYNAMO_DB_MAPPER_CONFIG)
                .build();
    }

    @Override
    protected DynamoDBMapperConfig getDynamoDbMapperConfig() {
        return DYNAMO_DB_MAPPER_CONFIG;
    }

    @Override
    protected String getIndexName() {
        throw new UnsupportedOperationException(NO_SECONDARY_INDEX_ERROR_MESSAGE);
    }

    @Override
    protected Country getTestResource1() {
        return COUNTRY_1;
    }

    @Override
    protected Country getTestResource2() {
        return COUNTRY_2;
    }

    @Override
    protected String getIdFromTestResource(Country country) {
        return country.getCountryId();
    }

    @Override
    protected String getParentIdFromTestResource(Country country) {
        throw new UnsupportedOperationException("Countries do not have a parent.");
    }

    @Override
    protected Class<Country> getTestResourceClass() {
        return Country.class;
    }

    @Override
    protected Country buildIndexHashKey(String parentId) {
        throw new UnsupportedOperationException(NO_SECONDARY_INDEX_ERROR_MESSAGE);
    }

    @Override
    protected Country buildResourceForDeletion(String resourceId) {
        return Country.builder()
                .countryId(resourceId)
                .build();
    }

    @Override
    @Test
    protected void getResources_returnsResourcesFromDynamoDb_whenResourcesExist() {
        runGetResourcesTest(ImmutableSet.of(COUNTRY_1, COUNTRY_2));
    }

    @Override
    @Test
    protected void getResources_returnsEmptySet_whenResourcesDoNotExist() {
        runGetResourcesTest(ImmutableSet.of());
    }

    @Test
    void buildIndexHashKey_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> ((CountriesDao) resourceDao).buildIndexHashKey("some-parent-id"));
    }

    @Test
    void getIndexName_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> ((CountriesDao) resourceDao).getIndexName());
    }

    @Test
    void getResources_throwsUnsupportedOperationException_whenParameterIsPassedIn() {
        assertThrows(UnsupportedOperationException.class, () -> resourceDao.getResources("some-parent-id"));
    }

    private void runGetResourcesTest(Set<Object> countries) {
        when(mockDynamoDbMapper.scan(any(), any(), any())).thenReturn(mockPaginatedScanList);
        when(mockPaginatedScanList.iterator()).thenReturn(countries.iterator());
        assertThat(((CountriesDao) resourceDao).getResources(), is(equalTo(countries)));
        verify(mockDynamoDbMapper).scan(eq(Country.class), any(DynamoDBScanExpression.class),
                eq(DYNAMO_DB_MAPPER_CONFIG));
        verify(mockPaginatedScanList).iterator();
    }
}
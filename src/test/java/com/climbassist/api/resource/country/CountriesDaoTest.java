package com.climbassist.api.resource.country;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceWithoutParentDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class CountriesDaoTest extends AbstractResourceWithoutParentDaoTest<Country, CountriesDao> {

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
    protected Country getTestResource1() {
        return COUNTRY_1;
    }

    @Override
    protected Country getTestResource2() {
        return COUNTRY_2;
    }

    @Override
    protected Class<Country> getTestResourceClass() {
        return Country.class;
    }

    @Override
    protected Country buildResourceForDeletion(String resourceId) {
        return Country.builder()
                .countryId(resourceId)
                .build();
    }
}

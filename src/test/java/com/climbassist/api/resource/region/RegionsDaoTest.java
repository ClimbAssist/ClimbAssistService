package com.climbassist.api.resource.region;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceWithParentDaoTest;
import com.climbassist.api.resource.country.Country;
import lombok.Getter;
import org.mockito.Mock;

class RegionsDaoTest extends AbstractResourceWithParentDaoTest<Region, Country, RegionsDao> {

    private static final Region REGION_1 = Region.builder()
            .regionId("region-1")
            .countryId("country-1")
            .name("Region 1")
            .build();
    private static final Region REGION_2 = Region.builder()
            .regionId("region-2")
            .countryId("country-1")
            .name("Region 2")
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Regions"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected RegionsDao buildResourceDao() {
        return RegionsDao.builder()
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
        return Region.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Region getTestResource1() {
        return REGION_1;
    }

    @Override
    protected Region getTestResource2() {
        return REGION_2;
    }

    @Override
    protected Class<Region> getTestResourceClass() {
        return Region.class;
    }

    @Override
    protected Region buildIndexHashKey(String parentId) {
        return Region.builder()
                .countryId(parentId)
                .build();
    }

    @Override
    protected Region buildResourceForDeletion(String resourceId) {
        return Region.builder()
                .regionId(resourceId)
                .build();
    }
}

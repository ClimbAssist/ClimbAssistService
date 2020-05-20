package com.climbassist.api.resource.area;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceWithParentDaoTest;
import com.climbassist.api.resource.region.Region;
import lombok.Getter;
import org.mockito.Mock;

class AreasDaoTest extends AbstractResourceWithParentDaoTest<Area, Region, AreasDao> {

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Areas"))
            .build();
    private static final Area AREA_1 = Area.builder()
            .areaId("area-1")
            .regionId("region-1")
            .name("Area 1")
            .description("Area 1")
            .build();
    private static final Area AREA_2 = Area.builder()
            .areaId("area-2")
            .regionId("region-2")
            .name("Area 2")
            .description("Area 2")
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected AreasDao buildResourceDao() {
        return AreasDao.builder()
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
        return Area.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Area getTestResource1() {
        return AREA_1;
    }

    @Override
    protected Area getTestResource2() {
        return AREA_2;
    }

    @Override
    protected Class<Area> getTestResourceClass() {
        return Area.class;
    }

    @Override
    protected Area buildIndexHashKey(String parentId) {
        return Area.builder()
                .regionId(parentId)
                .build();
    }

    @Override
    protected Area buildResourceForDeletion(String resourceId) {
        return Area.builder()
                .areaId(resourceId)
                .build();
    }
}

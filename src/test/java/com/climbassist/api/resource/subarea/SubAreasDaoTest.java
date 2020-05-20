package com.climbassist.api.resource.subarea;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.common.AbstractResourceWithParentDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class SubAreasDaoTest extends AbstractResourceWithParentDaoTest<SubArea, Area, SubAreasDao> {

    private static final SubArea SUB_AREA_1 = SubArea.builder()
            .subAreaId("sub-area-1")
            .areaId("area-1")
            .name("Sub-Area 1")
            .description("description 1")
            .build();
    private static final SubArea SUB_AREA_2 = SubArea.builder()
            .subAreaId("sub-area-2")
            .areaId("area-1")
            .name("Sub-Area 2")
            .description("description 2")
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("SubAreas"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected SubAreasDao buildResourceDao() {
        return SubAreasDao.builder()
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
        return SubArea.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected SubArea getTestResource1() {
        return SUB_AREA_1;
    }

    @Override
    protected SubArea getTestResource2() {
        return SUB_AREA_2;
    }

    @Override
    protected Class<SubArea> getTestResourceClass() {
        return SubArea.class;
    }

    @Override
    protected SubArea buildIndexHashKey(String parentId) {
        return SubArea.builder()
                .areaId(parentId)
                .build();
    }

    @Override
    protected SubArea buildResourceForDeletion(String resourceId) {
        return SubArea.builder()
                .subAreaId(resourceId)
                .build();
    }
}

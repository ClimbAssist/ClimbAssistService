package com.climbassist.api.resource.wall;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class WallsDaoTest extends AbstractResourceDaoTest<Wall> {

    private static final Wall WALL_1 = Wall.builder()
            .wallId("wall-1")
            .cragId("crag-1")
            .name("Wall 1")
            .build();
    private static final Wall WALL_2 = Wall.builder()
            .wallId("wall-2")
            .cragId("crag-1")
            .wallId("Wall 2")
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Walls"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected WallsDao buildResourceDao() {
        return WallsDao.builder()
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
        return Wall.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Wall getTestResource1() {
        return WALL_1;
    }

    @Override
    protected Wall getTestResource2() {
        return WALL_2;
    }

    @Override
    protected String getIdFromTestResource(Wall wall) {
        return wall.getWallId();
    }

    @Override
    protected String getParentIdFromTestResource(Wall wall) {
        return wall.getCragId();
    }

    @Override
    protected Class<Wall> getTestResourceClass() {
        return Wall.class;
    }

    @Override
    protected Wall buildIndexHashKey(String parentId) {
        return Wall.builder()
                .cragId(parentId)
                .build();
    }

    @Override
    protected Wall buildResourceForDeletion(String resourceId) {
        return Wall.builder()
                .wallId(resourceId)
                .build();
    }
}
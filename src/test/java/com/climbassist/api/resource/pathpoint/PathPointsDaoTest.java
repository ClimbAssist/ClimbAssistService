package com.climbassist.api.resource.pathpoint;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class PathPointsDaoTest extends AbstractResourceDaoTest<PathPoint> {

    private static final PathPoint PATH_POINT_1 = PathPoint.builder()
            .pathPointId("path-point-1")
            .pathId("path-1")
            .latitude(1.0)
            .longitude(1.0)
            .first(true)
            .next("path-point-2")
            .build();
    private static final PathPoint PATH_POINT_2 = PathPoint.builder()
            .pathPointId("path-point-1")
            .pathId("path-1")
            .latitude(1.0)
            .longitude(1.0)
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("PathPoints"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected PathPointsDao buildResourceDao() {
        return PathPointsDao.builder()
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
        return PathPoint.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected PathPoint getTestResource1() {
        return PATH_POINT_1;
    }

    @Override
    protected PathPoint getTestResource2() {
        return PATH_POINT_2;
    }

    @Override
    protected String getIdFromTestResource(PathPoint pathPoint) {
        return pathPoint.getPathPointId();
    }

    @Override
    protected String getParentIdFromTestResource(PathPoint pathPoint) {
        return pathPoint.getPathId();
    }

    @Override
    protected Class<PathPoint> getTestResourceClass() {
        return PathPoint.class;
    }

    @Override
    protected PathPoint buildIndexHashKey(String parentId) {
        return PathPoint.builder()
                .pathId(parentId)
                .build();
    }

    @Override
    protected PathPoint buildResourceForDeletion(String resourceId) {
        return PathPoint.builder()
                .pathPointId(resourceId)
                .build();
    }

}

package com.climbassist.api.resource.point;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class PointsDaoTest extends AbstractResourceDaoTest<Point> {

    private static final Point POINT_1 = Point.builder()
            .pointId("point-1")
            .pitchId("pitch-1")
            .x(1.0)
            .y(1.0)
            .z(1.0)
            .first(true)
            .next("point-2")
            .build();
    private static final Point POINT_2 = Point.builder()
            .pointId("point-1")
            .pitchId("pitch-1")
            .x(1.0)
            .y(1.0)
            .z(1.0)
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Points"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected PointsDao buildResourceDao() {
        return PointsDao.builder()
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
        return Point.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Point getTestResource1() {
        return POINT_1;
    }

    @Override
    protected Point getTestResource2() {
        return POINT_2;
    }

    @Override
    protected String getIdFromTestResource(Point point) {
        return point.getPointId();
    }

    @Override
    protected String getParentIdFromTestResource(Point point) {
        return point.getPitchId();
    }

    @Override
    protected Class<Point> getTestResourceClass() {
        return Point.class;
    }

    @Override
    protected Point buildIndexHashKey(String parentId) {
        return Point.builder()
                .pitchId(parentId)
                .build();
    }

    @Override
    protected Point buildResourceForDeletion(String resourceId) {
        return Point.builder()
                .pointId(resourceId)
                .build();
    }
}
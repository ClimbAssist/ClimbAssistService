package com.climbassist.api.resource.path;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class PathsDaoTest extends AbstractResourceDaoTest<Path> {

    private static final Path PATH_1 = Path.builder()
            .pathId("path-1")
            .cragId("crag-1")
            .build();
    private static final Path PATH_2 = Path.builder()
            .pathId("path-2")
            .cragId("crag-1")
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Paths"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected PathsDao buildResourceDao() {
        return PathsDao.builder()
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
        return Path.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Path getTestResource1() {
        return PATH_1;
    }

    @Override
    protected Path getTestResource2() {
        return PATH_2;
    }

    @Override
    protected String getIdFromTestResource(Path path) {
        return path.getPathId();
    }

    @Override
    protected String getParentIdFromTestResource(Path path) {
        return path.getCragId();
    }

    @Override
    protected Class<Path> getTestResourceClass() {
        return Path.class;
    }

    @Override
    protected Path buildIndexHashKey(String parentId) {
        return Path.builder()
                .cragId(parentId)
                .build();
    }

    @Override
    protected Path buildResourceForDeletion(String resourceId) {
        return Path.builder()
                .pathId(resourceId)
                .build();
    }

}

package com.climbassist.api.resource.pitch;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class PitchesDaoTest extends AbstractResourceDaoTest<Pitch> {

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Pitches"))
            .build();
    private static final Pitch PITCH_1 = Pitch.builder()
            .pitchId("pitch-1")
            .routeId("route-1")
            .description("Pitch 1")
            .grade(11)
            .gradeModifier("b/c")
            .anchors(Anchors.builder()
                    .x(1.0)
                    .y(1.0)
                    .z(1.0)
                    .fixed(true)
                    .build())
            .first(true)
            .next("pitch-2")
            .build();
    private static final Pitch PITCH_2 = Pitch.builder()
            .pitchId("pitch-2")
            .routeId("route-1")
            .description("Pitch 2")
            .grade(11)
            .gradeModifier("b/c")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(true)
                    .build())
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected PitchesDao buildResourceDao() {
        return PitchesDao.builder()
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
        return Pitch.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Pitch getTestResource1() {
        return PITCH_1;
    }

    @Override
    protected Pitch getTestResource2() {
        return PITCH_2;
    }

    @Override
    protected String getIdFromTestResource(Pitch pitch) {
        return pitch.getId();
    }

    @Override
    protected String getParentIdFromTestResource(Pitch pitch) {
        return pitch.getRouteId();
    }

    @Override
    protected Class<Pitch> getTestResourceClass() {
        return Pitch.class;
    }

    @Override
    protected Pitch buildIndexHashKey(String parentId) {
        return Pitch.builder()
                .routeId(parentId)
                .build();
    }

    @Override
    protected Pitch buildResourceForDeletion(String resourceId) {
        return Pitch.builder()
                .pitchId(resourceId)
                .build();
    }
}
package com.climbassist.api.resource.crag;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.state.AbstractResourceWithStateDaoTest;
import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.user.UserManager;
import lombok.Getter;
import org.mockito.Mock;

class CragsDaoTest extends AbstractResourceWithStateDaoTest<Crag, SubArea, CragsDao> {

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Crags"))
            .build();
    private static final Crag CRAG_1 = Crag.builder()
            .cragId("crag-1")
            .subAreaId("sub-area-1")
            .name("Crag 1")
            .description("Crag 1")
            .imageLocation("url1")
            .location(Location.builder()
                    .latitude(1.0)
                    .longitude(1.0)
                    .zoom(1.0)
                    .build())
            .model(Model.builder()
                    .azimuth(Azimuth.builder()
                            .minimum(1.0)
                            .maximum(1.0)
                            .build())
                    .light(1.0)
                    .lowResModelLocation("url1")
                    .modelLocation("url1")
                    .modelAngle(1.0)
                    .scale(1.0)
                    .build())
            .state(State.PUBLIC.toString())
            .build();
    private static final Crag CRAG_2 = Crag.builder()
            .cragId("crag-2")
            .subAreaId("sub-area-1")
            .name("Crag 2")
            .description("Crag 2")
            .imageLocation("url2")
            .location(Location.builder()
                    .latitude(2.0)
                    .longitude(2.0)
                    .zoom(2.0)
                    .build())
            .model(Model.builder()
                    .azimuth(Azimuth.builder()
                            .minimum(2.0)
                            .maximum(2.0)
                            .build())
                    .light(2.0)
                    .lowResModelLocation("url2")
                    .modelLocation("url2")
                    .modelAngle(2.0)
                    .scale(2.0)
                    .build())
            .state(State.PUBLIC.toString())
            .build();
    private static final Crag CRAG_IN_REVIEW = Crag.builder()
            .cragId("crag-3")
            .subAreaId("sub-area-1")
            .name("Crag 3")
            .description("Crag 3")
            .imageLocation("url3")
            .location(Location.builder()
                    .latitude(3.0)
                    .longitude(3.0)
                    .zoom(3.0)
                    .build())
            .model(Model.builder()
                    .azimuth(Azimuth.builder()
                            .minimum(3.0)
                            .maximum(3.0)
                            .build())
                    .light(3.0)
                    .lowResModelLocation("url3")
                    .modelLocation("url3")
                    .modelAngle(3.0)
                    .scale(3.0)
                    .build())
            .state(State.IN_REVIEW.toString())
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Mock
    private UserManager mockUserManager;

    @Override
    protected CragsDao buildResourceDao() {
        return CragsDao.builder()
                .dynamoDBMapper(mockDynamoDbMapper)
                .dynamoDBMapperConfig(DYNAMO_DB_MAPPER_CONFIG)
                .userManager(mockUserManager)
                .build();
    }

    @Override
    protected DynamoDBMapperConfig getDynamoDbMapperConfig() {
        return DYNAMO_DB_MAPPER_CONFIG;
    }

    @Override
    protected String getIndexName() {
        return Crag.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Crag getTestResource1() {
        return CRAG_1;
    }

    @Override
    protected Crag getTestResource2() {
        return CRAG_2;
    }

    @Override
    protected Class<Crag> getTestResourceClass() {
        return Crag.class;
    }

    @Override
    protected Crag buildIndexHashKey(String parentId) {
        return Crag.builder()
                .subAreaId(parentId)
                .build();
    }

    @Override
    protected Crag buildResourceForDeletion(String resourceId) {
        return Crag.builder()
                .cragId(resourceId)
                .build();
    }

    @Override
    protected Crag getTestResourceInReview() {
        return CRAG_IN_REVIEW;
    }
}

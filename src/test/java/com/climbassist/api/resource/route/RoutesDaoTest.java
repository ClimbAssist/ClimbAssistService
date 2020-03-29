package com.climbassist.api.resource.route;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceDaoTest;
import lombok.Getter;
import org.mockito.Mock;

class RoutesDaoTest extends AbstractResourceDaoTest<Route> {

    private static final Route ROUTE_1 = Route.builder()
            .routeId("route-1")
            .wallId("wall-1")
            .name("Route 1")
            .description("Route 1")
            .center(Center.builder()
                    .x(1.0)
                    .y(2.0)
                    .z(3.0)
                    .build())
            .grade(1)
            .gradeModifier("a")
            .mainImageLocation("mainImageLocation")
            .protection("protection")
            .style("sport")
            .build();
    private static final Route ROUTE_2 = Route.builder()
            .routeId("route-2")
            .wallId("wall-1")
            .name("Route 2")
            .description("Route 2")
            .center(Center.builder()
                    .x(4.0)
                    .y(5.0)
                    .z(6.0)
                    .build())
            .grade(2)
            .gradeModifier("b")
            .mainImageLocation("mainImageLocation")
            .protection("protection")
            .style("trad")
            .build();

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("Routes"))
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected RoutesDao buildResourceDao() {
        return RoutesDao.builder()
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
        return Route.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Route getTestResource1() {
        return ROUTE_1;
    }

    @Override
    protected Route getTestResource2() {
        return ROUTE_2;
    }

    @Override
    protected String getIdFromTestResource(Route route) {
        return route.getRouteId();
    }

    @Override
    protected String getParentIdFromTestResource(Route route) {
        return route.getWallId();
    }

    @Override
    protected Class<Route> getTestResourceClass() {
        return Route.class;
    }

    @Override
    protected Route buildIndexHashKey(String parentId) {
        return Route.builder()
                .wallId(parentId)
                .build();
    }

    @Override
    protected Route buildResourceForDeletion(String resourceId) {
        return Route.builder()
                .routeId(resourceId)
                .build();
    }
}
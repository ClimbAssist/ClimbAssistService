package com.climbassist.api.v2;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.user.UserData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourcesDaoTest {

    private static final String TABLE_NAME = "Resources";
    private static final ProvisionedThroughput PROVISIONED_THROUGHPUT = new ProvisionedThroughput(1L, 1L);
    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.CLOBBER)
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(TABLE_NAME))
            .build();
    private static final Country COUNTRY_1_PUBLIC = buildCountry1(State.PUBLIC);
    private static final Country COUNTRY_1_IN_REVIEW = buildCountry1(State.IN_REVIEW);
    private static final Country COUNTRY_2_IN_REVIEW = Country.builder()
            .id("id-2")
            .name("name-2")
            .state(State.IN_REVIEW)
            .build();
    private static final UserData USER_DATA_ADMINISTRATOR = buildUserData(true);
    private static final UserData USER_DATA_NOT_ADMINISTRATOR = buildUserData(false);

    private static AmazonDynamoDBLocal amazonDynamoDBLocal;
    private static AmazonDynamoDB amazonDynamoDB;

    @Mock
    private ResourceFactory mockResourceFactory;

    private ResourcesDao resourcesDao;

    @BeforeAll
    static void setupClass() {
        System.setProperty("sqlite4java.library.path", "native-libs");
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        amazonDynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
    }

    @AfterAll
    static void teardownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @BeforeEach
    void setUp() {
        amazonDynamoDB.createTable(new CreateTableRequest().withTableName(TABLE_NAME)
                .withAttributeDefinitions(new AttributeDefinition("id", ScalarAttributeType.S),
                        new AttributeDefinition("type", ScalarAttributeType.S),
                        new AttributeDefinition("state", ScalarAttributeType.S),
                        new AttributeDefinition("parentId", ScalarAttributeType.S),
                        new AttributeDefinition("parentType", ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement("id", KeyType.HASH), new KeySchemaElement("type", KeyType.RANGE))
                .withGlobalSecondaryIndexes(buildBaseGlobalSecondaryIndex().withIndexName("TypeIndex")
                                .withKeySchema(new KeySchemaElement("type", KeyType.HASH),
                                        new KeySchemaElement("state", KeyType.RANGE)),
                        buildBaseGlobalSecondaryIndex().withIndexName("ParentIndex")
                                .withKeySchema(new KeySchemaElement("parentId", KeyType.HASH),
                                        new KeySchemaElement("parentType", KeyType.RANGE)))
                .withProvisionedThroughput(PROVISIONED_THROUGHPUT));
        resourcesDao = ResourcesDao.builder()
                .dynamoDbMapper(new DynamoDBMapper(amazonDynamoDB))
                .dynamoDbMapperConfig(DYNAMO_DB_MAPPER_CONFIG)
                .resourceFactory(mockResourceFactory)
                .build();
    }

    @AfterEach
    void tearDown() {
        amazonDynamoDB.deleteTable(TABLE_NAME);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(DynamoDBMapper.class, new DynamoDBMapper(amazonDynamoDB));
        nullPointerTester.setDefault(DynamoDBMapperConfig.class, DYNAMO_DB_MAPPER_CONFIG);
        nullPointerTester.setDefault(ResourceFactory.class, mockResourceFactory);
        nullPointerTester.testConstructors(ResourcesDao.class, NullPointerTester.Visibility.PACKAGE);
        nullPointerTester.testInstanceMethods(resourcesDao, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_returnsEmpty_whenResourceDoesNotExist() {
        String id = "does-not-exist";
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(id)
                .build());
        assertThat(resourcesDao.getResource("does-not-exist", Country.class), is(equalTo(Optional.empty())));
        verify(mockResourceFactory).buildResource(Country.class, id);
    }

    @Test
    void getResource_returnsEmpty_whenResourceIsInReviewAndUserDataIsNotPassed() {
        resourcesDao.saveResource(COUNTRY_1_IN_REVIEW);
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_IN_REVIEW.getId())
                .build());
        assertThat(resourcesDao.getResource(COUNTRY_1_IN_REVIEW.getId(), Country.class), is(equalTo(Optional.empty())));
        verify(mockResourceFactory).buildResource(Country.class, COUNTRY_1_IN_REVIEW.getId());
    }

    @Test
    void getResource_returnsEmpty_whenResourceIsInReviewAndUserIsNotAdministrator() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_IN_REVIEW.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_IN_REVIEW);
        assertThat(resourcesDao.getResource(COUNTRY_1_IN_REVIEW.getId(), Country.class, USER_DATA_NOT_ADMINISTRATOR),
                is(equalTo(Optional.empty())));
        verify(mockResourceFactory).buildResource(Country.class, COUNTRY_1_IN_REVIEW.getId());
    }

    @Test
    void getResource_returnsResource_whenResourceIsPublicAndUserIsNotSignedIn() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_PUBLIC.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        assertThat(resourcesDao.getResource(COUNTRY_1_PUBLIC.getId(), Country.class),
                is(equalTo(Optional.of(COUNTRY_1_PUBLIC))));
        verify(mockResourceFactory).buildResource(Country.class, COUNTRY_1_PUBLIC.getId());
    }

    @Test
    void getResource_returnsResource_whenResourceIsPublicAndUserIsNotAdministrator() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_PUBLIC.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        assertThat(resourcesDao.getResource(COUNTRY_1_PUBLIC.getId(), Country.class, USER_DATA_NOT_ADMINISTRATOR),
                is(equalTo(Optional.of(COUNTRY_1_PUBLIC))));
        verify(mockResourceFactory).buildResource(Country.class, COUNTRY_1_PUBLIC.getId());
    }

    @Test
    void getResource_returnsResource_whenResourceIsInReviewAndUserIsAdministrator() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_IN_REVIEW.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_IN_REVIEW);
        assertThat(resourcesDao.getResource(COUNTRY_1_IN_REVIEW.getId(), Country.class, USER_DATA_ADMINISTRATOR),
                is(equalTo(Optional.of(COUNTRY_1_IN_REVIEW))));
        verify(mockResourceFactory).buildResource(Country.class, COUNTRY_1_IN_REVIEW.getId());
    }

    @Test
    void listResources_returnsEmptySet_whenThereAreNoResources() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(new Country());
        assertThat(resourcesDao.listResources(Country.class), is(empty()));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void listResources_returnsEmptySet_whenAllResourcesAreInReviewAndUserIsNotSignedIn() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(Country.builder()
                .state(State.PUBLIC)
                .build());
        resourcesDao.saveResource(COUNTRY_1_IN_REVIEW);
        resourcesDao.saveResource(COUNTRY_2_IN_REVIEW);
        assertThat(resourcesDao.listResources(Country.class), is(empty()));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void listResources_returnsEmptySet_whenAllResourcesAreInReviewAndUserIsNotAdministrator() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(Country.builder()
                .state(State.PUBLIC)
                .build());
        resourcesDao.saveResource(COUNTRY_1_IN_REVIEW);
        resourcesDao.saveResource(COUNTRY_2_IN_REVIEW);
        assertThat(resourcesDao.listResources(Country.class, USER_DATA_NOT_ADMINISTRATOR), is(empty()));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void listResources_returnsOnlyPublicResources_whenUserIsNotSignedIn() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(new Country());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        resourcesDao.saveResource(COUNTRY_2_IN_REVIEW);
        assertThat(resourcesDao.listResources(Country.class), containsInAnyOrder(COUNTRY_1_PUBLIC));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void listResources_returnsOnlyPublicResources_whenUserIsNotAdministrator() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(new Country());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        resourcesDao.saveResource(COUNTRY_2_IN_REVIEW);
        assertThat(resourcesDao.listResources(Country.class, USER_DATA_NOT_ADMINISTRATOR),
                containsInAnyOrder(COUNTRY_1_PUBLIC));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void listResources_returnsAllResources_whenUserIsAdministrator() {
        when(mockResourceFactory.buildResource(Country.class)).thenReturn(new Country());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        resourcesDao.saveResource(COUNTRY_2_IN_REVIEW);
        assertThat(resourcesDao.listResources(Country.class, USER_DATA_ADMINISTRATOR),
                containsInAnyOrder(COUNTRY_1_PUBLIC, COUNTRY_2_IN_REVIEW));
        verify(mockResourceFactory).buildResource(Country.class);
    }

    @Test
    void saveResource_savesResource() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_PUBLIC.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        assertThat(resourcesDao.getResource(COUNTRY_1_PUBLIC.getId(), Country.class),
                is(equalTo(Optional.of(COUNTRY_1_PUBLIC))));
    }

    @Test
    void deleteResource_doesNothing_whenResourceDoesNotExist() {
        String id = "does-not-exist";
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(id)
                .build());
        resourcesDao.deleteResource(id, Country.class);
        verify(mockResourceFactory).buildResource(Country.class, id);
    }

    @Test
    void deleteResource_deletesResources_whenResourceExists() {
        when(mockResourceFactory.buildResource(eq(Country.class), any(String.class))).thenReturn(Country.builder()
                .id(COUNTRY_1_PUBLIC.getId())
                .build());
        resourcesDao.saveResource(COUNTRY_1_PUBLIC);
        resourcesDao.deleteResource(COUNTRY_1_PUBLIC.getId(), Country.class);
        assertThat(resourcesDao.getResource(COUNTRY_1_PUBLIC.getId(), Country.class), is(equalTo(Optional.empty())));
        verify(mockResourceFactory, times(2)).buildResource(Country.class, COUNTRY_1_PUBLIC.getId());
    }

    private static GlobalSecondaryIndex buildBaseGlobalSecondaryIndex() {
        return new GlobalSecondaryIndex().withProvisionedThroughput(PROVISIONED_THROUGHPUT)
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
    }

    private static UserData buildUserData(final boolean isAdministrator) {
        return UserData.builder()
                .userId("king-koopa")
                .username("bowser")
                .email("bowser@mushroom-kingdom.com")
                .isEmailVerified(true)
                .isAdministrator(isAdministrator)
                .build();
    }

    private static Country buildCountry1(final State state) {
        return Country.builder()
                .id("id-1")
                .name("name-1")
                .state(state)
                .build();
    }
}

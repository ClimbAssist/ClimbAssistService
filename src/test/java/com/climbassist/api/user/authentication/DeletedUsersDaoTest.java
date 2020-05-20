package com.climbassist.api.user.authentication;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.common.AbstractResourceWithoutParentDaoTest;
import com.climbassist.api.user.UserData;
import lombok.Getter;
import org.mockito.Mock;

class DeletedUsersDaoTest extends AbstractResourceWithoutParentDaoTest<UserData, DeletedUsersDao> {

    private static final DynamoDBMapperConfig DYNAMO_DB_MAPPER_CONFIG = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride("DeletedUsers"))
            .build();
    private static final UserData USER_DATA_1 = UserData.builder()
            .userId("user-1")
            .username("username-1")
            .email("email-1@test.com")
            .isEmailVerified(false)
            .isAdministrator(false)
            .expirationTime(420L)
            .build();
    private static final UserData USER_DATA_2 = UserData.builder()
            .userId("user-2")
            .username("username-2")
            .email("email-2@test.com")
            .isEmailVerified(true)
            .isAdministrator(true)
            .expirationTime(69L)
            .build();

    @Getter
    @Mock
    private DynamoDBMapper mockDynamoDbMapper;

    @Override
    protected DeletedUsersDao buildResourceDao() {
        return DeletedUsersDao.builder()
                .dynamoDBMapper(mockDynamoDbMapper)
                .dynamoDBMapperConfig(DYNAMO_DB_MAPPER_CONFIG)
                .build();
    }

    @Override
    protected DynamoDBMapperConfig getDynamoDbMapperConfig() {
        return DYNAMO_DB_MAPPER_CONFIG;
    }

    @Override
    protected UserData getTestResource1() {
        return USER_DATA_1;
    }

    @Override
    protected UserData getTestResource2() {
        return USER_DATA_2;
    }

    @Override
    protected Class<UserData> getTestResourceClass() {
        return UserData.class;
    }

    @Override
    protected UserData buildResourceForDeletion(String resourceId) {
        return UserData.builder()
                .userId(resourceId)
                .build();
    }
}

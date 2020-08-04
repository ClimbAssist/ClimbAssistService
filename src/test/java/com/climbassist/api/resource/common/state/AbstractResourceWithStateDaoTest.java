package com.climbassist.api.resource.common.state;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.climbassist.api.resource.common.AbstractResourceWithParentDaoTest;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
// @formatter:off
public abstract class AbstractResourceWithStateDaoTest<Resource extends ResourceWithState & ResourceWithParent<ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>, ResourceDao extends ResourceWithStateDao<Resource,
        ParentResource>> extends AbstractResourceWithParentDaoTest<Resource, ParentResource, ResourceDao> {
// @formatter:on

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<UserData> MAYBE_USER_DATA_ADMINISTRATOR = Optional.of(UserData.builder()
            .userId("mithrandir")
            .username("gandalf-the-grey")
            .email("gandalf@middleeath.com")
            .isEmailVerified(true)
            .isAdministrator(true)
            .build());

    @Test
    void getResource_returnsResourceFromTable_whenResourceIsInReviewAndUserIsAdministrator() {
        runGetResourceTest(MAYBE_USER_DATA_ADMINISTRATOR, getTestResourceInReview(),
                Optional.of(getTestResourceInReview()));
    }

    @Test
    void getResource_returnsResourceFromTable_whenResourceIsPublicAndUserIsAdministrator() {
        runGetResourceTest(MAYBE_USER_DATA_ADMINISTRATOR, getTestResource1(), Optional.of(getTestResource1()));
    }

    @Test
    void getResource_returnsEmpty_whenResourceIsInReviewAndUserIsNotAdministrator() {
        runGetResourceTest(MAYBE_USER_DATA, getTestResourceInReview(), Optional.empty());
    }

    @Test
    void getResource_returnsEmpty_whenResourceIsInReviewAndUserDataIsNotPresent() {
        runGetResourceTest(Optional.empty(), getTestResourceInReview(), Optional.empty());
    }

    @Test
    void getResource_returnsResourceFromTable_whenResourceIsPublicAndUserDataIsNotPresent() {
        runGetResourceTest(Optional.empty(), getTestResource1(), Optional.of(getTestResource1()));
    }

    @Test
    void getResource_returnsResourceFromTable_whenResourceIsPublicAndUserIsNotAdministrator() {
        runGetResourceTest(MAYBE_USER_DATA, getTestResource1(), Optional.of(getTestResource1()));
    }

    @Override
    @Test
    protected void getResource_returnsEmpty_whenResourceDoesNotExist() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(null);
        assertThat(resourceDao.getResource(getTestResource1().getId(), MAYBE_USER_DATA), is(equalTo(Optional.empty())));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getTestResource1().getId(),
                getDynamoDbMapperConfig());
    }

    @Override
    @Test
    protected void getResources_returnsResourcesFromDynamoDb_whenResourcesExist() {
        runGetResourcesTest(ImmutableSet.of(getTestResource1(), getTestResource2(), getTestResourceInReview()),
                MAYBE_USER_DATA_ADMINISTRATOR);
    }

    @Override
    @Test
    protected void getResources_returnsEmptySet_whenResourcesDoNotExist() {
        runGetResourcesTest(ImmutableSet.of(), MAYBE_USER_DATA_ADMINISTRATOR);
    }

    @Test
    void getResources_returnsOnlyPublicResources_whenSomeResourcesAreInReviewAndUserIsNotAdministrator() {
        runGetResourcesNotAdministratorTest(MAYBE_USER_DATA);
    }

    @Test
    void getResources_returnsOnlyPublicResources_whenSomeResourcesAreInReviewAndUserDataIsNotPresent() {
        runGetResourcesNotAdministratorTest(Optional.empty());
    }

    @Test
    protected abstract Resource getTestResourceInReview();

    private void runGetResourceTest(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UserData> maybeUserData,
            Resource testResource,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Resource> expectedResource) {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(testResource);
        assertThat(resourceDao.getResource(testResource.getId(), maybeUserData), is(equalTo(expectedResource)));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), testResource.getId(), getDynamoDbMapperConfig());
    }

    private void runGetResourcesNotAdministratorTest(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UserData> maybeUserData) {
        DynamoDBQueryExpression<Resource> expectedDynamoDbQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(
                        buildIndexHashKey(getTestResource1().getParentId()))
                        .withIndexName(getIndexName());

        Set<Resource> resources = ImmutableSet.of(getTestResource1(), getTestResource2());

        when(mockPaginatedQueryList.iterator()).thenReturn(resources.iterator());
        when(getMockDynamoDbMapper().query(eq(getTestResourceClass()), any(), any())).thenReturn(
                mockPaginatedQueryList);
        assertThat(resourceDao.getResources(getTestResource1().getParentId(), maybeUserData), is(equalTo(resources)));

        verify(getMockDynamoDbMapper()).query(eq(getTestResourceClass()),
                dynamoDbQueryExpressionArgumentCaptor.capture(), eq(getDynamoDbMapperConfig()));
        DynamoDBQueryExpression<Resource> actualDynamoDbQueryExpression =
                dynamoDbQueryExpressionArgumentCaptor.getValue();
        assertThat(actualDynamoDbQueryExpression.getHashKeyValues(),
                is(equalTo(expectedDynamoDbQueryExpression.getHashKeyValues())));
        assertThat(actualDynamoDbQueryExpression.getIndexName(),
                is(equalTo(expectedDynamoDbQueryExpression.getIndexName())));
        assertThat(actualDynamoDbQueryExpression.getQueryFilter(), is(equalTo(ImmutableMap.of("state",
                new Condition().withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(State.PUBLIC.toString()))))));
    }

}

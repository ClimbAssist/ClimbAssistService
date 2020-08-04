package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
public abstract class AbstractResourceWithParentDaoTest<Resource extends ResourceWithParent<ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>,
        ResourceDao extends ResourceWithParentDao<Resource, ParentResource>>
        extends AbstractResourceDaoTest<Resource, ResourceDao> {

    @Mock
    protected PaginatedQueryList<Resource> mockPaginatedQueryList;
    @Captor
    protected ArgumentCaptor<DynamoDBQueryExpression<Resource>> dynamoDbQueryExpressionArgumentCaptor;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() throws NoSuchMethodException {
        super.parametersMarkedWithNonNull_throwNullPointerException_forNullValues();
        NullPointerTester nullPointerTester = new NullPointerTester();
        // have to call these methods out explicitly because they are in a superclass in a different package than the
        // subclass
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("getResources", String.class, Optional.class));
    }

    @Test
    protected void getResource_returnsResourceFromTable() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(getTestResource1());
        assertThat(resourceDao.getResource(getTestResource1().getId(), MAYBE_USER_DATA),
                is(equalTo(Optional.of(getTestResource1()))));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getTestResource1().getId(),
                getDynamoDbMapperConfig());
    }

    @Test
    protected void getResource_returnsEmpty_whenResourceDoesNotExist() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(null);
        assertThat(resourceDao.getResource(getTestResource1().getId(), MAYBE_USER_DATA), is(equalTo(Optional.empty())));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getTestResource1().getId(),
                getDynamoDbMapperConfig());
    }

    @Test
    protected void getResources_returnsResourcesFromDynamoDb_whenResourcesExist() {
        runGetResourcesTest(ImmutableSet.of(getTestResource1(), getTestResource2()), MAYBE_USER_DATA);
    }

    @Test
    protected void getResources_returnsEmptySet_whenResourcesDoNotExist() {
        runGetResourcesTest(ImmutableSet.of(), MAYBE_USER_DATA);
    }

    @Test
    void saveResource_savesResource() {
        resourceDao.saveResource(getTestResource1());
        verify(getMockDynamoDbMapper()).save(getTestResource1(), getDynamoDbMapperConfig());
    }

    @Test
    void deleteResource_deletesResource() {
        resourceDao.deleteResource(getTestResource1().getId());
        verify(getMockDynamoDbMapper()).delete(buildResourceForDeletion(getTestResource1().getId()),
                getDynamoDbMapperConfig());
    }

    protected void runGetResourcesTest(@NonNull Set<Resource> resources,
                                       @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                       @NonNull Optional<UserData> maybeUserData) {
        DynamoDBQueryExpression<Resource> expectedDynamoDbQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(
                        buildIndexHashKey(getTestResource1().getParentId()))
                        .withIndexName(getIndexName());

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
    }

    protected abstract String getIndexName();

    protected abstract Resource buildIndexHashKey(String parentId);
}

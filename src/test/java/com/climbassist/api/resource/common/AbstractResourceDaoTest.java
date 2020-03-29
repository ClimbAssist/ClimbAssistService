package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
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
public abstract class AbstractResourceDaoTest<Resource> {

    protected ResourceDao<Resource> resourceDao;
    @Mock
    private PaginatedQueryList<Object> mockPaginatedQueryList;
    @Captor
    private ArgumentCaptor<DynamoDBQueryExpression<Resource>> dynamoDbQueryExpressionArgumentCaptor;

    @BeforeEach
    void setUp() {
        resourceDao = buildResourceDao();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() throws NoSuchMethodException {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceDao, NullPointerTester.Visibility.PACKAGE);
        // have to call these methods out explicitly because they are in a superclass in a different package than the
        // subclass
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("getResource", String.class));
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("getResources", String.class));
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("saveResource", Object.class));
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("deleteResource", String.class));
    }

    @Test
    void getResource_returnsResourceFromTable() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(getTestResource1());
        assertThat(resourceDao.getResource(getIdFromTestResource(getTestResource1())),
                is(equalTo(Optional.of(getTestResource1()))));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getIdFromTestResource(getTestResource1()),
                getDynamoDbMapperConfig());
    }

    @Test
    void getResource_returnsEmpty_whenResourceDoesNotExist() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(null);
        assertThat(resourceDao.getResource(getIdFromTestResource(getTestResource1())), is(equalTo(Optional.empty())));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getIdFromTestResource(getTestResource1()),
                getDynamoDbMapperConfig());
    }

    @Test
    protected void getResources_returnsResourcesFromDynamoDb_whenResourcesExist() {
        runGetResourcesTest(ImmutableSet.of(getTestResource1(), getTestResource2()));
    }

    @Test
    protected void getResources_returnsEmptySet_whenResourcesDoNotExist() {
        runGetResourcesTest(ImmutableSet.of());
    }

    @Test
    void saveResource_savesResource() {
        resourceDao.saveResource(getTestResource1());
        verify(getMockDynamoDbMapper()).save(getTestResource1(), getDynamoDbMapperConfig());
    }

    @Test
    void deleteResource_deletesResource() {
        resourceDao.deleteResource(getIdFromTestResource(getTestResource1()));
        verify(getMockDynamoDbMapper()).delete(buildResourceForDeletion(getIdFromTestResource(getTestResource1())),
                getDynamoDbMapperConfig());
    }

    private void runGetResourcesTest(Set<Object> resources) {
        DynamoDBQueryExpression<Resource> expectedDynamoDbQueryExpression =
                new DynamoDBQueryExpression<Resource>().withHashKeyValues(
                        buildIndexHashKey(getParentIdFromTestResource(getTestResource1())))
                        .withIndexName(getIndexName());

        when(mockPaginatedQueryList.iterator()).thenReturn(resources.iterator());
        when(getMockDynamoDbMapper().query(any(), any(), any())).thenReturn(mockPaginatedQueryList);
        assertThat(resourceDao.getResources(getParentIdFromTestResource(getTestResource1())), is(equalTo(resources)));

        verify(getMockDynamoDbMapper()).query(eq(getTestResourceClass()),
                dynamoDbQueryExpressionArgumentCaptor.capture(), eq(getDynamoDbMapperConfig()));
        DynamoDBQueryExpression<Resource> actualDynamoDbQueryExpression =
                dynamoDbQueryExpressionArgumentCaptor.getValue();
        assertThat(actualDynamoDbQueryExpression.getHashKeyValues(),
                is(equalTo(expectedDynamoDbQueryExpression.getHashKeyValues())));
        assertThat(actualDynamoDbQueryExpression.getIndexName(),
                is(equalTo(expectedDynamoDbQueryExpression.getIndexName())));
    }

    protected abstract ResourceDao<Resource> buildResourceDao();

    protected abstract DynamoDBMapperConfig getDynamoDbMapperConfig();

    protected abstract String getIndexName();

    protected abstract Resource getTestResource1();

    protected abstract Resource getTestResource2();

    protected abstract String getIdFromTestResource(Resource resource);

    protected abstract String getParentIdFromTestResource(Resource resource);

    protected abstract Class<Resource> getTestResourceClass();

    protected abstract Resource buildIndexHashKey(String parentId);

    protected abstract DynamoDBMapper getMockDynamoDbMapper();

    protected abstract Resource buildResourceForDeletion(String resourceId);

}
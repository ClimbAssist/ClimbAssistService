package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @formatter:off
public abstract class AbstractResourceWithoutParentDaoTest<
        Resource extends com.climbassist.api.resource.common.Resource,
        ResourceDao extends ResourceWithoutParentDao<Resource>>
        extends AbstractResourceDaoTest<Resource, ResourceDao> {
// @formatter:on

    @Mock
    private PaginatedScanList<Resource> mockPaginatedScanList;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() throws NoSuchMethodException {
        super.parametersMarkedWithNonNull_throwNullPointerException_forNullValues();
        NullPointerTester nullPointerTester = new NullPointerTester();
        // have to call these methods out explicitly because they are in a superclass in a different package than the
        // subclass
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("getResources"));
    }

    @Test
    protected void getResources_returnsResourcesFromDynamoDb_whenResourcesExist() {
        runGetResourcesTest(ImmutableSet.of(getTestResource1(), getTestResource2()));
    }

    @Test
    protected void getResources_returnsEmptySet_whenResourcesDoNotExist() {
        runGetResourcesTest(ImmutableSet.of());
    }

    private void runGetResourcesTest(Set<Resource> resources) {
        when(getMockDynamoDbMapper().scan(eq(getTestResourceClass()), any(), any())).thenReturn(mockPaginatedScanList);
        when(mockPaginatedScanList.iterator()).thenReturn(resources.iterator());
        assertThat(resourceDao.getResources(), is(equalTo(resources)));
        verify(getMockDynamoDbMapper()).scan(eq(getTestResourceClass()), any(DynamoDBScanExpression.class),
                eq(getDynamoDbMapperConfig()));
        verify(mockPaginatedScanList).iterator();
    }

}

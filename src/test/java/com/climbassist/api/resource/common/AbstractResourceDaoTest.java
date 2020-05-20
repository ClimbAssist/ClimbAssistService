package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractResourceDaoTest<Resource extends com.climbassist.api.resource.common.Resource,
        ResourceDao extends com.climbassist.api.resource.common.ResourceDao<Resource>> {

    protected ResourceDao resourceDao;

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
                .getMethod("saveResource", com.climbassist.api.resource.common.Resource.class));
        nullPointerTester.testMethod(resourceDao, resourceDao.getClass()
                .getMethod("deleteResource", String.class));
    }

    @Test
    void getResource_returnsResourceFromTable() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(getTestResource1());
        assertThat(resourceDao.getResource(getTestResource1().getId()), is(equalTo(Optional.of(getTestResource1()))));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getTestResource1().getId(),
                getDynamoDbMapperConfig());
    }

    @Test
    void getResource_returnsEmpty_whenResourceDoesNotExist() {
        when(getMockDynamoDbMapper().load(any(), any(), any())).thenReturn(null);
        assertThat(resourceDao.getResource(getTestResource1().getId()), is(equalTo(Optional.empty())));
        verify(getMockDynamoDbMapper()).load(getTestResourceClass(), getTestResource1().getId(),
                getDynamoDbMapperConfig());
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

    protected abstract ResourceDao buildResourceDao();

    protected abstract DynamoDBMapperConfig getDynamoDbMapperConfig();

    protected abstract Resource getTestResource1();

    protected abstract Resource getTestResource2();

    protected abstract Class<Resource> getTestResourceClass();

    protected abstract DynamoDBMapper getMockDynamoDbMapper();

    protected abstract Resource buildResourceForDeletion(String resourceId);

}

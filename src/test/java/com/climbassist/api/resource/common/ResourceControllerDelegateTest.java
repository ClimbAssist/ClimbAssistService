package com.climbassist.api.resource.common;

import com.climbassist.api.user.UserData;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceControllerDelegateTest {

    @Builder
    @Value
    private static class ResourceImpl implements Resource {

        String id;
        String name;
    }

    @Builder
    private static final class NewResourceImpl implements NewResource<ResourceImpl> {

        private final String name;
    }

    private static final class ResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        private ResourceNotFoundExceptionImpl(String resourceId) {
            super("resource-impl", resourceId);
        }
    }

    @Builder
    @Getter
    private static final class CreateResourceResultImpl implements CreateResourceResult<ResourceImpl> {

        private final String resourceId;
    }

    private static final ResourceImpl RESOURCE = ResourceImpl.builder()
            .id("id")
            .name("name")
            .build();
    private static final ResourceImpl UPDATED_RESOURCE = ResourceImpl.builder()
            .id(RESOURCE.getId())
            .name("new name")
            .build();
    private static final ResourceNotFoundExceptionImpl RESOURCE_NOT_FOUND_EXCEPTION = new ResourceNotFoundExceptionImpl(
            RESOURCE.getId());
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceDao<ResourceImpl> mockResourceDao;
    @Mock
    private ResourceFactory<ResourceImpl, NewResourceImpl> mockResourceFactory;
    @Mock
    private ResourceNotFoundExceptionFactory<ResourceImpl> mockResourceNotFoundExceptionFactory;
    @Mock
    private CreateResourceResultFactory<ResourceImpl> mockCreateResourceResultFactory;

    private ResourceControllerDelegate<ResourceImpl, NewResourceImpl> resourceControllerDelegate;

    @BeforeEach
    void setUp() {
        resourceControllerDelegate = ResourceControllerDelegate.<ResourceImpl, NewResourceImpl>builder().resourceDao(
                mockResourceDao)
                .resourceFactory(mockResourceFactory)
                .resourceNotFoundExceptionFactory(mockResourceNotFoundExceptionFactory)
                .createResourceResultFactory(mockCreateResourceResultFactory)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceControllerDelegate, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_returnsResource_whenResourceExists() throws ResourceNotFoundException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.of(RESOURCE));
        assertThat(resourceControllerDelegate.getResource(RESOURCE.getId(), MAYBE_USER_DATA), is(equalTo(RESOURCE)));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
    }

    @Test
    void getResource_throwsResourceNotFoundException_whenResourceDoesNotExist() throws ResourceNotFoundException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockResourceNotFoundExceptionFactory.create(any())).thenReturn(RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> resourceControllerDelegate.getResource(RESOURCE.getId(), MAYBE_USER_DATA));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockResourceNotFoundExceptionFactory).create(RESOURCE.getId());
    }

    @Test
    void createResource_createsResource() {
        NewResourceImpl newResource = NewResourceImpl.builder()
                .name(RESOURCE.getName())
                .build();
        CreateResourceResultImpl createResourceResult = CreateResourceResultImpl.builder()
                .resourceId(RESOURCE.getId())
                .build();
        when(mockResourceFactory.create(any())).thenReturn(RESOURCE);
        when(mockCreateResourceResultFactory.create(any())).thenReturn(createResourceResult);
        assertThat(resourceControllerDelegate.createResource(newResource), is(equalTo(createResourceResult)));
        verify(mockResourceFactory).create(newResource);
        verify(mockCreateResourceResultFactory).create(RESOURCE.getId());
    }

    @Test
    void updateResource_updatesResource_whenResourceExists() throws ResourceNotFoundException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.of(RESOURCE));
        assertThat(resourceControllerDelegate.updateResource(UPDATED_RESOURCE, MAYBE_USER_DATA), is(equalTo(
                UpdateResourceResult.builder()
                        .successful(true)
                        .build())));
        verify(mockResourceDao).getResource(UPDATED_RESOURCE.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao).saveResource(UPDATED_RESOURCE);
    }

    @Test
    void updateResource_throwsResourceNotFoundException_whenResourceDoesNotExist() {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockResourceNotFoundExceptionFactory.create(any())).thenReturn(RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> resourceControllerDelegate.updateResource(UPDATED_RESOURCE, MAYBE_USER_DATA));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockResourceNotFoundExceptionFactory).create(RESOURCE.getId());
        verify(mockResourceDao, never()).saveResource(any());
    }

    @Test
    void deleteResource_deletesResource_whenResourceExists() throws ResourceNotFoundException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.of(RESOURCE));
        assertThat(resourceControllerDelegate.deleteResource(RESOURCE.getId(), MAYBE_USER_DATA), is(equalTo(
                DeleteResourceResult.builder()
                        .successful(true)
                        .build())));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao).deleteResource(RESOURCE.getId());
    }

    @Test
    void deleteResource_throwsResourceNotFoundException_whenResourceDoesNotExist() {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockResourceNotFoundExceptionFactory.create(any())).thenReturn(RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> resourceControllerDelegate.deleteResource(RESOURCE.getId(), MAYBE_USER_DATA));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockResourceNotFoundExceptionFactory).create(RESOURCE.getId());
        verify(mockResourceDao, never()).deleteResource(any());
    }
}

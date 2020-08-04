package com.climbassist.api.resource.common;

import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceWithParentControllerDelegateTest {

    @Builder
    @Value
    private static class ResourceImpl implements ResourceWithParent<ParentResourceImpl> {

        String id;
        String parentId;
        String name;
    }

    @Builder
    @Value
    private static class NewResourceImpl implements NewResourceWithParent<ResourceImpl, ParentResourceImpl> {

        String name;
        String parentId;
    }

    @Builder
    @Data
    private static final class ParentResourceImpl implements ResourceWithChildren<ParentResourceImpl> {

        private String id;
        private String name;

        @Override
        public <ChildResource extends ResourceWithParent<ParentResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            // unused
        }
    }

    private static final class ParentResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        private ParentResourceNotFoundExceptionImpl(String resourceId) {
            super("resource-impl", resourceId);
        }
    }

    @Builder
    @Getter
    private static final class CreateResourceResultImpl implements CreateResourceResult<ResourceImpl> {

        private final String resourceId;
    }

    private static final ParentResourceImpl PARENT_RESOURCE_1 = ParentResourceImpl.builder()
            .id("parentId1")
            .name("parent name 1")
            .build();
    private static final ParentResourceImpl PARENT_RESOURCE_2 = ParentResourceImpl.builder()
            .id("parentId2")
            .name("parent name 2")
            .build();
    private static final ResourceImpl RESOURCE_1 = ResourceImpl.builder()
            .id("id1")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("name 1")
            .build();
    private static final ResourceImpl RESOURCE_2 = ResourceImpl.builder()
            .id("id2")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("name 2")
            .build();
    private static final NewResourceImpl NEW_RESOURCE_1 = NewResourceImpl.builder()
            .name(RESOURCE_1.getName())
            .parentId(PARENT_RESOURCE_1.getId())
            .build();
    private static final ResourceImpl UPDATED_RESOURCE_1 = ResourceImpl.builder()
            .id(RESOURCE_1.getId())
            .parentId(PARENT_RESOURCE_2.getId())
            .name("new name")
            .build();
    private static final ParentResourceNotFoundExceptionImpl PARENT_RESOURCE_NOT_FOUND_EXCEPTION =
            new ParentResourceNotFoundExceptionImpl(PARENT_RESOURCE_1.getId());
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceWithParentDao<ResourceImpl, ParentResourceImpl> mockResourceDao;
    @Mock
    private ResourceDao<ParentResourceImpl> mockParentResourceDao;
    @Mock
    private ResourceNotFoundExceptionFactory<ParentResourceImpl> mockParentResourceNotFoundExceptionFactory;
    @Mock
    private ResourceControllerDelegate<ResourceImpl, NewResourceImpl> mockResourceControllerDelegate;

    private ResourceWithParentControllerDelegate<ResourceImpl, NewResourceImpl, ParentResourceImpl>
            resourceWithParentControllerDelegate;

    @BeforeEach
    void setUp() {
        resourceWithParentControllerDelegate =
                ResourceWithParentControllerDelegate.<ResourceImpl, NewResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .parentResourceDao(mockParentResourceDao)
                        .parentResourceNotFoundExceptionFactory(mockParentResourceNotFoundExceptionFactory)
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceWithParentControllerDelegate,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResourcesForParent_returnsResources_whenParentExists() throws ResourceNotFoundException {
        Set<ResourceImpl> resources = ImmutableSet.of(RESOURCE_1, RESOURCE_2);
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        when(mockResourceDao.getResources(RESOURCE_1.getParentId(), MAYBE_USER_DATA)).thenReturn(resources);
        assertThat(
                resourceWithParentControllerDelegate.getResourcesForParent(RESOURCE_1.getParentId(), MAYBE_USER_DATA),
                is(equalTo(resources)));
        verify(mockParentResourceDao).getResource(RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        verify(mockResourceDao).getResources(RESOURCE_1.getParentId(), MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_throwsParentResourceNotFoundException_whenParentDoesNotExist() {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockParentResourceNotFoundExceptionFactory.create(any())).thenReturn(PARENT_RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ParentResourceNotFoundExceptionImpl.class,
                () -> resourceWithParentControllerDelegate.getResourcesForParent(RESOURCE_1.getParentId(),
                        MAYBE_USER_DATA));
        verify(mockParentResourceDao).getResource(RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockParentResourceNotFoundExceptionFactory).create(PARENT_RESOURCE_1.getId());
        verify(mockResourceDao, never()).getResources(any(), any());
    }

    @Test
    void createResource_callsResourceControllerDelegate_whenParentExists() throws ResourceNotFoundException {
        CreateResourceResultImpl createResourceResult = CreateResourceResultImpl.builder()
                .resourceId(RESOURCE_1.getId())
                .build();
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        when(mockResourceControllerDelegate.createResource(any())).thenReturn(createResourceResult);
        assertThat(resourceWithParentControllerDelegate.createResource(NEW_RESOURCE_1, MAYBE_USER_DATA),
                is(equalTo(createResourceResult)));
        verify(mockParentResourceDao).getResource(RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        verify(mockResourceControllerDelegate).createResource(NEW_RESOURCE_1);
    }

    @Test
    void createResource_throwsParentResourceNotFoundException_whenParentDoesNotExist() {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockParentResourceNotFoundExceptionFactory.create(any())).thenReturn(PARENT_RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ParentResourceNotFoundExceptionImpl.class,
                () -> resourceWithParentControllerDelegate.createResource(NEW_RESOURCE_1, MAYBE_USER_DATA));
        verify(mockParentResourceDao).getResource(RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockParentResourceNotFoundExceptionFactory).create(PARENT_RESOURCE_1.getId());
        verify(mockResourceControllerDelegate, never()).createResource(any());
    }

    @Test
    void updateResource_callsResourceControllerDelegate_whenParentExists() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_2));
        when(mockResourceControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(resourceWithParentControllerDelegate.updateResource(UPDATED_RESOURCE_1, MAYBE_USER_DATA),
                is(equalTo(updateResourceResult)));
        verify(mockParentResourceDao).getResource(UPDATED_RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        verify(mockResourceControllerDelegate).updateResource(UPDATED_RESOURCE_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_throwsParentResourceNotFoundException_whenParentDoesNotExist()
            throws ResourceNotFoundException {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockParentResourceNotFoundExceptionFactory.create(any())).thenReturn(PARENT_RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ParentResourceNotFoundExceptionImpl.class,
                () -> resourceWithParentControllerDelegate.updateResource(UPDATED_RESOURCE_1, MAYBE_USER_DATA));
        verify(mockParentResourceDao).getResource(UPDATED_RESOURCE_1.getParentId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockParentResourceNotFoundExceptionFactory).create(UPDATED_RESOURCE_1.getParentId());
        verify(mockResourceControllerDelegate, never()).updateResource(any(), any());
    }
}

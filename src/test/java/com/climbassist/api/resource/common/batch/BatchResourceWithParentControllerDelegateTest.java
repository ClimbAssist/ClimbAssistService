package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.NewResource;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchResourceWithParentControllerDelegateTest {

    @Builder
    @Value
    private static class ResourceImpl implements OrderableResourceWithParent<ResourceImpl, ParentResourceImpl> {

        String id;
        String parentId;
        String name;
        boolean first;
        String next;
    }

    @Builder
    @Value
    private static class NewResourceImpl implements NewResource<ResourceImpl> {

        String parentId;
        String name;
        boolean first;
        String next;
    }

    @Builder
    @Value
    private static class ParentResourceImpl implements ResourceWithChildren<ParentResourceImpl> {

        String id;

        @Override
        public <ChildResource extends ResourceWithParent<ParentResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            // unused
        }
    }

    @Builder
    @Value
    private static class BatchNewResourceImpl implements BatchNewResource<ResourceImpl, ParentResourceImpl> {

        String name;
    }

    @Builder
    @Value
    private static class BatchNewResourcesImpl
            implements BatchNewResources<ResourceImpl, ParentResourceImpl, BatchNewResourceImpl> {

        List<BatchNewResourceImpl> batchNewResources;
    }

    @Builder
    @Value
    private static class CreateResourceResultImpl implements CreateResourceResult<ResourceImpl> {

        String resourceId;
    }

    @Builder
    @Value
    private static class BatchCreateResourcesResultImpl
            implements BatchCreateResourcesResult<ResourceImpl, ParentResourceImpl> {

        List<String> resourceIds;
    }

    private static class ParentResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        public ParentResourceNotFoundExceptionImpl(String resourceId) {
            super(ParentResourceImpl.class.getSimpleName(), resourceId);
        }
    }

    private static class ResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        public ResourceNotFoundExceptionImpl(String resourceId) {
            super(ResourceImpl.class.getSimpleName(), resourceId);
        }
    }

    private static final ParentResourceImpl PARENT_RESOURCE_1 = ParentResourceImpl.builder()
            .id("1")
            .build();
    private static final ResourceImpl RESOURCE_3 = ResourceImpl.builder()
            .id("3")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("3")
            .first(false)
            .build();
    private static final ResourceImpl RESOURCE_2 = ResourceImpl.builder()
            .id("2")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("2")
            .first(false)
            .next(RESOURCE_3.getId())
            .build();
    private static final ResourceImpl RESOURCE_1 = ResourceImpl.builder()
            .id("1")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("1")
            .first(true)
            .next(RESOURCE_2.getId())
            .build();
    private static final NewResourceImpl NEW_RESOURCE_1 = NewResourceImpl.builder()
            .parentId(RESOURCE_1.getParentId())
            .name(RESOURCE_1.getName())
            .first(RESOURCE_1.isFirst())
            .next(RESOURCE_1.getNext())
            .build();
    private static final NewResourceImpl NEW_RESOURCE_2 = NewResourceImpl.builder()
            .parentId(RESOURCE_2.getParentId())
            .name(RESOURCE_2.getName())
            .first(RESOURCE_2.isFirst())
            .next(RESOURCE_2.getNext())
            .build();
    private static final NewResourceImpl NEW_RESOURCE_3 = NewResourceImpl.builder()
            .parentId(RESOURCE_3.getParentId())
            .name(RESOURCE_3.getName())
            .first(RESOURCE_3.isFirst())
            .next(RESOURCE_3.getNext())
            .build();
    private static final BatchNewResourceImpl BATCH_NEW_RESOURCE_1 = BatchNewResourceImpl.builder()
            .name(RESOURCE_1.getName())
            .build();
    private static final BatchNewResourceImpl BATCH_NEW_RESOURCE_2 = BatchNewResourceImpl.builder()
            .name(RESOURCE_2.getName())
            .build();
    private static final BatchNewResourceImpl BATCH_NEW_RESOURCE_3 = BatchNewResourceImpl.builder()
            .name(RESOURCE_3.getName())
            .build();
    private static final BatchNewResourcesImpl BATCH_NEW_RESOURCES = BatchNewResourcesImpl.builder()
            .batchNewResources(ImmutableList.of(BATCH_NEW_RESOURCE_1, BATCH_NEW_RESOURCE_2, BATCH_NEW_RESOURCE_3))
            .build();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceControllerDelegate<ResourceImpl, NewResourceImpl> mockResourceControllerDelegate;
    @Mock
    private ResourceWithParentDao<ResourceImpl, ParentResourceImpl> mockResourceDao;
    @Mock
    private ResourceDao<ParentResourceImpl> mockParentResourceDao;
    @Mock
    private ResourceNotFoundExceptionFactory<ResourceImpl> mockResourceNotFoundExceptionFactory;
    @Mock
    private ResourceNotFoundExceptionFactory<ParentResourceImpl> mockParentResourceNotFoundExceptionFactory;
    @Mock
    private BatchResourceFactory<ResourceImpl, NewResourceImpl, ParentResourceImpl, BatchNewResourceImpl>
            mockBatchResourceFactory;
    @Mock
    private BatchCreateResourceResultFactory<ResourceImpl, ParentResourceImpl> mockBatchCreateResourceResultFactory;
    private BatchResourceWithParentControllerDelegate<ResourceImpl, NewResourceImpl, ParentResourceImpl,
            BatchNewResourceImpl>
            batchResourceWithParentControllerDelegate;

    @BeforeEach
    void setUp() {
        batchResourceWithParentControllerDelegate =
                BatchResourceWithParentControllerDelegate.<ResourceImpl, NewResourceImpl, ParentResourceImpl,
                        BatchNewResourceImpl>builder().resourceControllerDelegate(
                        mockResourceControllerDelegate)
                        .resourceDao(mockResourceDao)
                        .parentResourceDao(mockParentResourceDao)
                        .resourceNotFoundExceptionFactory(mockResourceNotFoundExceptionFactory)
                        .parentResourceNotFoundExceptionFactory(mockParentResourceNotFoundExceptionFactory)
                        .batchResourceFactory(mockBatchResourceFactory)
                        .batchCreateResourceResultFactory(mockBatchCreateResourceResultFactory)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(BatchNewResources.class, BATCH_NEW_RESOURCES);
        nullPointerTester.testInstanceMethods(batchResourceWithParentControllerDelegate,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void batchCreateResources_callsResourceControllerDelegateForEachNewResource() throws ResourceNotFoundException {
        BatchCreateResourcesResultImpl batchCreateResourcesResult = BatchCreateResourcesResultImpl.builder()
                .resourceIds(ImmutableList.of(RESOURCE_1.getId(), RESOURCE_2.getId(), RESOURCE_3.getId()))
                .build();
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        doReturn(NEW_RESOURCE_3).when(mockBatchResourceFactory)
                .create(anyString(), any(), anyBoolean());
        doReturn(NEW_RESOURCE_2, NEW_RESOURCE_1).when(mockBatchResourceFactory)
                .create(anyString(), any(), anyBoolean(), anyString());
        doReturn(CreateResourceResultImpl.builder()
                .resourceId(RESOURCE_3.getId())
                .build()).when(mockResourceControllerDelegate)
                .createResource(NEW_RESOURCE_3);
        doReturn(CreateResourceResultImpl.builder()
                .resourceId(RESOURCE_2.getId())
                .build()).when(mockResourceControllerDelegate)
                .createResource(NEW_RESOURCE_2);
        doReturn(CreateResourceResultImpl.builder()
                .resourceId(RESOURCE_1.getId())
                .build()).when(mockResourceControllerDelegate)
                .createResource(NEW_RESOURCE_1);
        when(mockBatchCreateResourceResultFactory.create(any())).thenReturn(batchCreateResourcesResult);
        assertThat(
                batchResourceWithParentControllerDelegate.batchCreateResources(RESOURCE_1.getId(), BATCH_NEW_RESOURCES,
                        MAYBE_USER_DATA), is(equalTo(batchCreateResourcesResult)));
        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockBatchResourceFactory).create(PARENT_RESOURCE_1.getId(), BATCH_NEW_RESOURCE_3, false);
        verify(mockResourceControllerDelegate).createResource(NEW_RESOURCE_3);
        verify(mockBatchResourceFactory).create(PARENT_RESOURCE_1.getId(), BATCH_NEW_RESOURCE_2, false,
                RESOURCE_3.getId());
        verify(mockResourceControllerDelegate).createResource(NEW_RESOURCE_2);
        verify(mockBatchResourceFactory).create(PARENT_RESOURCE_1.getId(), BATCH_NEW_RESOURCE_1, true,
                RESOURCE_2.getId());
        verify(mockResourceControllerDelegate).createResource(NEW_RESOURCE_1);
        verify(mockBatchCreateResourceResultFactory).create(batchCreateResourcesResult.getResourceIds());
    }

    @Test
    void batchCreateResources_callsResourceControllerDelegate_whenThereIsOnlyOneResource()
            throws ResourceNotFoundException {
        NewResourceImpl newResource = NewResourceImpl.builder()
                .parentId(PARENT_RESOURCE_1.getId())
                .first(true)
                .name(RESOURCE_1.getName())
                .build();
        List<String> resourceIds = ImmutableList.of(RESOURCE_1.getId());
        BatchCreateResourcesResultImpl batchCreateResourcesResult = BatchCreateResourcesResultImpl.builder()
                .resourceIds(ImmutableList.of(RESOURCE_1.getId()))
                .build();

        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        when(mockBatchResourceFactory.create(anyString(), any(), anyBoolean())).thenReturn(newResource);
        when(mockResourceControllerDelegate.createResource(any())).thenReturn(CreateResourceResultImpl.builder()
                .resourceId(RESOURCE_1.getId())
                .build());
        when(mockBatchCreateResourceResultFactory.create(any())).thenReturn(BatchCreateResourcesResultImpl.builder()
                .resourceIds(ImmutableList.of(RESOURCE_1.getId()))
                .build());

        assertThat(batchResourceWithParentControllerDelegate.batchCreateResources(PARENT_RESOURCE_1.getId(),
                BatchNewResourcesImpl.builder()
                        .batchNewResources(ImmutableList.of(BATCH_NEW_RESOURCE_1))
                        .build(), MAYBE_USER_DATA), is(equalTo(batchCreateResourcesResult)));

        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockBatchResourceFactory).create(PARENT_RESOURCE_1.getId(), BATCH_NEW_RESOURCE_1, true);
        verify(mockResourceControllerDelegate).createResource(newResource);
        verify(mockBatchCreateResourceResultFactory).create(resourceIds);
    }

    @Test
    void batchCreateResources_throwsParentResourceNotFoundException_whenParentResourceDoesNotExist() {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockParentResourceNotFoundExceptionFactory.create(any())).thenReturn(
                new ParentResourceNotFoundExceptionImpl(PARENT_RESOURCE_1.getId()));
        assertThrows(ParentResourceNotFoundExceptionImpl.class,
                () -> batchResourceWithParentControllerDelegate.batchCreateResources(PARENT_RESOURCE_1.getId(),
                        BATCH_NEW_RESOURCES, MAYBE_USER_DATA));
        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockParentResourceNotFoundExceptionFactory).create(PARENT_RESOURCE_1.getId());
        verify(mockResourceControllerDelegate, never()).createResource(any());
    }

    @Test
    void batchDeleteResources_throwsResourceNotFoundException_whenParentIdDoesNotExist() {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockParentResourceNotFoundExceptionFactory.create(any())).thenReturn(
                new ResourceNotFoundExceptionImpl(PARENT_RESOURCE_1.getId()));
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> batchResourceWithParentControllerDelegate.batchDeleteResources(PARENT_RESOURCE_1.getId(),
                        MAYBE_USER_DATA));
        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockParentResourceNotFoundExceptionFactory).create(PARENT_RESOURCE_1.getId());
    }

    @Test
    void batchDeleteResources_doesNothing_whenParentResourceHasNoChildren() throws ResourceNotFoundException {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        when(mockResourceDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        assertThat(batchResourceWithParentControllerDelegate.batchDeleteResources(PARENT_RESOURCE_1.getId(),
                MAYBE_USER_DATA), is(equalTo(DeleteResourceResult.builder()
                .successful(true)
                .build())));
        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao).getResources(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao, never()).deleteResource(any());
    }

    @Test
    void batchDeleteResources_deletesAllResourcesUnderParent() throws ResourceNotFoundException {
        when(mockParentResourceDao.getResource(any(), any())).thenReturn(Optional.of(PARENT_RESOURCE_1));
        when(mockResourceDao.getResources(any(), any())).thenReturn(
                ImmutableSet.of(RESOURCE_1, RESOURCE_2, RESOURCE_3));
        assertThat(batchResourceWithParentControllerDelegate.batchDeleteResources(PARENT_RESOURCE_1.getId(),
                MAYBE_USER_DATA), is(equalTo(DeleteResourceResult.builder()
                .successful(true)
                .build())));
        verify(mockParentResourceDao).getResource(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao).getResources(PARENT_RESOURCE_1.getId(), MAYBE_USER_DATA);
        verify(mockResourceDao).deleteResource(RESOURCE_1.getId());
        verify(mockResourceDao).deleteResource(RESOURCE_2.getId());
        verify(mockResourceDao).deleteResource(RESOURCE_3.getId());
    }

}

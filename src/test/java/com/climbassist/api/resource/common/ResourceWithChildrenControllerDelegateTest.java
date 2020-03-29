package com.climbassist.api.resource.common;

import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceWithChildrenControllerDelegateTest {

    @Builder
    @Data
    private static final class ResourceImpl implements ResourceWithChildren<ResourceImpl> {

        private String id;
        private String name;
        private Collection<ChildResourceImpl1> childResources1;
        private Collection<ChildResourceImpl2> childResources2;

        public <ChildResource extends ResourceWithParent<ResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            if (childResourceClass == ChildResourceImpl1.class) {
                //noinspection unchecked
                this.childResources1 = (Collection<ChildResourceImpl1>) childResources;
            }
            else if (childResourceClass == ChildResourceImpl2.class) {
                //noinspection unchecked
                this.childResources2 = (Collection<ChildResourceImpl2>) childResources;
            }
            else {
                throw new InvalidChildException(getClass(), childResourceClass);
            }
        }
    }

    @Builder
    private static final class NewResourceImpl implements NewResource<ResourceImpl> {

        private String name;
    }

    @Builder
    @Value
    private static final class ChildResourceImpl1 implements ResourceWithParent<ResourceImpl> {

        private String id;
        private String parentId;
    }

    @Builder
    @Value
    private static final class ChildResourceImpl2 implements ResourceWithParent<ResourceImpl> {

        private String id;
        private String parentId;
    }

    private static final class ResourceNotEmptyExceptionImpl extends ResourceNotEmptyException {

        private ResourceNotEmptyExceptionImpl(String resourceId) {
            super("resource-impl", resourceId);
        }
    }

    private static final ResourceImpl RESOURCE = ResourceImpl.builder()
            .id("id")
            .build();
    private static final Collection<ChildResourceImpl1> CHILD_RESOURCES_1 = ImmutableSet.of(ChildResourceImpl1.builder()
            .id("childId1")
            .parentId(RESOURCE.getId())
            .build(), ChildResourceImpl1.builder()
            .id("childId2")
            .parentId(RESOURCE.getId())
            .build());
    private static final Collection<ChildResourceImpl2> CHILD_RESOURCES_2 = ImmutableSet.of(ChildResourceImpl2.builder()
            .id("childId1")
            .parentId(RESOURCE.getId())
            .build(), ChildResourceImpl2.builder()
            .id("childId2")
            .parentId(RESOURCE.getId())
            .build());
    private static final ResourceImpl RESOURCE_WITH_CHILDREN_1 = ResourceImpl.builder()
            .id(RESOURCE.getId())
            .childResources1(CHILD_RESOURCES_1)
            .build();
    private static final ResourceImpl RESOURCE_WITH_CHILDREN_2 = ResourceImpl.builder()
            .id(RESOURCE.getId())
            .childResources1(CHILD_RESOURCES_1)
            .childResources2(CHILD_RESOURCES_2)
            .build();
    private static final int DEPTH = 5;
    @Mock
    private ResourceDao<ChildResourceImpl1> mockChildResourceDao1;
    @Mock
    private ResourceDao<ChildResourceImpl2> mockChildResourceDao2;
    @Mock
    private ResourceNotEmptyExceptionFactory<ResourceImpl> mockResourceNotEmptyExceptionFactory;
    @Mock
    private ResourceControllerDelegate<ResourceImpl, NewResourceImpl> mockResourceControllerDelegate;
    @Mock
    private RecursiveResourceRetriever<ChildResourceImpl1, ResourceImpl> mockRecursiveResourceRetriever1;
    @Mock
    private RecursiveResourceRetriever<ChildResourceImpl2, ResourceImpl> mockRecursiveResourceRetriever2;

    private ResourceWithChildrenControllerDelegate<ResourceImpl, NewResourceImpl>
            resourceWithChildrenControllerDelegate;

    @BeforeEach
    void setUp() {
        resourceWithChildrenControllerDelegate =
                ResourceWithChildrenControllerDelegate.<ResourceImpl, NewResourceImpl>builder().childResourceDaos(
                        ImmutableSet.of(mockChildResourceDao1))
                        .resourceNotEmptyExceptionFactory(mockResourceNotEmptyExceptionFactory)
                        .recursiveResourceRetrievers(ImmutableSet.of(mockRecursiveResourceRetriever1))
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceWithChildrenControllerDelegate,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceControllerDelegateAndGetsChildrenFromRecursiveResourceRetriever_whenDepthIsGreaterThanZeroAndThereIsOneRecursiveResourceRetriever()
            throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(RESOURCE);
        when(mockRecursiveResourceRetriever1.getChildrenRecursively(any(), anyInt())).thenReturn(CHILD_RESOURCES_1);
        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        assertThat(resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), DEPTH),
                is(equalTo(RESOURCE_WITH_CHILDREN_1)));
        verify(mockResourceControllerDelegate).getResource(RESOURCE.getId());
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE.getId(), DEPTH);
        verify(mockRecursiveResourceRetriever1).getChildClass();
    }

    @Test
    void getResource_callsResourceControllerDelegateAndGetsChildrenFromBothRecursiveResourceRetrievers_whenDepthIsGreaterThanZeroAndThereAreTwoRecursiveResourceRetrievers()
            throws ResourceNotFoundException {
        resourceWithChildrenControllerDelegate =
                ResourceWithChildrenControllerDelegate.<ResourceImpl, NewResourceImpl>builder().childResourceDaos(
                        ImmutableSet.of(mockChildResourceDao1))
                        .resourceNotEmptyExceptionFactory(mockResourceNotEmptyExceptionFactory)
                        .recursiveResourceRetrievers(
                                ImmutableSet.of(mockRecursiveResourceRetriever1, mockRecursiveResourceRetriever2))
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(RESOURCE);
        when(mockRecursiveResourceRetriever1.getChildrenRecursively(any(), anyInt())).thenReturn(CHILD_RESOURCES_1);
        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        when(mockRecursiveResourceRetriever2.getChildrenRecursively(any(), anyInt())).thenReturn(CHILD_RESOURCES_2);
        when(mockRecursiveResourceRetriever2.getChildClass()).thenReturn(ChildResourceImpl2.class);
        assertThat(resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), DEPTH),
                is(equalTo(RESOURCE_WITH_CHILDREN_2)));
        verify(mockResourceControllerDelegate).getResource(RESOURCE.getId());
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE.getId(), DEPTH);
        verify(mockRecursiveResourceRetriever1).getChildClass();
        verify(mockRecursiveResourceRetriever2).getChildrenRecursively(RESOURCE.getId(), DEPTH);
        verify(mockRecursiveResourceRetriever2).getChildClass();
    }

    @Test
    void getResource_returnsResourceWithNullChildren_whenResourceHasNoChildren() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(RESOURCE);
        when(mockRecursiveResourceRetriever1.getChildrenRecursively(any(), anyInt())).thenReturn(ImmutableSet.of());
        assertThat(resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), DEPTH), is(equalTo(RESOURCE)));
        verify(mockResourceControllerDelegate).getResource(RESOURCE.getId());
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE.getId(), DEPTH);
    }

    @Test
    void getResource_returnsResourceWithNullChildren_whenThereAreNoRecursiveResourceRetrievers()
            throws ResourceNotFoundException {
        resourceWithChildrenControllerDelegate =
                ResourceWithChildrenControllerDelegate.<ResourceImpl, NewResourceImpl>builder().childResourceDaos(
                        ImmutableSet.of(mockChildResourceDao1))
                        .resourceNotEmptyExceptionFactory(mockResourceNotEmptyExceptionFactory)
                        .recursiveResourceRetrievers(ImmutableSet.of())
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(RESOURCE);
        assertThat(resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), DEPTH), is(equalTo(RESOURCE)));
        verify(mockResourceControllerDelegate).getResource(RESOURCE.getId());
    }

    @Test
    void getResource_throwsIllegalArgumentException_whenDepthIsLessThanZero() {
        assertThrows(IllegalArgumentException.class,
                () -> resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), -5));
    }

    @Test
    void getResource_returnsResourceWithoutChildren_whenDepthIsZero() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(RESOURCE);
        assertThat(resourceWithChildrenControllerDelegate.getResource(RESOURCE.getId(), 0), is(equalTo(RESOURCE)));
        verify(mockResourceControllerDelegate).getResource(RESOURCE.getId());
        verify(mockRecursiveResourceRetriever1, never()).getChildrenRecursively(any(), anyInt());
    }

    @Test
    void deleteResource_callsResourceControllerDelegate_whenResourceIsEmptyAndThereIsOneChildResourceDao()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockChildResourceDao1.getResources(any())).thenReturn(ImmutableSet.of());
        when(mockResourceControllerDelegate.deleteResource(any())).thenReturn(deleteResourceResult);
        assertThat(resourceWithChildrenControllerDelegate.deleteResource(RESOURCE.getId()),
                is(equalTo(deleteResourceResult)));
        verify(mockChildResourceDao1).getResources(RESOURCE.getId());
        verify(mockResourceControllerDelegate).deleteResource(RESOURCE.getId());
    }

    @Test
    void deleteResource_callsResourceControllerDelegate_whenResourceIsEmptyAndThereAreTwoChildResourceDaos()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        resourceWithChildrenControllerDelegate =
                ResourceWithChildrenControllerDelegate.<ResourceImpl, NewResourceImpl>builder().childResourceDaos(
                        ImmutableSet.of(mockChildResourceDao1, mockChildResourceDao2))
                        .resourceNotEmptyExceptionFactory(mockResourceNotEmptyExceptionFactory)
                        .recursiveResourceRetrievers(ImmutableSet.of(mockRecursiveResourceRetriever1))
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();

        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockChildResourceDao1.getResources(any())).thenReturn(ImmutableSet.of());
        when(mockChildResourceDao2.getResources(any())).thenReturn(ImmutableSet.of());
        when(mockResourceControllerDelegate.deleteResource(any())).thenReturn(deleteResourceResult);
        assertThat(resourceWithChildrenControllerDelegate.deleteResource(RESOURCE.getId()),
                is(equalTo(deleteResourceResult)));
        verify(mockChildResourceDao1).getResources(RESOURCE.getId());
        verify(mockChildResourceDao2).getResources(RESOURCE.getId());
        verify(mockResourceControllerDelegate).deleteResource(RESOURCE.getId());
    }

    @Test
    void deleteResource_callsResourceControllerDelegate_whenThereAreNoChildResourceDaos()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        resourceWithChildrenControllerDelegate =
                ResourceWithChildrenControllerDelegate.<ResourceImpl, NewResourceImpl>builder().childResourceDaos(
                        ImmutableSet.of())
                        .resourceNotEmptyExceptionFactory(mockResourceNotEmptyExceptionFactory)
                        .recursiveResourceRetrievers(ImmutableSet.of(mockRecursiveResourceRetriever1))
                        .resourceControllerDelegate(mockResourceControllerDelegate)
                        .build();

        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceControllerDelegate.deleteResource(any())).thenReturn(deleteResourceResult);
        assertThat(resourceWithChildrenControllerDelegate.deleteResource(RESOURCE.getId()),
                is(equalTo(deleteResourceResult)));
        verify(mockResourceControllerDelegate).deleteResource(RESOURCE.getId());
    }

    @Test
    void deleteResource_throwsResourceNotEmptyException_whenResourceIsNotEmpty() throws ResourceNotFoundException {
        ChildResourceImpl1 childResource = ChildResourceImpl1.builder()
                .id("childId")
                .parentId(RESOURCE.getId())
                .build();
        ResourceNotEmptyExceptionImpl resourceNotEmptyException = new ResourceNotEmptyExceptionImpl(RESOURCE.getId());
        when(mockChildResourceDao1.getResources(any())).thenReturn(ImmutableSet.of(childResource));
        when(mockResourceNotEmptyExceptionFactory.create(any())).thenReturn(resourceNotEmptyException);
        assertThrows(ResourceNotEmptyExceptionImpl.class,
                () -> resourceWithChildrenControllerDelegate.deleteResource(RESOURCE.getId()));
        //noinspection ThrowableNotThrown
        verify(mockResourceNotEmptyExceptionFactory).create(RESOURCE.getId());
        verify(mockResourceControllerDelegate, never()).deleteResource(any());
    }
}

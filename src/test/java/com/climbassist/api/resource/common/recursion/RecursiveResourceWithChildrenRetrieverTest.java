package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.user.UserData;
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
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursiveResourceWithChildrenRetrieverTest {

    @Builder(toBuilder = true)
    @Data
    private static final class ResourceImpl implements ResourceWithParentAndChildren<ResourceImpl, ParentResourceImpl> {

        private String id;
        private String parentId;
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
    @Data
    private static final class ParentResourceImpl implements ResourceWithChildren<ParentResourceImpl> {

        private String id;
        private Collection<ResourceImpl> childResources;

        @Override
        public <ChildResource extends ResourceWithParent<ParentResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            if (childResourceClass != ResourceImpl.class) {
                throw new InvalidChildException(getClass(), childResourceClass);
            }
            //noinspection unchecked
            this.childResources = (Collection<ResourceImpl>) childResources;
        }
    }

    @Builder
    @Value
    private static class ChildResourceImpl1 implements ResourceWithParent<ResourceImpl> {

        String id;
        String parentId;
    }

    @Builder
    @Value
    private static class ChildResourceImpl2 implements ResourceWithParent<ResourceImpl> {

        String id;
        String parentId;
    }

    private static final ParentResourceImpl PARENT_RESOURCE = ParentResourceImpl.builder()
            .id("parent-1")
            .build();
    private static final ResourceImpl RESOURCE_1 = ResourceImpl.builder()
            .id("resource-1")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .build();
    private static final ResourceImpl RESOURCE_2 = ResourceImpl.builder()
            .id("resource-2")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .build();
    private static final ResourceImpl RESOURCE_3 = ResourceImpl.builder()
            .id("resource-3")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .build();
    private static final ChildResourceImpl1 CHILD_RESOURCE_1_1 = ChildResourceImpl1.builder()
            .id("child-1")
            .parentId(RESOURCE_1.getId())
            .build();
    private static final ChildResourceImpl1 CHILD_RESOURCE_1_2 = ChildResourceImpl1.builder()
            .id("child-2")
            .parentId(RESOURCE_1.getId())
            .build();
    private static final ChildResourceImpl1 CHILD_RESOURCE_1_3 = ChildResourceImpl1.builder()
            .id("child-3")
            .parentId(RESOURCE_2.getId())
            .build();
    private static final ChildResourceImpl2 CHILD_RESOURCE_2_1 = ChildResourceImpl2.builder()
            .id("child-1")
            .parentId(RESOURCE_1.getId())
            .build();
    private static final ChildResourceImpl2 CHILD_RESOURCE_2_2 = ChildResourceImpl2.builder()
            .id("child-2")
            .parentId(RESOURCE_1.getId())
            .build();
    private static final ChildResourceImpl2 CHILD_RESOURCE_2_3 = ChildResourceImpl2.builder()
            .id("child-3")
            .parentId(RESOURCE_2.getId())
            .build();
    private static final ResourceImpl RESOURCE_1_WITH_CHILDREN_1 = RESOURCE_1.toBuilder()
            .childResources1(ImmutableSet.of(CHILD_RESOURCE_1_1, CHILD_RESOURCE_1_2))
            .build();
    private static final ResourceImpl RESOURCE_2_WITH_CHILDREN_1 = RESOURCE_2.toBuilder()
            .childResources1(ImmutableSet.of(CHILD_RESOURCE_1_3))
            .build();
    private static final ResourceImpl RESOURCE_1_WITH_CHILDREN_2 = RESOURCE_1.toBuilder()
            .childResources1(ImmutableSet.of(CHILD_RESOURCE_1_1, CHILD_RESOURCE_1_2))
            .childResources2(ImmutableSet.of(CHILD_RESOURCE_2_1, CHILD_RESOURCE_2_2))
            .build();
    private static final ResourceImpl RESOURCE_2_WITH_CHILDREN_2 = RESOURCE_2.toBuilder()
            .childResources1(ImmutableSet.of(CHILD_RESOURCE_1_3))
            .childResources2(ImmutableSet.of(CHILD_RESOURCE_2_3))
            .build();
    private static final Set<ResourceImpl> RESOURCES = ImmutableSet.of(RESOURCE_1, RESOURCE_2, RESOURCE_3);
    private static final int DEPTH = 5;
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
    private RecursiveResourceRetriever<ChildResourceImpl1, ResourceImpl> mockRecursiveResourceRetriever1;
    @Mock
    private RecursiveResourceRetriever<ChildResourceImpl2, ResourceImpl> mockRecursiveResourceRetriever2;

    private RecursiveResourceWithChildrenRetriever<ResourceImpl, ParentResourceImpl>
            recursiveResourceWithChildrenRetriever;

    @BeforeEach
    void setUp() {
        recursiveResourceWithChildrenRetriever =
                RecursiveResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(ImmutableSet.of(mockRecursiveResourceRetriever1))
                        .childClass(ResourceImpl.class)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(recursiveResourceWithChildrenRetriever,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsLessThanZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveResourceWithChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(), -5,
                        MAYBE_USER_DATA));
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveResourceWithChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(), 0,
                        MAYBE_USER_DATA));
    }

    @Test
    void getChildrenRecursively_returnsResourcesWithoutChildren_whenDepthIsOne() {
        when(mockResourceDao.getResources(any(), any())).thenReturn(RESOURCES);
        assertThat(recursiveResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), 1,
                MAYBE_USER_DATA), is(equalTo(RESOURCES)));
        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId(), MAYBE_USER_DATA);
    }

    @Test
    void getChildrenRecursively_returnsResourcesWithoutChildren_whenResourceHasNoChildren() {
        when(mockResourceDao.getResources(any(), any())).thenReturn(RESOURCES);
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt(), any());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt(), any());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt(), any());

        assertThat(recursiveResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH,
                MAYBE_USER_DATA), is(equalTo(RESOURCES)));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId(), MAYBE_USER_DATA);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_1.getId(), DEPTH - 1, MAYBE_USER_DATA);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_2.getId(), DEPTH - 1, MAYBE_USER_DATA);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_3.getId(), DEPTH - 1, MAYBE_USER_DATA);
    }

    @Test
    void getChildRecursively_returnsResourcesWithoutChildren_whenRecursiveResourceRetrieversIsEmpty() {
        recursiveResourceWithChildrenRetriever =
                RecursiveResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(ImmutableSet.of())
                        .childClass(ResourceImpl.class)
                        .build();
        when(mockResourceDao.getResources(any(), any())).thenReturn(RESOURCES);
        assertThat(recursiveResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), 1,
                MAYBE_USER_DATA), is(equalTo(RESOURCES)));
        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId(), MAYBE_USER_DATA);
    }

    @Test
    void getChildrenRecursively_returnsResourcesWithChildren_whenDepthIsGreaterThanOneAndThereIsOneRecursiveResourceRetriever() {
        Set<ResourceImpl> resourcesCopy = ImmutableSet.of(RESOURCE_1.toBuilder()
                .build(), RESOURCE_2.toBuilder()
                .build(), RESOURCE_3.toBuilder()
                .build());
        when(mockResourceDao.getResources(any(), any())).thenReturn(resourcesCopy);
        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_1.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt(), any());
        doReturn(RESOURCE_2_WITH_CHILDREN_1.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt(), any());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt(), any());

        assertThat(recursiveResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH,
                MAYBE_USER_DATA),
                containsInAnyOrder(RESOURCE_1_WITH_CHILDREN_1, RESOURCE_2_WITH_CHILDREN_1, RESOURCE_3));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId(), MAYBE_USER_DATA);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever1);
    }

    @Test
    void getChildrenRecursively_returnsResourcesWithTwoTypesOfChildren_whenDepthIsGreaterThanOneAndThereAreTwoRecursiveResourceRetriever() {
        recursiveResourceWithChildrenRetriever =
                RecursiveResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(
                                ImmutableSet.of(mockRecursiveResourceRetriever1, mockRecursiveResourceRetriever2))
                        .childClass(ResourceImpl.class)
                        .build();
        Set<ResourceImpl> resourcesCopy = ImmutableSet.of(RESOURCE_1.toBuilder()
                .build(), RESOURCE_2.toBuilder()
                .build(), RESOURCE_3.toBuilder()
                .build());
        when(mockResourceDao.getResources(any(), any())).thenReturn(resourcesCopy);

        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_2.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt(), any());
        doReturn(RESOURCE_2_WITH_CHILDREN_2.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt(), any());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt(), any());

        when(mockRecursiveResourceRetriever2.getChildClass()).thenReturn(ChildResourceImpl2.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_2.getChildResources2()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt(), any());
        doReturn(RESOURCE_2_WITH_CHILDREN_2.getChildResources2()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt(), any());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt(), any());

        assertThat(recursiveResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH,
                MAYBE_USER_DATA),
                containsInAnyOrder(RESOURCE_1_WITH_CHILDREN_2, RESOURCE_2_WITH_CHILDREN_2, RESOURCE_3));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId(), MAYBE_USER_DATA);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever1);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever2);
    }

    private void verifyRecursiveResourceRetrieverMocks(
            RecursiveResourceRetriever<?, ?> mockRecursiveResourceRetriever) {
        verify(mockRecursiveResourceRetriever, times(2)).getChildClass();
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_1.getId(), DEPTH - 1, MAYBE_USER_DATA);
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_2.getId(), DEPTH - 1, MAYBE_USER_DATA);
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_3.getId(), DEPTH - 1, MAYBE_USER_DATA);
    }
}

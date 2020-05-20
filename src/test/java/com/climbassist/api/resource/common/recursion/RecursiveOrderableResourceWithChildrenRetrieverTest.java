package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParentAndChildren;
import com.google.common.collect.ImmutableList;
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
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
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
class RecursiveOrderableResourceWithChildrenRetrieverTest {

    @Builder(toBuilder = true)
    @Data
    private static final class ResourceImpl
            implements OrderableResourceWithParentAndChildren<ResourceImpl, ParentResourceImpl> {

        private String id;
        private String parentId;
        private String name;
        private Collection<ChildResourceImpl1> childResources1;
        private Collection<ChildResourceImpl2> childResources2;

        private boolean first;
        private String next;

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

    private static final ParentResourceImpl PARENT_RESOURCE = ParentResourceImpl.builder()
            .id("parent-1")
            .build();
    private static final ResourceImpl RESOURCE_3 = ResourceImpl.builder()
            .id("resource-3")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .build();
    private static final ResourceImpl RESOURCE_2 = ResourceImpl.builder()
            .id("resource-2")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .next(RESOURCE_3.getId())
            .build();
    private static final ResourceImpl RESOURCE_1 = ResourceImpl.builder()
            .id("resource-1")
            .parentId(PARENT_RESOURCE.getId())
            .name("name")
            .first(true)
            .next(RESOURCE_2.getId())
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
    private static final List<ResourceImpl> RESOURCE_LIST = ImmutableList.of(RESOURCE_1, RESOURCE_2, RESOURCE_3);
    private static final Set<ResourceImpl> RESOURCE_SET = ImmutableSet.<ResourceImpl>builder().addAll(RESOURCE_LIST)
            .build();
    private static final int DEPTH = 5;

    @Mock
    private ResourceWithParentDao<ResourceImpl, ParentResourceImpl> mockResourceDao;
    @Mock
    private RecursiveResourceRetriever<ChildResourceImpl1, ResourceImpl> mockRecursiveResourceRetriever1;
    @Mock
    private RecursiveResourceRetriever<ChildResourceImpl2, ResourceImpl> mockRecursiveResourceRetriever2;
    @Mock
    private OrderableListBuilder<ResourceImpl, ParentResourceImpl> mockOrderableListBuilder;

    private RecursiveOrderableResourceWithChildrenRetriever<ResourceImpl, ParentResourceImpl>
            recursiveOrderableResourceWithChildrenRetriever;

    @BeforeEach
    void setUp() {
        recursiveOrderableResourceWithChildrenRetriever =
                RecursiveOrderableResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(ImmutableSet.of(mockRecursiveResourceRetriever1))
                        .orderableListBuilder(mockOrderableListBuilder)
                        .childClass(ResourceImpl.class)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(recursiveOrderableResourceWithChildrenRetriever,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsLessThanZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(),
                        -5));
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(),
                        0));
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrderWithoutChildren_whenDepthIsOne() {
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        when(mockOrderableListBuilder.buildList(any())).thenReturn(RESOURCE_LIST);
        assertThat(recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), 1),
                is(equalTo(RESOURCE_LIST)));
        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrderWithoutChildren_whenThereAreNoRecursiveResourceRetrievers() {
        recursiveOrderableResourceWithChildrenRetriever =
                RecursiveOrderableResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(ImmutableSet.of())
                        .orderableListBuilder(mockOrderableListBuilder)
                        .childClass(ResourceImpl.class)
                        .build();
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        when(mockOrderableListBuilder.buildList(any())).thenReturn(RESOURCE_LIST);
        assertThat(recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), 1),
                is(equalTo(RESOURCE_LIST)));
        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrderWithoutChildren_whenResourceHasNoChildren() {
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt());
        when(mockOrderableListBuilder.buildList(any())).thenReturn(RESOURCE_LIST);

        assertThat(
                recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH),
                is(equalTo(RESOURCE_LIST)));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_1.getId(), DEPTH - 1);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_2.getId(), DEPTH - 1);
        verify(mockRecursiveResourceRetriever1).getChildrenRecursively(RESOURCE_3.getId(), DEPTH - 1);
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrderWithChildren_whenDepthIsGreaterThanOne() {
        List<ResourceImpl> resourceListCopy = ImmutableList.of(RESOURCE_1.toBuilder()
                .build(), RESOURCE_2.toBuilder()
                .build(), RESOURCE_3.toBuilder()
                .build());
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        when(mockOrderableListBuilder.buildList(any())).thenReturn(resourceListCopy);
        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_1.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt());
        doReturn(RESOURCE_2_WITH_CHILDREN_1.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt());

        assertThat(
                recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH),
                is(equalTo(ImmutableList.of(RESOURCE_1_WITH_CHILDREN_1, RESOURCE_2_WITH_CHILDREN_1, RESOURCE_3))));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever1);
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrderWithTwoTypesOfChildren_whenDepthIsGreaterThanOneAndThereAreTwoRecursiveResourceRetrievers() {
        recursiveOrderableResourceWithChildrenRetriever =
                RecursiveOrderableResourceWithChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .recursiveResourceRetrievers(
                                ImmutableSet.of(mockRecursiveResourceRetriever1, mockRecursiveResourceRetriever2))
                        .orderableListBuilder(mockOrderableListBuilder)
                        .childClass(ResourceImpl.class)
                        .build();

        List<ResourceImpl> resourceListCopy = ImmutableList.of(RESOURCE_1.toBuilder()
                .build(), RESOURCE_2.toBuilder()
                .build(), RESOURCE_3.toBuilder()
                .build());
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        when(mockOrderableListBuilder.buildList(any())).thenReturn(resourceListCopy);

        when(mockRecursiveResourceRetriever1.getChildClass()).thenReturn(ChildResourceImpl1.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_2.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt());
        doReturn(RESOURCE_2_WITH_CHILDREN_2.getChildResources1()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever1)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt());

        when(mockRecursiveResourceRetriever2.getChildClass()).thenReturn(ChildResourceImpl2.class);
        doReturn(RESOURCE_1_WITH_CHILDREN_2.getChildResources2()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_1.getId()), anyInt());
        doReturn(RESOURCE_2_WITH_CHILDREN_2.getChildResources2()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_2.getId()), anyInt());
        doReturn(ImmutableSet.of()).when(mockRecursiveResourceRetriever2)
                .getChildrenRecursively(eq(RESOURCE_3.getId()), anyInt());

        assertThat(
                recursiveOrderableResourceWithChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(), DEPTH),
                is(equalTo(ImmutableList.of(RESOURCE_1_WITH_CHILDREN_2, RESOURCE_2_WITH_CHILDREN_2, RESOURCE_3))));

        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever1);
        verifyRecursiveResourceRetrieverMocks(mockRecursiveResourceRetriever2);
    }

    private void verifyRecursiveResourceRetrieverMocks(
            RecursiveResourceRetriever<?, ?> mockRecursiveResourceRetriever) {
        verify(mockRecursiveResourceRetriever, times(2)).getChildClass();
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_1.getId(), DEPTH - 1);
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_2.getId(), DEPTH - 1);
        verify(mockRecursiveResourceRetriever).getChildrenRecursively(RESOURCE_3.getId(), DEPTH - 1);
    }
}

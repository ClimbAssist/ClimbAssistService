package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Data;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursiveOrderableResourceWithNoChildrenRetrieverTest {

    @Builder(toBuilder = true)
    @Data
    private static final class ResourceImpl implements OrderableResourceWithParent<ResourceImpl, ParentResourceImpl> {

        private String id;
        private String parentId;
        private String name;
        private boolean first;
        private String next;
    }

    @Builder
    @Data
    private static final class ParentResourceImpl implements ResourceWithChildren<ParentResourceImpl> {

        private String id;

        @Override
        public <ChildResource extends ResourceWithParent<ParentResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            // unused
        }
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
    private static final List<ResourceImpl> RESOURCE_LIST = ImmutableList.of(RESOURCE_1, RESOURCE_2, RESOURCE_3);
    private static final Set<ResourceImpl> RESOURCE_SET = ImmutableSet.<ResourceImpl>builder().addAll(RESOURCE_LIST)
            .build();

    @Mock
    private ResourceWithParentDao<ResourceImpl, ParentResourceImpl> mockResourceDao;
    @Mock
    private OrderableListBuilder<ResourceImpl, ParentResourceImpl> mockOrderableListBuilder;

    private RecursiveOrderableResourceWithNoChildrenRetriever<ResourceImpl, ParentResourceImpl>
            recursiveOrderableResourceWithNoChildrenRetriever;

    @BeforeEach
    void setUp() {
        recursiveOrderableResourceWithNoChildrenRetriever =
                RecursiveOrderableResourceWithNoChildrenRetriever.<ResourceImpl, ParentResourceImpl>builder().resourceDao(
                        mockResourceDao)
                        .orderableListBuilder(mockOrderableListBuilder)
                        .childClass(ResourceImpl.class)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(recursiveOrderableResourceWithNoChildrenRetriever,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsLessThanZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveOrderableResourceWithNoChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(),
                        -5));
    }

    @Test
    void getChildrenRecursively_throwsIllegalArgumentException_whenDepthIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> recursiveOrderableResourceWithNoChildrenRetriever.getChildrenRecursively(RESOURCE_1.getParentId(),
                        0));
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrder_whenDepthIsOne() {
        runSuccessTest(1);
    }

    @Test
    void getChildrenRecursively_returnsResourcesInOrder_whenDepthIsGreaterThanOne() {
        runSuccessTest(5);
    }

    private void runSuccessTest(int depth) {
        when(mockResourceDao.getResources(any())).thenReturn(RESOURCE_SET);
        when(mockOrderableListBuilder.buildList(any())).thenReturn(RESOURCE_LIST);
        assertThat(recursiveOrderableResourceWithNoChildrenRetriever.getChildrenRecursively(PARENT_RESOURCE.getId(),
                depth), is(equalTo(RESOURCE_LIST)));
        verify(mockResourceDao).getResources(PARENT_RESOURCE.getId());
        verify(mockOrderableListBuilder).buildList(RESOURCE_SET);
    }
}

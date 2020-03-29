package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderableListBuilderTest {

    @Data
    @Builder
    private static class OrderableResourceWithParentImpl
            implements OrderableResourceWithParent<OrderableResourceWithParentImpl, ParentResourceImpl> {

        @NonNull
        private String id;
        @NonNull
        @Getter
        private String parentId;
        private boolean first;
        private String next;
    }

    private static class ParentResourceImpl implements ResourceWithChildren<ParentResourceImpl> {

        @Getter
        private String id;

        @Override
        public <ChildResource extends ResourceWithParent<ParentResourceImpl>> void setChildResources(
                Collection<?> childResources, Class<ChildResource> childResourceClass) {
            // unused
        }
    }

    private static final OrderableResourceWithParentImpl ORDERABLE_1 = OrderableResourceWithParentImpl.builder()
            .id("1")
            .parentId("1")
            .first(true)
            .next("2")
            .build();
    private static final OrderableResourceWithParentImpl ORDERABLE_2 = OrderableResourceWithParentImpl.builder()
            .id("2")
            .parentId("1")
            .next("3")
            .build();
    private static final OrderableResourceWithParentImpl ORDERABLE_3 = OrderableResourceWithParentImpl.builder()
            .id("3")
            .parentId("1")
            .build();

    private OrderableListBuilder<OrderableResourceWithParentImpl, ParentResourceImpl> orderableListBuilder;

    @BeforeEach
    void setUp() {
        orderableListBuilder = new OrderableListBuilder<>();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(orderableListBuilder, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void buildList_returnsEmptyList_whenInputSetIsEmpty() throws InvalidOrderingException {
        assertThat(orderableListBuilder.buildList(ImmutableSet.of()), is(equalTo(ImmutableList.of())));
    }

    @Test
    void buildList_throwsInvalidOrderingException_whenNoOrderablesAreFirst() {
        assertThrows(InvalidOrderingException.class,
                () -> orderableListBuilder.buildList(ImmutableSet.of(ORDERABLE_2, ORDERABLE_3)));
    }

    @Test
    void buildList_throwsInvalidOrderingException_whenTwoOrderablesAreFirst() {
        OrderableResourceWithParentImpl alsoFirst = OrderableResourceWithParentImpl.builder()
                .id("oops")
                .parentId("1")
                .first(true)
                .build();
        assertThrows(InvalidOrderingException.class,
                () -> orderableListBuilder.buildList(ImmutableSet.of(ORDERABLE_1, alsoFirst)));
    }

    @Test
    void buildList_throwsInvalidOrderingException_whenListContainsALoop() {
        OrderableResourceWithParentImpl loopOrderable = OrderableResourceWithParentImpl.builder()
                .id("3")
                .parentId("1")
                .next("2")
                .build();
        assertThrows(InvalidOrderingException.class,
                () -> orderableListBuilder.buildList(ImmutableSet.of(ORDERABLE_1, ORDERABLE_2, loopOrderable)));
    }

    @Test
    void buildList_throwsInvalidOrderingException_whenListContainsOrphan() {
        OrderableResourceWithParentImpl orphanOrderable = OrderableResourceWithParentImpl.builder()
                .id("4")
                .parentId("1")
                .build();
        assertThrows(InvalidOrderingException.class, () -> orderableListBuilder.buildList(
                ImmutableSet.of(ORDERABLE_1, ORDERABLE_2, ORDERABLE_3, orphanOrderable)));
    }

    @Test
    void buildList_returnsSingleElement_whenInputOnlyHasOneElement() throws InvalidOrderingException {
        OrderableResourceWithParentImpl singleOrderable = OrderableResourceWithParentImpl.builder()
                .id("1")
                .parentId("1")
                .first(true)
                .build();
        assertThat(orderableListBuilder.buildList(ImmutableSet.of(singleOrderable)),
                is(equalTo(ImmutableList.of(singleOrderable))));
    }

    @Test
    void buildList_throwsInvalidOrderingException_whenElementHasInvalidNextId() {
        OrderableResourceWithParentImpl invalidNextIdOrderable = OrderableResourceWithParentImpl.builder()
                .id("2")
                .parentId("1")
                .next("4")
                .build();
        assertThrows(InvalidOrderingException.class, () -> orderableListBuilder.buildList(
                ImmutableSet.of(ORDERABLE_1, invalidNextIdOrderable, ORDERABLE_3)));
    }

    @Test
    void buildList_returnsSortedList() throws InvalidOrderingException {
        assertThat(orderableListBuilder.buildList(ImmutableSet.of(ORDERABLE_1, ORDERABLE_2, ORDERABLE_3)),
                is(equalTo(ImmutableList.of(ORDERABLE_1, ORDERABLE_2, ORDERABLE_3))));
    }
}

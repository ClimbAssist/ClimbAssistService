package com.climbassist.api.resource.common;

import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import com.climbassist.api.user.UserData;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderableResourceWithParentControllerDelegateTest {

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

    private static final ParentResourceImpl PARENT_RESOURCE_1 = ParentResourceImpl.builder()
            .id("parentId1")
            .name("parent name 1")
            .build();
    private static final ResourceImpl RESOURCE_2 = ResourceImpl.builder()
            .id("id2")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("name 2")
            .build();
    private static final ResourceImpl RESOURCE_1 = ResourceImpl.builder()
            .id("id1")
            .parentId(PARENT_RESOURCE_1.getId())
            .name("name 1")
            .first(true)
            .next(RESOURCE_2.getId())
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
    private ResourceWithParentControllerDelegate<ResourceImpl, NewResourceImpl, ParentResourceImpl>
            mockResourceWithParentControllerDelegate;
    @Mock
    private OrderableListBuilder<ResourceImpl, ParentResourceImpl> mockOrderableListBuilder;

    private OrderableResourceWithParentControllerDelegate<ResourceImpl, NewResourceImpl, ParentResourceImpl>
            orderableResourceWithParentControllerDelegate;

    @BeforeEach
    void setUp() {
        orderableResourceWithParentControllerDelegate =
                OrderableResourceWithParentControllerDelegate.<ResourceImpl, NewResourceImpl, ParentResourceImpl>builder().orderableListBuilder(
                        mockOrderableListBuilder)
                        .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                        .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(orderableResourceWithParentControllerDelegate,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResourcesForParent_returnsResourcesInAnyOrder_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        Set<ResourceImpl> resources = ImmutableSet.of(RESOURCE_2, RESOURCE_1);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(eq(RESOURCE_1.getParentId()),
                any())).thenReturn(resources);
        assertThat(orderableResourceWithParentControllerDelegate.getResourcesForParent(RESOURCE_1.getParentId(), false,
                MAYBE_USER_DATA), containsInAnyOrder(resources.toArray()));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(RESOURCE_1.getParentId(),
                MAYBE_USER_DATA);
        verify(mockOrderableListBuilder, never()).buildList(any());
    }

    @Test
    void getResourcesForParent_returnsResourcesInOrder_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        Set<ResourceImpl> resourceSet = ImmutableSet.of(RESOURCE_2, RESOURCE_1);
        List<ResourceImpl> resourceList = ImmutableList.of(RESOURCE_1, RESOURCE_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(eq(RESOURCE_1.getParentId()),
                any())).thenReturn(new HashSet<>(resourceSet));
        when(mockOrderableListBuilder.buildList(any())).thenReturn(resourceList);
        assertThat(orderableResourceWithParentControllerDelegate.getResourcesForParent(RESOURCE_1.getParentId(), true,
                MAYBE_USER_DATA), is(equalTo(resourceList)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(RESOURCE_1.getParentId(),
                MAYBE_USER_DATA);
        verify(mockOrderableListBuilder).buildList(resourceSet);
    }
}

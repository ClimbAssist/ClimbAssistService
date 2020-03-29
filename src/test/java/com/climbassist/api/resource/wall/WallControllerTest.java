package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.crag.Crag;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WallControllerTest {

    private static final Wall WALL_1 = Wall.builder()
            .wallId("wall-1")
            .cragId("crag-1")
            .name("Wall 1")
            .first(true)
            .next("wall-2")
            .build();
    private static final Wall WALL_2 = Wall.builder()
            .wallId("wall-2")
            .cragId("crag-1")
            .name("Wall 2")
            .next("wall-3")
            .build();
    private static final NewWall NEW_WALL_1 = NewWall.builder()
            .cragId("crag-1")
            .name("Wall 1")
            .first(true)
            .next("wall-2")
            .build();
    private static final Wall UPDATED_WALL_1 = Wall.builder()
            .wallId(WALL_1.getWallId())
            .cragId("crag-2")
            .name("New wall")
            .first(false)
            .next("wall-3")
            .build();
    private static final int DEPTH = 5;

    @Mock
    private ResourceWithParentControllerDelegate<Wall, NewWall, Crag> mockResourceWithParentControllerDelegate;
    @Mock
    private OrderableResourceWithParentControllerDelegate<Wall, NewWall, Crag>
            mockOrderableResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Wall, NewWall> mockResourceWithChildrenControllerDelegate;

    private WallController wallController;

    @BeforeEach
    void setUp() {
        wallController = WallController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(mockOrderableResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(wallController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt())).thenReturn(WALL_1);
        assertThat(wallController.getResource(WALL_1.getWallId(), DEPTH), is(equalTo(WALL_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(WALL_1.getWallId(), DEPTH);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Wall> walls = ImmutableList.of(WALL_1, WALL_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                walls);
        assertThat(wallController.getResourcesForParent(WALL_1.getCragId(), false), is(equalTo(walls)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(WALL_1.getCragId(), false);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Wall> walls = ImmutableList.of(WALL_1, WALL_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                walls);
        assertThat(wallController.getResourcesForParent(WALL_1.getCragId(), true), is(equalTo(walls)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(WALL_1.getCragId(), true);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateWallResult createWallResult = CreateWallResult.builder()
                .wallId(WALL_1.getWallId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any())).thenReturn(createWallResult);
        assertThat(wallController.createResource(NEW_WALL_1), is(equalTo(createWallResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_WALL_1);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any())).thenReturn(updateResourceResult);
        assertThat(wallController.updateResource(UPDATED_WALL_1), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_WALL_1);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any())).thenReturn(deleteResourceResult);
        assertThat(wallController.deleteResource(WALL_1.getWallId()), is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(WALL_1.getWallId());
    }
}

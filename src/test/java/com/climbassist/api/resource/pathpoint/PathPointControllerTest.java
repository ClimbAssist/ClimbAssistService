package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.batch.BatchResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.path.Path;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathPointControllerTest {

    private static final Path PATH_1 = Path.builder()
            .pathId("path-1")
            .cragId("crag-1")
            .build();
    private static final PathPoint PATH_POINT_3 = PathPoint.builder()
            .pathPointId("path-point-3")
            .pathId(PATH_1.getPathId())
            .latitude(3.0)
            .longitude(3.0)
            .build();
    private static final PathPoint PATH_POINT_2 = PathPoint.builder()
            .pathPointId("path-point-2")
            .pathId(PATH_1.getPathId())
            .latitude(2.0)
            .longitude(2.0)
            .next(PATH_POINT_3.getPathPointId())
            .build();
    private static final PathPoint PATH_POINT_1 = PathPoint.builder()
            .pathPointId("path-point-1")
            .pathId(PATH_1.getPathId())
            .latitude(1.0)
            .longitude(1.0)
            .first(true)
            .next(PATH_POINT_2.getPathPointId())
            .build();
    private static final NewPathPoint NEW_PATH_POINT_1 = NewPathPoint.builder()
            .pathId(PATH_POINT_1.getPathId())
            .build();
    private static final PathPoint UPDATED_PATH_POINT_1 = PathPoint.builder()
            .pathPointId(PATH_POINT_1.getPathPointId())
            .pathId("path-2")
            .build();
    private static final BatchNewPathPoints BATCH_NEW_PATH_POINTS = BatchNewPathPoints.builder()
            .newPathPoints(ImmutableList.of(BatchNewPathPoint.builder()
                    .latitude(PATH_POINT_1.getLatitude())
                    .longitude(PATH_POINT_1.getLongitude())
                    .build(), BatchNewPathPoint.builder()
                    .latitude(PATH_POINT_2.getLatitude())
                    .longitude(PATH_POINT_2.getLongitude())
                    .build(), BatchNewPathPoint.builder()
                    .latitude(PATH_POINT_3.getLatitude())
                    .longitude(PATH_POINT_3.getLongitude())
                    .build()))
            .build();
    private static final DeleteResourceResult DELETE_RESOURCE_RESULT = DeleteResourceResult.builder()
            .successful(true)
            .build();
    private static final BatchDeletePathPointsRequest BATCH_DELETE_PATH_POINTS_REQUEST =
            BatchDeletePathPointsRequest.builder()
                    .pathPointIds(ImmutableSet.of(PATH_POINT_1.getPathPointId(), PATH_POINT_2.getPathPointId(),
                            PATH_POINT_3.getPathPointId()))
                    .build();

    @Mock
    private ResourceControllerDelegate<PathPoint, NewPathPoint> mockResourceControllerDelegate;
    @Mock
    private ResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path>
            mockResourceWithParentControllerDelegate;
    @Mock
    private OrderableResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path>
            mockOrderableResourceWithParentControllerDelegate;
    @Mock
    private BatchResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path, BatchNewPathPoint>
            mockBatchResourceWithParentControllerDelegate;

    private PathPointController pathPointController;

    @BeforeEach
    void setUp() {
        pathPointController = PathPointController.builder()
                .resourceControllerDelegate(mockResourceControllerDelegate)
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(mockOrderableResourceWithParentControllerDelegate)
                .batchResourceWithParentControllerDelegate(mockBatchResourceWithParentControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(BatchNewPathPoints.class, BATCH_NEW_PATH_POINTS);
        nullPointerTester.setDefault(BatchDeletePathPointsRequest.class, BATCH_DELETE_PATH_POINTS_REQUEST);
        nullPointerTester.testInstanceMethods(pathPointController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(PATH_POINT_1);
        assertThat(pathPointController.getResource(PATH_POINT_1.getPathPointId()), is(equalTo(PATH_POINT_1)));
        verify(mockResourceControllerDelegate).getResource(PATH_POINT_1.getPathPointId());
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<PathPoint> pathPoints = ImmutableList.of(PATH_POINT_1, PATH_POINT_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                pathPoints);
        assertThat(pathPointController.getResourcesForParent(PATH_POINT_1.getPathId(), false), is(equalTo(pathPoints)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(PATH_POINT_1.getPathId(),
                false);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<PathPoint> pathPoints = ImmutableList.of(PATH_POINT_1, PATH_POINT_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                pathPoints);
        assertThat(pathPointController.getResourcesForParent(PATH_POINT_1.getPathId(), true), is(equalTo(pathPoints)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(PATH_POINT_1.getPathId(), true);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreatePathPointResult createPathPointResult = CreatePathPointResult.builder()
                .pathPointId(PATH_POINT_1.getPathPointId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any())).thenReturn(createPathPointResult);
        assertThat(pathPointController.createResource(NEW_PATH_POINT_1), is(equalTo(createPathPointResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_PATH_POINT_1);
    }

    @Test
    void batchCreateResource_callsBatchResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        BatchCreatePathPointsResult batchCreatePathPointsResult = BatchCreatePathPointsResult.builder()
                .pathPointIds(ImmutableList.of(PATH_POINT_1.getId(), PATH_POINT_2.getId()))
                .build();
        when(mockBatchResourceWithParentControllerDelegate.batchCreateResources(anyString(), any())).thenReturn(
                batchCreatePathPointsResult);
        assertThat(pathPointController.batchCreateResources(PATH_1.getPathId(), BATCH_NEW_PATH_POINTS),
                is(equalTo(batchCreatePathPointsResult)));
        verify(mockBatchResourceWithParentControllerDelegate).batchCreateResources(PATH_1.getPathId(),
                BATCH_NEW_PATH_POINTS);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any())).thenReturn(updateResourceResult);
        assertThat(pathPointController.updateResource(UPDATED_PATH_POINT_1), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_PATH_POINT_1);
    }

    @Test
    void deleteResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.deleteResource(any())).thenReturn(DELETE_RESOURCE_RESULT);
        assertThat(pathPointController.deleteResource(PATH_POINT_1.getPathPointId()),
                is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockResourceControllerDelegate).deleteResource(PATH_POINT_1.getPathPointId());
    }

    @Test
    void batchDeleteResources_callsBatchResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        when(mockBatchResourceWithParentControllerDelegate.batchDeleteResources(any())).thenReturn(
                DELETE_RESOURCE_RESULT);
        assertThat(pathPointController.batchDeleteResources(BATCH_DELETE_PATH_POINTS_REQUEST),
                is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockBatchResourceWithParentControllerDelegate).batchDeleteResources(BATCH_DELETE_PATH_POINTS_REQUEST);
    }
}

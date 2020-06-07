package com.climbassist.api.resource.point;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.batch.BatchDeleteResourcesRequest;
import com.climbassist.api.resource.common.batch.BatchResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.pitch.Anchors;
import com.climbassist.api.resource.pitch.Pitch;
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
class PointControllerTest {

    private static final Pitch PITCH_1 = Pitch.builder()
            .pitchId("pitch-1")
            .routeId("route-1")
            .description("Pitch 1")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(1.0)
                    .y(1.0)
                    .z(1.0)
                    .fixed(true)
                    .build())
            .first(true)
            .next("pitch-2")
            .build();
    private static final Point POINT_3 = Point.builder()
            .pointId("point-3")
            .pitchId(PITCH_1.getPitchId())
            .x(3.0)
            .y(3.0)
            .z(3.0)
            .build();
    private static final Point POINT_2 = Point.builder()
            .pointId("point-2")
            .pitchId(PITCH_1.getPitchId())
            .x(2.0)
            .y(2.0)
            .z(2.0)
            .next(POINT_3.getPointId())
            .build();
    private static final Point POINT_1 = Point.builder()
            .pointId("point-1")
            .pitchId(PITCH_1.getPitchId())
            .x(1.0)
            .y(1.0)
            .z(1.0)
            .first(true)
            .next(POINT_2.getPointId())
            .build();
    private static final NewPoint NEW_POINT_3 = NewPoint.builder()
            .pitchId(POINT_3.getPitchId())
            .x(POINT_3.getX())
            .y(POINT_3.getY())
            .z(POINT_3.getZ())
            .build();
    private static final NewPoint NEW_POINT_2 = NewPoint.builder()
            .pitchId(POINT_2.getPitchId())
            .x(POINT_2.getX())
            .y(POINT_2.getY())
            .z(POINT_2.getZ())
            .next(POINT_3.getPointId())
            .build();
    private static final NewPoint NEW_POINT_1 = NewPoint.builder()
            .pitchId(PITCH_1.getPitchId())
            .x(1.0)
            .y(1.0)
            .z(1.0)
            .first(true)
            .next(POINT_2.getPointId())
            .build();
    private static final BatchNewPoint BATCH_NEW_POINT_1 = BatchNewPoint.builder()
            .x(NEW_POINT_1.getX())
            .y(NEW_POINT_1.getY())
            .z(NEW_POINT_1.getZ())
            .build();
    private static final BatchNewPoint BATCH_NEW_POINT_2 = BatchNewPoint.builder()
            .x(NEW_POINT_2.getX())
            .y(NEW_POINT_2.getY())
            .z(NEW_POINT_2.getZ())
            .build();
    private static final BatchNewPoint BATCH_NEW_POINT_3 = BatchNewPoint.builder()
            .x(NEW_POINT_3.getX())
            .y(NEW_POINT_3.getY())
            .z(NEW_POINT_3.getZ())
            .build();
    private static final BatchNewPoints BATCH_NEW_POINTS = BatchNewPoints.builder()
            .newPoints(ImmutableList.of(BATCH_NEW_POINT_1, BATCH_NEW_POINT_2, BATCH_NEW_POINT_3))
            .build();
    private static final Point UPDATED_POINT_1 = Point.builder()
            .pointId(POINT_1.getPointId())
            .pitchId("pitch-2")
            .x(2.0)
            .y(2.0)
            .z(2.0)
            .first(false)
            .next(POINT_3.getPointId())
            .build();
    private static final DeleteResourceResult DELETE_RESOURCE_RESULT = DeleteResourceResult.builder()
            .successful(true)
            .build();
    private static final BatchDeletePointsRequest BATCH_DELETE_POINTS_REQUEST = BatchDeletePointsRequest.builder()
            .pointIds(ImmutableSet.of(POINT_1.getId(), POINT_2.getId(), POINT_3.getId()))
            .build();
    @Mock
    private ResourceControllerDelegate<Point, NewPoint> mockResourceControllerDelegate;
    @Mock
    private ResourceWithParentControllerDelegate<Point, NewPoint, Pitch> mockResourceWithParentControllerDelegate;
    @Mock
    private OrderableResourceWithParentControllerDelegate<Point, NewPoint, Pitch>
            mockOrderableResourceWithParentControllerDelegate;
    @Mock
    private BatchResourceWithParentControllerDelegate<Point, NewPoint, Pitch, BatchNewPoint>
            mockBatchResourceWithParentControllerDelegate;

    private PointController pointController;

    @BeforeEach
    void setUp() {
        pointController = PointController.builder()
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
        nullPointerTester.setDefault(BatchNewPoints.class, BATCH_NEW_POINTS);
        nullPointerTester.setDefault(BatchDeletePointsRequest.class, BATCH_DELETE_POINTS_REQUEST);
        nullPointerTester.testInstanceMethods(pointController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.getResource(any())).thenReturn(POINT_1);
        assertThat(pointController.getResource(POINT_1.getPointId()), is(equalTo(POINT_1)));
        verify(mockResourceControllerDelegate).getResource(POINT_1.getPointId());
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Point> points = ImmutableList.of(POINT_1, POINT_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                points);
        assertThat(pointController.getResourcesForParent(POINT_1.getPitchId(), false), is(equalTo(points)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(POINT_1.getPitchId(), false);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Point> points = ImmutableList.of(POINT_1, POINT_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                points);
        assertThat(pointController.getResourcesForParent(POINT_1.getPitchId(), true), is(equalTo(points)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(POINT_1.getPitchId(), true);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreatePointResult createPointResult = CreatePointResult.builder()
                .pointId(POINT_1.getPointId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any())).thenReturn(createPointResult);
        assertThat(pointController.createResource(NEW_POINT_1), is(equalTo(createPointResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_POINT_1);
    }

    @Test
    void batchCreateResource_callsBatchResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        BatchCreatePointsResult batchCreatePointsResult = BatchCreatePointsResult.builder()
                .pointIds(ImmutableList.of(POINT_1.getPointId(), POINT_2.getPointId(), POINT_3.getPointId()))
                .build();
        when(mockBatchResourceWithParentControllerDelegate.batchCreateResources(anyString(), any())).thenReturn(
                batchCreatePointsResult);
        assertThat(pointController.batchCreateResources(PITCH_1.getPitchId(), BATCH_NEW_POINTS),
                is(equalTo(batchCreatePointsResult)));
        verify(mockBatchResourceWithParentControllerDelegate).batchCreateResources(PITCH_1.getPitchId(),
                BATCH_NEW_POINTS);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any())).thenReturn(updateResourceResult);
        assertThat(pointController.updateResource(UPDATED_POINT_1), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_POINT_1);
    }

    @Test
    void deleteResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceControllerDelegate.deleteResource(any())).thenReturn(DELETE_RESOURCE_RESULT);
        assertThat(pointController.deleteResource(POINT_1.getPointId()), is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockResourceControllerDelegate).deleteResource(POINT_1.getPointId());
    }

    @Test
    void batchDeleteResources_callsBatchResourceWithParentControllerDelegate_whenPointsListIsSupplied()
            throws ResourceNotFoundException {
        when(mockBatchResourceWithParentControllerDelegate.batchDeleteResources(
                any(BatchDeleteResourcesRequest.class))).thenReturn(DELETE_RESOURCE_RESULT);
        assertThat(pointController.batchDeleteResources(BATCH_DELETE_POINTS_REQUEST),
                is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockBatchResourceWithParentControllerDelegate).batchDeleteResources(BATCH_DELETE_POINTS_REQUEST);
    }

    @Test
    void batchDeleteResources_callsBatchResourceWithParentControllerDelegate_whenPitchIdIsSupplied()
            throws ResourceNotFoundException {
        when(mockBatchResourceWithParentControllerDelegate.batchDeleteResources(any(String.class))).thenReturn(
                DELETE_RESOURCE_RESULT);
        assertThat(pointController.batchDeleteResources(PITCH_1.getId()), is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockBatchResourceWithParentControllerDelegate).batchDeleteResources(PITCH_1.getId());
    }
}

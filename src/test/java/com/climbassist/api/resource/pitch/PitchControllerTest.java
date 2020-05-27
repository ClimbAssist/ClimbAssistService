package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.route.Center;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RouteNotFoundException;
import com.climbassist.api.resource.route.RouteNotFoundExceptionFactory;
import com.climbassist.api.resource.route.RoutesDao;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PitchControllerTest {

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
    private static final Pitch PITCH_2 = Pitch.builder()
            .pitchId("pitch-2")
            .routeId("route-1")
            .description("Pitch 2")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(true)
                    .build())
            .next("pitch-3")
            .build();
    private static final Pitch PITCH_3 = Pitch.builder()
            .pitchId("pitch-3")
            .routeId("route-1")
            .description("Pitch 3")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(3.0)
                    .y(3.0)
                    .z(3.0)
                    .fixed(true)
                    .build())
            .build();
    private static final Pitch PITCH_4 = Pitch.builder()
            .pitchId("pitch-4")
            .routeId("route-2")
            .description("Pitch 4")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(true)
                    .build())
            .build();
    private static final Pitch PITCH_5 = Pitch.builder()
            .pitchId("pitch-5")
            .routeId("route-2")
            .description("Pitch 5")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(3.0)
                    .y(3.0)
                    .z(3.0)
                    .fixed(true)
                    .build())
            .build();
    private static final Route ROUTE_1 = Route.builder()
            .routeId("route-1")
            .wallId("wall-1")
            .name("Route 1")
            .description("Route 1")
            .center(Center.builder()
                    .x(1.0)
                    .y(2.0)
                    .z(3.0)
                    .build())
            .grade(1)
            .gradeModifier("b/c")
            .danger("R")
            .mainImageLocation("mainImageLocation")
            .protection("protection")
            .style("sport")
            .first(true)
            .next("route-2")
            .build();
    private static final Route ROUTE_2 = Route.builder()
            .routeId("route-2")
            .wallId("wall-2")
            .name("Route 2")
            .description("Route 2")
            .grade(2)
            .gradeModifier("c/d")
            .danger("X")
            .mainImageLocation("mainImageLocation")
            .protection("protection")
            .style("sport")
            .build();
    private static final NewPitch NEW_PITCH_1 = NewPitch.builder()
            .routeId(PITCH_1.getRouteId())
            .description(PITCH_1.getDescription())
            .grade(PITCH_1.getGrade())
            .gradeModifier(PITCH_1.getGradeModifier())
            .danger(PITCH_1.getDanger())
            .anchors(Anchors.builder()
                    .x(PITCH_1.getAnchors()
                            .getX())
                    .y(PITCH_1.getAnchors()
                            .getY())
                    .z(PITCH_1.getAnchors()
                            .getZ())
                    .fixed(PITCH_1.getAnchors()
                            .getFixed())
                    .build())
            .first(PITCH_1.getFirst())
            .next(PITCH_1.getNext())
            .build();
    private static final Pitch UPDATED_PITCH_1_NEW_ROUTE = Pitch.builder()
            .pitchId(PITCH_1.getPitchId())
            .routeId("route-2")
            .description("New description")
            .grade(12)
            .gradeModifier("d")
            .danger("X")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(false)
                    .build())
            .first(false)
            .next("pitch-3")
            .build();
    private static final Pitch UPDATED_PITCH_1_SAME_ROUTE = Pitch.builder()
            .pitchId(PITCH_1.getPitchId())
            .routeId("route-1")
            .description("New description")
            .grade(12)
            .gradeModifier("d")
            .danger("X")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(false)
                    .build())
            .first(false)
            .next("pitch-3")
            .build();
    private static final Route UPDATED_ROUTE_1_FROM_UPDATED_PITCH = Route.builder()
            .routeId(ROUTE_1.getRouteId())
            .wallId(ROUTE_1.getWallId())
            .name(ROUTE_1.getName())
            .description(ROUTE_1.getDescription())
            .center(Center.builder()
                    .x(ROUTE_1.getCenter()
                            .getX())
                    .y(ROUTE_1.getCenter()
                            .getY())
                    .z(ROUTE_1.getCenter()
                            .getZ())
                    .build())
            .grade(12)
            .gradeModifier("d")
            .danger("X")
            .mainImageLocation(ROUTE_1.getMainImageLocation())
            .protection(ROUTE_1.getProtection())
            .style(ROUTE_1.getStyle())
            .first(ROUTE_1.isFirst())
            .next(ROUTE_1.getNext())
            .build();
    private static final Route UPDATED_ROUTE_1_FROM_NEW_PITCH = Route.builder()
            .routeId(ROUTE_1.getRouteId())
            .wallId(ROUTE_1.getWallId())
            .name(ROUTE_1.getName())
            .description(ROUTE_1.getDescription())
            .center(Center.builder()
                    .x(ROUTE_1.getCenter()
                            .getX())
                    .y(ROUTE_1.getCenter()
                            .getY())
                    .z(ROUTE_1.getCenter()
                            .getZ())
                    .build())
            .grade(NEW_PITCH_1.getGrade())
            .gradeModifier(NEW_PITCH_1.getGradeModifier())
            .danger(NEW_PITCH_1.getDanger())
            .mainImageLocation(ROUTE_1.getMainImageLocation())
            .protection(ROUTE_1.getProtection())
            .style(ROUTE_1.getStyle())
            .first(ROUTE_1.isFirst())
            .next(ROUTE_1.getNext())
            .build();
    private static final Route UPDATED_ROUTE_2 = Route.builder()
            .routeId(ROUTE_2.getRouteId())
            .wallId(ROUTE_2.getWallId())
            .name(ROUTE_2.getName())
            .description(ROUTE_2.getDescription())
            .grade(12)
            .gradeModifier("d")
            .danger("X")
            .mainImageLocation(ROUTE_2.getMainImageLocation())
            .protection(ROUTE_2.getProtection())
            .style(ROUTE_2.getStyle())
            .first(ROUTE_2.isFirst())
            .next(ROUTE_2.getNext())
            .build();
    private static final Route UPDATED_ROUTE_1_FROM_DELETION = Route.builder()
            .routeId(ROUTE_1.getRouteId())
            .wallId(ROUTE_1.getWallId())
            .name(ROUTE_1.getName())
            .description(ROUTE_1.getDescription())
            .center(Center.builder()
                    .x(ROUTE_1.getCenter()
                            .getX())
                    .y(ROUTE_1.getCenter()
                            .getY())
                    .z(ROUTE_1.getCenter()
                            .getZ())
                    .build())
            .grade(PITCH_2.getGrade())
            .gradeModifier(PITCH_2.getGradeModifier())
            .danger(PITCH_2.getDanger())
            .mainImageLocation(ROUTE_1.getMainImageLocation())
            .protection(ROUTE_1.getProtection())
            .style(ROUTE_1.getStyle())
            .first(ROUTE_1.isFirst())
            .next(ROUTE_1.getNext())
            .build();
    private static final Route UPDATED_ROUTE_1_NO_GRADES = Route.builder()
            .routeId(ROUTE_1.getRouteId())
            .wallId(ROUTE_1.getWallId())
            .name(ROUTE_1.getName())
            .description(ROUTE_1.getDescription())
            .center(Center.builder()
                    .x(ROUTE_1.getCenter()
                            .getX())
                    .y(ROUTE_1.getCenter()
                            .getY())
                    .z(ROUTE_1.getCenter()
                            .getZ())
                    .build())
            .mainImageLocation(ROUTE_1.getMainImageLocation())
            .protection(ROUTE_1.getProtection())
            .style(ROUTE_1.getStyle())
            .first(ROUTE_1.isFirst())
            .next(ROUTE_1.getNext())
            .build();
    private static final PitchNotFoundException PITCH_NOT_FOUND_EXCEPTION = new PitchNotFoundException(
            PITCH_1.getPitchId());
    private static final RouteNotFoundException ROUTE_NOT_FOUND_EXCEPTION = new RouteNotFoundException(
            PITCH_1.getRouteId());
    private static final PitchNotEmptyException PITCH_NOT_EMPTY_EXCEPTION = new PitchNotEmptyException(
            PITCH_1.getPitchId());
    private static final CreatePitchResult CREATE_PITCH_RESULT = CreatePitchResult.builder()
            .pitchId(PITCH_1.getPitchId())
            .build();
    private static final int DEPTH = 5;
    private static final Point POINT_1 = Point.builder()
            .pointId("point-1")
            .pitchId(PITCH_1.getPitchId())
            .build();
    private static final Point POINT_2 = Point.builder()
            .pointId("point-2")
            .pitchId(PITCH_1.getPitchId())
            .build();

    @Mock
    private OrderableResourceWithParentControllerDelegate<Pitch, NewPitch, Route>
            mockOrderableResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Pitch, NewPitch> mockResourceWithChildrenControllerDelegate;
    @Mock
    private PitchesDao mockPitchesDao;
    @Mock
    private RoutesDao mockRoutesDao;
    @Mock
    private PointsDao mockPointsDao;
    @Mock
    private PitchFactory mockPitchFactory;
    @Mock
    private PitchNotFoundExceptionFactory mockPitchNotFoundExceptionFactory;
    @Mock
    private RouteNotFoundExceptionFactory mockRouteNotFoundExceptionFactory;
    @Mock
    private PitchNotEmptyExceptionFactory mockPitchNotEmptyExceptionFactory;
    @Mock
    private CreatePitchResultFactory mockCreatePitchResultFactory;
    @Mock
    private PitchConsistencyWaiter mockPitchConsistencyWaiter;

    private PitchController pitchController;

    @BeforeEach
    void setUp() {
        pitchController = PitchController.builder()
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .orderableResourceWithParentControllerDelegate(mockOrderableResourceWithParentControllerDelegate)
                .pitchesDao(mockPitchesDao)
                .routesDao(mockRoutesDao)
                .pointsDao(mockPointsDao)
                .pitchFactory(mockPitchFactory)
                .pitchNotFoundExceptionFactory(mockPitchNotFoundExceptionFactory)
                .routeNotFoundExceptionFactory(mockRouteNotFoundExceptionFactory)
                .pitchNotEmptyExceptionFactory(mockPitchNotEmptyExceptionFactory)
                .createPitchResultFactory(mockCreatePitchResultFactory)
                .pitchConsistencyWaiter(mockPitchConsistencyWaiter)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(pitchController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt())).thenReturn(PITCH_1);
        assertThat(pitchController.getResource(PITCH_2.getPitchId(), DEPTH), is(equalTo(PITCH_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(PITCH_2.getPitchId(), DEPTH);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Pitch> pitches = ImmutableList.of(PITCH_1, PITCH_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                pitches);
        assertThat(pitchController.getResourcesForParent(PITCH_1.getRouteId(), false), is(equalTo(pitches)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(PITCH_1.getRouteId(), false);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Pitch> pitches = ImmutableList.of(PITCH_1, PITCH_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean())).thenReturn(
                pitches);
        assertThat(pitchController.getResourcesForParent(PITCH_1.getRouteId(), true), is(equalTo(pitches)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(PITCH_1.getRouteId(), true);
    }

    @Test
    void createResource_throwsRouteNotFoundException_whenParentDoesNotExist() {
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.empty());
        when(mockRouteNotFoundExceptionFactory.create(any())).thenReturn(ROUTE_NOT_FOUND_EXCEPTION);

        assertThrows(RouteNotFoundException.class, () -> pitchController.createResource(NEW_PITCH_1));

        verify(mockRoutesDao).getResource(ROUTE_1.getId());
        //noinspection ThrowableNotThrown
        verify(mockRouteNotFoundExceptionFactory).create(NEW_PITCH_1.getRouteId());
        verify(mockRoutesDao, never()).saveResource(any());
        verify(mockPitchesDao, never()).saveResource(any());
    }

    @Test
    void createResource_createsPitchAndUpdatesRoute()
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.of(ROUTE_1));
        when(mockPitchFactory.create(any())).thenReturn(PITCH_1);
        when(mockPitchesDao.getResources(any())).thenReturn(ImmutableSet.of(PITCH_1, PITCH_2, PITCH_3));
        when(mockCreatePitchResultFactory.create(any())).thenReturn(CREATE_PITCH_RESULT);

        assertThat(pitchController.createResource(NEW_PITCH_1), is(equalTo(CREATE_PITCH_RESULT)));

        verify(mockRoutesDao).getResource(ROUTE_1.getId());
        verify(mockPitchFactory).create(NEW_PITCH_1);
        verify(mockPitchesDao).saveResource(PITCH_1);
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_1.getRouteId(), PITCH_1, true);
        verify(mockPitchesDao).getResources(ROUTE_1.getId());
        verify(mockRoutesDao).saveResource(UPDATED_ROUTE_1_FROM_NEW_PITCH);
        verify(mockCreatePitchResultFactory).create(PITCH_1.getPitchId());
    }

    @Test
    void updateResource_throwsPitchNotFoundException_whenPitchDoesNotExist() {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.empty());
        when(mockPitchNotFoundExceptionFactory.create(any())).thenReturn(PITCH_NOT_FOUND_EXCEPTION);

        assertThrows(PitchNotFoundException.class, () -> pitchController.updateResource(UPDATED_PITCH_1_SAME_ROUTE));

        verify(mockPitchesDao).getResource(UPDATED_PITCH_1_SAME_ROUTE.getPitchId());
        //noinspection ThrowableNotThrown
        verify(mockPitchNotFoundExceptionFactory).create(UPDATED_PITCH_1_SAME_ROUTE.getPitchId());
    }

    @Test
    void updateResource_throwsRouteNotFoundException_whenRouteDoesNotExist() {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.empty());
        when(mockRouteNotFoundExceptionFactory.create(any())).thenReturn(ROUTE_NOT_FOUND_EXCEPTION);

        assertThrows(RouteNotFoundException.class, () -> pitchController.updateResource(UPDATED_PITCH_1_NEW_ROUTE));

        verify(mockPitchesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getPitchId());
        verify(mockRoutesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        //noinspection ThrowableNotThrown
        verify(mockRouteNotFoundExceptionFactory).create(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
    }

    @Test
    void updateResource_updatesPitchAndRoute_whenRouteIsNotChangedAndExists()
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.of(ROUTE_1));
        when(mockPitchesDao.getResources(any())).thenReturn(
                ImmutableSet.of(UPDATED_PITCH_1_SAME_ROUTE, PITCH_2, PITCH_3));

        assertThat(pitchController.updateResource(UPDATED_PITCH_1_SAME_ROUTE), is(equalTo(UpdateResourceResult.builder()
                .successful(true)
                .build())));

        verify(mockPitchesDao).getResource(UPDATED_PITCH_1_SAME_ROUTE.getId());
        verify(mockRoutesDao).getResource(ROUTE_1.getId());
        verify(mockPitchesDao).saveResource(UPDATED_PITCH_1_SAME_ROUTE);
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_1.getRouteId(), UPDATED_PITCH_1_SAME_ROUTE, true);
        verify(mockPitchesDao).getResources(UPDATED_PITCH_1_SAME_ROUTE.getRouteId());
        verify(mockRoutesDao).saveResource(UPDATED_ROUTE_1_FROM_UPDATED_PITCH);
    }

    // this case technically shouldn't happen, but it's possible if the database is modified manually, or
    // potentially in a rare race condition
    @Test
    void updateResource_updatesPitchAndNewRoute_whenRouteIsChangedAndOldRouteDoesNotExist()
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        doReturn(Optional.of(ROUTE_2)).when(mockRoutesDao)
                .getResource(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        when(mockPitchesDao.getResources(any())).thenReturn(
                ImmutableSet.of(UPDATED_PITCH_1_NEW_ROUTE, PITCH_2, PITCH_3));
        doReturn(Optional.empty()).when(mockRoutesDao)
                .getResource(PITCH_1.getRouteId());

        assertThat(pitchController.updateResource(UPDATED_PITCH_1_NEW_ROUTE), is(equalTo(UpdateResourceResult.builder()
                .successful(true)
                .build())));

        verify(mockPitchesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getPitchId());
        verify(mockRoutesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        verify(mockPitchesDao).saveResource(UPDATED_PITCH_1_NEW_ROUTE);
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_2.getRouteId(), UPDATED_PITCH_1_NEW_ROUTE, true);
        verify(mockPitchesDao).getResources(ROUTE_2.getRouteId());
        verify(mockRoutesDao).getResource(ROUTE_2.getRouteId());
        verify(mockRoutesDao).saveResource(UPDATED_ROUTE_2);
        verify(mockRoutesDao).getResource(PITCH_1.getRouteId());
        verify(mockRoutesDao, never()).saveResource(UPDATED_ROUTE_1_FROM_UPDATED_PITCH);
        verify(mockPitchesDao, never()).getResources(ROUTE_1.getRouteId());
    }

    @Test
    void updateResource_updatesPitchAndBothRoutes_whenRouteIsChangedAndBothRoutesExist()
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        doReturn(Optional.of(ROUTE_2)).when(mockRoutesDao)
                .getResource(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        doReturn(ImmutableSet.of(UPDATED_PITCH_1_NEW_ROUTE, PITCH_2, PITCH_3)).when(mockPitchesDao)
                .getResources(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        doReturn(Optional.of(ROUTE_1)).when(mockRoutesDao)
                .getResource(PITCH_1.getRouteId());
        doReturn(ImmutableSet.of(PITCH_4, PITCH_5)).when(mockPitchesDao)
                .getResources(PITCH_1.getRouteId());

        assertThat(pitchController.updateResource(UPDATED_PITCH_1_NEW_ROUTE), is(equalTo(UpdateResourceResult.builder()
                .successful(true)
                .build())));

        verify(mockPitchesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getPitchId());
        verify(mockRoutesDao).getResource(UPDATED_PITCH_1_NEW_ROUTE.getRouteId());
        verify(mockPitchesDao).saveResource(UPDATED_PITCH_1_NEW_ROUTE);
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_2.getRouteId(), UPDATED_PITCH_1_NEW_ROUTE, true);
        verify(mockPitchesDao).getResources(ROUTE_2.getRouteId());
        verify(mockRoutesDao).getResource(ROUTE_2.getRouteId());
        verify(mockRoutesDao).saveResource(UPDATED_ROUTE_2);
        verify(mockRoutesDao).getResource(PITCH_1.getRouteId());
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_1.getRouteId(), UPDATED_PITCH_1_NEW_ROUTE, false);
        verify(mockPitchesDao).getResources(ROUTE_1.getRouteId());
        verify(mockRoutesDao).saveResource(UPDATED_ROUTE_1_FROM_DELETION);
    }

    @Test
    void deleteResource_throwsPitchNotFoundException_whenPitchDoesNotExist() {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.empty());
        when(mockPitchNotFoundExceptionFactory.create(any())).thenReturn(PITCH_NOT_FOUND_EXCEPTION);
        assertThrows(PitchNotFoundException.class, () -> pitchController.deleteResource(PITCH_1.getPitchId()));
        verify(mockPitchesDao).getResource(PITCH_1.getPitchId());
        //noinspection ThrowableNotThrown
        verify(mockPitchNotFoundExceptionFactory).create(PITCH_1.getPitchId());
    }

    @Test
    void deleteResource_throwsPitchNotEmptyException_whenPitchHasChildren() {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        when(mockPointsDao.getResources(any())).thenReturn(ImmutableSet.of(POINT_1, POINT_2));
        when(mockPitchNotEmptyExceptionFactory.create(any())).thenReturn(PITCH_NOT_EMPTY_EXCEPTION);
        assertThrows(PitchNotEmptyException.class, () -> pitchController.deleteResource(PITCH_1.getPitchId()));
        verify(mockPitchesDao).getResource(PITCH_1.getPitchId());
        verify(mockPointsDao).getResources(PITCH_1.getPitchId());
        //noinspection ThrowableNotThrown
        verify(mockPitchNotEmptyExceptionFactory).create(PITCH_1.getPitchId());
    }

    // this case technically shouldn't happen, but it's possible if the database is modified manually, or
    // potentially in a rare race condition
    @Test
    void deletePitch_deletesPitch_whenPitchIsEmptyAndRouteDoesNotExist()
            throws ResourceNotFoundException, ResourceNotEmptyException, PitchConsistencyException,
            InterruptedException {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        when(mockPointsDao.getResources(any())).thenReturn(ImmutableSet.of());
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.empty());

        assertThat(pitchController.deleteResource(PITCH_1.getPitchId()), is(equalTo(DeleteResourceResult.builder()
                .successful(true)
                .build())));

        verify(mockPitchesDao).getResource(PITCH_1.getPitchId());
        verify(mockPointsDao).getResources(PITCH_1.getPitchId());
        verify(mockRoutesDao).getResource(PITCH_1.getRouteId());
        verify(mockPitchesDao).deleteResource(PITCH_1.getPitchId());
        verify(mockRoutesDao, never()).saveResource(any());
    }

    @Test
    void deletePitch_deletesPitchAndUpdatesRoute_whenPitchIsEmptyAndRouteHasOtherPitches()
            throws ResourceNotFoundException, ResourceNotEmptyException, PitchConsistencyException,
            InterruptedException {
        runDeletePitchTest(ImmutableSet.of(PITCH_2, PITCH_3), UPDATED_ROUTE_1_FROM_DELETION);
    }

    @Test
    void deletePitch_deletesPitchAndUpdatesRoute_whenPitchIsEmptyAndRouteHasNoOtherPitches()
            throws ResourceNotFoundException, ResourceNotEmptyException, PitchConsistencyException,
            InterruptedException {
        runDeletePitchTest(ImmutableSet.of(), UPDATED_ROUTE_1_NO_GRADES);
    }

    private void runDeletePitchTest(Set<Pitch> siblingPitches, Route expectedUpdatedRoute)
            throws InterruptedException, PitchConsistencyException, ResourceNotFoundException,
            ResourceNotEmptyException {
        when(mockPitchesDao.getResource(any())).thenReturn(Optional.of(PITCH_1));
        when(mockPointsDao.getResources(any())).thenReturn(ImmutableSet.of());
        when(mockRoutesDao.getResource(any())).thenReturn(Optional.of(ROUTE_1));
        when(mockPitchesDao.getResources(any())).thenReturn(siblingPitches);

        assertThat(pitchController.deleteResource(PITCH_1.getPitchId()), is(equalTo(DeleteResourceResult.builder()
                .successful(true)
                .build())));

        verify(mockPitchesDao).getResource(PITCH_1.getPitchId());
        verify(mockPointsDao).getResources(PITCH_1.getPitchId());
        verify(mockRoutesDao).getResource(PITCH_1.getRouteId());
        verify(mockPitchConsistencyWaiter).waitForConsistency(ROUTE_1.getRouteId(), PITCH_1, false);
        verify(mockPitchesDao).getResources(ROUTE_1.getRouteId());
        verify(mockPitchesDao).deleteResource(PITCH_1.getPitchId());
        verify(mockRoutesDao).saveResource(expectedUpdatedRoute);
    }
}

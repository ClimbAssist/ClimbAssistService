package com.climbassist.api.resource.route;

import com.climbassist.api.resource.MultipartFileTestUtils;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.image.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.image.UploadImageResult;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.user.UserData;
import com.climbassist.common.s3.S3Proxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    private static final String ROUTE_1_ID = "crag-1";
    private static final String IMAGES_BUCKET_NAME = "photos";
    private static final String IMAGE_KEY = String.format("%s/%s.webp", ROUTE_1_ID, ROUTE_1_ID);
    private static final Route ROUTE_1 = Route.builder()
            .routeId(ROUTE_1_ID)
            .wallId("wall-1")
            .name("Route 1")
            .description("Route 1")
            .center(Center.builder()
                    .x(1.0)
                    .y(2.0)
                    .z(3.0)
                    .build())
            .grade(1)
            .gradeModifier("a")
            .mainImageLocation(String.format("https://%s.s3.amazonaws.com/%s", IMAGES_BUCKET_NAME, IMAGE_KEY))
            .protection("protection")
            .style("sport")
            .first(true)
            .next("route-2")
            .build();
    private static final Route ROUTE_2 = Route.builder()
            .routeId("route-2")
            .wallId("wall-1")
            .name("Route 2")
            .description("Route 2")
            .center(Center.builder()
                    .x(4.0)
                    .y(5.0)
                    .z(6.0)
                    .build())
            .grade(2)
            .gradeModifier("b")
            .protection("protection")
            .style("trad")
            .next("route-3")
            .build();
    private static final NewRoute NEW_ROUTE_1 = NewRoute.builder()
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
            .protection(ROUTE_1.getProtection())
            .style(ROUTE_1.getStyle())
            .first(ROUTE_1.getFirst())
            .next(ROUTE_1.getNext())
            .build();
    private static final Route UPDATED_ROUTE_1 = Route.builder()
            .routeId(ROUTE_1.getRouteId())
            .wallId("wall-2")
            .name("New name")
            .description("New description")
            .center(Center.builder()
                    .x(4.0)
                    .y(5.0)
                    .z(6.0)
                    .build())
            .grade(2)
            .gradeModifier("b")
            .mainImageLocation(ROUTE_1.getMainImageLocation())
            .protection("new protection")
            .style("trad")
            .first(false)
            .next("route-3")
            .build();
    private static final MultipartFile IMAGE = MultipartFileTestUtils.buildMultipartFile("image.webp", "image");
    private static final DeleteResourceResult DELETE_RESOURCE_RESULT = DeleteResourceResult.builder()
            .successful(true)
            .build();
    private static final Pitch PITCH = Pitch.builder()
            .pitchId("pitch-1")
            .routeId(ROUTE_1.getId())
            .build();
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
    private ResourceWithParentControllerDelegate<Route, NewRoute, Wall> mockResourceWithParentControllerDelegate;
    @Mock
    private OrderableResourceWithParentControllerDelegate<Route, NewRoute, Wall>
            mockOrderableResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Route, NewRoute> mockResourceWithChildrenControllerDelegate;
    @Mock
    private ResourceWithImageControllerDelegate<Route> mockResourceWithImageControllerDelegate;
    @Mock
    private RoutesDao mockRoutesDao;
    @Mock
    private S3Proxy mockS3Proxy;
    @Mock
    private RouteNotFoundExceptionFactory mockRouteNotFoundExceptionFactory;
    @Mock
    private PitchesDao mockPitchesDao;
    @Mock
    private RouteNotEmptyExceptionFactory mockRouteNotEmptyExceptionFactory;

    private RouteController routeController;

    @BeforeEach
    void setUp() {
        routeController = RouteController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .orderableResourceWithParentControllerDelegate(mockOrderableResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .resourceWithImageControllerDelegate(mockResourceWithImageControllerDelegate)
                .routesDao(mockRoutesDao)
                .s3Proxy(mockS3Proxy)
                .routeNotFoundExceptionFactory(mockRouteNotFoundExceptionFactory)
                .pitchesDao(mockPitchesDao)
                .routeNotEmptyExceptionFactory(mockRouteNotEmptyExceptionFactory)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(routeController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(ROUTE_1);
        assertThat(routeController.getResource(ROUTE_1.getRouteId(), DEPTH, MAYBE_USER_DATA), is(equalTo(ROUTE_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(ROUTE_1.getRouteId(), DEPTH, MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsFalse()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Route> routes = ImmutableList.of(ROUTE_1, ROUTE_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean(),
                any())).thenReturn(routes);
        assertThat(routeController.getResourcesForParent(ROUTE_1.getWallId(), false, MAYBE_USER_DATA),
                is(equalTo(routes)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(ROUTE_1.getWallId(), false,
                MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsOrderableResourceWithParentControllerDelegate_whenOrderedIsTrue()
            throws ResourceNotFoundException, InvalidOrderingException {
        List<Route> routes = ImmutableList.of(ROUTE_1, ROUTE_2);
        when(mockOrderableResourceWithParentControllerDelegate.getResourcesForParent(any(), anyBoolean(),
                any())).thenReturn(routes);
        assertThat(routeController.getResourcesForParent(ROUTE_1.getWallId(), true, MAYBE_USER_DATA),
                is(equalTo(routes)));
        verify(mockOrderableResourceWithParentControllerDelegate).getResourcesForParent(ROUTE_1.getWallId(), true,
                MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateRouteResult createRouteResult = CreateRouteResult.builder()
                .routeId(ROUTE_1.getRouteId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(createRouteResult);
        assertThat(routeController.createResource(NEW_ROUTE_1, MAYBE_USER_DATA), is(equalTo(createRouteResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_ROUTE_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(routeController.updateResource(UPDATED_ROUTE_1, MAYBE_USER_DATA), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_ROUTE_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_deletesResourceAndImage_whenResourceExistsAndIsEmptyAndHasImageLocation()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockRoutesDao.getResource(any(), any())).thenReturn(Optional.of(ROUTE_1));
        when(mockPitchesDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        assertThat(routeController.deleteResource(ROUTE_1.getId(), MAYBE_USER_DATA),
                is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockRoutesDao).getResource(ROUTE_1.getId(), MAYBE_USER_DATA);
        verify(mockPitchesDao).getResources(ROUTE_1.getId(), MAYBE_USER_DATA);
        verify(mockRoutesDao).deleteResource(ROUTE_1.getId());
        verify(mockS3Proxy).deleteObject(IMAGES_BUCKET_NAME, IMAGE_KEY);
    }

    @Test
    void deleteResource_deletesResource_whenResourceExistsAndIsEmptyAndDoesNotHaveImageLocation()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockRoutesDao.getResource(any(), any())).thenReturn(Optional.of(ROUTE_2));
        when(mockPitchesDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        assertThat(routeController.deleteResource(ROUTE_2.getId(), MAYBE_USER_DATA),
                is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockRoutesDao).getResource(ROUTE_2.getId(), MAYBE_USER_DATA);
        verify(mockPitchesDao).getResources(ROUTE_2.getId(), MAYBE_USER_DATA);
        verify(mockRoutesDao).deleteResource(ROUTE_2.getId());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void deleteResource_throwsResourceNotFoundException_whenResourceDoesNotExist() {
        when(mockRoutesDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockRouteNotFoundExceptionFactory.create(any())).thenReturn(new RouteNotFoundException(ROUTE_1.getId()));
        assertThrows(RouteNotFoundException.class,
                () -> routeController.deleteResource(ROUTE_1.getId(), MAYBE_USER_DATA));
        verify(mockRoutesDao).getResource(ROUTE_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockRouteNotFoundExceptionFactory).create(ROUTE_1.getId());
        verify(mockRoutesDao, never()).deleteResource(any());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void deleteResource_throwsResourceNotEmptyException_whenResourceIsNotEmpty() {
        when(mockRoutesDao.getResource(any(), any())).thenReturn(Optional.of(ROUTE_1));
        when(mockPitchesDao.getResources(any(), any())).thenReturn(ImmutableSet.of(PITCH));
        when(mockRouteNotEmptyExceptionFactory.create(any())).thenReturn(new RouteNotEmptyException(ROUTE_1.getId()));
        assertThrows(RouteNotEmptyException.class,
                () -> routeController.deleteResource(ROUTE_1.getId(), MAYBE_USER_DATA));
        verify(mockRoutesDao).getResource(ROUTE_1.getId(), MAYBE_USER_DATA);
        verify(mockPitchesDao).getResources(ROUTE_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockRouteNotEmptyExceptionFactory).create(ROUTE_1.getId());
        verify(mockRoutesDao, never()).deleteResource(any());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void uploadImage_callsResourceWithImageAndChildrenControllerDelegate()
            throws ResourceNotFoundException, IOException {
        UploadImageResult uploadImageResult = UploadImageResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithImageControllerDelegate.uploadImage(any(), any(), any())).thenReturn(uploadImageResult);
        assertThat(routeController.uploadImage(ROUTE_1.getRouteId(), IMAGE, MAYBE_USER_DATA),
                is(equalTo(uploadImageResult)));
        verify(mockResourceWithImageControllerDelegate).uploadImage(ROUTE_1.getRouteId(), IMAGE, MAYBE_USER_DATA);
    }
}

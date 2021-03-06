package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.image.UploadImageResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.route.Center;
import com.climbassist.api.resource.route.CreateRouteResult;
import com.climbassist.api.resource.route.NewRoute;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.user.TestUserManager;
import com.climbassist.test.integration.api.user.TestUserManagerConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.climbassist.test.integration.s3.S3Configuration;
import com.climbassist.test.integration.s3.S3Proxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(
        classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class, S3Configuration.class,
                TestUserManagerConfiguration.class})
public class RouteIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final String DESCRIPTION = "integ";
    private static final int RESOURCE_DEPTH = 6;
    private static final File IMAGE_1_JPG = new File("testData/image-1.jpg");
    private static final File IMAGE_1_WEBP = new File("testData/image-1.webp");
    private static final File IMAGE_2_JPG = new File("testData/image-2.jpg");
    private static final File IMAGE_2_WEBP = new File("testData/image-2.webp");
    private static final File INVALID_JPG = new File("testData/not-an-image.txt");

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private S3Proxy s3Proxy;

    private String username;
    private Set<Cookie> cookies;

    @BeforeClass
    public void setUpClass() throws IOException {
        username = TestIdGenerator.generateTestId();
        cookies = testUserManager.createVerifyAndSignInTestUser(username);
    }

    @AfterMethod
    public void tearDown() {
        resourceManager.cleanUp(cookies);
        testUserManager.makeUserNotAdministrator(username);
    }

    @AfterClass
    public void tearDownClass() {
        testUserManager.cleanUp();
    }

    @Test
    public void getRoute_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        ApiResponse<Route> apiResponse = climbAssistClient.getRoute("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getRoute_returnsRouteWithNoChildren_whenDepthIsNotSpecifiedAndRouteHasChildren() {
        runGetRouteTest(1, Optional.empty());
    }

    @Test
    public void getRoute_returnsRouteWithNoChildren_whenDepthIsZeroAndRouteHasChildren() {
        runGetRouteTest(2, Optional.of(0));
    }

    @Test
    public void getRoute_returnsRouteWithNoChildren_whenDepthIsGreaterThanZeroAndRouteHasNoChildren() {
        runGetRouteTest(0, Optional.of(1));
    }

    @Test
    public void getRoute_returnsRouteWithChildren_whenDepthIsEqualToChildDepth() {
        runGetRouteTest(2, Optional.of(2));
    }

    @Test
    public void getRoute_returnsRouteWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetRouteTest(2, Optional.of(5));
    }

    @Test
    public void getRoute_returnsRouteWithAllChildren_whenRouteHasFullDepthOfChildren() {
        runGetRouteTest(3, Optional.of(3));
    }

    @Test
    public void listRoutes_returnsWallNotFoundException_whenWallDoesNotExist() {
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listRoutes_returnsEmptyList_whenThereAreNoRoutes() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listRoutes_returnsSingleRoute_whenThereIsOnlyOneRoute() {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(route.getWallId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableList.of(route))));
    }

    @Test
    public void listRoutes_listsAllRoutesInAnyOrder_whenOrderedIsNotSpecified() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Route> routes = resourceManager.createRoutes(wall.getWallId(), cookies);
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(routes.toArray()));
    }

    @Test
    public void listRoutes_listsAllRoutesInOrder_whenOrderedIsTrue() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Route> routes = resourceManager.createRoutes(wall.getWallId(), cookies);
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), true, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(routes)));
    }

    @Test
    public void listRoutes_listsAllRoutesInAnyOrder_whenOrderedIsFalse() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Route> routes = resourceManager.createRoutes(wall.getWallId(), cookies);
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), false, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(routes.toArray()));
    }

    @Test
    public void listRoutes_returnsInvalidOrderingException_whenOrderedIsTrueAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.createRoute(wall.getWallId(), cookies, true, 0);
        resourceManager.createRoute(wall.getWallId(), cookies, true, 0);
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), true, cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "InvalidOrderingException");
    }

    @Test
    public void listRoutes_returnsRoutesInAnyOrder_whenOrderedIsFalseAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Route route1 = resourceManager.createRoute(wall.getWallId(), cookies, true, 0);
        Route route2 = resourceManager.createRoute(wall.getWallId(), cookies, true, 0);
        ApiResponse<List<Route>> apiResponse = climbAssistClient.listRoutes(wall.getWallId(), false, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(route1, route2));
    }

    @Test
    public void createRoute_returnsWallNotFoundException_whenWallDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateRouteResult> apiResponse = climbAssistClient.createRoute(NewRoute.builder()
                .wallId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .style("trad")
                .protection("protection")
                .center(Center.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createRoute_createsRoute() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Route expectedRoute = resourceManager.createRoute(wall.getWallId(), cookies, true, 0);
        Route actualRoute = climbAssistClient.getRoute(expectedRoute.getRouteId(), cookies)
                .getData();
        assertThat(actualRoute, is(equalTo(expectedRoute)));
    }

    @Test
    public void createRoute_createsRoute_whenRouteWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Route expectedRoute2 = resourceManager.createRoute(wall.getWallId(), cookies, false, 0);
        Route expectedRoute1 =
                resourceManager.createRoute(wall.getWallId(), cookies, true, expectedRoute2.getRouteId(), 0);
        Route actualRoute1 = climbAssistClient.getRoute(expectedRoute1.getRouteId(), cookies)
                .getData();
        Route actualRoute2 = climbAssistClient.getRoute(expectedRoute2.getRouteId(), cookies)
                .getData();
        assertThat(actualRoute1, is(equalTo(expectedRoute1)));
        assertThat(actualRoute2, is(equalTo(expectedRoute2)));
        assertThat(actualRoute1.getRouteId(), is(not(equalTo(actualRoute2.getRouteId()))));
    }

    @Test
    public void createRoute_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateRouteResult> apiResponse = climbAssistClient.createRoute(NewRoute.builder()
                .wallId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createRoute_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateRouteResult> apiResponse = climbAssistClient.createRoute(NewRoute.builder()
                .wallId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updateRoute_returnsWallNotFoundException_whenWallDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRoute(Route.builder()
                .routeId(route.getRouteId())
                .wallId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .style("trad")
                .protection("protection")
                .center(Center.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateRoute_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = ResourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRoute(Route.builder()
                .routeId("does-not-exist")
                .wallId(wall.getWallId())
                .name(NAME)
                .description(DESCRIPTION)
                .style("trad")
                .protection("protection")
                .center(Center.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateRoute_updatesRoute() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Route originalRoute = ResourceManager.getRoute(country);
        Route nextRoute = resourceManager.createRoute(originalRoute.getWallId(), cookies, true, 0);
        Route updatedRoute = Route.builder()
                .routeId(originalRoute.getRouteId())
                .wallId(resourceManager.createWall(ResourceManager.getCrag(country)
                        .getCragId(), cookies, true, 0)
                        .getWallId())
                .name(NAME + "-updated")
                .description(DESCRIPTION)
                .style("sport")
                .protection("protection updated")
                .center(Center.builder()
                        .x(2.0)
                        .y(2.0)
                        .z(2.0)
                        .build())
                .first(false)
                .next(nextRoute.getRouteId())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRoute(updatedRoute, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        updatedRoute.setFirst(null);
        Route actualRoute = climbAssistClient.getRoute(originalRoute.getRouteId(), cookies)
                .getData();
        assertThat(actualRoute, is(equalTo(updatedRoute)));
    }

    @Test
    public void uploadImage_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UploadImageResult> apiResponse =
                climbAssistClient.uploadRouteImage("does-not-exist", IMAGE_1_JPG, ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void uploadImage_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<UploadImageResult> apiResponse =
                climbAssistClient.uploadRouteImage("does-not-exist", IMAGE_1_JPG, cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void uploadImage_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<UploadImageResult> apiResponse =
                climbAssistClient.uploadRouteImage("does-not-exist", IMAGE_1_JPG, cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void uploadImage_uploadsImage() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadImage(route.getRouteId(), IMAGE_1_JPG, IMAGE_1_WEBP);
    }

    @Test
    public void uploadImage_replacesOldImage_whenRouteAlreadyHasAnImage() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadImage(route.getRouteId(), IMAGE_1_JPG, IMAGE_1_WEBP);
        uploadImage(route.getRouteId(), IMAGE_2_JPG, IMAGE_2_WEBP);
    }

    @Test
    public void uploadImage_returnsWebpConverterException_whenImageIsNotAValidJpg() {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UploadImageResult> apiResponse =
                climbAssistClient.uploadRouteImage(route.getRouteId(), INVALID_JPG, cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 400, "WebpConverterException");
    }

    @Test
    public void deleteRoute_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse =
                climbAssistClient.deleteRoute("does-not-exist", ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteRoute_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRoute("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteRoute_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRoute("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deleteRoute_deletesRoute() {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Route> getRouteResult = climbAssistClient.getRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertResourceNotFoundException(getRouteResult);
    }

    @Test
    public void deleteRoute_returnsResourceNotEmptyException_whenRouteHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    @Test
    public void deleteRoute_deletesImages_whenRouteHasImages() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadImage(route.getRouteId(), IMAGE_1_JPG, IMAGE_1_WEBP);
        route = climbAssistClient.getRoute(route.getRouteId(), cookies)
                .getData();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Route> getRouteResult = climbAssistClient.getRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertResourceNotFoundException(getRouteResult);
        s3Proxy.assertS3ObjectDoesNotExist(route.getImageLocation());
        s3Proxy.assertS3ObjectDoesNotExist(route.getJpgImageLocation());
    }

    private void runGetRouteTest(int actualDepth,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Route route = ResourceManager.getRoute(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(route, Route.class, maybeRequestDepth.orElse(0));
        ApiResponse<Route> apiResponse = maybeRequestDepth.isPresent() ?
                climbAssistClient.getRoute(route.getRouteId(), maybeRequestDepth.get(), cookies) :
                climbAssistClient.getRoute(route.getRouteId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(route)));
    }

    private void uploadImage(String routeId, File jpgImage, File webpImage) throws IOException {
        Route originalRoute = climbAssistClient.getRoute(routeId, cookies)
                .getData();
        ApiResponse<UploadImageResult> apiResponse = climbAssistClient.uploadRouteImage(routeId, jpgImage, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Route actualRoute = climbAssistClient.getRoute(routeId, cookies)
                .getData();
        assertThat(actualRoute.getImageLocation(), is(not(nullValue())));
        assertThat(actualRoute.getJpgImageLocation(), is(not(nullValue())));
        originalRoute.setMainImageLocation(actualRoute.getImageLocation());
        originalRoute.setJpgMainImageLocation(actualRoute.getJpgImageLocation());
        assertThat(actualRoute, is(equalTo(originalRoute)));

        s3Proxy.assertS3ObjectEquals(actualRoute.getJpgImageLocation(), new FileInputStream(jpgImage));
        s3Proxy.assertS3ObjectEquals(actualRoute.getImageLocation(), new FileInputStream(webpImage));
    }
}

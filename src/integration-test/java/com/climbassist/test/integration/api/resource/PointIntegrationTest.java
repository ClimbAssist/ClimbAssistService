package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.point.BatchCreatePointsResult;
import com.climbassist.api.resource.point.BatchNewPoint;
import com.climbassist.api.resource.point.CreatePointResult;
import com.climbassist.api.resource.point.NewPoint;
import com.climbassist.api.resource.point.Point;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.user.TestUserManager;
import com.climbassist.test.integration.api.user.TestUserManagerConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class PointIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final int RESOURCE_DEPTH = 8;

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;
    @Autowired
    private ResourceManager resourceManager;

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
    public void getPoint_returnsPointNotFoundException_whenPointDoesNotExist() {
        ApiResponse<Point> apiResponse = climbAssistClient.getPoint("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getPoint_returnsPoint() {
        testUserManager.makeUserAdministrator(username);
        Point point = ResourceManager.getPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Point> apiResponse = climbAssistClient.getPoint(point.getPointId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(point)));
    }

    @Test
    public void listPoints_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listPoints_returnsEmptyList_whenThereAreNoPoints() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listPoints_returnsSinglePoint_whenThereIsOnlyOnePoint() {
        testUserManager.makeUserAdministrator(username);
        Point point = ResourceManager.getPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(point.getPitchId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableList.of(point))));
    }

    @Test
    public void listPoints_listsAllPointsInAnyOrder_whenOrderedIsNotSpecified() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Point> points = resourceManager.createPoints(pitch.getPitchId(), cookies);
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(points.toArray()));
    }

    @Test
    public void listPoints_listsAllPointsInOrder_whenOrderedIsTrue() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Point> points = resourceManager.createPoints(pitch.getPitchId(), cookies);
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId(), true);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(points)));
    }

    @Test
    public void listPoints_listsAllPointsInAnyOrder_whenOrderedIsFalse() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Point> points = resourceManager.createPoints(pitch.getPitchId(), cookies);
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(points.toArray()));
    }

    @Test
    public void listPoints_returnsInvalidOrderingException_whenOrderedIsTrueAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.createPoint(pitch.getPitchId(), cookies, true);
        resourceManager.createPoint(pitch.getPitchId(), cookies, true);
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId(), true);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "InvalidOrderingException");
    }

    @Test
    public void listPoints_returnsPointsInAnyOrder_whenOrderedIsFalseAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Point point1 = resourceManager.createPoint(pitch.getPitchId(), cookies, true);
        Point point2 = resourceManager.createPoint(pitch.getPitchId(), cookies, true);
        ApiResponse<List<Point>> apiResponse = climbAssistClient.listPoints(pitch.getPitchId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(point1, point2));
    }

    @Test
    public void createPoint_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreatePointResult> apiResponse = climbAssistClient.createPoint(NewPoint.builder()
                .pitchId("does-not-exist")
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createPoint_createsPoint() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Point expectedPoint = resourceManager.createPoint(pitch.getPitchId(), cookies, true);
        Point actualPoint = climbAssistClient.getPoint(expectedPoint.getPointId())
                .getData();
        assertThat(actualPoint, is(equalTo(expectedPoint)));
    }

    @Test
    public void createPoint_createsPoint_whenPointWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Point expectedPoint2 = resourceManager.createPoint(pitch.getPitchId(), cookies, false);
        Point expectedPoint1 = resourceManager.createPoint(pitch.getPitchId(), cookies, true,
                expectedPoint2.getPointId());
        Point actualPoint1 = climbAssistClient.getPoint(expectedPoint1.getPointId())
                .getData();
        Point actualPoint2 = climbAssistClient.getPoint(expectedPoint2.getPointId())
                .getData();
        assertThat(actualPoint1, is(equalTo(expectedPoint1)));
        assertThat(actualPoint2, is(equalTo(expectedPoint2)));
        assertThat(actualPoint1.getPointId(), is(not(equalTo(actualPoint2.getPointId()))));
    }

    @Test
    public void createPoint_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreatePointResult> apiResponse = climbAssistClient.createPoint(NewPoint.builder()
                .pitchId("does-not-exist")
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(true)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createPoint_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreatePointResult> apiResponse = climbAssistClient.createPoint(NewPoint.builder()
                .pitchId("does-not-exist")
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchCreatePoints_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<BatchCreatePointsResult> apiResponse = climbAssistClient.batchCreatePoints("does-not-exist",
                cookies, BatchNewPoint.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchCreatePoints_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<BatchCreatePointsResult> apiResponse = climbAssistClient.batchCreatePoints("does-not-exist",
                cookies, BatchNewPoint.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build());
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void batchCreatePoints_createsSinglePoint_whenListHasOneNewPoint() {
        runBatchCreatePointsTest(1);
    }

    @Test
    public void batchCreatePoints_createsAllPointsInOrder_whenListHasMultiplePoints() {
        runBatchCreatePointsTest(5);
    }

    @Test
    public void batchCreatePoints_doesNotOverwriteExistingPoints_whenPitchAlreadyHasSomePoints() {
        int numberOfPoints = 5;
        testUserManager.makeUserAdministrator(username);
        Point originalPoint = ResourceManager.getPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        String pitchId = originalPoint.getPitchId();

        ApiResponse<BatchCreatePointsResult> apiResponse = climbAssistClient.batchCreatePoints(pitchId, cookies,
                buildBatchNewPoints(numberOfPoints));
        ExceptionUtils.assertNoException(apiResponse);
        List<String> pointIds = apiResponse.getData()
                .getPointIds();
        pointIds.forEach(pointId -> resourceManager.addResourceToResourceIds(Point.class, pointId));
        assertThat(pointIds, hasSize(numberOfPoints));
        List<Point> actualPoints = climbAssistClient.listPoints(pitchId)
                .getData();
        assertThat(actualPoints, containsInAnyOrder(Stream.concat(
                buildExpectedPoints(numberOfPoints, pointIds, pitchId), Stream.of(originalPoint))
                .distinct()
                .toArray()));
    }

    @Test
    public void updatePoint_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Point point = ResourceManager.getPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePoint(Point.builder()
                .pointId(point.getPointId())
                .pitchId("does-not-exist")
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePoint_returnsPointNotFoundException_whenPointDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePoint(Point.builder()
                .pointId("does-not-exist")
                .pitchId(pitch.getPitchId())
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePoint_updatesPoint() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Point originalPoint = ResourceManager.getPoint(country);
        Point nextPoint = resourceManager.createPoint(originalPoint.getPitchId(), cookies, true);
        Point updatedPoint = Point.builder()
                .pointId(originalPoint.getPointId())
                .pitchId(resourceManager.createPitch(ResourceManager.getRoute(country)
                        .getRouteId(), cookies, true, 0)
                        .getPitchId())
                .x(2.0)
                .y(2.0)
                .z(2.0)
                .first(false)
                .next(nextPoint.getPointId())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePoint(updatedPoint, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        updatedPoint.setFirst(null);
        Point actualPoint = climbAssistClient.getPoint(originalPoint.getPointId())
                .getData();
        assertThat(actualPoint, is(equalTo(updatedPoint)));
    }

    @Test
    public void deletePoint_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePoint("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePoint_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePoint("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePoint_returnsPointNotFoundException_whenPointDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePoint("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deletePoint_deletesPoint() {
        testUserManager.makeUserAdministrator(username);
        Point point = ResourceManager.getPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePoint(point.getPointId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Point> getPointResult = climbAssistClient.getPoint(point.getPointId());
        ExceptionUtils.assertResourceNotFoundException(getPointResult);
    }

    @Test
    public void batchDeletePoints_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePoints("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchDeletePoints_returnsResourceNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePoints("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void batchDeletePoints_doesNothing_whenPitchHasNoPoints() {
        testUserManager.makeUserAdministrator(username);
        String pitchId = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPitchId();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePoints(pitchId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPoints(pitchId)
                .getData(), is(empty()));
    }

    @Test
    public void batchDeletePoints_deletesSinglePoint_whenPitchHasOnePoint() {
        testUserManager.makeUserAdministrator(username);
        String pitchId = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH))
                .getPitchId();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePoints(pitchId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPoints(pitchId)
                .getData(), is(empty()));
    }

    @Test
    public void batchDeletePoints_deletesAllPointsInPitch() {
        testUserManager.makeUserAdministrator(username);
        String pitchId = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPitchId();
        resourceManager.createPoints(pitchId, cookies);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePoints(pitchId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPoints(pitchId)
                .getData(), is(empty()));
    }

    private void runBatchCreatePointsTest(int numberOfPoints) {
        testUserManager.makeUserAdministrator(username);
        String pitchId = ResourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPitchId();

        ApiResponse<BatchCreatePointsResult> apiResponse = climbAssistClient.batchCreatePoints(pitchId, cookies,
                buildBatchNewPoints(numberOfPoints));
        ExceptionUtils.assertNoException(apiResponse);
        List<String> pointIds = apiResponse.getData()
                .getPointIds();
        pointIds.forEach(pointId -> resourceManager.addResourceToResourceIds(Point.class, pointId));
        assertThat(pointIds, hasSize(numberOfPoints));
        List<Point> expectedPoints = buildExpectedPoints(numberOfPoints, pointIds, pitchId).collect(
                Collectors.toList());
        List<Point> actualPoints = climbAssistClient.listPoints(pitchId, true)
                .getData();
        assertThat(actualPoints, is(equalTo(expectedPoints)));
    }

    private Stream<Point> buildExpectedPoints(int numberOfPoints, List<String> pointIds, String pitchId) {
        return IntStream.range(0, numberOfPoints)
                .boxed()
                .map(i -> Point.builder()
                        .pointId(pointIds.get(i))
                        .pitchId(pitchId)
                        .x((double) i)
                        .y((double) i)
                        .z((double) i)
                        .first(i == 0 ? true : null)
                        .next(i == numberOfPoints - 1 ? null : pointIds.get(i + 1))
                        .build());
    }

    private BatchNewPoint[] buildBatchNewPoints(int numberOfPoints) {
        return IntStream.range(0, numberOfPoints)
                .boxed()
                .map(integer -> BatchNewPoint.builder()
                        .x((double) integer)
                        .y((double) integer)
                        .z((double) integer)
                        .build())
                .toArray(BatchNewPoint[]::new);
    }
}

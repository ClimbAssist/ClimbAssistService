package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.pathpoint.BatchCreatePathPointsResult;
import com.climbassist.api.resource.pathpoint.BatchNewPathPoint;
import com.climbassist.api.resource.pathpoint.CreatePathPointResult;
import com.climbassist.api.resource.pathpoint.NewPathPoint;
import com.climbassist.api.resource.pathpoint.PathPoint;
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
public class PathPointIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final int RESOURCE_DEPTH = 6;

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
    public void getPathPoint_returnsPathPointNotFoundException_whenPathPointDoesNotExist() {
        ApiResponse<PathPoint> apiResponse = climbAssistClient.getPathPoint("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getPathPoint_returnsPathPoint() {
        testUserManager.makeUserAdministrator(username);
        PathPoint pathPoint = ResourceManager.getPathPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<PathPoint> apiResponse = climbAssistClient.getPathPoint(pathPoint.getPathPointId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(pathPoint)));
    }

    @Test
    public void listPathPoints_returnsPathNotFoundException_whenPathDoesNotExist() {
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listPathPoints_returnsEmptyList_whenThereAreNoPathPoints() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listPathPoints_returnsSinglePathPoint_whenThereIsOnlyOnePathPoint() {
        testUserManager.makeUserAdministrator(username);
        PathPoint pathPoint = ResourceManager.getPathPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(pathPoint.getPathId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableList.of(pathPoint))));
    }

    @Test
    public void listPathPoints_listsAllPathPointsInAnyOrder_whenOrderedIsNotSpecified() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<PathPoint> pathPoints = resourceManager.createPathPoints(path.getPathId(), cookies);
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(pathPoints.toArray()));
    }

    @Test
    public void listPathPoints_listsAllPathPointsInOrder_whenOrderedIsTrue() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<PathPoint> pathPoints = resourceManager.createPathPoints(path.getPathId(), cookies);
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId(), true);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(pathPoints)));
    }

    @Test
    public void listPathPoints_listsAllPathPointsInAnyOrder_whenOrderedIsFalse() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<PathPoint> pathPoints = resourceManager.createPathPoints(path.getPathId(), cookies);
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(pathPoints.toArray()));
    }

    @Test
    public void listPathPoints_returnsInvalidOrderingException_whenOrderedIsTrueAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.createPathPoint(path.getPathId(), cookies, true);
        resourceManager.createPathPoint(path.getPathId(), cookies, true);
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId(), true);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "InvalidOrderingException");
    }

    @Test
    public void listPathPoints_returnsPathPointsInAnyOrder_whenOrderedIsFalseAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        PathPoint pathPoint1 = resourceManager.createPathPoint(path.getPathId(), cookies, true);
        PathPoint pathPoint2 = resourceManager.createPathPoint(path.getPathId(), cookies, true);
        ApiResponse<List<PathPoint>> apiResponse = climbAssistClient.listPathPoints(path.getPathId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(pathPoint1, pathPoint2));
    }

    @Test
    public void createPathPoint_returnsPathNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreatePathPointResult> apiResponse = climbAssistClient.createPathPoint(NewPathPoint.builder()
                .pathId("does-not-exist")
                .latitude(1.0)
                .longitude(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createPathPoint_createsPathPoint() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        PathPoint expectedPathPoint = resourceManager.createPathPoint(path.getPathId(), cookies, true);
        PathPoint actualPathPoint = climbAssistClient.getPathPoint(expectedPathPoint.getPathPointId())
                .getData();
        assertThat(actualPathPoint, is(equalTo(expectedPathPoint)));
    }

    @Test
    public void createPathPoint_createsPathPoint_whenPathPointWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        PathPoint expectedPathPoint2 = resourceManager.createPathPoint(path.getPathId(), cookies, false);
        PathPoint expectedPathPoint1 = resourceManager.createPathPoint(path.getPathId(), cookies, true,
                expectedPathPoint2.getPathPointId());
        PathPoint actualPathPoint1 = climbAssistClient.getPathPoint(expectedPathPoint1.getPathPointId())
                .getData();
        PathPoint actualPathPoint2 = climbAssistClient.getPathPoint(expectedPathPoint2.getPathPointId())
                .getData();
        assertThat(actualPathPoint1, is(equalTo(expectedPathPoint1)));
        assertThat(actualPathPoint2, is(equalTo(expectedPathPoint2)));
        assertThat(actualPathPoint1.getPathPointId(), is(not(equalTo(actualPathPoint2.getPathPointId()))));
    }

    @Test
    public void createPathPoint_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreatePathPointResult> apiResponse = climbAssistClient.createPathPoint(NewPathPoint.builder()
                .pathId("does-not-exist")
                .latitude(1.0)
                .longitude(1.0)
                .first(true)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createPathPoint_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreatePathPointResult> apiResponse = climbAssistClient.createPathPoint(NewPathPoint.builder()
                .pathId("does-not-exist")
                .latitude(1.0)
                .longitude(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchCreatePathPoints_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<BatchCreatePathPointsResult> apiResponse = climbAssistClient.batchCreatePathPoints("does-not-exist",
                cookies, BatchNewPathPoint.builder()
                        .latitude(1.0)
                        .longitude(1.0)
                        .build());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchCreatePathPoints_returnsPathNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<BatchCreatePathPointsResult> apiResponse = climbAssistClient.batchCreatePathPoints("does-not-exist",
                cookies, BatchNewPathPoint.builder()
                        .latitude(1.0)
                        .longitude(1.0)
                        .build());
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void batchCreatePathPoints_createsSinglePathPoint_whenListHasOneNewPathPoint() {
        runBatchCreatePathPointsTest(1);
    }

    @Test
    public void batchCreatePathPoints_createsAllPathPointsInOrder_whenListHasMultiplePathPoints() {
        runBatchCreatePathPointsTest(5);
    }

    @Test
    public void batchCreatePathPoints_doesNotOverwriteExistingPathPoints_whenPathAlreadyHasSomePathPoints() {
        int numberOfPathPoints = 5;
        testUserManager.makeUserAdministrator(username);
        PathPoint originalPathPoint = ResourceManager.getPathPoint(
                resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        String pathId = originalPathPoint.getPathId();

        ApiResponse<BatchCreatePathPointsResult> apiResponse = climbAssistClient.batchCreatePathPoints(pathId, cookies,
                buildBatchNewPathPoints(numberOfPathPoints));
        ExceptionUtils.assertNoException(apiResponse);
        List<String> pathPointIds = apiResponse.getData()
                .getPathPointIds();
        pathPointIds.forEach(pathPointId -> resourceManager.addResourceToResourceIds(PathPoint.class, pathPointId));
        assertThat(pathPointIds, hasSize(numberOfPathPoints));
        List<PathPoint> actualPathPoints = climbAssistClient.listPathPoints(pathId)
                .getData();
        assertThat(actualPathPoints, containsInAnyOrder(Stream.concat(
                buildExpectedPathPoints(numberOfPathPoints, pathPointIds, pathId), Stream.of(originalPathPoint))
                .distinct()
                .toArray()));
    }

    @Test
    public void updatePathPoint_returnsPathNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        PathPoint pathPoint = ResourceManager.getPathPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePathPoint(PathPoint.builder()
                .pathPointId(pathPoint.getPathPointId())
                .pathId("does-not-exist")
                .latitude(1.0)
                .longitude(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePathPoint_returnsPathPointNotFoundException_whenPathPointDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePathPoint(PathPoint.builder()
                .pathPointId("does-not-exist")
                .pathId(path.getPathId())
                .latitude(1.0)
                .longitude(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePathPoint_updatesPathPoint() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        PathPoint originalPathPoint = ResourceManager.getPathPoint(country);
        PathPoint nextPathPoint = resourceManager.createPathPoint(originalPathPoint.getPathId(), cookies, true);
        PathPoint updatedPathPoint = PathPoint.builder()
                .pathPointId(originalPathPoint.getPathPointId())
                .pathId(resourceManager.createPath(ResourceManager.getCrag(country)
                        .getCragId(), cookies, 0)
                        .getPathId())
                .latitude(2.0)
                .longitude(2.0)
                .first(false)
                .next(nextPathPoint.getPathPointId())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePathPoint(updatedPathPoint, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        updatedPathPoint.setFirst(null);
        PathPoint actualPathPoint = climbAssistClient.getPathPoint(originalPathPoint.getPathPointId())
                .getData();
        assertThat(actualPathPoint, is(equalTo(updatedPathPoint)));
    }

    @Test
    public void deletePathPoint_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePathPoint("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePathPoint_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePathPoint("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePathPoint_returnsPathPointNotFoundException_whenPathPointDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePathPoint("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deletePathPoint_deletesPathPoint() {
        testUserManager.makeUserAdministrator(username);
        PathPoint pathPoint = ResourceManager.getPathPoint(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePathPoint(pathPoint.getPathPointId(),
                cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<PathPoint> getPathPointResult = climbAssistClient.getPathPoint(pathPoint.getPathPointId());
        ExceptionUtils.assertResourceNotFoundException(getPathPointResult);
    }

    @Test
    public void batchDeletePathPoints_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePathPoints("does-not-exist",
                cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void batchDeletePathPoints_returnsResourceNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePathPoints("does-not-exist",
                cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void batchDeletePathPoints_doesNothing_whenPathHasNoPathPoints() {
        testUserManager.makeUserAdministrator(username);
        String pathId = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPathId();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePathPoints(pathId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPathPoints(pathId)
                .getData(), is(empty()));
    }

    @Test
    public void batchDeletePathPoints_deletesSinglePathPoint_whenPathHasOnePathPoint() {
        testUserManager.makeUserAdministrator(username);
        String pathId = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH))
                .getPathId();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePathPoints(pathId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPathPoints(pathId)
                .getData(), is(empty()));
    }

    @Test
    public void batchDeletePathPoints_deletesAllPathPointsInPath() {
        testUserManager.makeUserAdministrator(username);
        String pathId = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPathId();
        resourceManager.createPathPoints(pathId, cookies);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.batchDeletePathPoints(pathId, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        assertThat(climbAssistClient.listPathPoints(pathId)
                .getData(), is(empty()));
    }

    private void runBatchCreatePathPointsTest(int numberOfPathPoints) {
        testUserManager.makeUserAdministrator(username);
        String pathId = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1))
                .getPathId();

        ApiResponse<BatchCreatePathPointsResult> apiResponse = climbAssistClient.batchCreatePathPoints(pathId, cookies,
                buildBatchNewPathPoints(numberOfPathPoints));
        ExceptionUtils.assertNoException(apiResponse);
        List<String> pathPointIds = apiResponse.getData()
                .getPathPointIds();
        pathPointIds.forEach(pathPointId -> resourceManager.addResourceToResourceIds(PathPoint.class, pathPointId));
        assertThat(pathPointIds, hasSize(numberOfPathPoints));
        List<PathPoint> expectedPathPoints = buildExpectedPathPoints(numberOfPathPoints, pathPointIds, pathId).collect(
                Collectors.toList());
        List<PathPoint> actualPathPoints = climbAssistClient.listPathPoints(pathId, true)
                .getData();
        assertThat(actualPathPoints, is(equalTo(expectedPathPoints)));
    }

    private Stream<PathPoint> buildExpectedPathPoints(int numberOfPathPoints, List<String> pathPointIds,
                                                      String pathId) {
        return IntStream.range(0, numberOfPathPoints)
                .boxed()
                .map(i -> PathPoint.builder()
                        .pathPointId(pathPointIds.get(i))
                        .pathId(pathId)
                        .latitude((double) i)
                        .longitude((double) i)
                        .first(i == 0 ? true : null)
                        .next(i == numberOfPathPoints - 1 ? null : pathPointIds.get(i + 1))
                        .build());
    }

    private BatchNewPathPoint[] buildBatchNewPathPoints(int numberOfPathPoints) {
        return IntStream.range(0, numberOfPathPoints)
                .boxed()
                .map(i -> BatchNewPathPoint.builder()
                        .latitude((double) i)
                        .longitude((double) i)
                        .build())
                .toArray(BatchNewPathPoint[]::new);
    }
}

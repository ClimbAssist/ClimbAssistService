package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.wall.CreateWallResult;
import com.climbassist.api.resource.wall.NewWall;
import com.climbassist.api.resource.wall.Wall;
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
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class WallIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final int RESOURCE_DEPTH = 5;

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
    public void getWall_returnsWallNotFoundException_whenWallDoesNotExist() {
        ApiResponse<Wall> apiResponse = climbAssistClient.getWall("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getWall_returnsWallWithNoChildren_whenDepthIsNotSpecifiedAndWallHasChildren() {
        runGetWallTest(1, Optional.empty());
    }

    @Test
    public void getWall_returnsWallWithNoChildren_whenDepthIsZeroAndWallHasChildren() {
        runGetWallTest(2, Optional.of(0));
    }

    @Test
    public void getWall_returnsWallWithNoChildren_whenDepthIsGreaterThanZeroAndWallHasNoChildren() {
        runGetWallTest(0, Optional.of(1));
    }

    @Test
    public void getWall_returnsWallWithChildren_whenDepthIsEqualToChildDepth() {
        runGetWallTest(2, Optional.of(2));
    }

    @Test
    public void getWall_returnsWallWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetWallTest(2, Optional.of(5));
    }

    @Test
    public void getWall_returnsWallWithAllChildren_whenWallHasFullDepthOfChildren() {
        runGetWallTest(3, Optional.of(3));
    }

    @Test
    public void listWalls_returnsCragNotFoundException_whenCragDoesNotExist() {
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listWalls_returnsEmptyList_whenThereAreNoWalls() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listWalls_returnsSingleWall_whenThereIsOnlyOneWall() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = resourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(wall.getCragId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableList.of(wall))));
    }

    @Test
    public void listWalls_listsAllWallsInAnyOrder_whenOrderedIsNotSpecified() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Wall> walls = resourceManager.createWalls(crag.getCragId(), cookies);
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(walls.toArray()));
    }

    @Test
    public void listWalls_listsAllWallsInOrder_whenOrderedIsTrue() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Wall> walls = resourceManager.createWalls(crag.getCragId(), cookies);
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId(), true);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(walls)));
    }

    @Test
    public void listWalls_listsAllWallsInAnyOrder_whenOrderedIsFalse() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Wall> walls = resourceManager.createWalls(crag.getCragId(), cookies);
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(walls.toArray()));
    }

    @Test
    public void listWalls_returnsInvalidOrderingException_whenOrderedIsTrueAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId(), true);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "InvalidOrderingException");
    }

    @Test
    public void listWalls_returnsWallsInAnyOrder_whenOrderedIsFalseAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Wall wall1 = resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        Wall wall2 = resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        ApiResponse<List<Wall>> apiResponse = climbAssistClient.listWalls(crag.getCragId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(wall1, wall2));
    }

    @Test
    public void createWall_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateWallResult> apiResponse = climbAssistClient.createWall(NewWall.builder()
                .cragId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createWall_createsWall() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Wall expectedWall = resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        Wall actualWall = climbAssistClient.getWall(expectedWall.getWallId())
                .getData();
        assertThat(actualWall, is(equalTo(expectedWall)));
    }

    @Test
    public void createWall_createsWall_whenWallWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Wall expectedWall2 = resourceManager.createWall(crag.getCragId(), cookies, false, 0);
        Wall expectedWall1 = resourceManager.createWall(crag.getCragId(), cookies, true, expectedWall2.getWallId(), 0);
        Wall actualWall1 = climbAssistClient.getWall(expectedWall1.getWallId())
                .getData();
        Wall actualWall2 = climbAssistClient.getWall(expectedWall2.getWallId())
                .getData();
        assertThat(actualWall1, is(equalTo(expectedWall1)));
        assertThat(actualWall2, is(equalTo(expectedWall2)));
        assertThat(actualWall1.getWallId(), is(not(equalTo(actualWall2.getWallId()))));
    }

    @Test
    public void createWall_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateWallResult> apiResponse = climbAssistClient.createWall(NewWall.builder()
                .cragId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createWall_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateWallResult> apiResponse = climbAssistClient.createWall(NewWall.builder()
                .cragId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updateWall_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Wall wall = resourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateWall(Wall.builder()
                .wallId(wall.getWallId())
                .cragId("does-not-exist")
                .name(NAME)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateWall_returnsWallNotFoundException_whenWallDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateWall(Wall.builder()
                .wallId("does-not-exist")
                .cragId(crag.getCragId())
                .name(NAME)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateWall_updatesWall() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Wall originalWall = resourceManager.getWall(country);
        Wall nextWall = resourceManager.createWall(originalWall.getCragId(), cookies, true, 0);
        Wall updatedWall = Wall.builder()
                .wallId(originalWall.getWallId())
                .cragId(resourceManager.createCrag(resourceManager.getSubArea(country)
                        .getSubAreaId(), cookies, 0)
                        .getCragId())
                .name(NAME + "-updated")
                .next(nextWall.getWallId())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateWall(updatedWall, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Wall actualWall = climbAssistClient.getWall(originalWall.getWallId())
                .getData();
        assertThat(actualWall, is(equalTo(updatedWall)));
    }

    @Test
    public void deleteWall_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteWall("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteWall_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteWall("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteWall_returnsWallNotFoundException_whenWallDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteWall("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deleteWall_deletesWall() {
        testUserManager.makeUserAdministrator(username);
        Wall Wall = resourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteWall(Wall.getWallId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Wall> getWallResult = climbAssistClient.getWall(Wall.getWallId());
        ExceptionUtils.assertResourceNotFoundException(getWallResult);
    }

    @Test
    public void deleteWall_returnsResourceNotEmptyException_whenWallHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Wall Wall = resourceManager.getWall(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteWall(Wall.getWallId(), cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    private void runGetWallTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Wall Wall = resourceManager.getWall(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(Wall, Wall.class, maybeRequestDepth.orElse(0));
        ApiResponse<Wall> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getWall(Wall.getWallId(),
                maybeRequestDepth.get()) : climbAssistClient.getWall(Wall.getWallId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(Wall)));
    }
}

package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.path.CreatePathResult;
import com.climbassist.api.resource.path.NewPath;
import com.climbassist.api.resource.path.Path;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.user.TestUserManager;
import com.climbassist.test.integration.api.user.TestUserManagerConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
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
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class PathIntegrationTest extends AbstractTestNGSpringContextTests {

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
    public void getPath_returnsPathNotFoundException_whenPathDoesNotExist() {
        ApiResponse<Path> apiResponse = climbAssistClient.getPath("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getPath_returnsPathWithNoChildren_whenDepthIsNotSpecifiedAndPathHasChildren() {
        runGetPathTest(1, Optional.empty());
    }

    @Test
    public void getPath_returnsPathWithNoChildren_whenDepthIsZeroAndPathHasChildren() {
        runGetPathTest(2, Optional.of(0));
    }

    @Test
    public void getPath_returnsPathWithNoChildren_whenDepthIsGreaterThanZeroAndPathHasNoChildren() {
        runGetPathTest(0, Optional.of(1));
    }

    @Test
    public void getPath_returnsPathWithChildren_whenDepthIsEqualToChildDepth() {
        runGetPathTest(3, Optional.of(3));
    }

    @Test
    public void getPath_returnsPathWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetPathTest(3, Optional.of(5));
    }

    @Test
    public void getPath_returnsPathWithAllChildren_whenPathHasFullDepthOfChildren() {
        runGetPathTest(7, Optional.of(7));
    }

    @Test
    public void listPaths_returnsCragNotFoundException_whenCragDoesNotExist() {
        ApiResponse<Set<Path>> apiResponse = climbAssistClient.listPaths("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listPaths_returnsEmptyList_whenThereAreNoPaths() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = ResourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<Set<Path>> apiResponse = climbAssistClient.listPaths(crag.getCragId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listPaths_returnsSinglePath_whenThereIsOnlyOnePath() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Set<Path>> apiResponse = climbAssistClient.listPaths(path.getCragId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(path))));
    }

    @Test
    public void listPaths_listAllPaths() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = ResourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Path path1 = resourceManager.createPath(crag.getCragId(), cookies, 0);
        Path path2 = resourceManager.createPath(crag.getCragId(), cookies, 0);
        ApiResponse<Set<Path>> apiResponse = climbAssistClient.listPaths(crag.getCragId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(path1, path2))));
    }

    @Test
    public void createPath_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreatePathResult> apiResponse = climbAssistClient.createPath(NewPath.builder()
                .cragId("does-not-exist")
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createPath_createsPath() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = ResourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Path expectedPath = resourceManager.createPath(crag.getCragId(), cookies, 0);
        Path actualPath = climbAssistClient.getPath(expectedPath.getPathId(), cookies)
                .getData();
        assertThat(actualPath, is(equalTo(expectedPath)));
    }

    @Test
    public void createPath_createsPath_whenPathWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = ResourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Path expectedPath1 = resourceManager.createPath(crag.getCragId(), cookies, 0);
        Path expectedPath2 = resourceManager.createPath(crag.getCragId(), cookies, 0);
        Path actualPath1 = climbAssistClient.getPath(expectedPath1.getPathId(), cookies)
                .getData();
        Path actualPath2 = climbAssistClient.getPath(expectedPath2.getPathId(), cookies)
                .getData();
        assertThat(actualPath1, is(equalTo(expectedPath1)));
        assertThat(actualPath2, is(equalTo(expectedPath2)));
        assertThat(actualPath1.getPathId(), is(not(equalTo(actualPath2.getPathId()))));
    }

    @Test
    public void createPath_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreatePathResult> apiResponse = climbAssistClient.createPath(NewPath.builder()
                .cragId("does-not-exist")
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createPath_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreatePathResult> apiResponse = climbAssistClient.createPath(NewPath.builder()
                .cragId("does-not-exist")
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updatePath_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePath(Path.builder()
                .cragId("does-not-exist")
                .pathId(path.getPathId())
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePath_returnsPathNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = ResourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePath(Path.builder()
                .cragId(crag.getCragId())
                .pathId("does-not-exist")
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePath_updatesPath() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Path originalPath = ResourceManager.getPath(country);
        Path updatedPath = Path.builder()
                .pathId(originalPath.getPathId())
                .cragId(resourceManager.createCrag(ResourceManager.getSubArea(country)
                        .getSubAreaId(), cookies, 0)
                        .getCragId())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePath(updatedPath, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Path actualPath = climbAssistClient.getPath(originalPath.getPathId(), cookies)
                .getData();
        assertThat(actualPath, is(equalTo(updatedPath)));
    }

    @Test
    public void deletePath_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePath("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePath_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePath("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePath_returnsPathNotFoundException_whenPathDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePath("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deletePath_deletesPath() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePath(path.getPathId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Path> getPathResult = climbAssistClient.getPath(path.getPathId(), cookies);
        ExceptionUtils.assertResourceNotFoundException(getPathResult);
    }

    @Test
    public void deletePath_returnsResourceNotEmptyException_whenPathHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePath(path.getPathId(), cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    private void runGetPathTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Path path = ResourceManager.getPath(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(path, Path.class, maybeRequestDepth.orElse(0));
        ApiResponse<Path> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getPath(path.getPathId(),
                maybeRequestDepth.get(), cookies) : climbAssistClient.getPath(path.getPathId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(path)));
    }

}

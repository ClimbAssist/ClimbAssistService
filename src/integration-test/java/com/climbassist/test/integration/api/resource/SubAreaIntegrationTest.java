package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.subarea.CreateSubAreaResult;
import com.climbassist.api.resource.subarea.NewSubArea;
import com.climbassist.api.resource.subarea.SubArea;
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
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class SubAreaIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final String DESCRIPTION = "integ";
    private static final int RESOURCE_DEPTH = 3;

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
    public void getSubArea_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        ApiResponse<SubArea> apiResponse = climbAssistClient.getSubArea("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getSubArea_returnsSubAreaWithNoChildren_whenDepthIsNotSpecifiedAndSubAreaHasChildren() {
        runGetSubAreaTest(1, Optional.empty());
    }

    @Test
    public void getSubArea_returnsSubAreaWithNoChildren_whenDepthIsZeroAndSubAreaHasChildren() {
        runGetSubAreaTest(2, Optional.of(0));
    }

    @Test
    public void getSubArea_returnsSubAreaWithNoChildren_whenDepthIsGreaterThanZeroAndSubAreaHasNoChildren() {
        runGetSubAreaTest(0, Optional.of(1));
    }

    @Test
    public void getSubArea_returnsSubAreaWithChildren_whenDepthIsEqualToChildDepth() {
        runGetSubAreaTest(3, Optional.of(3));
    }

    @Test
    public void getSubArea_returnsSubAreaWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetSubAreaTest(3, Optional.of(5));
    }

    @Test
    public void getSubArea_returnsSubAreaWithAllChildren_whenSubAreaHasFullDepthOfChildren() {
        runGetSubAreaTest(5, Optional.of(5));
    }

    @Test
    public void listSubAreas_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        ApiResponse<Set<SubArea>> apiResponse = climbAssistClient.listSubAreas("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listSubAreas_returnsEmptyList_whenThereAreNoSubAreas() {
        testUserManager.makeUserAdministrator(username);
        Area area = resourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<Set<SubArea>> apiResponse = climbAssistClient.listSubAreas(area.getAreaId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listSubAreas_returnsSingleSubArea_whenThereIsOnlyOneSubArea() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Set<SubArea>> apiResponse = climbAssistClient.listSubAreas(subArea.getAreaId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(subArea))));
    }

    @Test
    public void listSubAreas_listAllSubAreas() {
        testUserManager.makeUserAdministrator(username);
        Area area = resourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        SubArea subArea1 = resourceManager.createSubArea(area.getAreaId(), cookies, 0);
        SubArea subArea2 = resourceManager.createSubArea(area.getAreaId(), cookies, 0);
        ApiResponse<Set<SubArea>> apiResponse = climbAssistClient.listSubAreas(area.getAreaId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(subArea1, subArea2))));
    }

    @Test
    public void createSubArea_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateSubAreaResult> apiResponse = climbAssistClient.createSubArea(NewSubArea.builder()
                .areaId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createSubArea_createsSubArea() {
        testUserManager.makeUserAdministrator(username);
        Area area = resourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        SubArea expectedSubArea = resourceManager.createSubArea(area.getAreaId(), cookies, 0);
        SubArea actualSubArea = climbAssistClient.getSubArea(expectedSubArea.getSubAreaId())
                .getData();
        assertThat(actualSubArea, is(equalTo(expectedSubArea)));
    }

    @Test
    public void createSubArea_createsSubArea_whenSubAreaWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Area area = resourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        SubArea expectedSubArea1 = resourceManager.createSubArea(area.getAreaId(), cookies, 0);
        SubArea expectedSubArea2 = resourceManager.createSubArea(area.getAreaId(), cookies, 0);
        SubArea actualSubArea1 = climbAssistClient.getSubArea(expectedSubArea1.getSubAreaId())
                .getData();
        SubArea actualSubArea2 = climbAssistClient.getSubArea(expectedSubArea2.getSubAreaId())
                .getData();
        assertThat(actualSubArea1, is(equalTo(expectedSubArea1)));
        assertThat(actualSubArea2, is(equalTo(expectedSubArea2)));
        assertThat(actualSubArea1.getSubAreaId(), is(not(equalTo(actualSubArea2.getSubAreaId()))));
    }

    @Test
    public void createSubArea_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateSubAreaResult> apiResponse = climbAssistClient.createSubArea(NewSubArea.builder()
                .areaId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createSubArea_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateSubAreaResult> apiResponse = climbAssistClient.createSubArea(NewSubArea.builder()
                .areaId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updateSubArea_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateSubArea(SubArea.builder()
                .subAreaId(subArea.getSubAreaId())
                .areaId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateSubArea_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Area area = resourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateSubArea(SubArea.builder()
                .subAreaId("does-not-exist")
                .areaId(area.getAreaId())
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateSubArea_updatesSubArea() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        SubArea originalSubArea = resourceManager.getSubArea(country);
        SubArea updatedSubArea = SubArea.builder()
                .subAreaId(originalSubArea.getSubAreaId())
                .areaId(resourceManager.createArea(resourceManager.getRegion(country)
                        .getRegionId(), cookies, 0)
                        .getAreaId())
                .name(NAME + "-updated")
                .description(DESCRIPTION)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateSubArea(updatedSubArea, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        SubArea actualSubArea = climbAssistClient.getSubArea(originalSubArea.getSubAreaId())
                .getData();
        assertThat(actualSubArea, is(equalTo(updatedSubArea)));
    }

    @Test
    public void deleteSubArea_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteSubArea("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteSubArea_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteSubArea("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteSubArea_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteSubArea("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deleteSubArea_deletesSubArea() {
        testUserManager.makeUserAdministrator(username);
        SubArea SubArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteSubArea(SubArea.getSubAreaId(),
                cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<SubArea> getSubAreaResult = climbAssistClient.getSubArea(SubArea.getSubAreaId());
        ExceptionUtils.assertResourceNotFoundException(getSubAreaResult);
    }

    @Test
    public void deleteSubArea_returnsResourceNotEmptyException_whenSubAreaHasChildren() {
        testUserManager.makeUserAdministrator(username);
        SubArea SubArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteSubArea(SubArea.getSubAreaId(),
                cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    private void runGetSubAreaTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        SubArea SubArea = resourceManager.getSubArea(
                resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(SubArea, SubArea.class, maybeRequestDepth.orElse(0));
        ApiResponse<SubArea> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getSubArea(
                SubArea.getSubAreaId(), maybeRequestDepth.get()) : climbAssistClient.getSubArea(SubArea.getSubAreaId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(SubArea)));
    }

}

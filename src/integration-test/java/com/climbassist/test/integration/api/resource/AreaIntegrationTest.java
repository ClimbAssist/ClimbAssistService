package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.CreateAreaResult;
import com.climbassist.api.resource.area.NewArea;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.region.Region;
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
public class AreaIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final String DESCRIPTION = "integ";
    private static final int RESOURCE_DEPTH = 2;

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
    public void getArea_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        ApiResponse<Area> apiResponse = climbAssistClient.getArea("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getArea_returnsAreaWithNoChildren_whenDepthIsNotSpecifiedAndAreaHasChildren() {
        runGetAreaTest(1, Optional.empty());
    }

    @Test
    public void getArea_returnsAreaWithNoChildren_whenDepthIsZeroAndAreaHasChildren() {
        runGetAreaTest(2, Optional.of(0));
    }

    @Test
    public void getArea_returnsAreaWithNoChildren_whenDepthIsGreaterThanZeroAndAreaHasNoChildren() {
        runGetAreaTest(0, Optional.of(1));
    }

    @Test
    public void getArea_returnsAreaWithChildren_whenDepthIsEqualToChildDepth() {
        runGetAreaTest(3, Optional.of(3));
    }

    @Test
    public void getArea_returnsAreaWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetAreaTest(3, Optional.of(5));
    }

    @Test
    public void getArea_returnsAreaWithAllChildren_whenAreaHasFullDepthOfChildren() {
        runGetAreaTest(6, Optional.of(6));
    }

    @Test
    public void listAreas_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        ApiResponse<Set<Area>> apiResponse = climbAssistClient.listAreas("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listAreas_returnsEmptyList_whenThereAreNoAreas() {
        testUserManager.makeUserAdministrator(username);
        Region region = ResourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<Set<Area>> apiResponse = climbAssistClient.listAreas(region.getRegionId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listAreas_returnsSingleArea_whenThereIsOnlyOneArea() {
        testUserManager.makeUserAdministrator(username);
        Area area = ResourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Set<Area>> apiResponse = climbAssistClient.listAreas(area.getRegionId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(area))));
    }

    @Test
    public void listAreas_listAllAreas() {
        testUserManager.makeUserAdministrator(username);
        Region region = ResourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Area Area1 = resourceManager.createArea(region.getRegionId(), cookies, 0);
        Area Area2 = resourceManager.createArea(region.getRegionId(), cookies, 0);
        ApiResponse<Set<Area>> apiResponse = climbAssistClient.listAreas(region.getRegionId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(Area1, Area2))));
    }

    @Test
    public void createArea_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateAreaResult> apiResponse = climbAssistClient.createArea(NewArea.builder()
                .regionId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createArea_createsArea() {
        testUserManager.makeUserAdministrator(username);
        Region region = ResourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Area expectedArea = resourceManager.createArea(region.getRegionId(), cookies, 0);
        Area actualArea = climbAssistClient.getArea(expectedArea.getAreaId())
                .getData();
        assertThat(actualArea, is(equalTo(expectedArea)));
    }

    @Test
    public void createArea_createsArea_whenAreaWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Region region = ResourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Area expectedArea1 = resourceManager.createArea(region.getRegionId(), cookies, 0);
        Area expectedArea2 = resourceManager.createArea(region.getRegionId(), cookies, 0);
        Area actualArea1 = climbAssistClient.getArea(expectedArea1.getAreaId())
                .getData();
        Area actualArea2 = climbAssistClient.getArea(expectedArea2.getAreaId())
                .getData();
        assertThat(actualArea1, is(equalTo(expectedArea1)));
        assertThat(actualArea2, is(equalTo(expectedArea2)));
        assertThat(actualArea1.getAreaId(), is(not(equalTo(actualArea2.getAreaId()))));
    }

    @Test
    public void createArea_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateAreaResult> apiResponse = climbAssistClient.createArea(NewArea.builder()
                .regionId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createArea_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateAreaResult> apiResponse = climbAssistClient.createArea(NewArea.builder()
                .regionId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updateArea_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Area area = ResourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateArea(Area.builder()
                .areaId(area.getAreaId())
                .regionId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateArea_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Region region = ResourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateArea(Area.builder()
                .areaId("does-not-exist")
                .regionId(region.getRegionId())
                .name(NAME)
                .description(DESCRIPTION)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateArea_updatesArea() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Area originalArea = ResourceManager.getArea(country);
        Area updatedArea = Area.builder()
                .areaId(originalArea.getAreaId())
                .regionId(resourceManager.createRegion(country.getCountryId(), cookies, 0)
                        .getRegionId())
                .name(NAME + "-updated")
                .description(DESCRIPTION)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateArea(updatedArea, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Area actualArea = climbAssistClient.getArea(originalArea.getAreaId())
                .getData();
        assertThat(actualArea, is(equalTo(updatedArea)));
    }

    @Test
    public void deleteArea_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteArea("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteArea_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteArea("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteArea_returnsAreaNotFoundException_whenAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteArea("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deleteArea_deletesArea() {
        testUserManager.makeUserAdministrator(username);
        Area Area = ResourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteArea(Area.getAreaId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Area> getAreaResult = climbAssistClient.getArea(Area.getAreaId());
        ExceptionUtils.assertResourceNotFoundException(getAreaResult);
    }

    @Test
    public void deleteArea_returnsResourceNotEmptyException_whenAreaHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Area Area = ResourceManager.getArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteArea(Area.getAreaId(), cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    private void runGetAreaTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Area Area = ResourceManager.getArea(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(Area, Area.class, maybeRequestDepth.orElse(0));
        ApiResponse<Area> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getArea(Area.getAreaId(),
                maybeRequestDepth.get()) : climbAssistClient.getArea(Area.getAreaId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(Area)));
    }

}

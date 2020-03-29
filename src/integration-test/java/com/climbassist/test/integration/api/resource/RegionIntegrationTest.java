package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.region.CreateRegionResult;
import com.climbassist.api.resource.region.NewRegion;
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
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class RegionIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final int RESOURCE_DEPTH = 1;

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
    public void getRegion_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        ApiResponse<Region> apiResponse = climbAssistClient.getRegion("does-not-exist");
        ExceptionUtils.assertRegionNotFoundException(apiResponse);
    }

    @Test
    public void getRegion_returnsRegionWithNoChildren_whenDepthIsNotSpecifiedAndRegionHasChildren() {
        runGetRegionTest(1, Optional.empty());
    }

    @Test
    public void getRegion_returnsRegionWithNoChildren_whenDepthIsZeroAndRegionHasChildren() {
        runGetRegionTest(2, Optional.of(0));
    }

    @Test
    public void getRegion_returnsRegionWithNoChildren_whenDepthIsGreaterThanZeroAndRegionHasNoChildren() {
        runGetRegionTest(0, Optional.of(1));
    }

    @Test
    public void getRegion_returnsRegionWithChildren_whenDepthIsEqualToChildDepth() {
        runGetRegionTest(3, Optional.of(3));
    }

    @Test
    public void getRegion_returnsRegionWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetRegionTest(3, Optional.of(5));
    }

    @Test
    public void getRegion_returnsRegionWithAllChildren_whenRegionHasFullDepthOfChildren() {
        runGetRegionTest(7, Optional.of(7));
    }

    @Test
    public void listRegions_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        ApiResponse<Set<Region>> apiResponse = climbAssistClient.listRegions("does-not-exist");
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void listRegions_returnsEmptyList_whenThereAreNoRegions() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1);
        ApiResponse<Set<Region>> apiResponse = climbAssistClient.listRegions(country.getCountryId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listRegions_returnsSingleRegion_whenThereIsOnlyOneRegion() {
        testUserManager.makeUserAdministrator(username);
        Region region = resourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Set<Region>> apiResponse = climbAssistClient.listRegions(region.getCountryId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(region))));
    }

    @Test
    public void listRegions_listAllRegions() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1);
        Region region1 = resourceManager.createRegion(country.getCountryId(), cookies, 0);
        Region region2 = resourceManager.createRegion(country.getCountryId(), cookies, 0);
        ApiResponse<Set<Region>> apiResponse = climbAssistClient.listRegions(country.getCountryId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(region1, region2))));
    }

    @Test
    public void createRegion_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateRegionResult> apiResponse = climbAssistClient.createRegion(NewRegion.builder()
                .countryId("does-not-exist")
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void createRegion_createsRegion() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1);
        Region expectedRegion = resourceManager.createRegion(country.getCountryId(), cookies, 0);
        Region actualRegion = climbAssistClient.getRegion(expectedRegion.getRegionId())
                .getData();
        assertThat(actualRegion, is(equalTo(expectedRegion)));
    }

    @Test
    public void createRegion_createsRegion_whenRegionWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1);
        Region expectedRegion1 = resourceManager.createRegion(country.getCountryId(), cookies, 0);
        Region expectedRegion2 = resourceManager.createRegion(country.getCountryId(), cookies, 0);
        Region actualRegion1 = climbAssistClient.getRegion(expectedRegion1.getRegionId())
                .getData();
        Region actualRegion2 = climbAssistClient.getRegion(expectedRegion2.getRegionId())
                .getData();
        assertThat(actualRegion1, is(equalTo(expectedRegion1)));
        assertThat(actualRegion2, is(equalTo(expectedRegion2)));
        assertThat(actualRegion1.getRegionId(), is(not(equalTo(actualRegion2.getRegionId()))));
    }

    @Test
    public void createRegion_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateRegionResult> apiResponse = climbAssistClient.createRegion(NewRegion.builder()
                .name(NAME)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void createRegion_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateRegionResult> apiResponse = climbAssistClient.createRegion(NewRegion.builder()
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void updateRegion_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Region region = resourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRegion(Region.builder()
                .countryId("does-not-exist")
                .regionId(region.getRegionId())
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void updateRegion_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1);
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRegion(Region.builder()
                .countryId(country.getCountryId())
                .regionId("does-not-exist")
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertRegionNotFoundException(apiResponse);
    }

    @Test
    public void updateRegion_updatesRegion() {
        testUserManager.makeUserAdministrator(username);
        Region originalRegion = resourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        Region updatedRegion = Region.builder()
                .regionId(originalRegion.getRegionId())
                .countryId(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1)
                        .getCountryId())
                .name(NAME + "-updated")
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateRegion(updatedRegion, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Region actualRegion = climbAssistClient.getRegion(originalRegion.getRegionId())
                .getData();
        assertThat(actualRegion, is(equalTo(updatedRegion)));
    }

    @Test
    public void deleteRegion_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRegion("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteRegion_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRegion("does-not-exist", cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteRegion_returnsRegionNotFoundException_whenRegionDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRegion("does-not-exist", cookies);
        ExceptionUtils.assertRegionNotFoundException(apiResponse);
    }

    @Test
    public void deleteRegion_deletesRegion() {
        testUserManager.makeUserAdministrator(username);
        Region region = resourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRegion(region.getRegionId(), cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Region> getRegionResult = climbAssistClient.getRegion(region.getRegionId());
        ExceptionUtils.assertRegionNotFoundException(getRegionResult);
    }

    @Test
    public void deleteRegion_returnsResourceNotEmptyException_whenRegionHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Region region = resourceManager.getRegion(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteRegion(region.getRegionId(), cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "RegionNotEmptyException");
    }

    private void runGetRegionTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Region region = resourceManager.getRegion(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(region, Region.class, maybeRequestDepth.orElse(0));
        ApiResponse<Region> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getRegion(
                region.getRegionId(), maybeRequestDepth.get()) : climbAssistClient.getRegion(region.getRegionId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(region)));
    }

}

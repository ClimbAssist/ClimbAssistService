package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.CreateCountryResult;
import com.climbassist.api.resource.country.NewCountry;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class CountryIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";

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
    public void getCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        ApiResponse<Country> apiResponse = climbAssistClient.getCountry("does-not-exist");
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void getCountry_returnsCountryWithNoChildren_whenDepthIsNotSpecifiedAndCountryHasChildren() {
        runGetCountryTest(1, Optional.empty());
    }

    @Test
    public void getCountry_returnsCountryWithNoChildren_whenDepthIsZeroAndCountryHasChildren() {
        runGetCountryTest(1, Optional.of(0));

    }

    @Test
    public void getCountry_returnsCountryWithNoChildren_whenDepthIsGreaterThanZeroAndCountryHasNoChildren() {
        runGetCountryTest(0, Optional.of(1));
    }

    @Test
    public void getCountry_returnsCountryWithChildren_whenDepthIsEqualToChildDepth() {
        runGetCountryTest(3, Optional.of(3));
    }

    @Test
    public void getCountry_returnsCountryWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetCountryTest(3, Optional.of(5));
    }

    @Test
    public void getCountry_returnsCountryWithAllChildren_whenCountryHasFullDepthOfChildren() {
        runGetCountryTest(8, Optional.of(8));
    }

    @Test
    public void listCountries_listAllCountries() {
        testUserManager.makeUserAdministrator(username);
        Country country1 = resourceManager.createCountry(cookies, 0);
        Country country2 = resourceManager.createCountry(cookies, 0);
        ApiResponse<Set<Country>> apiResponse = climbAssistClient.listCountries();
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), hasItems(country1, country2));
    }

    @Test
    public void createCountry_createsCountry() {
        testUserManager.makeUserAdministrator(username);
        Country expectedCountry = resourceManager.createCountry(cookies, 0);
        Country actualCountry = climbAssistClient.getCountry(expectedCountry.getCountryId())
                .getData();
        assertThat(actualCountry, is(equalTo(expectedCountry)));
    }

    @Test
    public void createCountry_createsCountry_whenCountryWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Country expectedCountry1 = resourceManager.createCountry(cookies, 0);
        Country expectedCountry2 = resourceManager.createCountry(cookies, 0);
        Country actualCountry1 = climbAssistClient.getCountry(expectedCountry1.getCountryId())
                .getData();
        Country actualCountry2 = climbAssistClient.getCountry(expectedCountry2.getCountryId())
                .getData();
        assertThat(actualCountry1, is(equalTo(expectedCountry1)));
        assertThat(actualCountry2, is(equalTo(expectedCountry2)));
        assertThat(actualCountry1.getCountryId(), is(not(equalTo(actualCountry2.getCountryId()))));
    }

    @Test
    public void createCountry_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateCountryResult> apiResponse = climbAssistClient.createCountry(NewCountry.builder()
                .name(NAME)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void createCountry_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateCountryResult> apiResponse = climbAssistClient.createCountry(NewCountry.builder()
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void updateCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCountry(Country.builder()
                .countryId("does-not-exist")
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void updateCountry_updatesCountry() {
        testUserManager.makeUserAdministrator(username);
        Country originalCountry = resourceManager.createCountry(cookies, 0);
        Country updatedCountry = Country.builder()
                .countryId(originalCountry.getCountryId())
                .name(NAME + "-updated")
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCountry(updatedCountry, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Country actualCountry = climbAssistClient.getCountry(originalCountry.getCountryId())
                .getData();
        assertThat(actualCountry, is(equalTo(updatedCountry)));
    }

    @Test
    public void deleteCountry_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCountry("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCountry_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCountry(NAME, cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCountry("does-not-exist", cookies);
        ExceptionUtils.assertCountryNotFoundException(apiResponse);
    }

    @Test
    public void deleteCountry_deletesCountry() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, 0);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCountry(country.getCountryId(),
                cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Country> getCountryResult = climbAssistClient.getCountry(country.getCountryId());
        ExceptionUtils.assertCountryNotFoundException(getCountryResult);
    }

    @Test
    public void deleteCountry_returnsResourceNotEmptyException_whenCountryHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, 1);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCountry(country.getCountryId(),
                cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "CountryNotEmptyException");
    }

    private void runGetCountryTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, actualDepth);
        resourceManager.removeChildren(country, Country.class, maybeRequestDepth.orElse(0));
        ApiResponse<Country> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getCountry(
                country.getCountryId(), maybeRequestDepth.get()) : climbAssistClient.getCountry(country.getCountryId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

}

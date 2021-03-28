package com.climbassist.test.integration.api.v2;

import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.resource.country.NewCountry;
import com.climbassist.api.v2.Country;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.resource.ResourceManagerConfiguration;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class CountryIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final NewCountry NEW_COUNTRY = NewCountry.builder()
            .name(NAME)
            .build();

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;

    private String username;
    private Set<Cookie> cookies;
    private Set<String> countryIds;

    @BeforeClass
    public void setUpClass() throws IOException {
        username = TestIdGenerator.generateTestId();
        cookies = testUserManager.createVerifyAndSignInTestUser(username);
    }

    @BeforeMethod
    public void setUp() {
        countryIds = new HashSet<>();
    }

    @AfterMethod
    public void tearDown() {
        testUserManager.makeUserAdministrator(username);
        countryIds.forEach(id -> climbAssistClient.deleteCountryV2(id, cookies));
        testUserManager.makeUserNotAdministrator(username);
    }

    @AfterClass
    public void tearDownClass() {
        testUserManager.cleanUp();
    }

    @Test
    public void getCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getCountry_returnsCountryNotFoundException_whenCountryIsInReviewAndUserIsNotSignedIn() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), ImmutableSet.of());
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getCountry_returnsCountryNotFoundException_whenCountryIsInReviewAndUserIsNotAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        testUserManager.makeUserNotAdministrator(username);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getCountry_returnsCountry_whenCountryIsPublicAndUserIsNotSignedIn() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        country.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country, cookies);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), ImmutableSet.of());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

    @Test
    public void getCountry_returnsCountry_whenCountryIsPublicAndUserIsNotAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        country.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country, cookies);
        testUserManager.makeUserNotAdministrator(username);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

    @Test
    public void getCountry_returnsCountry_whenCountryIsInReviewAndUserIsAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

    @Test
    public void getCountry_returnsCountry_whenCountryIsPublicAndUserIsAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        country.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country, cookies);
        ApiResponse<Country> apiResponse = climbAssistClient.getCountryV2(country.getId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

    @Test
    public void listCountries_returnsOnlyPublicCountries_whenUserIsNotSignedIn() {
        testUserManager.makeUserAdministrator(username);
        Country country1 = createCountry(NewCountry.builder()
                .name(NAME + "-1")
                .build());
        country1.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country1, cookies);
        Country country2 = createCountry(NewCountry.builder()
                .name(NAME + "-2")
                .build());
        ApiResponse<Set<Country>> apiResponse = climbAssistClient.listCountriesV2(ImmutableSet.of());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), hasItems(country1));
        assertThat(apiResponse.getData(), not(hasItems(country2)));
    }

    @Test
    public void listCountries_returnsOnlyPublicCountries_whenUserIsNotAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country1 = createCountry(NewCountry.builder()
                .name(NAME + "-1")
                .build());
        country1.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country1, cookies);
        Country country2 = createCountry(NewCountry.builder()
                .name(NAME + "-2")
                .build());
        testUserManager.makeUserNotAdministrator(username);
        ApiResponse<Set<Country>> apiResponse = climbAssistClient.listCountriesV2(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), hasItems(country1));
        assertThat(apiResponse.getData(), not(hasItems(country2)));
    }

    @Test
    public void listCountries_returnsAllCountries_whenUserIsAdministrator() {
        testUserManager.makeUserAdministrator(username);
        Country country1 = createCountry(NewCountry.builder()
                .name(NAME + "-1")
                .build());
        country1.setState(State.PUBLIC);
        climbAssistClient.updateCountryV2(country1, cookies);
        Country country2 = createCountry(NewCountry.builder()
                .name(NAME + "-2")
                .build());
        ApiResponse<Set<Country>> apiResponse = climbAssistClient.listCountriesV2(cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), hasItems(country1, country2));
    }

    @Test
    public void createCountry_createsCountryInReview() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        assertThat(climbAssistClient.getCountryV2(country.getId(), cookies)
                .getData(), is(equalTo(country)));
        assertThat(country.getState(), is(equalTo(State.IN_REVIEW)));
    }

    @Test
    public void createCountry_createsCountry_whenCountryWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Country country1 = createCountry(NEW_COUNTRY);
        Country country2 = createCountry(NEW_COUNTRY);
        assertThat(country2.getId(), is(not(equalTo(country1.getId()))));
        assertThat(country2.getName(), is(equalTo(country1.getName())));
        assertThat(country2.getState(), is(equalTo(State.IN_REVIEW)));
    }

    @Test
    public void createCountry_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<Country> apiResponse = climbAssistClient.createCountryV2(NEW_COUNTRY, ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createCountry_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<Country> apiResponse = climbAssistClient.createCountryV2(NEW_COUNTRY, cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updateCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<Country> apiResponse = climbAssistClient.updateCountryV2(Country.builder()
                .id("does-not-exist")
                .name(NAME)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updateCountry_updatesCountry() throws InterruptedException {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        country.setName(NAME + "-updated");
        country.setState(State.PUBLIC);
        ApiResponse<Country> apiResponse = climbAssistClient.updateCountryV2(country, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
    }

    @Test
    public void deleteCountry_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<Country> apiResponse = climbAssistClient.deleteCountryV2("does-not-exist", ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCountry_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<Country> apiResponse = climbAssistClient.deleteCountryV2("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCountry_returnsCountryNotFoundException_whenCountryDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<Country> apiResponse = climbAssistClient.deleteCountryV2("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deleteCountry_deletesCountry() {
        testUserManager.makeUserAdministrator(username);
        Country country = createCountry(NEW_COUNTRY);
        ApiResponse<Country> apiResponse = climbAssistClient.deleteCountryV2(country.getId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(country)));
        ApiResponse<Country> getCountryResult = climbAssistClient.getCountryV2(country.getId(), cookies);
        ExceptionUtils.assertResourceNotFoundException(getCountryResult);
    }

    private Country createCountry(NewCountry newCountry) {
        ApiResponse<Country> apiResponse = climbAssistClient.createCountryV2(newCountry, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        Country country = apiResponse.getData();
        countryIds.add(country.getId());
        return country;
    }
}

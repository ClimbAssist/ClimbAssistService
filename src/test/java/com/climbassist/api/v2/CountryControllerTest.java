package com.climbassist.api.v2;

import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.state.State;
import com.climbassist.api.resource.country.CountryNotFoundException;
import com.climbassist.api.resource.country.NewCountry;
import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    private static final Country COUNTRY_1 = Country.builder()
            .id("id-1")
            .name("name-1")
            .state(State.IN_REVIEW)
            .build();
    private static final Country UPDATED_COUNTRY_1 = Country.builder()
            .id(COUNTRY_1.getId())
            .name("updated-name")
            .state(State.PUBLIC)
            .build();
    private static final Country COUNTRY_2 = Country.builder()
            .id("id-2")
            .name("name-2")
            .state(State.IN_REVIEW)
            .build();
    private static final NewCountry NEW_COUNTRY_1 = NewCountry.builder()
            .name(COUNTRY_1.getName())
            .build();
    private static final UserData USER_DATA = UserData.builder()
            .userId("237")
            .username("jack-torrance")
            .email("dull-boy@overlook.com")
            .isAdministrator(true)
            .build();

    @Mock
    private ResourceIdGenerator mockResourceIdGenerator;
    @Mock
    private ResourcesDao mockResourcesDao;

    private CountryController countryController;

    @BeforeEach
    void setUp() {
        countryController = CountryController.builder()
                .resourceIdGenerator(mockResourceIdGenerator)
                .resourcesDao(mockResourcesDao)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(ResourcesDao.class, mockResourcesDao);
        nullPointerTester.setDefault(ResourceIdGenerator.class, mockResourceIdGenerator);
        nullPointerTester.testConstructors(CountryController.class, NullPointerTester.Visibility.PACKAGE);
        nullPointerTester.testInstanceMethods(countryController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getCountry_throwsCountryNotFoundException_whenCountryDoesNotExist() {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.empty());
        assertThrows(CountryNotFoundException.class,
                () -> countryController.getCountry(COUNTRY_1.getId(), Optional.empty()));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class);
    }

    @Test
    void getCountry_returnsCountry_whenUserIsNotSignedIn() throws ResourceNotFoundException {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.getCountry(COUNTRY_1.getId(), Optional.empty()), is(equalTo(COUNTRY_1)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class);
    }

    @Test
    void getCountry_returnsCountry_whenUserIsSignedIn() throws ResourceNotFoundException {
        when(mockResourcesDao.getResource(any(), any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.getCountry(COUNTRY_1.getId(), Optional.of(USER_DATA)), is(equalTo(COUNTRY_1)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class, USER_DATA);
    }

    @Test
    void listCountries_returnsEmptySet_whenThereAreNoCountries() {
        when(mockResourcesDao.listResources(any())).thenReturn(ImmutableSet.of());
        assertThat(countryController.listCountries(Optional.empty()), is(empty()));
        verify(mockResourcesDao).listResources(Country.class);
    }

    @Test
    void listCountries_returnsCountries_whenUserIsNotSignedIn() {
        when(mockResourcesDao.listResources(any())).thenReturn(ImmutableSet.of(COUNTRY_1, COUNTRY_2));
        assertThat(countryController.listCountries(Optional.empty()), containsInAnyOrder(COUNTRY_1, COUNTRY_2));
        verify(mockResourcesDao).listResources(Country.class);
    }

    @Test
    void listCountries_returnsCountries_whenUserIsSignedIn() {
        when(mockResourcesDao.listResources(any(), any())).thenReturn(ImmutableSet.of(COUNTRY_1, COUNTRY_2));
        assertThat(countryController.listCountries(Optional.of(USER_DATA)), containsInAnyOrder(COUNTRY_1, COUNTRY_2));
        verify(mockResourcesDao).listResources(Country.class, USER_DATA);
    }

    @Test
    void createCountry_createsCountry() {
        when(mockResourceIdGenerator.generateResourceId(any())).thenReturn(COUNTRY_1.getId());
        assertThat(countryController.createCountry(NEW_COUNTRY_1), is(equalTo(COUNTRY_1)));
        verify(mockResourceIdGenerator).generateResourceId(NEW_COUNTRY_1.getName());
        verify(mockResourcesDao).saveResource(COUNTRY_1);
    }

    @Test
    void updateCountry_throwsCountryNotFoundException_whenCountryDoesNotExistAndUserIsNotSignedIn() {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.empty());
        assertThrows(CountryNotFoundException.class,
                () -> countryController.updateCountry(COUNTRY_1, Optional.empty()));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class);
        verify(mockResourcesDao, never()).saveResource(any());
    }

    @Test
    void updateCountry_throwsCountryNotFoundException_whenCountryDoesNotExistAndUserIsSignedIn() {
        when(mockResourcesDao.getResource(any(), any(), any())).thenReturn(Optional.empty());
        assertThrows(CountryNotFoundException.class,
                () -> countryController.updateCountry(COUNTRY_1, Optional.of(USER_DATA)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class, USER_DATA);
        verify(mockResourcesDao, never()).saveResource(any());
    }

    @Test
    void updateCountry_updatesCountry_whenUserIsSignedIn() throws ResourceNotFoundException {
        when(mockResourcesDao.getResource(any(), any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.updateCountry(UPDATED_COUNTRY_1, Optional.of(USER_DATA)), is(UPDATED_COUNTRY_1));
        verify(mockResourcesDao).getResource(UPDATED_COUNTRY_1.getId(), Country.class, USER_DATA);
        verify(mockResourcesDao).saveResource(UPDATED_COUNTRY_1);
    }

    @Test
    void updateCountry_updatesCountry_whenUserIsNotSignedIn() throws ResourceNotFoundException {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.updateCountry(UPDATED_COUNTRY_1, Optional.empty()), is(UPDATED_COUNTRY_1));
        verify(mockResourcesDao).getResource(UPDATED_COUNTRY_1.getId(), Country.class);
        verify(mockResourcesDao).saveResource(UPDATED_COUNTRY_1);
    }

    @Test
    void deleteCountry_throwsCountryNotFoundException_whenCountryDoesNotExistAndUserIsNotSignedIn() {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.empty());
        assertThrows(CountryNotFoundException.class,
                () -> countryController.deleteCountry(COUNTRY_1.getId(), Optional.empty()));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class);
        verify(mockResourcesDao, never()).deleteResource(any(), any());
    }

    @Test
    void deleteCountry_throwsCountryNotFoundException_whenCountryDoesNotExistAndUserIsSignedIn() {
        when(mockResourcesDao.getResource(any(), any(), any())).thenReturn(Optional.empty());
        assertThrows(CountryNotFoundException.class,
                () -> countryController.deleteCountry(COUNTRY_1.getId(), Optional.of(USER_DATA)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class, USER_DATA);
        verify(mockResourcesDao, never()).deleteResource(any(), any());
    }

    @Test
    void deleteCountry_deletesCountry_whenUserIsNotSignedIn()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockResourcesDao.getResource(any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.deleteCountry(COUNTRY_1.getId(), Optional.empty()), is(equalTo(COUNTRY_1)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class);
        verify(mockResourcesDao).deleteResource(COUNTRY_1.getId(), Country.class);
    }

    @Test
    void deleteCountry_deletesCountry_whenUserIsSignedIn() throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockResourcesDao.getResource(any(), any(), any())).thenReturn(Optional.of(COUNTRY_1));
        assertThat(countryController.deleteCountry(COUNTRY_1.getId(), Optional.of(USER_DATA)), is(equalTo(COUNTRY_1)));
        verify(mockResourcesDao).getResource(COUNTRY_1.getId(), Country.class, USER_DATA);
        verify(mockResourcesDao).deleteResource(COUNTRY_1.getId(), Country.class);
    }
}

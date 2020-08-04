package com.climbassist.api.resource.country;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.user.UserData;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    private static final Country COUNTRY_1 = Country.builder()
            .countryId("country-1")
            .name("Country 1")
            .build();
    private static final Country COUNTRY_2 = Country.builder()
            .countryId("country-2")
            .name("Country 2")
            .build();
    private static final NewCountry NEW_COUNTRY_1 = NewCountry.builder()
            .name(COUNTRY_1.getName())
            .build();
    private static final Country UPDATED_COUNTRY_1 = Country.builder()
            .countryId(COUNTRY_1.getCountryId())
            .name("new name")
            .build();
    private static final int DEPTH = 5;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceControllerDelegate<Country, NewCountry> mockResourceControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Country, NewCountry> mockResourceWithChildrenControllerDelegate;
    @Mock
    private CountriesDao mockCountriesDao;

    private CountryController countryController;

    @BeforeEach
    void setUp() {
        countryController = CountryController.builder()
                .resourceControllerDelegate(mockResourceControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .countriesDao(mockCountriesDao)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(countryController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(COUNTRY_1);
        assertThat(countryController.getResource(COUNTRY_1.getCountryId(), DEPTH, MAYBE_USER_DATA),
                is(equalTo(COUNTRY_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(COUNTRY_1.getCountryId(), DEPTH,
                MAYBE_USER_DATA);
    }

    @Test
    void getResources_callsCountriesDao() {
        Set<Country> countries = ImmutableSet.of(COUNTRY_1, COUNTRY_2);
        when(mockCountriesDao.getResources()).thenReturn(countries);
        assertThat(countryController.getResources(), is(equalTo(countries)));
        verify(mockCountriesDao).getResources();
    }

    @Test
    void createResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        CreateCountryResult createCountryResult = CreateCountryResult.builder()
                .countryId(COUNTRY_1.getCountryId())
                .build();
        when(mockResourceControllerDelegate.createResource(any())).thenReturn(createCountryResult);
        assertThat(countryController.createResource(NEW_COUNTRY_1), is(equalTo(createCountryResult)));
        verify(mockResourceControllerDelegate).createResource(NEW_COUNTRY_1);
    }

    @Test
    void updateResource_callsResourceControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(countryController.updateResource(UPDATED_COUNTRY_1, MAYBE_USER_DATA),
                is(equalTo(updateResourceResult)));
        verify(mockResourceControllerDelegate).updateResource(UPDATED_COUNTRY_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any(), any())).thenReturn(deleteResourceResult);
        assertThat(countryController.deleteResource(COUNTRY_1.getCountryId(), MAYBE_USER_DATA),
                is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(COUNTRY_1.getCountryId(), MAYBE_USER_DATA);
    }
}

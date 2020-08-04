package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
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
class RegionControllerTest {

    private static final Region REGION_1 = Region.builder()
            .regionId("region-1")
            .countryId("country-1")
            .name("Region 1")
            .build();
    private static final Region REGION_2 = Region.builder()
            .regionId("region-2")
            .countryId("country-1")
            .name("Region 2")
            .build();
    private static final NewRegion NEW_REGION_1 = NewRegion.builder()
            .countryId(REGION_1.getCountryId())
            .name(REGION_1.getName())
            .build();
    private static final Region UPDATED_REGION_1 = Region.builder()
            .regionId(REGION_1.getRegionId())
            .countryId("country-2")
            .name("New name")
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
    private ResourceWithParentControllerDelegate<Region, NewRegion, Country> mockResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Region, NewRegion> mockResourceWithChildrenControllerDelegate;

    private RegionController regionController;

    @BeforeEach
    void setUp() {
        regionController = RegionController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(regionController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(REGION_1);
        assertThat(regionController.getResource(REGION_1.getRegionId(), DEPTH, MAYBE_USER_DATA), is(equalTo(REGION_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(REGION_1.getRegionId(), DEPTH, MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        Set<Region> regions = ImmutableSet.of(REGION_1, REGION_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(any(), any())).thenReturn(regions);
        assertThat(regionController.getResourcesForParent(REGION_1.getCountryId(), MAYBE_USER_DATA),
                is(equalTo(regions)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(REGION_1.getCountryId(),
                MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateRegionResult createRegionResult = CreateRegionResult.builder()
                .regionId(REGION_1.getRegionId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(createRegionResult);
        assertThat(regionController.createResource(NEW_REGION_1, MAYBE_USER_DATA), is(equalTo(createRegionResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_REGION_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(regionController.updateResource(UPDATED_REGION_1, MAYBE_USER_DATA),
                is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_REGION_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any(), any())).thenReturn(deleteResourceResult);
        assertThat(regionController.deleteResource(REGION_1.getRegionId(), MAYBE_USER_DATA),
                is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(REGION_1.getRegionId(), MAYBE_USER_DATA);
    }
}

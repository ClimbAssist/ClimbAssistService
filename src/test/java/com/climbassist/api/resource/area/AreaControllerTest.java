package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.region.Region;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaControllerTest {

    private static final Area AREA_1 = Area.builder()
            .areaId("area-1")
            .regionId("region-1")
            .name("Area 1")
            .description("Area 1")
            .build();
    private static final Area AREA_2 = Area.builder()
            .areaId("area-2")
            .regionId("region-2")
            .name("Area 2")
            .description("Area 2")
            .build();
    private static final Area UPDATED_AREA_1 = Area.builder()
            .areaId(AREA_1.getAreaId())
            .regionId("region-2")
            .name("new name")
            .description("new description")
            .build();
    private static final NewArea NEW_AREA_1 = NewArea.builder()
            .regionId(AREA_1.getRegionId())
            .name(AREA_1.getName())
            .description(AREA_1.getDescription())
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
    private ResourceWithParentControllerDelegate<Area, NewArea, Region> mockResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Area, NewArea> mockResourceWithChildrenControllerDelegate;

    private AreaController areaController;

    @BeforeEach
    void setUp() {
        areaController = AreaController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(areaController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(
                AREA_1); // TODO use a real value instead of MAYBE_USER_DATA
        assertThat(areaController.getResource(AREA_1.getAreaId(), DEPTH, MAYBE_USER_DATA), is(equalTo(AREA_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(AREA_1.getAreaId(), DEPTH, MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        Set<Area> areas = ImmutableSet.of(AREA_1, AREA_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(any(), any())).thenReturn(areas);
        assertThat(areaController.getResourcesForParent(AREA_1.getRegionId(), MAYBE_USER_DATA), is(equalTo(areas)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(AREA_1.getRegionId(), MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateAreaResult createAreaResult = CreateAreaResult.builder()
                .areaId(AREA_1.getAreaId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(
                createAreaResult);
        assertThat(areaController.createResource(NEW_AREA_1, MAYBE_USER_DATA), is(equalTo(createAreaResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_AREA_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(
                updateResourceResult);
        assertThat(areaController.updateResource(UPDATED_AREA_1, MAYBE_USER_DATA), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_AREA_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any(), any())).thenReturn(
                deleteResourceResult);
        assertThat(areaController.deleteResource(AREA_1.getAreaId(), MAYBE_USER_DATA),
                is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(AREA_1.getAreaId(), MAYBE_USER_DATA);
    }
}

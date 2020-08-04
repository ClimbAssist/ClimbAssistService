package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
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
class SubAreaControllerTest {

    private static final SubArea SUB_AREA_1 = SubArea.builder()
            .subAreaId("sub-area-1")
            .areaId("area-1")
            .name("Sub-Area 1")
            .description("description 1")
            .build();
    private static final SubArea SUB_AREA_2 = SubArea.builder()
            .subAreaId("sub-area-2")
            .areaId("area-1")
            .name("Sub-Area 2")
            .description("description 2")
            .build();
    private static final NewSubArea NEW_SUB_AREA_1 = NewSubArea.builder()
            .areaId(SUB_AREA_1.getAreaId())
            .name(SUB_AREA_1.getName())
            .description(SUB_AREA_1.getDescription())
            .build();
    private static final SubArea UPDATED_SUB_AREA_1 = SubArea.builder()
            .subAreaId(SUB_AREA_1.getSubAreaId())
            .areaId("area-2")
            .name("New name")
            .description("New description")
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
    private ResourceWithParentControllerDelegate<SubArea, NewSubArea, Area> mockResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<SubArea, NewSubArea> mockResourceWithChildrenControllerDelegate;

    private SubAreaController subAreaController;

    @BeforeEach
    void setUp() {
        subAreaController = SubAreaController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(subAreaController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(SUB_AREA_1);
        assertThat(subAreaController.getResource(SUB_AREA_1.getSubAreaId(), DEPTH, MAYBE_USER_DATA),
                is(equalTo(SUB_AREA_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(SUB_AREA_1.getSubAreaId(), DEPTH,
                MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        Set<SubArea> subAreas = ImmutableSet.of(SUB_AREA_1, SUB_AREA_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(any(), any())).thenReturn(subAreas);
        assertThat(subAreaController.getResourcesForParent(SUB_AREA_1.getAreaId(), MAYBE_USER_DATA),
                is(equalTo(subAreas)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(SUB_AREA_1.getAreaId(), MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateSubAreaResult createSubAreaResult = CreateSubAreaResult.builder()
                .subAreaId(SUB_AREA_1.getSubAreaId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(createSubAreaResult);
        assertThat(subAreaController.createResource(NEW_SUB_AREA_1, MAYBE_USER_DATA), is(equalTo(createSubAreaResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_SUB_AREA_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(subAreaController.updateResource(UPDATED_SUB_AREA_1, MAYBE_USER_DATA),
                is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_SUB_AREA_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any(), any())).thenReturn(deleteResourceResult);
        assertThat(subAreaController.deleteResource(SUB_AREA_1.getSubAreaId(), MAYBE_USER_DATA),
                is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(SUB_AREA_1.getSubAreaId(), MAYBE_USER_DATA);
    }
}

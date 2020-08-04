package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.crag.Crag;
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
class PathControllerTest {

    private static final Path PATH_1 = Path.builder()
            .pathId("path-1")
            .cragId("crag-1")
            .build();
    private static final Path PATH_2 = Path.builder()
            .pathId("path-2")
            .cragId("crag-1")
            .build();
    private static final NewPath NEW_PATH_1 = NewPath.builder()
            .cragId(PATH_1.getCragId())
            .build();
    private static final Path UPDATED_PATH_1 = Path.builder()
            .pathId(PATH_1.getPathId())
            .cragId("crag-2")
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
    private ResourceControllerDelegate<Path, NewPath> mockResourceControllerDelegate;
    @Mock
    private ResourceWithParentControllerDelegate<Path, NewPath, Crag> mockResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Path, NewPath> mockResourceWithChildrenControllerDelegate;

    private PathController pathController;

    @BeforeEach
    void setUp() {
        pathController = PathController.builder()
                .resourceControllerDelegate(mockResourceControllerDelegate)
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(pathController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(PATH_1);
        assertThat(pathController.getResource(PATH_1.getPathId(), DEPTH, MAYBE_USER_DATA), is(equalTo(PATH_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(PATH_1.getPathId(), DEPTH, MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        Set<Path> paths = ImmutableSet.of(PATH_1, PATH_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(any(), any())).thenReturn(paths);
        assertThat(pathController.getResourcesForParent(PATH_1.getCragId(), MAYBE_USER_DATA), is(equalTo(paths)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(PATH_1.getCragId(), MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreatePathResult createPathResult = CreatePathResult.builder()
                .pathId(PATH_1.getPathId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(createPathResult);
        assertThat(pathController.createResource(NEW_PATH_1, MAYBE_USER_DATA), is(equalTo(createPathResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_PATH_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(pathController.updateResource(UPDATED_PATH_1, MAYBE_USER_DATA), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_PATH_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_callsResourceWithChildrenControllerDelegate()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        DeleteResourceResult deleteResourceResult = DeleteResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithChildrenControllerDelegate.deleteResource(any(), any())).thenReturn(deleteResourceResult);
        assertThat(pathController.deleteResource(PATH_1.getPathId(), MAYBE_USER_DATA), is(equalTo(deleteResourceResult)));
        verify(mockResourceWithChildrenControllerDelegate).deleteResource(PATH_1.getPathId(), MAYBE_USER_DATA);
    }
}

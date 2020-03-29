package com.climbassist.api.user;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String EMAIL = "dull-boy@overlook.com";
    private static final UserData USER_DATA = UserData.builder()
            .username("jack-torrance")
            .email(EMAIL)
            .isAdministrator(true)
            .build();
    private static final String ACCESS_TOKEN = "access token";
    private static final UpdateUserRequest UPDATE_USER_REQUEST = UpdateUserRequest.builder()
            .email(EMAIL)
            .build();

    @Mock
    private UserManager mockUserManager;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = UserController.builder()
                .userManager(mockUserManager)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(UpdateUserRequest.class, UPDATE_USER_REQUEST);
        nullPointerTester.testInstanceMethods(userController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getUser_returnsUserDataFromUserManager() {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        assertThat(userController.getUser(ACCESS_TOKEN), is(equalTo(USER_DATA)));
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
    }

    @Test
    void updateUser_updatesUserAndReturnsUserData() {
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA);
        assertThat(userController.updateUser(UpdateUserRequest.builder()
                .email(USER_DATA.getEmail())
                .build(), ACCESS_TOKEN), is(equalTo(USER_DATA)));
        verify(mockUserManager).updateUser(ACCESS_TOKEN, USER_DATA.getEmail());
        verify(mockUserManager).getUserData(ACCESS_TOKEN);
    }
}
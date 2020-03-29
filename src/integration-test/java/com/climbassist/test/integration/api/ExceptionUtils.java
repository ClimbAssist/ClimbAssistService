package com.climbassist.test.integration.api;

import lombok.experimental.UtilityClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@UtilityClass
public class ExceptionUtils {

    public static void assertUserAuthorizationException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "UserAuthorizationException");
    }

    public static void assertEmailAlreadyVerifiedException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 409, "EmailAlreadyVerifiedException");
    }

    public static void assertInvalidPasswordException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "InvalidPasswordException");
    }

    public static void assertEmailNotVerifiedException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "EmailNotVerifiedException");
    }

    public static void assertUserNotVerifiedException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "UserNotVerifiedException");
    }

    public static void assertUserNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "UserNotFoundException");
    }

    public static void assertInvalidVerificationCodeException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "InvalidVerificationCodeException");
    }

    public static void assertCountryNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "CountryNotFoundException");
    }

    public static void assertRegionNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "RegionNotFoundException");
    }

    public static void assertAreaNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "AreaNotFoundException");
    }

    public static void assertSubAreaNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "SubAreaNotFoundException");
    }

    public static void assertCragNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "CragNotFoundException");
    }

    public static void assertWallNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "WallNotFoundException");
    }


    public static void assertSpecificException(ApiResponse<?> apiResponse, int code, String type) {
        assertThat(apiResponse.getData(), is(nullValue()));
        assertThat(apiResponse.getError()
                .getType(), is(equalTo(type)));
        assertThat(apiResponse.getError()
                .getCode(), is(equalTo(code)));
    }
}

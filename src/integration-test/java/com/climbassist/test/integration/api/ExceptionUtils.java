package com.climbassist.test.integration.api;

import lombok.experimental.UtilityClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@UtilityClass
public class ExceptionUtils {

    public static void assertNoException(ApiResponse<?> apiResponse) {
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getHttpStatus(), is(200));
    }

    public static void assertAuthorizationException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "AuthorizationException");
    }

    public static void assertEmailAlreadyVerifiedException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 409, "EmailAlreadyVerifiedException");
    }

    public static void assertIncorrectPasswordException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "IncorrectPasswordException");
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

    public static void assertIncorrectVerificationCodeException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 401, "IncorrectVerificationCodeException");
    }

    public static void assertResourceNotFoundException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 404, "ResourceNotFoundException");
    }

    public static void assertResourceNotEmptyException(ApiResponse<?> apiResponse) {
        assertSpecificException(apiResponse, 409, "ResourceNotEmptyException");
    }

    public static void assertSpecificException(ApiResponse<?> apiResponse, int httpStatus, String type) {
        assertThat(apiResponse.getData(), is(nullValue()));
        assertThat(apiResponse.getError()
                .getType(), is(equalTo(type)));
        assertThat(apiResponse.getHttpStatus(), is(equalTo(httpStatus)));
    }
}

package com.climbassist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * This class creates an HTTP response from the exception that was thrown. Additionally, it always sets the
 * Content-Length header to include extra space for when the response is eventually wrapped by ApiResponseFilter.
 */
@Builder
@ControllerAdvice(basePackages = "com.climbassist.api")
@RestController
@Slf4j
public class ApiExceptionHandler {

    private static final String GENERIC_EXCEPTION_MESSAGE =
            "ClimbAssist was unable to process your request at this time. Please try again later.";

    @NonNull
    private final ObjectMapper objectMapper;

    /**
     * This is only here to catch any requests to an API that doesn't exist and throw an PathNotFoundException instead
     * of falling back to Spring's default exception handling
     */
    @RequestMapping(path = {ApiConfiguration.V1_API_PATH, ApiConfiguration.V2_API_PATH})
    public void handleApiWithNoMapping(HttpServletRequest httpServletRequest) throws ApiNotFoundException {
        throw new ApiNotFoundException(httpServletRequest.getServletPath(), httpServletRequest.getMethod());
    }

    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<Object> handleApiException(@NonNull ApiException apiException)
            throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(apiException));
        return buildResponseEntity(apiException.getType(), apiException.getMessage(), apiException.getHttpStatus());
    }

    @ExceptionHandler(value = {RuntimeApiException.class})
    public ResponseEntity<Object> handleRuntimeApiException(@NonNull RuntimeApiException runtimeApiException)
            throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(runtimeApiException));
        return buildResponseEntity(runtimeApiException.getType(), runtimeApiException.getMessage(),
                runtimeApiException.getHttpStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException methodArgumentNotValidException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(methodArgumentNotValidException));
        String errorMessage = getErrorMessageFromBindingResult(methodArgumentNotValidException.getBindingResult(),
                methodArgumentNotValidException);
        return handleBadRequestException(errorMessage);
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<Object> handleBindException(BindException bindException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(bindException));
        String errorMessage = getErrorMessageFromBindingResult(bindException, bindException);
        return handleBadRequestException(errorMessage);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException constraintViolationException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(constraintViolationException));
        String errorMessage = constraintViolationException.getConstraintViolations()
                .iterator()
                .next()
                .getMessage();
        return handleBadRequestException(errorMessage);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException httpMessageNotReadableException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(httpMessageNotReadableException));
        if (httpMessageNotReadableException.getMostSpecificCause() instanceof ConstraintViolationException) {
            return handleConstraintViolationException(
                    (ConstraintViolationException) httpMessageNotReadableException.getMostSpecificCause());
        }
        if (httpMessageNotReadableException.getMessage() != null) {
            if (httpMessageNotReadableException.getMessage()
                    .contains("Required request body is missing")) {
                return handleBadRequestException("Required request body is missing.");
            }
            if (httpMessageNotReadableException.getMessage()
                    .contains("JSON parse error")) {
                return handleBadRequestException("Unable to parse input.");
            }
        }
        return buildGenericResponseEntity();
    }

    @ExceptionHandler(value = MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPartException(
            MissingServletRequestPartException missingServletRequestPartException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(missingServletRequestPartException));
        String errorMessage = missingServletRequestPartException.getMessage();
        return handleBadRequestException(errorMessage);
    }

    @ExceptionHandler(value = MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException multipartException)
            throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(multipartException));
        return handleBadRequestException(multipartException.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) throws JsonProcessingException {
        log.warn(Throwables.getStackTraceAsString(methodArgumentTypeMismatchException));
        String message = String.format("Argument %s must be of type %s.", methodArgumentTypeMismatchException.getName(),
                methodArgumentTypeMismatchException.getParameter()
                        .getParameterType()
                        .getSimpleName());
        return handleBadRequestException(message);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) throws JsonProcessingException {
        log.error(Throwables.getStackTraceAsString(exception));
        return buildGenericResponseEntity();
    }

    private ResponseEntity<Object> handleBadRequestException(String message) throws JsonProcessingException {
        return buildResponseEntity("InvalidRequestException", message, HttpStatus.BAD_REQUEST);
    }

    // This grabs the first validation message and returns it
    private String getErrorMessageFromBindingResult(BindingResult bindingResult, Exception exception) {
        return bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(exception.getMessage());
    }

    private ResponseEntity<Object> buildGenericResponseEntity() throws JsonProcessingException {
        return buildResponseEntity("InternalFailure", GENERIC_EXCEPTION_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> buildResponseEntity(String exceptionType, String exceptionMessage,
            HttpStatus httpStatus) throws JsonProcessingException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String error = objectMapper.writeValueAsString(ApiResponse.Error.builder()
                .type(exceptionType)
                .message(exceptionMessage)
                .build());
        // we have to add the Content-Length header, otherwise Spring adds it for us and it's too short because it
        // doesn't include the wrapper ApiResponse object
        headers.add("Content-Length", Integer.toString(error.length() + ApiResponse.EXTRA_CHARACTERS_FOR_ERROR));
        // we have to explicitly remove the JSESSIONID cookie (again) here because removing it in ApiResponseFilter
        // doesn't work when requests throw exceptions.
        headers.add("Set-Cookie", "JSESSIONID=; Max-Age=0; Path=/");
        return new ResponseEntity<>(error, headers, httpStatus);
    }
}

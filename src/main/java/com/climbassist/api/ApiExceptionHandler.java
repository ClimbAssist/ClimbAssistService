package com.climbassist.api;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.user.authentication.AliasExistsException;
import com.climbassist.api.user.authentication.EmailAlreadyVerifiedException;
import com.climbassist.api.user.authentication.UserAuthenticationException;
import com.climbassist.api.user.authentication.UserNotFoundException;
import com.climbassist.api.user.authorization.UserAuthorizationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @RequestMapping(path = ApiConfiguration.API_PATH)
    public void handleApiWithNoMapping(HttpServletRequest httpServletRequest) throws PathNotFoundException {
        throw new PathNotFoundException(httpServletRequest.getServletPath());
    }

    @ExceptionHandler(
            value = {ResourceNotFoundException.class, PathNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception exception) throws JsonProcessingException {
        log.warn("Caught not found exception.", exception);
        return buildResponseEntity(exception.getClass()
                .getSimpleName(), exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(
            value = {AliasExistsException.class, EmailAlreadyVerifiedException.class, ResourceNotEmptyException.class})
    public ResponseEntity<Object> handleConflictException(Exception exception) throws JsonProcessingException {
        log.warn(String.format("Caught %s.", exception.getClass()
                .getSimpleName()), exception);
        return buildResponseEntity(exception.getClass()
                .getSimpleName(), exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {UserAuthenticationException.class, UserAuthorizationException.class})
    public ResponseEntity<Object> handleUnauthorizedException(Exception exception) throws JsonProcessingException {
        log.warn(String.format("Caught %s.", exception.getClass()
                .getSimpleName()), exception);
        return buildResponseEntity(exception.getClass()
                .getSimpleName(), exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = InvalidOrderingException.class)
    public ResponseEntity<Object> handleInvalidOrderingException(InvalidOrderingException exception)
            throws JsonProcessingException {
        log.warn(String.format("Caught %s.", exception.getClass()
                .getSimpleName()), exception);
        return buildResponseEntity(exception.getClass()
                .getSimpleName(), exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException methodArgumentNotValidException) throws JsonProcessingException {
        log.warn("Caught MethodArgumentNotValidException.", methodArgumentNotValidException);
        String errorMessage = getErrorMessageFromBindingResult(methodArgumentNotValidException.getBindingResult(),
                methodArgumentNotValidException);
        return buildResponseEntity(methodArgumentNotValidException.getClass()
                .getSimpleName(), errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<Object> handleBindException(BindException bindException) throws JsonProcessingException {
        log.warn("Caught BindException.", bindException);
        String errorMessage = getErrorMessageFromBindingResult(bindException, bindException);
        return buildResponseEntity(bindException.getClass()
                .getSimpleName(), errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException constraintViolationException) throws JsonProcessingException {
        log.warn("Caught ConstraintViolationException.", constraintViolationException);
        String errorMessage = constraintViolationException.getConstraintViolations()
                .iterator()
                .next()
                .getMessage();
        return buildResponseEntity(constraintViolationException.getClass()
                .getSimpleName(), errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException httpMessageNotReadableException) throws JsonProcessingException {
        log.warn("Caught HttpMessageNotReadableException.", httpMessageNotReadableException);
        if (httpMessageNotReadableException.getMessage() != null) {
            if (httpMessageNotReadableException.getMessage()
                    .contains("Required request body is missing")) {
                return buildResponseEntity(httpMessageNotReadableException.getClass()
                        .getSimpleName(), "Required request body is missing.", HttpStatus.BAD_REQUEST);
            }
            if (httpMessageNotReadableException.getMessage()
                    .contains("JSON parse error")) {
                return buildResponseEntity(httpMessageNotReadableException.getClass()
                        .getSimpleName(), "Unable to parse input.", HttpStatus.BAD_REQUEST);
            }
        }
        return buildGenericResponseEntity();
    }

    @ExceptionHandler(value = MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPartException(
            MissingServletRequestPartException missingServletRequestPartException) throws JsonProcessingException {
        log.warn("Caught MissingServletRequestPartException.", missingServletRequestPartException);
        String errorMessage = missingServletRequestPartException.getMessage();
        return buildResponseEntity(missingServletRequestPartException.getClass()
                .getSimpleName(), errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException multipartException)
            throws JsonProcessingException {
        log.warn("Caught MultipartException.", multipartException);
        return buildResponseEntity(multipartException.getClass()
                .getSimpleName(), multipartException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) throws JsonProcessingException {
        log.warn("Caught MethodArgumentTypeMismatchException.", methodArgumentTypeMismatchException);
        String message = String.format("Argument %s must be of type %s.", methodArgumentTypeMismatchException.getName(),
                methodArgumentTypeMismatchException.getParameter()
                        .getParameterType()
                        .getSimpleName());
        return buildResponseEntity(methodArgumentTypeMismatchException.getClass()
                .getSimpleName(), message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) throws JsonProcessingException {
        log.error("Encountered an unexpected failure.", exception);
        return buildGenericResponseEntity();
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
                .code(httpStatus.value())
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

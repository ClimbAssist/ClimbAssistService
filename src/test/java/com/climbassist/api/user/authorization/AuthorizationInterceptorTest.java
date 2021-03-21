package com.climbassist.api.user.authorization;

import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationInterceptorTest {

    private static class NullAuthorizationHandler implements AuthorizationHandler {

        @Override
        public void checkAuthorization(@NonNull Optional<UserData> maybeUserData) {
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("kirby")
            .username("popopo")
            .email("kirby@dreamland.com")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private AuthorizationHandlerFactory mockAuthorizationHandlerFactory;
    @Mock
    private AuthorizationHandler mockAuthorizationHandler;

    private AuthorizationInterceptor authorizationInterceptor;

    @BeforeEach
    void setUp() {
        authorizationInterceptor = AuthorizationInterceptor.builder()
                .authorizationHandlerFactory(mockAuthorizationHandlerFactory)
                .build();
    }

    @Test
    void preHandle_returnsTrue_whenHandlerDoesNotHaveAuthorizationAnnotation()
            throws NoSuchMethodException, AuthorizationException {
        class TestClass {

            public void testMethod() {
            }
        }

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        assertThat(authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, handlerMethod),
                is(equalTo(true)));
    }

    @Test
    void preHandle_returnsTrue_whenRequestHasUserData() throws NoSuchMethodException, AuthorizationException {

        class TestClass {

            @Authorization(NullAuthorizationHandler.class)
            public void testMethod() {
            }
        }

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        //noinspection ConstantConditions
        mockHttpServletRequest.getSession()
                .setAttribute(SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME, MAYBE_USER_DATA);
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        when(mockAuthorizationHandlerFactory.create(any())).thenReturn(mockAuthorizationHandler);
        assertThat(authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, handlerMethod),
                is(equalTo(true)));
        verify(mockAuthorizationHandlerFactory).create(NullAuthorizationHandler.class);
        verify(mockAuthorizationHandler).checkAuthorization(MAYBE_USER_DATA);
    }
}

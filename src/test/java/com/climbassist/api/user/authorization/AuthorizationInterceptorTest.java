package com.climbassist.api.user.authorization;

import com.climbassist.api.user.CookieTestUtils;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationInterceptorTest {

    private static class NullAuthorizationHandler implements AuthorizationHandler {

        @Override
        public void checkAuthorization(@NonNull UserSessionData userSessionData) {
        }
    }

    private static final String ACCESS_TOKEN = "access token";
    private static final String REFRESH_TOKEN = "refresh token";
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken(ACCESS_TOKEN)
            .refreshToken(REFRESH_TOKEN)
            .build();

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
    void preHandle_throwsAuthorizationException_whenRequestDoesNotHaveSessionCookies() throws NoSuchMethodException {
        class TestClass {

            @Authorization(NullAuthorizationHandler.class)
            public void testMethod() {

            }
        }

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setCookies();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        assertThrows(AuthorizationException.class,
                () -> authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse,
                        handlerMethod));
    }

    @Test
    void preHandle_setsCookiesAndSetsAccessTokenSessionAttributeAndReturnsTrue_whenRequestHasSessionCookies()
            throws NoSuchMethodException, AuthorizationException {

        class TestClass {

            @Authorization(NullAuthorizationHandler.class)
            public void testMethod() {
            }
        }

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setCookies(CookieTestUtils.buildSessionCookies(ACCESS_TOKEN, REFRESH_TOKEN));
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        when(mockAuthorizationHandlerFactory.create(any())).thenReturn(mockAuthorizationHandler);
        assertThat(authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, handlerMethod),
                is(equalTo(true)));
        verify(mockAuthorizationHandlerFactory).create(NullAuthorizationHandler.class);
        verify(mockAuthorizationHandler).checkAuthorization(USER_SESSION_DATA);
    }
}

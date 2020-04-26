package com.climbassist.api.user.authorization;

import com.climbassist.api.user.CookieTestUtils;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.authentication.UserSessionData;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationInterceptorTest {

    private static class NullAuthorizationHandler implements AuthorizationHandler {

        @Override
        public UserSessionData checkAuthorization(@NonNull UserSessionData userSessionData) {
            return null;
        }
    }

    private static final String ACCESS_TOKEN = "access token";
    private static final String NEW_ACCESS_TOKEN = "new access token";
    private static final String REFRESH_TOKEN = "refresh token";
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken(ACCESS_TOKEN)
            .refreshToken(REFRESH_TOKEN)
            .build();
    private static final UserSessionData NEW_USER_SESSION_DATA = UserSessionData.builder()
            .accessToken(NEW_ACCESS_TOKEN)
            .refreshToken(REFRESH_TOKEN)
            .build();
    private static final Cookie ACCESS_TOKEN_COOKIE = CookieTestUtils.buildCookie(
            CookieTestUtils.ACCESS_TOKEN_COOKIE_NAME, ACCESS_TOKEN);
    private static final Cookie REFRESH_TOKEN_COOKIE = CookieTestUtils.buildCookie(
            CookieTestUtils.REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN);

    @Mock
    private AuthorizationHandlerFactory mockAuthorizationHandlerFactory;
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock
    private HttpServletResponse mockHttpServletResponse;
    @Mock
    private AuthorizationHandler mockAuthorizationHandler;
    @Mock
    private HttpSession mockHttpSession;

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

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        assertThat(authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, handlerMethod),
                is(equalTo(true)));
        verify(mockHttpServletRequest, never()).getCookies();
        verify(mockHttpServletRequest, never()).getSession();
        verify(mockHttpServletResponse, never()).addCookie(any());
    }

    @Test
    void preHandle_throwsAuthorizationException_whenRequestDoesNotHaveSessionCookies()
            throws NoSuchMethodException {
        class TestClass {

            @Authorization(NullAuthorizationHandler.class)
            public void testMethod() {

            }
        }

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{});
        assertThrows(AuthorizationException.class,
                () -> authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse,
                        handlerMethod));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
        verify(mockHttpServletRequest, never()).getSession();
        verify(mockHttpServletResponse, never()).addCookie(any());
    }

    @Test
    void preHandle_setsCookiesAndSetsAccessTokenSessionAttributeAndReturnsTrue()
            throws NoSuchMethodException, AuthorizationException {
        class TestClass {

            @Authorization(NullAuthorizationHandler.class)
            public void testMethod() {

            }
        }

        HandlerMethod handlerMethod = new HandlerMethod(new TestClass(), TestClass.class.getMethod("testMethod"));
        when(mockHttpServletRequest.getCookies()).thenReturn(new Cookie[]{ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE});
        when(mockAuthorizationHandlerFactory.create(any())).thenReturn(mockAuthorizationHandler);
        when(mockAuthorizationHandler.checkAuthorization(any())).thenReturn(NEW_USER_SESSION_DATA);
        when(mockHttpServletRequest.getSession()).thenReturn(mockHttpSession);
        assertThat(authorizationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, handlerMethod),
                is(equalTo(true)));
        verify(mockHttpServletRequest, atLeastOnce()).getCookies();
        verify(mockAuthorizationHandlerFactory).create(NullAuthorizationHandler.class);
        verify(mockAuthorizationHandler).checkAuthorization(USER_SESSION_DATA);
        CookieTestUtils.verifySessionCookiesAreSet(mockHttpServletResponse, NEW_USER_SESSION_DATA);
        verify(mockHttpServletRequest).getSession();
        verify(mockHttpSession).setAttribute(SessionUtils.ACCESS_TOKEN_SESSION_ATTRIBUTE_NAME, NEW_ACCESS_TOKEN);
    }
}

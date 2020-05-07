package com.climbassist.api;

import com.climbassist.api.user.SessionUtils;
import com.climbassist.wrapper.response.ResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class converts all API responses into JSON objects which comply with ClimbAssist REST standards
 */
@Builder
@Slf4j
public final class ApiResponseFilter implements Filter {

    @NonNull
    private final ObjectMapper objectMapper;
    @NonNull
    private final ApiResponseFactory apiResponseFactory;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        ResponseWrapper responseWrapper = new ResponseWrapper(httpServletResponse);
        responseWrapper.setCustomHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        filterChain.doFilter(servletRequest, responseWrapper);

        // some of our interceptors use sessions to pass data to the controller
        // we don't need this returned to the browser or exposed at all, so this removes the session ID cookie
        SessionUtils.removeJSessionIdCookie(httpServletResponse);

        if (httpServletResponse.getStatus() >= 400) {
            String error = responseWrapper.getBody();
            servletResponse.getOutputStream()
                    .write(objectMapper.writeValueAsBytes(apiResponseFactory.createFromError(error)));
        }
        else {
            String data = responseWrapper.getBody();
            servletResponse.getOutputStream()
                    .write(objectMapper.writeValueAsBytes(apiResponseFactory.createFromData(data)));
        }
    }
}

package com.climbassist.logging;

import com.climbassist.wrapper.request.RequestWrapper;
import com.climbassist.wrapper.response.ResponseWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class RequestResponseLoggingFilter implements Filter {

    @NonNull
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        boolean isHealthCheck = ((HttpServletRequest) servletRequest).getServletPath()
                .equals("/health");

        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);

        if (!isHealthCheck) {
            Map<String, String> queryParameters = UriComponentsBuilder.fromHttpRequest(
                    new ServletServerHttpRequest(requestWrapper))
                    .build()
                    .getQueryParams()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, queryParameter -> queryParameter.getValue()
                            .get(0)));

            boolean isJson = isJson(requestWrapper.getBody());
            LoggableRequest loggableRequest = LoggableRequest.builder()
                    .protocol(requestWrapper.getProtocol())
                    .sender(requestWrapper.getRemoteAddr())
                    .method(((HttpServletRequest) servletRequest).getMethod())
                    .path(((HttpServletRequest) servletRequest).getServletPath())
                    .queryString(requestWrapper.getQueryString())
                    .queryParameters(queryParameters)
                    .headers(getHeaders(requestWrapper))
                    .body(isJson ? null : requestWrapper.getBody())
                    .jsonBody(isJson ? requestWrapper.getBody() : null)
                    .build();
            log.info("Request: " + objectMapper.writeValueAsString(loggableRequest));
        }

        long startTime = System.currentTimeMillis();
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(requestWrapper, responseWrapper);
        long duration = System.currentTimeMillis() - startTime;

        if (!isHealthCheck) {
            boolean isJson = isJson(responseWrapper.getBody());
            LoggableResponse loggableResponse = LoggableResponse.builder()
                    .duration(duration)
                    .status(responseWrapper.getStatus())
                    .headers(getHeaders(responseWrapper))
                    .body(isJson ? null : responseWrapper.getBody())
                    .jsonBody(isJson ? responseWrapper.getBody() : null)
                    .build();
            log.info("Response: " + objectMapper.writeValueAsString(loggableResponse));
        }

        servletResponse.getOutputStream()
                .write(responseWrapper.getData());
    }

    private Multimap<String, String> getHeaders(HttpServletRequest httpServletRequest) {
        Multimap<String, String> headers = HashMultimap.create();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.put(headerName, headerValues.nextElement());
            }
        }
        return headers;
    }

    private Multimap<String, String> getHeaders(HttpServletResponse httpServletResponse) {
        Multimap<String, String> headers = HashMultimap.create();
        for (String headerName : httpServletResponse.getHeaderNames()) {
            for (String headerValue : httpServletResponse.getHeaders(headerName)) {
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    private boolean isJson(String string) {
        try {
            objectMapper.readTree(string);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}

package com.climbassist.logging;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * This class adds a request ID to the Slf4j context so that requests can be traced through the logs
 */
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_KEY = "RequestId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        MDC.put(REQUEST_ID_KEY, UUID.randomUUID()
                .toString());
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(REQUEST_ID_KEY);
    }
}

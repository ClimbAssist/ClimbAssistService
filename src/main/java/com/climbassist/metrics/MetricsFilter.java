package com.climbassist.metrics;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class emits error, fault, and duration metrics for APIs
 */
@Builder
@Slf4j
public class MetricsFilter implements Filter {

    @NonNull
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    @NonNull
    private final MetricsEmitter metricsEmitter;

    @Override
    public void doFilter(@NonNull ServletRequest servletRequest, @NonNull ServletResponse servletResponse,
                         @NonNull FilterChain filterChain) {
        try {
            double startTime = System.currentTimeMillis();
            filterChain.doFilter(servletRequest, servletResponse);
            double duration = System.currentTimeMillis() - startTime;

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            int status = httpServletResponse.getStatus();

            HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(httpServletRequest);
            if (handlerExecutionChain != null) {
                HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();
                Metrics metrics = handlerMethod.getMethodAnnotation(Metrics.class);
                if (metrics != null) {
                    String api = metrics.api();
                    boolean isError = status >= 400 && status < 500;
                    boolean isFault = status >= 500;

                    metricsEmitter.emitErrorMetric(api, isError);
                    metricsEmitter.emitFaultMetric(api, isFault);
                    metricsEmitter.emitDurationMetric(api, duration);
                }
            }
        } catch (Exception e) {
            log.error("Caught exception while emitting metrics", e);
            throw new MetricsException(e);
        }
    }
}

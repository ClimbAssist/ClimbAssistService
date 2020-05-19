package com.climbassist;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Utility to initialize the Spring MVC ClimbAssist application.
 */
public class ClimbAssistInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // These use beans that are defined with names matching the DeletingFilterProxy name.
        // These have to be registered in this order in order for the logging to occur after the response is wrapped.

        FilterRegistration.Dynamic dynamicFilterRegistration = servletContext.addFilter("RequestIdFilter",
                new DelegatingFilterProxy("requestIdFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/*");

        dynamicFilterRegistration = servletContext.addFilter("UserDataDecorationFilter",
                new DelegatingFilterProxy("userDataDecorationFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/*");

        dynamicFilterRegistration = servletContext.addFilter("RequestResponseLoggingFilter",
                new DelegatingFilterProxy("requestResponseLoggingFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/*");

        dynamicFilterRegistration = servletContext.addFilter("ApiResponseFilter",
                new DelegatingFilterProxy("apiResponseFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/v1/*");

        dynamicFilterRegistration = servletContext.addFilter("CharacterEncodingFilter",
                new DelegatingFilterProxy("characterEncodingFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/*");

        dynamicFilterRegistration = servletContext.addFilter("MetricsFilter",
                new DelegatingFilterProxy("metricsFilter"));
        dynamicFilterRegistration.addMappingForUrlPatterns(null, false, "/*");

        super.onStartup(servletContext);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{MvcConfiguration.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

}

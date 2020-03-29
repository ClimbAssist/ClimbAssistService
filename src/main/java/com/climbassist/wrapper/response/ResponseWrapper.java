package com.climbassist.wrapper.response;

import org.apache.commons.io.output.NullOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to wrap an HttpServletResponse to allow the body of the request to be logged and modified, as well
 * as allow the addition of headers.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private final Map<String, String> customHeaders;
    private ByteArrayOutputStream byteArrayOutputStream;
    private FilterServletOutputStream filterServletOutputStream;

    public ResponseWrapper(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        byteArrayOutputStream = new ByteArrayOutputStream();
        customHeaders = new HashMap<>();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (filterServletOutputStream == null) {
            filterServletOutputStream = new FilterServletOutputStream(byteArrayOutputStream);
        }
        return filterServletOutputStream;
    }


    public byte[] getData() {
        return byteArrayOutputStream.toByteArray();
    }

    public String getBody() {
        return new String(byteArrayOutputStream.toByteArray());
    }

    public void setCustomHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        if (customHeaders.containsKey(name)) {
            return customHeaders.get(name);
        }
        return ((HttpServletResponse) getResponse()).getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        Set<String> set = new HashSet<>(customHeaders.keySet());
        set.addAll(((HttpServletResponse) getResponse()).getHeaderNames());
        return set;
    }

    /**
     * This method isn't actually used by any application code, but it needs to be overridden so that calls to
     * getWriter() do not propagate to the underlying response. If this happens, getOutputStream() is unable to be
     * called.
     */
    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(new NullOutputStream());
    }
}
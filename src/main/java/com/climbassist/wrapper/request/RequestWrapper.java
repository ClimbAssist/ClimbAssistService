package com.climbassist.wrapper.request;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used to wrap an HttpServletRequest to allow the body of the request to be logged.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private static final long MAX_STRING_SIZE = 10 * FileUtils.ONE_KB;

    private byte[] buffer;

    public RequestWrapper(HttpServletRequest httpServletRequest) throws IOException {
        super(httpServletRequest);
        buffer = IOUtils.toByteArray(httpServletRequest.getInputStream());
    }

    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        return new FilterServletInputStream(byteArrayInputStream);
    }

    /**
     * returns the body of the request as a string, up to 1 MB before it is truncated
     */
    public String getBody() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ServletInputStream servletInputStream = getInputStream();
        long totalSize = 0L;
        byte[] stringBuffer = new byte[1024];
        int readSize;
        while ((readSize = servletInputStream.read(stringBuffer)) > 0) {
            byteArrayOutputStream.write(stringBuffer, 0, readSize);
            totalSize += readSize;
            if (totalSize > MAX_STRING_SIZE) {
                break;
            }
        }
        String body = new String(byteArrayOutputStream.toByteArray());
        if (totalSize > MAX_STRING_SIZE) {
            body = body + " [content truncated]";
        }
        return body;
    }
}

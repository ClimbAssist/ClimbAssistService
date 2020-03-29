package com.climbassist.wrapper.request;

import org.springframework.lang.NonNull;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;

/**
 * This class is used by RequestWrapper to allow the body of a request to be read before passing it to the controller.
 */
public class FilterServletInputStream extends ServletInputStream {

    private ByteArrayInputStream byteArrayInputStream;

    FilterServletInputStream(ByteArrayInputStream byteArrayInputStream) {
        this.byteArrayInputStream = byteArrayInputStream;
    }

    public int available() {
        return byteArrayInputStream.available();
    }

    public int read() {
        return byteArrayInputStream.read();
    }

    public int read(@NonNull byte[] buf, int off, int len) {
        return byteArrayInputStream.read(buf, off, len);
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }
}
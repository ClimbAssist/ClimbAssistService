package com.climbassist.wrapper.response;

import org.springframework.lang.NonNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterServletOutputStream extends ServletOutputStream {

    private DataOutputStream dataOutputStream;

    FilterServletOutputStream(OutputStream outputStream) {
        this.dataOutputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void write(int arg0) throws IOException {
        dataOutputStream.write(arg0);
    }

    @Override
    public void write(@NonNull byte[] arg0, int arg1, int arg2) throws IOException {
        dataOutputStream.write(arg0, arg1, arg2);
    }

    @Override
    public void write(@NonNull byte[] arg0) throws IOException {
        dataOutputStream.write(arg0);
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
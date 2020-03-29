package com.climbassist.api.resource;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@UtilityClass
public class MultipartFileTestUtils {

    public static MultipartFile buildMultipartFile(String name, String content) {
        try {
            return new MockMultipartFile(name, IOUtils.toInputStream(content));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

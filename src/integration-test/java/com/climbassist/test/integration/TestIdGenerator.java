package com.climbassist.test.integration;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

@UtilityClass
public class TestIdGenerator {

    public static String generateTestId() {
        return "integ-" + RandomStringUtils.randomAlphanumeric(10)
                .toLowerCase();
    }
}

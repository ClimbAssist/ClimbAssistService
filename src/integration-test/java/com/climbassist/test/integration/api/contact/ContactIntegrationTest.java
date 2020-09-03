package com.climbassist.test.integration.api.contact;

import com.climbassist.api.contact.SendContactEmailResult;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ClimbAssistClientConfiguration.class)
public class ContactIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ClimbAssistClient climbAssistClient;

    @Test
    public void sendContactEmail_returnsInvalidRequestException_whenUserIsNotSignedInAndRecaptchaResponseIsNotPresent() {
        ApiResponse<SendContactEmailResult> apiResponse = climbAssistClient.sendContactEmail("test", "test", "test",
                ImmutableSet.of());
        ExceptionUtils.assertSpecificException(apiResponse, 400, "InvalidRequestException");
    }
}

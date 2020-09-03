package com.climbassist.test.integration.api.recaptcha;

import com.climbassist.api.contact.GetRecaptchaSiteKeyResult;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = ClimbAssistClientConfiguration.class)
public class RecaptchaIntegrationTest extends AbstractTestNGSpringContextTests {

    // we don't run tests against prod right now, but if we ever add them we will need to add the prod site key here
    private static final Map<String, String> RECAPTCHA_SITE_KEYS = ImmutableMap.of("dev",
            "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI", "beta", "6LemOOkUAAAAAFCicdhEHdYWoW6kco7B57N9Ov05");

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private String stage;

    @Test
    public void getRecaptchaSiteKey_returnsRecaptchaSiteKey() {
        ApiResponse<GetRecaptchaSiteKeyResult> apiResponse = climbAssistClient.getRecaptchaSiteKey();
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .getSiteKey(), is(equalTo(RECAPTCHA_SITE_KEYS.get(stage))));
    }
}

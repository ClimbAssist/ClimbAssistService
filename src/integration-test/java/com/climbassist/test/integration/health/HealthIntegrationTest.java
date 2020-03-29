package com.climbassist.test.integration.health;

import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = ClimbAssistClientConfiguration.class)
public class HealthIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ClimbAssistClient climbAssistClient;

    @Test
    public void healthCheck_returns200() {
        HttpResponse httpResponse = climbAssistClient.get("/health");
        assertThat(httpResponse.getStatusLine()
                .getStatusCode(), equalTo(HttpStatus.SC_OK));
    }

}

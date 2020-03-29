package com.climbassist.test.integration.main;

import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = ClimbAssistClientConfiguration.class)
public class MainIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ClimbAssistClient climbAssistClient;

    @Test
    public void main_returnsIndex_whenPathIsEmpty() throws IOException {
        runMainTest("");
    }

    @Test
    public void main_returnsIndex_whenPathIsNotEmptyAndNotApi() throws IOException {
        runMainTest("/somepath/whatever");
    }

    private void runMainTest(String path) throws IOException {
        HttpResponse httpResponse = climbAssistClient.get(path);
        assertThat(httpResponse.getStatusLine()
                .getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertThat(IOUtils.toString(httpResponse.getEntity()
                .getContent()), is(not(emptyString())));
    }

}

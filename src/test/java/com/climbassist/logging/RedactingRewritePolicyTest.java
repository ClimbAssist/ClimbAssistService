package com.climbassist.logging;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.DefaultThreadContextStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class RedactingRewritePolicyTest {

    private static final String EXPECTED_REDACTED_STRING = "<REDACTED>";
    private static final String USERNAME = "han-solo";
    private static final String EMAIL = "han@milleniumfalcon.com";
    private static final String PASSWORD = "han-shot-first";
    private static final String NEW_PASSWORD = "<12parsecs";

    private RedactingRewritePolicy redactingRewritePolicy;

    @BeforeEach
    public void setUp() {
        redactingRewritePolicy = RedactingRewritePolicy.createPolicy();
    }

    @Test
    public void rewrite_doesNothing_whenMessageIsEmpty() {
        LogEvent logEvent = buildLogEvent("");
        assertThat(redactingRewritePolicy.rewrite(logEvent), is(equalTo(logEvent)));
    }

    @Test
    public void rewrite_doesNothing_whenMessageDoesHaveAnySensitiveFields() {
        LogEvent logEvent = buildLogEvent("this is just some message with nothing sensitive in it");
        assertThat(redactingRewritePolicy.rewrite(logEvent), is(equalTo(logEvent)));
    }

    @Test
    public void rewrite_replacesSensitiveFields_whenInputHasSensitiveFieldsInJson() throws IOException {
        runJsonTest("testData/sensitive-fields-template.json");
    }

    @Test
    public void rewrite_replacesSensitiveFields_whenInputHasSensitiveFieldsFormattedWeirdInJson() throws IOException {
        runJsonTest("testData/sensitive-fields-weird-formatting-template.json");
    }

    @Test
    public void rewrite_replacesSensitiveFields_whenInputHasAliasAlreadyExistsMessages() {
        String aliasAlreadyExistsMessageTemplate =
                "Caught exception! User with username %s already exists. Also! One other thing. User with email %s " +
                        "already exists.";
        LogEvent logEvent = buildLogEvent(String.format(aliasAlreadyExistsMessageTemplate, USERNAME, EMAIL));
        assertEquals(redactingRewritePolicy.rewrite(logEvent), buildLogEvent(
                String.format(aliasAlreadyExistsMessageTemplate, EXPECTED_REDACTED_STRING, EXPECTED_REDACTED_STRING)));
    }

    @Test
    public void rewrite_replacesSensitiveFields_whenInputHasAliasNotFoundMessages() {
        String aliasNotFoundMessageTemplate =
                "Caught exception! User with username %s does not exist. Also! One other thing. User with email %s " +
                        "does not exist.";
        LogEvent logEvent = buildLogEvent(String.format(aliasNotFoundMessageTemplate, USERNAME, EMAIL));
        assertEquals(redactingRewritePolicy.rewrite(logEvent), buildLogEvent(
                String.format(aliasNotFoundMessageTemplate, EXPECTED_REDACTED_STRING, EXPECTED_REDACTED_STRING)));
    }

    @Test
    public void rewrite_replacesSensitiveFields_whenInputHasDuplicatedMessagesWithSensitiveFields() {
        String aliasAlreadyExistsMessageTemplate =
                "Caught exception! User with username %s already exists. Also! One other thing. User with username %s" +
                        " already exists.";
        LogEvent logEvent = buildLogEvent(String.format(aliasAlreadyExistsMessageTemplate, USERNAME, USERNAME));
        assertEquals(redactingRewritePolicy.rewrite(logEvent), buildLogEvent(
                String.format(aliasAlreadyExistsMessageTemplate, EXPECTED_REDACTED_STRING, EXPECTED_REDACTED_STRING)));
    }

    private void runJsonTest(String jsonTemplateFilePath) throws IOException {
        String jsonStringWithSensitiveFields = formatJsonTemplateWithSensitiveValues(jsonTemplateFilePath);
        String jsonStringWithSensitiveFieldsRedacted = formatJsonTemplateWithRedactedValues(jsonTemplateFilePath);
        assertEquals(redactingRewritePolicy.rewrite(buildLogEvent(jsonStringWithSensitiveFields)),
                buildLogEvent(jsonStringWithSensitiveFieldsRedacted));
    }

    private LogEvent buildLogEvent(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setMarker(new MarkerManager.Log4jMarker("test"))
                .setLoggerFqcn("test")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .setThrown(new Throwable("test"))
                .setContextStack(new DefaultThreadContextStack(false))
                .setThreadName("test")
                .setSource(new StackTraceElement("test", "test", "test", 69))
                .setTimeMillis(42)
                .build();
    }

    private void assertEquals(LogEvent actualLogEvent, LogEvent expectedLogEvent) {
        assertThat(actualLogEvent.getLoggerName(), is(equalTo(expectedLogEvent.getLoggerName())));
        assertThat(actualLogEvent.getMarker(), is(equalTo(expectedLogEvent.getMarker())));
        assertThat(actualLogEvent.getLoggerFqcn(), is(equalTo(expectedLogEvent.getLoggerFqcn())));
        assertThat(actualLogEvent.getLevel(), is(equalTo(expectedLogEvent.getLevel())));
        assertThat(actualLogEvent.getMessage(), is(equalTo(expectedLogEvent.getMessage())));
        assertThat(actualLogEvent.getThrown()
                .getMessage(), is(equalTo(expectedLogEvent.getThrown()
                .getMessage())));
        assertThat(actualLogEvent.getContextStack()
                .getDepth(), is(equalTo(expectedLogEvent.getContextStack()
                .getDepth())));
        assertThat(actualLogEvent.getThreadName(), is(equalTo(expectedLogEvent.getThreadName())));
        assertThat(actualLogEvent.getSource(), is(equalTo(expectedLogEvent.getSource())));
        assertThat(actualLogEvent.getTimeMillis(), is(equalTo(expectedLogEvent.getTimeMillis())));
    }

    private String formatJsonTemplateWithSensitiveValues(String jsonTemplateFileName) throws IOException {
        return String.format(FileUtils.readFileToString(new File(jsonTemplateFileName)), USERNAME, EMAIL, PASSWORD,
                NEW_PASSWORD);
    }

    private String formatJsonTemplateWithRedactedValues(String jsonTemplateFileName) throws IOException {
        return String.format(FileUtils.readFileToString(new File(jsonTemplateFileName)), EXPECTED_REDACTED_STRING,
                EXPECTED_REDACTED_STRING, EXPECTED_REDACTED_STRING, EXPECTED_REDACTED_STRING);
    }

}

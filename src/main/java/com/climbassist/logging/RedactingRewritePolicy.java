package com.climbassist.logging;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.Set;
import java.util.regex.Pattern;

@Plugin(name = "RedactingRewritePolicy", category = "Core", elementType = "rewritePolicy", printObject = true)
public class RedactingRewritePolicy implements RewritePolicy {

    private static final Pattern JSON_KEY_VALUE_PATTERN = Pattern.compile(
            "(\"(?:username|email|password|newPassword|emailBody|replyToEmail|subject)\"[\\s]*:[\\s]*\")(?:[^\"]*)" +
                    "(\")");
    private static final Pattern ALIAS_EXISTS_PATTERN = Pattern.compile(
            "(User with (?:username|email) )(?:[^ ]*)( already exists.)");
    private static final Pattern USER_NOT_FOUND_PATTERN = Pattern.compile(
            "(User with (?:username|email) )(?:[^ ]*)( does not exist.)");
    private static final String REPLACEMENT = "$1<REDACTED>$2";

    private static final Set<Pattern> REDACTIONS = ImmutableSet.<Pattern>builder().add(JSON_KEY_VALUE_PATTERN)
            .add(ALIAS_EXISTS_PATTERN)
            .add(USER_NOT_FOUND_PATTERN)
            .build();

    @Override
    public LogEvent rewrite(final LogEvent logEvent) {
        String message = logEvent.getMessage()
                .getFormattedMessage();
        for (Pattern redaction : REDACTIONS) {
            message = redaction.matcher(message)
                    .replaceAll(REPLACEMENT);
        }

        return Log4jLogEvent.newBuilder()
                .setLoggerName(logEvent.getLoggerName())
                .setMarker(logEvent.getMarker())
                .setLoggerFqcn(logEvent.getLoggerFqcn())
                .setLevel(logEvent.getLevel())
                .setMessage(new SimpleMessage(message))
                .setThrown(logEvent.getThrown())
                .setContextStack(logEvent.getContextStack())
                .setThreadName(logEvent.getThreadName())
                .setSource(logEvent.getSource())
                .setTimeMillis(logEvent.getTimeMillis())
                .build();
    }

    @PluginFactory
    public static RedactingRewritePolicy createPolicy() {
        return new RedactingRewritePolicy();
    }

}

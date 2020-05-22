package com.climbassist.api.contact;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final String CLIMB_ASSIST_EMAIL = "info@climbassist.com";
    private static final SendContactEmailRequest SEND_CONTACT_EMAIL_REQUEST = SendContactEmailRequest.builder()
            .subject("subject")
            .emailBody("body")
            .replyToEmail("link@hyrule.com")
            .build();
    private static final RecaptchaKeys RECAPTCHA_KEYS = RecaptchaKeys.builder()
            .siteKey("site-key")
            .secretKey("secret-key")
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final GetRecaptchaSiteKeyResult GET_RECAPTCHA_SITE_KEY_RESULT = GetRecaptchaSiteKeyResult.builder()
            .siteKey(RECAPTCHA_KEYS.getSiteKey())
            .build();
    private static final String RECAPTCHA_KEYS_SECRET_ID = "ClimbAssistRecaptchaKeys";

    @Mock
    private AmazonSimpleEmailService mockAmazonSimpleEmailService;
    @Mock
    private AWSSecretsManager mockAwsSecretsManager;
    @Mock
    private ObjectMapper mockObjectMapper;

    private ContactController contactController;

    @BeforeEach
    void setUp() {
        contactController = ContactController.builder()
                .climbAssistEmail(CLIMB_ASSIST_EMAIL)
                .amazonSimpleEmailService(mockAmazonSimpleEmailService)
                .awsSecretsManager(mockAwsSecretsManager)
                .recaptchaKeysSecretId(RECAPTCHA_KEYS_SECRET_ID)
                .objectMapper(mockObjectMapper)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(contactController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void sendContactEmail_sendsEmail() {
        contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST);
        verify(mockAmazonSimpleEmailService).sendEmail(new SendEmailRequest().withSource(CLIMB_ASSIST_EMAIL)
                .withDestination(new Destination(ImmutableList.of(CLIMB_ASSIST_EMAIL)))
                .withReplyToAddresses(SEND_CONTACT_EMAIL_REQUEST.getReplyToEmail())
                .withMessage(new Message(new Content(SEND_CONTACT_EMAIL_REQUEST.getSubject()),
                        new Body(new Content(SEND_CONTACT_EMAIL_REQUEST.getEmailBody())))));
    }

    @Test
    void getRecaptchaSiteKey_returnsRecaptchaSiteKey() throws JsonProcessingException {
        String recaptchaKeysAsString = OBJECT_MAPPER.writeValueAsString(RECAPTCHA_KEYS);
        when(mockAwsSecretsManager.getSecretValue(any())).thenReturn(
                new GetSecretValueResult().withSecretString(recaptchaKeysAsString));
        when(mockObjectMapper.readValue(anyString(), eq(RecaptchaKeys.class))).thenReturn(RECAPTCHA_KEYS);
        assertThat(contactController.getRecaptchaSiteKey(), is(equalTo(GET_RECAPTCHA_SITE_KEY_RESULT)));
        verify(mockAwsSecretsManager).getSecretValue(
                new GetSecretValueRequest().withSecretId(RECAPTCHA_KEYS_SECRET_ID));
        verify(mockObjectMapper).readValue(recaptchaKeysAsString, RecaptchaKeys.class);
    }
}

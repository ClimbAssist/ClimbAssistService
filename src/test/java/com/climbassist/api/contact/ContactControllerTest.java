package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.climbassist.common.recaptcha.RecaptchaKeys;
import com.climbassist.common.recaptcha.RecaptchaKeysRetriever;
import com.climbassist.common.recaptcha.RecaptchaVerificationException;
import com.climbassist.common.recaptcha.RecaptchaVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final String CLIMB_ASSIST_EMAIL = "info@climbassist.com";
    private static final SendContactEmailRequest SEND_CONTACT_EMAIL_REQUEST = SendContactEmailRequest.builder()
            .subject("subject")
            .emailBody("body")
            .replyToEmail("link@hyrule.com")
            .recaptchaResponse("recaptcha-response")
            .build();
    private static final RecaptchaKeys RECAPTCHA_KEYS = RecaptchaKeys.builder()
            .siteKey("site-key")
            .secretKey("secret-key")
            .build();
    private static final GetRecaptchaSiteKeyResult GET_RECAPTCHA_SITE_KEY_RESULT = GetRecaptchaSiteKeyResult.builder()
            .siteKey(RECAPTCHA_KEYS.getSiteKey())
            .build();
    private static final HttpServletRequest HTTP_SERVLET_REQUEST = buildHttpServletRequest();

    @Mock
    private AmazonSimpleEmailService mockAmazonSimpleEmailService;
    @Mock
    private RecaptchaVerifier mockRecaptchaVerifier;
    @Mock
    private RecaptchaKeysRetriever mockRecaptchaKeysRetriever;

    private ContactController contactController;

    @BeforeEach
    void setUp() {
        contactController = ContactController.builder()
                .climbAssistEmail(CLIMB_ASSIST_EMAIL)
                .amazonSimpleEmailService(mockAmazonSimpleEmailService)
                .recaptchaVerifier(mockRecaptchaVerifier)
                .recaptchaKeysRetriever(mockRecaptchaKeysRetriever)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(SendContactEmailRequest.class, SEND_CONTACT_EMAIL_REQUEST);
        nullPointerTester.testInstanceMethods(contactController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void sendContactEmail_sendsEmail_whenRecaptchaVerificationSucceeds()
            throws IOException, RecaptchaVerificationException {
        contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST, HTTP_SERVLET_REQUEST);
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(SEND_CONTACT_EMAIL_REQUEST.getRecaptchaResponse(),
                HTTP_SERVLET_REQUEST.getRemoteAddr());
        verify(mockAmazonSimpleEmailService).sendEmail(new SendEmailRequest().withSource(CLIMB_ASSIST_EMAIL)
                .withDestination(new Destination(ImmutableList.of(CLIMB_ASSIST_EMAIL)))
                .withReplyToAddresses(SEND_CONTACT_EMAIL_REQUEST.getReplyToEmail())
                .withMessage(new Message(new Content(SEND_CONTACT_EMAIL_REQUEST.getSubject()),
                        new Body(new Content(SEND_CONTACT_EMAIL_REQUEST.getEmailBody())))));
    }

    @Test
    void sendContactEmail_throwsRecaptchaVerificationException_whenRecaptchaVerificationFails()
            throws IOException, RecaptchaVerificationException {
        doThrow(RecaptchaVerificationException.class).when(mockRecaptchaVerifier)
                .verifyRecaptchaResult(any(), any());
        assertThrows(RecaptchaVerificationException.class,
                () -> contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST, HTTP_SERVLET_REQUEST));
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(SEND_CONTACT_EMAIL_REQUEST.getRecaptchaResponse(),
                HTTP_SERVLET_REQUEST.getRemoteAddr());
        verify(mockAmazonSimpleEmailService, never()).sendEmail(any());
    }

    @Test
    void getRecaptchaSiteKey_returnsRecaptchaSiteKey() throws JsonProcessingException {
        when(mockRecaptchaKeysRetriever.retrieveRecaptchaKeys()).thenReturn(RECAPTCHA_KEYS);
        assertThat(contactController.getRecaptchaSiteKey(), is(equalTo(GET_RECAPTCHA_SITE_KEY_RESULT)));
        verify(mockRecaptchaKeysRetriever).retrieveRecaptchaKeys();
    }

    private static HttpServletRequest buildHttpServletRequest() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRemoteAddr("0.0.0.0");
        return mockHttpServletRequest;
    }
}

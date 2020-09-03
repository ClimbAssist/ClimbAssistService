package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.climbassist.api.InvalidRequestException;
import com.climbassist.api.recaptcha.RecaptchaKeys;
import com.climbassist.api.recaptcha.RecaptchaVerificationException;
import com.climbassist.api.recaptcha.RecaptchaVerifier;
import com.climbassist.api.user.UserData;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final String CLIMB_ASSIST_EMAIL = "info@climbassist.com";
    private static final String USER_EMAIL = "link@hyrule.com";
    private static final SendContactEmailRequest SEND_CONTACT_EMAIL_REQUEST = SendContactEmailRequest.builder()
            .subject("subject")
            .emailBody("body")
            .replyToEmail(USER_EMAIL)
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
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("hero-of-time")
            .username("link")
            .email(USER_EMAIL)
            .isAdministrator(false)
            .isEmailVerified(true)
            .build());

    @Mock
    private AmazonSimpleEmailService mockAmazonSimpleEmailService;
    @Mock
    private RecaptchaVerifier mockRecaptchaVerifier;

    private ContactController contactController;

    @BeforeEach
    void setUp() {
        contactController = ContactController.builder()
                .climbAssistEmail(CLIMB_ASSIST_EMAIL)
                .amazonSimpleEmailService(mockAmazonSimpleEmailService)
                .recaptchaVerifier(mockRecaptchaVerifier)
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
    void sendContactEmail_throwsInvalidRequestException_whenUserDataIsNotPresentAndRecaptchaResponseIsNull()
            throws IOException, RecaptchaVerificationException {
        assertThrows(InvalidRequestException.class, () -> contactController.sendContactEmail(
                SendContactEmailRequest.builder()
                        .subject("subject")
                        .emailBody("body")
                        .replyToEmail(USER_EMAIL)
                        .build(), Optional.empty(), HTTP_SERVLET_REQUEST));
        verify(mockRecaptchaVerifier, never()).verifyRecaptchaResult(any(), any());
        verify(mockAmazonSimpleEmailService, never()).sendEmail(any());
    }

    @Test
    void sendContactEmail_sendsEmail_whenUserDataIsNotPresentAndRecaptchaVerificationSucceeds()
            throws IOException, RecaptchaVerificationException, InvalidRequestException {
        contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST, Optional.empty(), HTTP_SERVLET_REQUEST);
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(SEND_CONTACT_EMAIL_REQUEST.getRecaptchaResponse(),
                HTTP_SERVLET_REQUEST.getRemoteAddr());
        verify(mockAmazonSimpleEmailService).sendEmail(new SendEmailRequest().withSource(CLIMB_ASSIST_EMAIL)
                .withDestination(new Destination(ImmutableList.of(CLIMB_ASSIST_EMAIL)))
                .withReplyToAddresses(SEND_CONTACT_EMAIL_REQUEST.getReplyToEmail())
                .withMessage(new Message(new Content(SEND_CONTACT_EMAIL_REQUEST.getSubject()),
                        new Body(new Content(SEND_CONTACT_EMAIL_REQUEST.getEmailBody())))));
    }

    @Test
    void sendContactEmail_throwsRecaptchaVerificationException_whenUserDataIsNotPresentAndRecaptchaVerificationFails()
            throws IOException, RecaptchaVerificationException {
        doThrow(RecaptchaVerificationException.class).when(mockRecaptchaVerifier)
                .verifyRecaptchaResult(any(), any());
        assertThrows(RecaptchaVerificationException.class,
                () -> contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST, Optional.empty(),
                        HTTP_SERVLET_REQUEST));
        verify(mockRecaptchaVerifier).verifyRecaptchaResult(SEND_CONTACT_EMAIL_REQUEST.getRecaptchaResponse(),
                HTTP_SERVLET_REQUEST.getRemoteAddr());
        verify(mockAmazonSimpleEmailService, never()).sendEmail(any());
    }

    @Test
    void sendContactEmail_sendsEmail_whenUserDataIsPresent()
            throws IOException, RecaptchaVerificationException, InvalidRequestException {
        contactController.sendContactEmail(SEND_CONTACT_EMAIL_REQUEST, MAYBE_USER_DATA, HTTP_SERVLET_REQUEST);
        verify(mockRecaptchaVerifier, never()).verifyRecaptchaResult(any(), any());
        verify(mockAmazonSimpleEmailService).sendEmail(new SendEmailRequest().withSource(CLIMB_ASSIST_EMAIL)
                .withDestination(new Destination(ImmutableList.of(CLIMB_ASSIST_EMAIL)))
                .withReplyToAddresses(SEND_CONTACT_EMAIL_REQUEST.getReplyToEmail())
                .withMessage(new Message(new Content(SEND_CONTACT_EMAIL_REQUEST.getSubject()),
                        new Body(new Content(SEND_CONTACT_EMAIL_REQUEST.getEmailBody())))));
    }

    private static HttpServletRequest buildHttpServletRequest() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRemoteAddr("0.0.0.0");
        return mockHttpServletRequest;
    }
}

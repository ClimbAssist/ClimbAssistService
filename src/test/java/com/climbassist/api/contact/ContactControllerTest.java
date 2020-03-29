package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final String CLIMB_ASSIST_EMAIL = "info@climbassist.com";
    private static final SendContactEmailRequest SEND_CONTACT_EMAIL_REQUEST = SendContactEmailRequest.builder()
            .subject("subject")
            .body("body")
            .replyToEmail("link@hyrule.com")
            .build();

    @Mock
    private AmazonSimpleEmailService mockAmazonSimpleEmailService;

    private ContactController contactController;

    @BeforeEach
    void setUp() {
        contactController = ContactController.builder()
                .climbAssistEmail(CLIMB_ASSIST_EMAIL)
                .amazonSimpleEmailService(mockAmazonSimpleEmailService)
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
                        new Body(new Content(SEND_CONTACT_EMAIL_REQUEST.getBody())))));
    }
}

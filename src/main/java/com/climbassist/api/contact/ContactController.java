package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.climbassist.api.InvalidRequestException;
import com.climbassist.api.recaptcha.RecaptchaVerificationException;
import com.climbassist.api.recaptcha.RecaptchaVerifier;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
import com.climbassist.metrics.Metrics;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@Builder
@RestController
@Validated
@Slf4j
public class ContactController {

    @NonNull
    private final String climbAssistEmail;
    @NonNull
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    @NonNull
    private final RecaptchaVerifier recaptchaVerifier;

    @Metrics(api = "SendContactEmail")
    @RequestMapping(path = "/v1/contact", method = RequestMethod.POST)
    public SendContactEmailResult sendContactEmail(
            @NonNull @Valid @RequestBody SendContactEmailRequest sendContactEmailRequest,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
            @NonNull Optional<UserData> maybeUserData, @NonNull HttpServletRequest httpServletRequest)
            throws IOException, RecaptchaVerificationException, InvalidRequestException {
        if (!maybeUserData.isPresent()) {
            if (sendContactEmailRequest.getRecaptchaResponse() == null) {
                throw new InvalidRequestException("reCAPTCHA response must be present if user is not signed-in.");
            }
            else {
                recaptchaVerifier.verifyRecaptchaResult(sendContactEmailRequest.getRecaptchaResponse(),
                        httpServletRequest.getRemoteAddr());
            }
        }
        amazonSimpleEmailService.sendEmail(new SendEmailRequest().withSource(climbAssistEmail)
                .withDestination(new Destination(ImmutableList.of(climbAssistEmail)))
                .withReplyToAddresses(sendContactEmailRequest.getReplyToEmail())
                .withMessage(new Message(new Content(sendContactEmailRequest.getSubject()),
                        new Body(new Content(sendContactEmailRequest.getEmailBody())))));
        return SendContactEmailResult.builder()
                .successful(true)
                .build();
    }
}

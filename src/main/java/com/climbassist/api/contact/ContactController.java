package com.climbassist.api.contact;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
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

import javax.validation.Valid;

@Builder
@RestController
@Validated
@Slf4j
public class ContactController {

    @NonNull
    final String climbAssistEmail;
    @NonNull
    final AmazonSimpleEmailService amazonSimpleEmailService;

    @Metrics(api = "SendContactEmail")
    @RequestMapping(path = "/v1/contact", method = RequestMethod.POST)
    public SendContactEmailResult sendContactEmail(
            @NonNull @Valid @RequestBody SendContactEmailRequest sendContactEmailRequest) {
        amazonSimpleEmailService.sendEmail(new SendEmailRequest().withSource(climbAssistEmail)
                .withDestination(new Destination(ImmutableList.of(climbAssistEmail)))
                .withReplyToAddresses(sendContactEmailRequest.getReplyToEmail())
                .withMessage(new Message(new Content(sendContactEmailRequest.getSubject()),
                        new Body(new Content(sendContactEmailRequest.getBody())))));
        return SendContactEmailResult.builder()
                .successful(true)
                .build();
    }
}
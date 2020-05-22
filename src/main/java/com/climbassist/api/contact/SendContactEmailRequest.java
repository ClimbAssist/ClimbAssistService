package com.climbassist.api.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor // Required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
class SendContactEmailRequest {

    @NotNull(message = "Subject must be present.")
    @Size(min = 1, max = 100, message = "Subject must be between 1 and 100 characters.")
    private String subject;

    @NotNull(message = "Email body must be present.")
    @Size(min = 1, max = 10000, message = "Email body must be between 1 and 10,000 characters.")
    private String emailBody;

    @NotNull(message = "Reply-to email must be present.")
    @Email(message = "Reply-to email must be a valid email address.")
    @Size(min = 3, max = 100, message = "Reply-to email must be between 3 and 100 characters.")
    private String replyToEmail;
}

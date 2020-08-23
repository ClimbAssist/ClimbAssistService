package com.climbassist.api.user.authentication;

import com.climbassist.api.user.ValidEmail;
import com.climbassist.api.user.ValidOptionalUsername;
import com.climbassist.api.user.ValidPassword;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class RegisterUserRequest {

    @NotNull(message = "Username must be present.")
    @ValidOptionalUsername
    private String username;

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;

    @NotNull(message = "ReCAPTCHA response must be present.")
    @Size(min = 1, max = 500, message = "ReCAPTCHA response must be between 1 and 500 characters.")
    @JsonProperty("recaptchaRes")
    private String recaptchaResponse;
}

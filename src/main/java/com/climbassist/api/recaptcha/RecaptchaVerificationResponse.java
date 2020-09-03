package com.climbassist.api.recaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

/**
 * The response from Google when verifying a reCAPTCHA response.
 * https://developers.google.com/recaptcha/docs/verify
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecaptchaVerificationResponse {

    private boolean success;

    @NonNull
    @JsonProperty("error-codes")
    private Set<String> errorCodes;
}

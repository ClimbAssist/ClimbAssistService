package com.climbassist.common.recaptcha;

import com.climbassist.api.user.authentication.AuthenticationException;
import lombok.NonNull;

import java.util.Set;

public class RecaptchaVerificationException extends AuthenticationException {

    public RecaptchaVerificationException(@NonNull Set<String> errorCodes) {
        super(String.format("Unable to verify reCAPTCHA response. Error codes: %s", errorCodes));
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
